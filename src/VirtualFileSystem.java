import java.io.IOException;

public class VirtualFileSystem implements Device {
    private final Device[] devices = new Device[20];
    private final int[] deviceIDs = new int[20];
    private final RandomDevice randomDevice = new RandomDevice();
    private final FakeFileSystem fileSystem = new FakeFileSystem();

    @Override
    public int open(String s) {
        String deviceType = s.substring(0, s.indexOf(' '));
        String parameter = s.substring(s.indexOf(' ') + 1);

        int i = 0;
        while (i < 20 && devices[i] != null) {
            i++;
        }
        // full on devices, cannot open a new one
        if (i == 20)
            return -1;

        // Create whichever a new entry for what was specified using the appropriate device
        switch (deviceType) {
            case "random" -> {
                devices[i] = randomDevice;
                deviceIDs[i] = randomDevice.open(parameter);
            }
            case "file" -> {
                devices[i] = fileSystem;
                try {
                    deviceIDs[i] = fileSystem.open(parameter);
                }
                catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            }
        }
        return i;
    }

    @Override
    public void close(int id) {
        devices[id].close(deviceIDs[id]);
        // reset arrays
        devices[id] = null;
        deviceIDs[id] = 0;
    }

    @Override
    public byte[] read(int id, int size) {
        return devices[id].read(deviceIDs[id], size);
    }

    @Override
    public int write(int id, byte[] data) {
        return devices[id].write(deviceIDs[id], data);
    }

    @Override
    public void seek(int id, int to) {
        devices[id].seek(deviceIDs[id], to);
    }

    // Used for testing only
    public Device[] getDevices() {
        return devices;
    }
}
