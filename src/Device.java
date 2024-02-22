import java.io.IOException;

public interface Device {
    int open(String s) throws IOException;

    void close(int id);

    byte[] read(int id, int size);

    int write(int id, byte[] data);

    void seek(int id, int to);
}
