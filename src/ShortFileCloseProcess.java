import java.nio.charset.StandardCharsets;

public class ShortFileCloseProcess extends UserlandProcess {

    public void main() {
        // Attempt to open a device until it is successful
        int fd = -1;
        while(fd == -1) {
            fd = OS.open("file test5.txt");
        }

        OS.sleep(500);
        byte[] message = "This process is nice and closes its device.\n".getBytes(StandardCharsets.UTF_8);
        OS.write(fd, message);
        OS.seek(fd, 0);
        byte[] data = OS.read(fd, message.length);
        OS.close(fd);
        System.out.print("Read from file: " + new String(data, StandardCharsets.UTF_8));
        exit();
    }
}