import java.io.IOException;

/**
 * Every device must implement this interface to be able to
 * communicate with the kernel and userland.
 */
public interface Device {
    int open(String s) throws IOException;

    void close(int id);

    byte[] read(int id, int size);

    int write(int id, byte[] data);

    void seek(int id, int to);
}
