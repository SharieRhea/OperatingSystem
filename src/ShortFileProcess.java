import java.nio.charset.StandardCharsets;

public class ShortFileProcess extends UserlandProcess {

    public void main() {
        // Attempt to open a device until it is successful
        int fd = -1;
        while(fd == -1) {
            fd = OS.open("file test4.txt");
        }

        OS.sleep(500);
        byte[] message = "This process only writes to and reads from this file once, then it stops.\n".getBytes(StandardCharsets.UTF_8);
        OS.write(fd, message);
        OS.seek(fd, 0);
        byte[] data = OS.read(fd, message.length);
        System.out.print("Read from file: " + new String(data, StandardCharsets.UTF_8));
        cooperate();
    }
}