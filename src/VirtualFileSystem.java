import java.io.IOException;

public class VirtualFileSystem implements Device {
    Device[] devices = new Device[20];
    int[] deviceIDs = new int[20];
    RandomDevice randomDevice = new RandomDevice();
    FakeFileSystem fileSystem = new FakeFileSystem();

    @Override
    public int open(String s) {
        String deviceType = s.substring(0, s.indexOf(' '));
        String parameter = s.substring(s.indexOf(' '));

        int i = 0;
        while (i < 20 && devices[i] != null) {
            i++;
        }
        // full on devices, cannot open a new one
        if (i == 20)
            return -1;

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
        return 0;
    }

    @Override
    public void close(int id) {
        devices[id].close(deviceIDs[id]);
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
}
