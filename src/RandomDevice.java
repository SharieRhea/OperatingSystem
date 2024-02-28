import java.util.Random;

public class RandomDevice implements Device {
    private final Random[] randoms = new Random[10];

    @Override
    public int open(String s) {
        Random random;
        if (s == null || s.isEmpty())
            random = new Random();
        else
            random = new Random(Long.parseLong(s));

        // find first empty spot, use that one
        int i = 0;
        while (i < 10 && randoms[i] != null) {
            i++;
        }
        // full on devices, fail
        if (i == 10)
            return -1;
        randoms[i] = random;
        // return where that device was just placed
        return i;
    }

    @Override
    public void close(int id) {
        randoms[id] = null;
    }

    @Override
    public byte[] read(int id, int size) {
        Random device = randoms[id];
        byte[] returnVal = new byte[size];
        device.nextBytes(returnVal);
        return returnVal;
    }

    @Override
    public int write(int id, byte[] data) {
        // no relevant behavior for write
        return 0;
    }

    @Override
    public void seek(int id, int to) {
        Random device = randoms[id];
        byte[] returnVal = new byte[to];
        device.nextBytes(returnVal);
        // no return as we are seeking not reading
    }
}
