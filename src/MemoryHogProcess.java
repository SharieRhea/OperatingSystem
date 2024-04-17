import java.util.Arrays;
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
            cooperate();
        }
        System.out.printf("Process %d finished writing.%n", pid);
        while (true) {
            byte[] values = new byte[20];
            for (int i = 0; i < 20; i++) {
                values[i] = read(pointer + i * 1024);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interruptedException) {
                    System.out.println(interruptedException.getMessage());
                }
            }
            System.out.printf("Process %d read %s%n", pid, Arrays.toString(values));
            cooperate();
        }
    }
}
