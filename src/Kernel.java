import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Kernel implements Device {
    private final Scheduler scheduler;
    private final Semaphore semaphore;
    private final VirtualFileSystem virtualFileSystem = new VirtualFileSystem();
    // Keep track of available space in PHYSICAL memory, true = in use
    private boolean[] physicalMemoryPages = new boolean[1024];

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
        int physicalPageNumber = scheduler.currentPCB.getVirtualMemoryPages()[virtualPageNumber];
        // mapping is not valid (not allocated), do NOT update TLB
        if (physicalPageNumber == -1)
            return;

        var TLB = scheduler.currentPCB.getUserlandProcess().getTLB();
        // randomly choose which row of the TLB to update
        Random random = new Random();
        int row = random.nextInt(2);
        TLB[row][0] = virtualPageNumber;
        TLB[row][1] = physicalPageNumber;
    }

    private void allocate(int size) {
        int numPages = size / 1024;
        var virtualPages = getScheduler().currentPCB.getVirtualMemoryPages();
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

            if (virtualPages[virtualMemoryIndex] == -1)
                contiguous++;
            else
                contiguous = 0;
            if (contiguous == numPages)
                spaceFound = true;
            virtualMemoryIndex++;
        }
        int virtualPointer = virtualMemoryIndex - numPages;

        // Find the first free page in physical memory
        int physicalMemoryIndex = 0;
        while (physicalMemoryPages[physicalMemoryIndex]) {
            physicalMemoryIndex++;
        }
        if (physicalMemoryIndex > physicalMemoryPages.length - 1) {
            // Not enough space in physical memory, fail
            OS.returnValue = -1;
            return;
        }
        for (int i = 0; i < numPages; i++) {
            // Set the virtual -> physical mapping and update in use
            virtualPages[virtualPointer + i] = physicalMemoryIndex;
            physicalMemoryPages[physicalMemoryIndex] = true;
            // Get the next free page in physical memory
            while (physicalMemoryIndex < physicalMemoryPages.length && physicalMemoryPages[physicalMemoryIndex]) {
                physicalMemoryIndex++;
            }
            if (physicalMemoryIndex > physicalMemoryPages.length - 1) {
                // Not enough space in physical memory, fail
                OS.returnValue = -1;
                return;
            }
        }
        // return the virtual address
        OS.returnValue = virtualPointer;
    }

    private void free(int pointer, int size) {
        // first, get the physical page from the virtual pointer
        int physicalPage = getScheduler().currentPCB.getVirtualMemoryPages()[pointer];
        int numPages = size / 1024;
        // Remove mappings for however many pages need to be freed
        for (int i = physicalPage; i < numPages; i++) {
            getScheduler().currentPCB.getVirtualMemoryPages()[i] = -1;
            physicalMemoryPages[i] = false;
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

    public boolean[] getPhysicalMemoryPages() {
        return physicalMemoryPages;
    }

    // Used for testing only
    public VirtualFileSystem getVirtualFileSystem() {
        return virtualFileSystem;
    }
}
