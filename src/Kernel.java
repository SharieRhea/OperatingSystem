import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class Kernel implements Device {
    private final Scheduler scheduler;
    private final Semaphore semaphore;
    private final VirtualFileSystem virtualFileSystem = new VirtualFileSystem();

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
            }
            if (scheduler.currentPCB != null) {
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
        PCB targetProcess = map.get(receiverCopy.getReceiverPID());
        targetProcess.addMessage(receiverCopy);
        // if this process was waiting, return to its runnable queue
        if (scheduler.getWaitingProcesses().containsKey(targetProcess.getPID()))
            scheduler.removeWaitingProcess(targetProcess.getPID());
    }

    private void waitForMessage() {
        scheduler.waitForMessage();
    }

    // Used for testing only
    public VirtualFileSystem getVirtualFileSystem() {
        return virtualFileSystem;
    }
}
