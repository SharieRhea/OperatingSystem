import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Kernel implements Device {
    private final Scheduler scheduler;
    private final Semaphore semaphore;
    private final VirtualFileSystem virtualFileSystem = new VirtualFileSystem();
    // Keep track of available space in PHYSICAL memory, true = in use
    private boolean[] freeSpace = new boolean[1024];
    private int swapFile = -1;
    private int nextDiskPage = 0;

    public Kernel() {
        scheduler = new Scheduler();
        Thread thread = new Thread(this::run);
        semaphore = new Semaphore(0);

        thread.start();
    }

    public void start() {
        semaphore.release();
    }

    public void run() {
        while (true) {
            try {
                semaphore.acquire();
            } catch (InterruptedException interruptedException) {
                System.out.println("Thread interruption: " + interruptedException.getMessage());
            }
            switch (OS.currentCall) {
                case CREATE_PROCESS -> createProcess();
                case SWITCH_PROCESS -> switchProcess();
                case CREATE_SWAPFILE -> createSwapfile();
                case SLEEP -> sleep();
                case OPEN -> open((String) OS.parameters.get(0));
                case CLOSE -> close((int) OS.parameters.get(0));
                case READ -> read((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                case WRITE -> write((int) OS.parameters.get(0), (byte[]) OS.parameters.get(1));
                case SEEK -> seek((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                case CURRENT_PID -> OS.returnValue = scheduler.currentPCB.getPID();
                case PID_BY_NAME -> pidByName((String) OS.parameters.get(0));
                case SEND_MESSAGE -> sendMessage((KernelMessage) OS.parameters.get(0));
                case WAIT_FOR_MESSAGE -> waitForMessage();
                case GET_MAPPING -> getMapping((int) OS.parameters.get(0));
                case ALLOCATE -> allocate(((int) OS.parameters.get(0)));
                case FREE -> free((int) OS.parameters.get(0), (int) OS.parameters.get(1));
            }
            if (scheduler.currentPCB != null) {
                // If a process is about to run but is also in the waiting queue, it must have received its message
                // Populate the message for immediate return and remove from waiting queue
                if (scheduler.getWaitingProcesses().containsKey(scheduler.currentPCB.getPID())) {
                    OS.returnValue = scheduler.currentPCB.getMessages().poll();
                    scheduler.getWaitingProcesses().remove(scheduler.currentPCB.getPID());
                }

                scheduler.currentPCB.start();

                // If this is the first time the process is being started, start the thread
                if (!scheduler.currentPCB.getUserlandProcess().isStarted())
                    scheduler.currentPCB.getUserlandProcess().run();
            }
        }
    }

    private void createProcess() {
        OS.returnValue = scheduler.createProcess((UserlandProcess) OS.parameters.get(0), (Priority) OS.parameters.get(1));
    }

    private void switchProcess() {
        scheduler.switchProcess();
    }

    private void createSwapfile() {
        // create a file to use as swap for RAM
        try {
            swapFile = virtualFileSystem.getFileSystem().open("swapfile.sys");
        }
        catch (IOException ioException) {
            System.out.println("Could not create swapfile: " + ioException.getMessage());
        }
    }

    private void sleep() {
        scheduler.sleep(((int) OS.parameters.get(0)));
    }

    private void pidByName(String name) {
        HashMap<Integer, PCB> map = scheduler.getAllLivingPCBs();
        // Find the first PCB with a matching name, if there is none, return -1
        Optional<PCB> pcb = map.values().stream().filter(it -> it.getName().equals(name)).findFirst();
        OS.returnValue = pcb.map(PCB::getPID).orElse(-1);
    }

    private void sendMessage(KernelMessage message) {
        KernelMessage receiverCopy = new KernelMessage(message);
        receiverCopy.setSenderPID(scheduler.currentPCB.getPID());
        HashMap<Integer, PCB> map = scheduler.getAllLivingPCBs();
        PCB targetProcess = map.get(message.getReceiverPID());
        if (targetProcess == null)
            return;
        targetProcess.getMessages().add(receiverCopy);
        // if this process was waiting, return to its runnable queue
        if (scheduler.getWaitingProcesses().containsKey(message.getReceiverPID()))
            scheduler.restoreProcess(message.getReceiverPID());
    }

    private void waitForMessage() {
        scheduler.waitForMessage();
    }

    private void getMapping(int virtualPageNumber) {
        if (scheduler.currentPCB.getMappings()[virtualPageNumber] == null)
            return;

        int physicalPageIndex = scheduler.currentPCB.getMappings()[virtualPageNumber].physicalPageNumber;
        int diskPageIndex = scheduler.currentPCB.getMappings()[virtualPageNumber].diskPageNumber;
        if (physicalPageIndex == -1) {
            // find a free physical page to assign
            physicalPageIndex = 0;
            while (physicalPageIndex < scheduler.currentPCB.getMappings().length &&
            freeSpace[physicalPageIndex]) {
                physicalPageIndex++;
            }

            if (physicalPageIndex >= scheduler.currentPCB.getMappings().length) {
                // no more physical pages, need to swap with a victim process
                boolean victimFound = false;
                int index = 0;
                VirtualToPhysicalMapping[] victimMappings = null;
                while (!victimFound) {
                    var victim = scheduler.getRandomProcess();
                    victimMappings = victim.getMappings();
                    // find a page from the victim to swap with
                    index = 0;
                    while (index < victimMappings.length && (victimMappings[index] == null || victimMappings[index].physicalPageNumber == -1)) {
                        index++;
                    }
                    if (index < victimMappings.length)
                        victimFound = true;
                }

                physicalPageIndex = victimMappings[index].physicalPageNumber;
                // write contents to swapfile
                virtualFileSystem.getFileSystem().seek(swapFile, nextDiskPage * 1024);
                byte[] contents = new byte[1024];
                byte[] memory = UserlandProcess.getMemory();
                System.arraycopy(memory, physicalPageIndex * 1024, contents, 0, 1024);
                virtualFileSystem.getFileSystem().write(swapFile, contents);

                victimMappings[index].physicalPageNumber = -1;
                victimMappings[index].diskPageNumber = nextDiskPage;
                scheduler.currentPCB.getMappings()[virtualPageNumber].physicalPageNumber = physicalPageIndex;
                nextDiskPage++;
            }

            if (diskPageIndex != -1) {
                // this page was written out to disk, need to retrieve it
                virtualFileSystem.getFileSystem().seek(swapFile, diskPageIndex * 1024);
                byte[] pageContents = virtualFileSystem.getFileSystem().read(swapFile, 1024);
                for (int i = 0; i < 1024; i++) {
                    UserlandProcess.setMemory(physicalPageIndex * 1024 + i, pageContents[i]);
                }
                // read back from disk, disk page number no longer valid
                scheduler.currentPCB.getMappings()[virtualPageNumber].diskPageNumber = -1;
            }
            else {
                // need to populate memory with 0s since we are assigning a new
                // physical page that may have left over data
                for (int i = 0; i < 1024; i++) {
                    UserlandProcess.setMemory(physicalPageIndex * 1024 + i, (byte) 0);
                }
            }
            freeSpace[physicalPageIndex] = true;
            scheduler.currentPCB.getMappings()[virtualPageNumber].physicalPageNumber = physicalPageIndex;
        }

        // finally, update the TLB which was why this was called in the first place
        var TLB = scheduler.currentPCB.getUserlandProcess().getTLB();
        // randomly choose which row of the TLB to update
        Random random = new Random();
        int row = random.nextInt(2);
        TLB[row][0] = virtualPageNumber;
        TLB[row][1] = physicalPageIndex;
    }

    private void allocate(int size) {
        int numPages = size / 1024;
        var virtualPages = getScheduler().currentPCB.getMappings();

        // find a contiguous space in virtual memory for this process
        int virtualMemoryIndex = 0;
        int contiguous = 0;
        boolean spaceFound = false;
        while (!spaceFound) {
            // fail allocation if there is not enough space in virtual memory
            if (virtualMemoryIndex > virtualPages.length - 1) {
                OS.returnValue = -1;
                return;
            }

            if (virtualPages[virtualMemoryIndex] == null)
                contiguous++;
            else
                contiguous = 0;
            if (contiguous == numPages)
                spaceFound = true;
            virtualMemoryIndex++;
        }
        int virtualPointer = virtualMemoryIndex - numPages;

        // instead of mapping physical memory, simply make promises
        for (int i = 0; i < numPages; i++) {
            virtualPages[i] = new VirtualToPhysicalMapping();
        }
        OS.returnValue = virtualPointer * 1024;
    }

    private void free(int pointer, int size) {
        int numPages = size / 1024;
        int virtualPageIndex = pointer / 1024;
        for (int i = 0; i < numPages; i++) {
            int physicalPage = getScheduler().currentPCB.getMappings()[virtualPageIndex + i].physicalPageNumber;
            if (physicalPage != -1) {
                freeSpace[physicalPage] = false;
            }
            getScheduler().currentPCB.getMappings()[i] = null;
        }

        OS.returnValue = true;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public int open(String s) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        int i = 0;
        while (i < 10 && fds[i] != -1) {
            i++;
        }
        // Full on devices, fail
        if (i == 10)
            return -1;
        fds[i] = virtualFileSystem.open(s);
        OS.returnValue = i;
        return i;
    }

    @Override
    public void close(int id) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        int vfsID = fds[id];
        virtualFileSystem.close(vfsID);
        // Device has been closed, remove its id from the process's file descriptors
        fds[id] = -1;
    }

    @Override
    public byte[] read(int id, int size) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        OS.returnValue = virtualFileSystem.read(fds[id], size);
        return (byte[]) OS.returnValue;
    }

    @Override
    public int write(int id, byte[] data) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        OS.returnValue = virtualFileSystem.write(fds[id], data);
        return (int) OS.returnValue;
    }

    @Override
    public void seek(int id, int to) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        virtualFileSystem.seek(fds[id], to);
    }

    public boolean[] getFreeSpace() {
        return freeSpace;
    }

    // Used for testing only
    public VirtualFileSystem getVirtualFileSystem() {
        return virtualFileSystem;
    }
}
