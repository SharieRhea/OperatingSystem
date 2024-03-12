import java.time.Clock;
import java.time.Instant;
import java.util.*;

public class Scheduler {
    private final Random random = new Random();
    private final Queue<PCB> realTimeProcesses = new ArrayDeque<>();
    private final Queue<PCB> interactiveProcesses = new ArrayDeque<>();
    private final Queue<PCB> backgroundProcesses = new ArrayDeque<>();
    private final HashMap<Instant, PCB> sleepingProcesses = new HashMap<>();
    private final HashMap<Integer, PCB> waitingProcesses = new HashMap<>();
    private final HashMap<Integer, PCB> livingProcesses = new HashMap<>();
    public PCB currentPCB = null;

    public Scheduler() {
        Timer timer = new Timer();
        TimerTask interrupt = new TimerTask() {
            @Override
            public void run() {
                if (currentPCB != null && currentPCB.getUserlandProcess() != null) {
                    currentPCB.getUserlandProcess().requestStop();
                    currentPCB.incrementTimeoutCounter();
                }
            }
        };
        // Schedule an interrupt to occur every 250ms and request the currentProcess to stop
        timer.scheduleAtFixedRate(interrupt, 0L, 250L);
    }

    public int createProcess(UserlandProcess userlandProcess, Priority priority) {
        PCB pcb = new PCB(userlandProcess, priority);
        // Place in correct queue
        addToQueue(pcb);
        // Add to bank of live processes
        livingProcesses.put(pcb.getPID(), pcb);
        // Starts the first process
        if (currentPCB == null)
            switchProcess();
        return pcb.getPID();
    }

    public void switchProcess() {
        // First, check to see if any sleeping process are eligible to run now
        wakeUpEligibleProcesses();

        if (currentPCB == null) {
            currentPCB = getQueueToRun().poll();
            return;
        }

        // Note: using getHasFinished() instead of isDone() because when a process "dies" it locks up the
        // semaphore unless it somehow signifies it's done before the last OS.switchProcess
        if (currentPCB.getUserlandProcess().getExited()) {
            // Process has ended, need to clean up any devices that were left open
            Kernel kernel = OS.getKernel();
            int[] fileDescriptors = currentPCB.getFileDescriptors();
            for (int i = 0; i < fileDescriptors.length; i++) {
                if (fileDescriptors[i] != -1) {
                    kernel.close(i);
                    fileDescriptors[i] = -1;
                }
            }
            // Remove from list of live processes
            livingProcesses.remove(currentPCB.getPID());
            // Early return so that we don't add this process back onto the queue
            currentPCB = getQueueToRun().poll();
            return;
        }

        // Check if this process should be demoted
        if (currentPCB.getTimeoutCounter() > 4 && currentPCB.getPriority() != Priority.BACKGROUND)
            currentPCB.demoteProcess();
        addToQueue(currentPCB);
        // Grab the next process to run
        currentPCB = getQueueToRun().poll();
    }

    public void sleep(int milliseconds) {
        // Determine the time to wake
        Instant timeToWake = Clock.systemUTC().instant().plusMillis(milliseconds);
        // Check for key collisions, if key already exists, add 1 millisecond and check again
        while (sleepingProcesses.containsKey(timeToWake))
            timeToWake = timeToWake.plusMillis(1);
        sleepingProcesses.put(timeToWake, currentPCB);
        currentPCB.resetTimeoutCounter();
        // DON'T call switchProcess because this process should not be added to a queue until
        // ready to wake, just run a new process
        currentPCB = getQueueToRun().poll();
    }

    public void waitForMessage() {
        if (currentPCB.getMessages().isEmpty()) {
            // No message, need to deschedule and pick a new process to run
            waitingProcesses.put(currentPCB.getPID(), currentPCB);
            currentPCB = getQueueToRun().poll();
        }
        else
            OS.returnValue = currentPCB.getMessages().poll();
    }

    public void restoreProcess(int pid) {
        // place the process into its proper queue but leave it in the wait queue as well
        // kernel uses the wait queue to tell if a process has received a message it was waiting for
        PCB process = waitingProcesses.get(pid);
        addToQueue(process);
    }

    private Queue<PCB> getQueueToRun() {
        int number = random.nextInt(100);
        // Use 6/3/1 scheme
        if (!realTimeProcesses.isEmpty()) {
            if (number < 10 && !backgroundProcesses.isEmpty())
                return backgroundProcesses;
            else if (number < 40 && !interactiveProcesses.isEmpty())
                return interactiveProcesses;
            else
                return realTimeProcesses;
        }
        // use 3/1 scheme
        else if (!interactiveProcesses.isEmpty()) {
            if (number < 25 && !backgroundProcesses.isEmpty())
                return backgroundProcesses;
            else
                return interactiveProcesses;
        }
        // There are only background processes left
        return backgroundProcesses;
    }

    private void wakeUpEligibleProcesses() {
        Instant now = Clock.systemUTC().instant();
        // Filter keySet to only Instants which are before the current time, in other words,
        // these are the processes which can be woken up
        var eligible = sleepingProcesses.keySet().stream().filter(now::isAfter).toList();
        for (Instant instant : eligible) {
            addToQueue(sleepingProcesses.get(instant));
            sleepingProcesses.remove(instant);
        }
    }

    private void addToQueue(PCB pcb) {
        // return a process to its appropriate queue
        switch (pcb.getPriority()) {
            case REAL_TIME -> realTimeProcesses.add(pcb);
            case INTERACTIVE -> interactiveProcesses.add(pcb);
            case BACKGROUND -> backgroundProcesses.add(pcb);
        }
    }

    // Provides a list of all PCBs for the kernel to search through by name and
    // to get a PCB using its PID
    public HashMap<Integer, PCB> getAllLivingPCBs() {
        return livingProcesses;
    }

    public HashMap<Integer, PCB> getWaitingProcesses() {
        return waitingProcesses;
    }

    // Used for testing only
    public Queue<PCB> getRealTimeProcesses() {
        return realTimeProcesses;
    }

    // Used for testing only
    public Queue<PCB> getInteractiveProcesses() {
        return interactiveProcesses;
    }

    // Used for testing only
    public Queue<PCB> getBackgroundProcesses() {
        return backgroundProcesses;
    }

    // Used for testing only
    public HashMap<Instant, PCB> getSleepingProcesses() {
        return sleepingProcesses;
    }
}
