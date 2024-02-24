import java.util.Arrays;

public class RandomFileProcess extends UserlandProcess {

    public void main() {
        // Attempt to open a device until it is successful
        int randomFD = -1;
        while(randomFD == -1) {
            randomFD = OS.open("random 200");
        }
        int fileFD = -1;
        while(fileFD == -1) {
            fileFD = OS.open("file test2");
        }

        while (true) {
            OS.sleep(500);
            byte[] data = OS.read(randomFD, 16);
            OS.seek(fileFD, 0);
            OS.write(fileFD, data);
            OS.seek(fileFD, 0);
            byte[] readData = OS.read(fileFD, data.length);
            System.out.println("Read from file: " + Arrays.toString(readData));
            cooperate();
        }
    }
}