public class PCB {
    private static int nextPID = 0;
    private final int pid;
    private final UserlandProcess userlandProcess;
    private Priority priority;
    private int timeoutCounter = 0;

    public PCB(UserlandProcess up, Priority priority) {
        pid = nextPID;
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
        timeoutCounter = 0;
    }

    public void demoteProcess() {
        switch (priority) {
            case REAL_TIME -> priority = Priority.INTERACTIVE;
            case INTERACTIVE -> priority = Priority.BACKGROUND;
        }
        resetTimeoutCounter();
    }
}
