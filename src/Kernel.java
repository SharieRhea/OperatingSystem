import java.util.concurrent.Semaphore;

public class Kernel {
    private Scheduler scheduler;
    private Thread thread;
    private Semaphore semaphore;

    public Kernel() {
        scheduler = new Scheduler();
        thread = new Thread(this::run);
        semaphore = new Semaphore(1);

        thread.start();
    }

    public void start() {
        semaphore.release();
    }

    // maybe? from rubric
    public void createProcess(UserlandProcess up) {
        scheduler.createProcess(up);
    }

    public void run() {
        while (true) {
            try {
                semaphore.acquire();
            }
            catch (InterruptedException interruptedException) {
                System.out.println("Thread interruption: " + interruptedException.getMessage());
            }
            // switch on OS.currentCall - for each call the func that implements them
            switch (OS.currentCall) {
                case CreateProcess -> {
                    OS.returnValue = scheduler.createProcess((UserlandProcess) OS.parameters.get(0));
                    OS.currentCall = CallType.SwitchProcess;
                }
                case SwitchProcess -> {
                    scheduler.switchProcess();
                }
            }
            if (scheduler.currentProcess.isStopped())
                scheduler.currentProcess.start();
            else
                scheduler.currentProcess.run();
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
