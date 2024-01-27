import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable {
    // todo: see if any of these need a default init
    protected Thread thread = new Thread(this::main);
    protected Semaphore semaphore = new Semaphore(1);
    protected boolean isQuantumValid;

    public void requestStop() {
        isQuantumValid = false;
    }

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

    public void run() {
        try {
            semaphore.acquire();
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interruption: " + interruptedException.getMessage());
        }
        thread.start();
    }

    public void cooperate() {
        if (isQuantumValid) {
            isQuantumValid = false;
            OS.switchProcess();
        }
    }
}
