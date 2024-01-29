import java.util.concurrent.Semaphore;

public class Kernel {
    private Scheduler scheduler;
    private Thread thread;
    private Semaphore semaphore;

    public Kernel() {
        scheduler = new Scheduler();
        thread = new Thread(this::run);
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
            }
            catch (InterruptedException interruptedException) {
                System.out.println("Thread interruption: " + interruptedException.getMessage());
            }
            switch (OS.currentCall) {
                case CreateProcess ->
                    createProcess();
                case SwitchProcess ->
                    switchProcess();
            }
            scheduler.currentProcess.start();

            // If this is the first time the process is being started, start the thread
            if (!scheduler.currentProcess.isStarted())
                scheduler.currentProcess.run();
        }
    }

    private void createProcess() {
        OS.returnValue = scheduler.createProcess((UserlandProcess) OS.parameters.get(0));
    }

    private void switchProcess() {
        scheduler.switchProcess();
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
