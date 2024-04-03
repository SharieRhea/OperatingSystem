import java.util.Arrays;
import java.util.Random;

public class RandomAllocationProcess extends UserlandProcess {

    public void main() {
        Random random = new Random();
        int size = 1024 * random.nextInt(10);
        int pointer = OS.allocateMemory(size);
        byte[] generated = new byte[5];
        random.nextBytes(generated);
        for (int i = 0; i < 5; i++) {
            write(pointer + i, generated[i]);
        }
        System.out.println("Some memory has been allocated and written to.");

        // continue reading to make sure memory doesn't change
        while (true) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException interruptedException) {
                System.out.println("Thread interruption: " + interruptedException.getMessage());
            }
            byte[] results = new byte[5];
            for (int i = 0; i < 5; i++) {
                results[i] = read(pointer + i);
            }
            System.out.println("Read: " + Arrays.toString(results));
            cooperate();
        }
    }
}
