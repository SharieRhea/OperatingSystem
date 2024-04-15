import org.junit.Test;

public class UnitTestsVirtualMemory {

    @Test
    public void basicMemoryUse() throws Exception {
        OS.startup(new BasicMemoryProcess());
        Thread.sleep(2000);
    }

    @Test
    public void forcePageSwapping() throws Exception {
        // Start enough memory hog processes to require page swapping
        // runs infinitely, after initialization, processes just read the written values forever
        // which shows that memory is read back in from disk correctly
        OS.startup(new MemoryHogProcess());
        for (int i = 0; i < 10; i++) {
            OS.createProcess(new MemoryHogProcess());
        }
        Thread.sleep(20000);
    }

    @Test
    public void manyProcesses() throws InterruptedException {
        OS.startup(new BasicMemoryProcess());
        OS.createProcess(new MemoryHogProcess());
        OS.createProcess(new SenderProcess());
        OS.createProcess(new ReceiverProcess());
        OS.createProcess(new HelloWorld(), Priority.BACKGROUND);
        OS.createProcess(new ShortFileProcess(), Priority.BACKGROUND);
        OS.createProcess(new ShortFileCloseProcess(), Priority.REAL_TIME);
        OS.createProcess(new BasicMemoryProcess());
        Thread.sleep(5000);
    }
}
