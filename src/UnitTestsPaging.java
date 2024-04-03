import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class UnitTestsPaging {

    @Test
    public void basicMemory() throws InterruptedException {
        OS.startup(new BasicMemoryProcess());
        Thread.sleep(5000);
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
        OS.startup(new NecessaryFreeProcess());
        Thread.sleep(2000);
    }

    @Test
    public void memoryClearedOnProcessExit() throws InterruptedException {
        // Process allocates and frees some memory, but does not free everything before exiting
        OS.startup(new NecessaryFreeProcess());
        Thread.sleep(2000);

        var space = OS.getKernel().getFreeSpace();
        for (boolean inUse : space) {
           assertFalse(inUse);
        }
    }

    @Test
    public void processesNotOverwritingMemory() throws InterruptedException {
        OS.startup(new BasicMemoryProcess());
        OS.createProcess(new RandomAllocationProcess());
        OS.createProcess(new BasicMemoryProcess());
        Thread.sleep(5000);
    }
}
