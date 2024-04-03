import java.util.Arrays;
import java.util.Random;

public class BasicMemoryProcess extends UserlandProcess {

    public void main() {
        Random random = new Random();
        for (int iterations = 0; iterations < 5; iterations++) {try {
            Thread.sleep(100);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interruption: " + interruptedException.getMessage());
        }
            int pointer = OS.allocateMemory(2048);
            if (pointer == -1) {
                System.out.println("Memory failed to allocate (full), exiting.");
                exit();
            }
            byte[] generated = new byte[5];
            random.nextBytes(generated);
            System.out.println("Randomly generated bytes: " + Arrays.toString(generated));
            for (int i = 0; i < 5; i++) {
                write(pointer + i, generated[i]);
            }
            byte[] results = new byte[5];
            for (int i = 0; i < 5; i++) {
                results[i] = read(pointer + i);
            }
            System.out.println("Results read from memory: " + Arrays.toString(results));
            OS.freeMemory(pointer, 2048);
            cooperate();
        }
        exit();
    }
}
