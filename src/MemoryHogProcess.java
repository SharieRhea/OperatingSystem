import java.util.Random;

public class MemoryHogProcess extends UserlandProcess {

    @Override
    public void main() {
        // allocate 20 pages
        int pointer = OS.allocateMemory(20480);
        int pid = OS.getCurrentPID();
        Random random = new Random();
        byte[] data = new byte[1];
        for (int i = 0; i < 20; i++) {
            // write to every page
            random.nextBytes(data);
            if (data[0] == 0)
                data[0] = (byte) 13;
            write(pointer + i * 1024, data[0]);
            System.out.printf("Process %d wrote to address %d%n", pid, pointer + i * 1024);
            cooperate();
        }
        while (true) {
            for (int i = 0; i < 20; i++) {
                byte value = read(pointer + i * 1024);
                System.out.printf("Process %d read value %d%n", pid, value);
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException interruptedException) {
                    System.out.println(interruptedException.getMessage());
                }
                cooperate();
            }
        }
    }
}
