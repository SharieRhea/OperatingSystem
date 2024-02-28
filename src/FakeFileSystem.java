import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device {
    private final RandomAccessFile[] files = new RandomAccessFile[10];

    @Override
    public int open(String s) throws IOException {
        if (s == null || s.isEmpty())
            throw new IOException("Must specify a filename to open.");

        RandomAccessFile file = new RandomAccessFile(s, "rw");
        // find first empty spot, use that one
        int i = 0;
        while (i < 10 && files[i] != null) {
            i++;
        }
        // devices are full, cannot open a new one
        if (i == 10)
            return -1;
        files[i] = file;
        // return where that device was just placed
        return i;
    }

    @Override
    public void close(int id) {
        RandomAccessFile file = files[id];
        try {
            file.close();
        }
        catch (IOException ioException) {
            throw new RuntimeException("Unable to close RandomAccessFile", ioException);
        }
        files[id] = null;
    }

    @Override
    public byte[] read(int id, int size) {
        RandomAccessFile file = files[id];
        byte[] returnVal = new byte[size];
        try {
            file.read(returnVal);
        }
        catch (IOException ioException) {
            throw new RuntimeException("Unable to read from RandomAccessFile", ioException);
        }
        return returnVal;
    }

    @Override
    public int write(int id, byte[] data) {
        RandomAccessFile file = files[id];
        try {
            file.write(data);
        }
        catch (IOException ioException) {
            throw new RuntimeException("Unable to write to RandomAccessFile", ioException);
        }
        // c-like write commands return the number of bytes written
        return data.length;
    }

    @Override
    public void seek(int id, int to) {
        // Seek is measured from beginning of file, NOT offset from current pointer
        RandomAccessFile file = files[id];
        try {
            file.seek(to);
        }
        catch (IOException ioException) {
            throw new RuntimeException("Unable to seek position in RandomAccessFile", ioException);
        }
    }
}
