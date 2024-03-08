import java.util.LinkedList;
import java.util.Optional;

public class PCB {
    private static int nextPID = 0;
    private final int pid;
    private final String name;

    private final UserlandProcess userlandProcess;
    private Priority priority;
    private int timeoutCounter = 0;

    private final int[] fileDescriptors = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private final LinkedList<KernelMessage> messageQueue = new LinkedList<>();

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

    public void addMessage(KernelMessage message) {
        messageQueue.add(message);
    }

    public Optional<KernelMessage> getMessage() {
        if (messageQueue.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(messageQueue.remove());
    }

    public String getName() {
        return name;
    }

    public int[] getFileDescriptors() {
        return fileDescriptors;
    }
}
