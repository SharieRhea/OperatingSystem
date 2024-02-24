import org.junit.Test;

import static org.junit.Assert.*;

public class UnitTestsPriorityScheduler {

    // Please note DEBUG messages in output for an understanding off when processes are being run and demoted.

    @Test
    public void eachPriorityNoDemotion() throws InterruptedException {
        OS.startup(new SleepProcess(), Priority.REAL_TIME);
        OS.createProcess(new SleepProcess(), Priority.INTERACTIVE);
        OS.createProcess(new SleepProcess(), Priority.BACKGROUND);
        OS.createProcess(new GoodbyeWorld(), Priority.BACKGROUND);
        // Stop every 1000 milliseconds and check the status of each queue to determine behavior
        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
            // Background processes should include Idle, GoodbyeWorld, and POSSIBLY a SleepProcess
            switch (OS.getKernel().getScheduler().currentPCB.getPriority()) {
                case BACKGROUND -> {
                    // Either Idle or GoodbyeWorld should always remain in backgroundProcesses
                    assertFalse(OS.getKernel().getScheduler().getBackgroundProcesses().isEmpty());
                    // If a background process is running, at least 2 of the SleepProcesses should be sleeping
                    assertTrue(OS.getKernel().getScheduler().getSleepingProcesses().size() >= 2);
                }
                case INTERACTIVE, REAL_TIME -> {
                    // Idle, GoodbyeWorld, and possibly a SleepProcess
                    assertTrue(OS.getKernel().getScheduler().getBackgroundProcesses().size() >= 2);
                    assertEquals(2, OS.getKernel().getScheduler().getSleepingProcesses().size());
                }
            }
        }
    }

    @Test
    public void interactiveDemotion() throws InterruptedException {
        OS.startup(new HelloWorld(), Priority.INTERACTIVE);
        Thread.sleep(500);
        // HelloWorld is either currently running or waiting in interactiveProcesses
        assertTrue(OS.getKernel().getScheduler().getInteractiveProcesses().size() == 1 ||
                OS.getKernel().getScheduler().currentPCB.getPriority() == Priority.INTERACTIVE);
        Thread.sleep(2000);
        // After run to timeout at least 5 times, there should be no interactive processes
        assertTrue(OS.getKernel().getScheduler().getInteractiveProcesses().isEmpty() &&
                OS.getKernel().getScheduler().currentPCB.getPriority() != Priority.INTERACTIVE);
    }

    @Test
    public void realTimeDemotion() throws InterruptedException {
        OS.startup(new HelloWorld(), Priority.REAL_TIME);
        Thread.sleep(500);
        // HelloWorld is either currently running or waiting in realTimeProcesses
        assertTrue(OS.getKernel().getScheduler().getRealTimeProcesses().size() == 1 ||
                OS.getKernel().getScheduler().currentPCB.getPriority() == Priority.REAL_TIME);

        Thread.sleep(1750);
        // After run to timeout at least 5 times, there should be 1 interactive processes, no realtime
        assertTrue(OS.getKernel().getScheduler().getRealTimeProcesses().isEmpty() &&
                OS.getKernel().getScheduler().currentPCB.getPriority() != Priority.REAL_TIME);
        assertTrue(OS.getKernel().getScheduler().getInteractiveProcesses().size() == 1 ||
                OS.getKernel().getScheduler().currentPCB.getPriority() == Priority.INTERACTIVE);

        Thread.sleep(2000);
        // After run to timeout at least 5 more times, there should be only background processes
        assertTrue(OS.getKernel().getScheduler().getInteractiveProcesses().isEmpty() &&
                OS.getKernel().getScheduler().currentPCB.getPriority() != Priority.INTERACTIVE);
        // Either Idle or HelloWorld will be running, the other will be in the queue
        assertTrue(OS.getKernel().getScheduler().getBackgroundProcesses().size() == 1 &&
                OS.getKernel().getScheduler().currentPCB.getPriority() == Priority.BACKGROUND);
    }

    @Test
    public void realTimeNoDemotion() throws InterruptedException {
        OS.startup(new SleepProcess(), Priority.REAL_TIME);
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
            // Sleep is either currently running, waiting in realTimeProcesses, or sleeping
            assertTrue(OS.getKernel().getScheduler().getRealTimeProcesses().size() == 1 ||
                    OS.getKernel().getScheduler().currentPCB.getPriority() == Priority.REAL_TIME ||
                    !OS.getKernel().getScheduler().getSleepingProcesses().isEmpty());
        }
    }

    @Test
    public void varietyOfProcesses() throws InterruptedException {
        OS.startup(new Sample1(), Priority.REAL_TIME);
        OS.createProcess(new Sample2(), Priority.INTERACTIVE);
        OS.createProcess(new HelloWorld(), Priority.BACKGROUND);
        Thread.sleep(10000);
        // Run for 10 seconds, observe that process 2 runs the most then at equal rate to HelloWorld,
        // process 1 sleeps regularly,
    }
}
