import java.util.ArrayDeque;

public class PCB {
    private static int nextPID = 0;
    private final int pid;
    private final String name;

    private final UserlandProcess userlandProcess;
    private Priority priority;
    private int timeoutCounter = 0;

    private final int[] fileDescriptors = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private final ArrayDeque<KernelMessage> messageQueue = new ArrayDeque<>();

    // This individual process's pages of memory, VIRTUAL
    private VirtualToPhysicalMapping[] mappings = new VirtualToPhysicalMapping[100];

    public PCB(UserlandProcess up, Priority priority) {
        pid = nextPID;
        name = up.getClass().getSimpleName();
        nextPID++;
        userlandProcess = up;
        this.priority = priority;
    }

    public void stop() {
        userlandProcess.stop();
        while (!userlandProcess.isStopped()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
    }

    // unused
    public boolean isDone() {
        return userlandProcess.isDone();
    }

    public void start() {
        userlandProcess.start();
    }

    public UserlandProcess getUserlandProcess() {
        return userlandProcess;
    }

    public int getPID() {
        return pid;
    }

    public Priority getPriority() {
        return priority;
    }

    public void incrementTimeoutCounter() {
        timeoutCounter++;
    }

    public int getTimeoutCounter() {
        return timeoutCounter;
    }

    public void resetTimeoutCounter() {
        // When a process sleeps or gets demoted, its counter should be reset
        timeoutCounter = 0;
    }

    public void demoteProcess() {
        // Bump priority down one level, a background process cannot be demoted further
        switch (priority) {
            case REAL_TIME -> priority = Priority.INTERACTIVE;
            case INTERACTIVE -> priority = Priority.BACKGROUND;
        }
        resetTimeoutCounter();
    }

    public ArrayDeque<KernelMessage> getMessages() {
        return messageQueue;
    }

    public String getName() {
        return name;
    }

    public int[] getFileDescriptors() {
        return fileDescriptors;
    }

    public VirtualToPhysicalMapping[] getMappings() {
        return mappings;
    }
}
