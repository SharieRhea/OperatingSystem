import java.util.Arrays;

public class RandomProcess extends UserlandProcess {

    public void main() {
        // Attempt to open a device until it is successful
        int fd = -1;
        while(fd == -1) {
            fd = OS.open("random 200");
        }

        while (true) {
            OS.sleep(500);
            byte[] data = OS.read(fd, 16);
            System.out.println(Arrays.toString(data));
            cooperate();
        }
    }
}