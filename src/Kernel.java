import java.util.concurrent.Semaphore;

public class Kernel {
    private final Scheduler scheduler;
    private final Semaphore semaphore;

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
                case CreateProcess -> createProcess();
                case SwitchProcess -> switchProcess();
                case Sleep -> sleep();
            }
            scheduler.currentPCB.getUserlandProcess().start();

            // If this is the first time the process is being started, start the thread
            if (!scheduler.currentPCB.getUserlandProcess().isStarted())
                scheduler.currentPCB.getUserlandProcess().run();
        }
    }

    private void createProcess() {
        OS.returnValue = scheduler.createProcess((UserlandProcess) OS.parameters.get(0), (Priority) OS.parameters.get(1));
    }

    private void switchProcess() {
        scheduler.switchProcess();
    }

    private void sleep() {
        //scheduler.sleep();
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
