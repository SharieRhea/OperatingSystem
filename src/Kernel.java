import java.util.concurrent.Semaphore;

public class Kernel implements Device {
    private final Scheduler scheduler;
    private final Semaphore semaphore;
    private final VirtualFileSystem virtualFileSystem = new VirtualFileSystem();

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
                case Open -> open((String) OS.parameters.get(0));
                case Close -> close((int) OS.parameters.get(0));
                case Read -> read((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                case Write -> write((int) OS.parameters.get(0), (byte[]) OS.parameters.get(1));
                case Seek -> seek((int) OS.parameters.get(0), (int) OS.parameters.get(1));
            }
            if (scheduler.currentPCB != null) {
                scheduler.currentPCB.start();

                // If this is the first time the process is being started, start the thread
                if (!scheduler.currentPCB.getUserlandProcess().isStarted())
                    scheduler.currentPCB.getUserlandProcess().run();
            }
        }
    }

    private void createProcess() {
        OS.returnValue = scheduler.createProcess((UserlandProcess) OS.parameters.get(0), (Priority) OS.parameters.get(1));
    }

    private void switchProcess() {
        scheduler.switchProcess();
    }

    private void sleep() {
        scheduler.sleep(((int) OS.parameters.get(0)));
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public int open(String s) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        int i = 0;
        while (i < 10 && fds[i] != -1) {
            i++;
        }

        if (i == 10)
            return -1;
        fds[i] = virtualFileSystem.open(s);
        OS.returnValue = i;
        return i;
    }

    @Override
    public void close(int id) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        int vfsID = fds[id];
        virtualFileSystem.close(vfsID);
    }

    @Override
    public byte[] read(int id, int size) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        OS.returnValue = virtualFileSystem.read(fds[id], size);
        return (byte[]) OS.returnValue;
    }

    @Override
    public int write(int id, byte[] data) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        OS.returnValue = virtualFileSystem.write(fds[id], data);
        return (int) OS.returnValue;
    }

    @Override
    public void seek(int id, int to) {
        int[] fds = scheduler.currentPCB.getFileDescriptors();
        virtualFileSystem.seek(fds[id], to);
    }
}
