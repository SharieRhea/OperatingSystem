import java.util.Random;

public class SegFaultProcess extends UserlandProcess {

    public void main() {
        while (true) {
            // don't allocate any memory, but try to read
            Random random = new Random();
            int address = random.nextInt(100000);
            System.out.printf("Attempting to access memory address %d without allocation.\n", address);
            byte result = read(address);
            System.out.println("Result was: " + result);
            cooperate();
        }
    }
}
