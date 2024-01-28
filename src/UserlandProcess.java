import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable {
    private Thread thread = new Thread(this::main);
    private Semaphore semaphore = new Semaphore(0);
    private boolean isQuantumInvalid = false;
    // Used to prevent calling thread.start() more than once
    private boolean started = false;

    public abstract void main();

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

    public boolean isStarted() {
        return started;
    }
}
