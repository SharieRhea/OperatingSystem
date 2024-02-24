import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

public class UnitTestsDevices {

    @Test
    public void randomDevice() throws InterruptedException {
        OS.startup(new RandomProcess());
        Thread.sleep(5000);
    }

    @Test
    public void fileDevice() throws InterruptedException {
        OS.startup(new FileProcess());
        Thread.sleep(5000);
    }

    @Test
    public void randomFileDevice() throws InterruptedException {
        OS.startup(new RandomFileProcess());
        // Continuously reads 16 random bytes from a random device, writes to a file, then reads
        // Seeks to the beginning of the file for every write and read, so final file should contain the last set of numbers
        // Note: raw bytes are written, file is not encoded
        Thread.sleep(5000);
    }

    @Test
    public void multipleProcessesAndDevices() throws InterruptedException {
        OS.startup(new RandomProcess(), Priority.BACKGROUND);
        OS.createProcess(new FileProcess(), Priority.BACKGROUND);
        OS.createProcess(new RandomFileProcess(), Priority.REAL_TIME);
        Thread.sleep(5000);
    }

    @Test
    public void twoDevicesConnectingToSameFile() throws InterruptedException {
        OS.startup(new FileProcess(), Priority.BACKGROUND);
        OS.createProcess(new FileProcessTwo(), Priority.BACKGROUND);
        Thread.sleep(5000);
    }

    @Test
    public void multipleProcessesAndDevicesAndSameFiles() throws InterruptedException {
        OS.startup(new RandomProcess(), Priority.BACKGROUND);
        OS.createProcess(new FileProcess(), Priority.REAL_TIME);
        OS.createProcess(new RandomFileProcess(), Priority.REAL_TIME);
        OS.createProcess(new FileProcessTwo(), Priority.BACKGROUND);
        OS.createProcess(new RandomProcess(), Priority.REAL_TIME);
        Thread.sleep(5000);
    }

    @Test
    public void finishingProcess() throws InterruptedException {
        OS.startup(new ShortFileProcess());
        OS.createProcess(new RandomFileProcess(), Priority.REAL_TIME);
        Thread.sleep(5000);
        //assertTrue(Arrays.stream(OS.getKernel().getVirtualFileSystem().devices).allMatch(Objects::isNull));
    }
}
