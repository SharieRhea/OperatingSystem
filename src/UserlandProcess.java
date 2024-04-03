import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable {
    private Thread thread = new Thread(this::main);
    private Semaphore semaphore = new Semaphore(0);
    private boolean isQuantumInvalid = false;
    // Used to prevent calling thread.start() more than once
    private boolean started = false;
    private boolean exited = false;

    private static byte[] memory = new byte[1048576];
    // Initialize TLB to have no valid mappings
    private static int[][] TLB = new int[][] { {-1, -1}, {-1, -1}};

    public abstract void main();

    public byte read(int address) {
        int physicalAddress = getPhysicalAddress(address);
        return memory[physicalAddress];
    }

    public void write(int address, byte value) {
        int physicalAddress = getPhysicalAddress(address);
        memory[physicalAddress] = value;
    }

    private int getPhysicalAddress(int address) {
        int virtualPage = address / 1024;
        if (virtualPage < 0 || virtualPage > 100) {
            // trying to access outside memory bounds, kill it
            System.out.printf("SegmentationFault: Killing PID: %d\n", OS.getCurrentPID());
            exit();
            return -1;
        }
        int offset = address % 1024;
        int physicalPage;
        if (TLB[0][0] == virtualPage) {
            physicalPage = TLB[0][1];
            return physicalPage * 1024 + offset;
        }
        else if (TLB[1][0] == virtualPage) {
            physicalPage = TLB[1][1];
            return physicalPage * 1024 + offset;
        }
        // wasn't in the TLB, try to get the mapping
        OS.getMapping(virtualPage);
        if (TLB[0][0] == virtualPage) {
            physicalPage = TLB[0][1];
            return physicalPage * 1024 + offset;
        }
        else if (TLB[1][0] == virtualPage) {
            physicalPage = TLB[1][1];
            return physicalPage * 1024 + offset;
        }
        // a TLB entry was not created for this process, which means this memory
        // does not belong or has not been allocated, kill the process
        System.out.printf("SegmentationFault: Killing PID: %d\n", OS.getCurrentPID());
        exit();
        return -1;
    }

    public boolean isStopped() {
        return semaphore.availablePermits() == 0;
    }

    public boolean isDone() {
        return !thread.isAlive();
    }

    public void start() {
        semaphore.release();
    }

    public void stop() {
        try {
            semaphore.acquire();
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interruption: " + interruptedException.getMessage());
        }
    }

    public void requestStop() {
        isQuantumInvalid = true;
    }

    public void run() {
        try {
            semaphore.acquire();
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interruption: " + interruptedException.getMessage());
        }
        started = true;
        thread.start();
    }

    public void cooperate() {
        if (isQuantumInvalid) {
            isQuantumInvalid = false;
            OS.switchProcess();
        }
    }

    // A process must declare when it has finished running in order to prevent a lock-up
    public void exit() {
        exited = true;
        OS.switchProcess();
    }

    public boolean isStarted() {
        return started;
    }

    public boolean getExited() {
        return exited;
    }

    public int[][] getTLB() {
        return TLB;
    }
}
