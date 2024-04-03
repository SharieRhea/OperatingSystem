import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class UnitTestsPaging {

    @Test
    public void basicMemory() throws InterruptedException {
        OS.startup(new BasicMemoryProcess());
        Thread.sleep(2000);
    }

    @Test
    public void attemptAccessMemory() throws InterruptedException {
        OS.startup(new SegFaultProcess());
        Thread.sleep(1000);
    }

    @Test
    public void multipleAllocatingProcesses() throws InterruptedException {
        // Two processes both allocating and freeing memory,
        // Seg fault attempts to access a random address, should be killed
        // Other processes should continue
        OS.startup(new BasicMemoryProcess());
        OS.createProcess(new SegFaultProcess(), Priority.REAL_TIME);
        OS.createProcess(new BasicMemoryProcess());
        Thread.sleep(2000);
    }

    @Test
    public void freeingMemory() throws InterruptedException {
        // This process requires free in order to allocate more memory
        OS.startup(new NecessaryFreeProcess());
        Thread.sleep(2000);
    }

    @Test
    public void memoryClearedOnProcessExit() throws InterruptedException {
        // Process allocates and frees some memory, but does not free everything before exiting
        // On exit, kernel should clear memory for it (aka page array in kernel should be empty)
        OS.startup(new NecessaryFreeProcess());
        Thread.sleep(2000);

        var freeSpace = OS.getKernel().getPhysicalMemoryPages();
        for (boolean inUse : freeSpace) {
           assertFalse(inUse);
        }
    }

    @Test
    public void processesNotOverwritingMemory() throws InterruptedException {
        // Multiple processes all allocating, reading, writing, and freeing
        // RandomAllocation should also read the same values (not being overwritten)
        OS.startup(new BasicMemoryProcess());
        OS.createProcess(new RandomAllocationProcess());
        OS.createProcess(new BasicMemoryProcess());
        Thread.sleep(5000);
    }

    @Test
    public void manyProcesses() throws InterruptedException {
        OS.startup(new BasicMemoryProcess());
        OS.createProcess(new SenderProcess());
        OS.createProcess(new ReceiverProcess());
        OS.createProcess(new SegFaultProcess(), Priority.REAL_TIME);
        OS.createProcess(new HelloWorld(), Priority.BACKGROUND);
        OS.createProcess(new ShortFileProcess(), Priority.BACKGROUND);
        OS.createProcess(new ShortFileCloseProcess(), Priority.REAL_TIME);
        OS.createProcess(new BasicMemoryProcess());
        Thread.sleep(5000);
    }
}
