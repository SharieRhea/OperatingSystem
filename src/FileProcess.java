import java.nio.charset.StandardCharsets;

public class FileProcess extends UserlandProcess {

    public void main() {
        // Attempt to open a device until it is successful
        int fd = -1;
        while(fd == -1) {
            fd = OS.open("file test1.txt");
        }

        while (true) {
            OS.sleep(500);
            byte[] message = "A line of text to write into a file.\n".getBytes(StandardCharsets.UTF_8);
            OS.seek(fd, 0);
            OS.write(fd, message);
            OS.seek(fd, 0);
            byte[] data = OS.read(fd, message.length);
            System.out.print("Read from file: " + new String(data, StandardCharsets.UTF_8));
            cooperate();
        }
    }
}