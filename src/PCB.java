public class PCB {
    private static int nextPID = 0;
    private final int pid;
    private final Priority priority;
    private final UserlandProcess userlandProcess;

    public PCB(UserlandProcess up, Priority priority) {
        pid = nextPID;
        nextPID++;
        userlandProcess = up;
        this.priority = priority;
        // create thread??
    }

    private void stop() {
        userlandProcess.stop();
        // loop with Thread.sleep() until up.stopped is true
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

    public void run() {
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
}
