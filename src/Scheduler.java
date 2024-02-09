import java.time.Clock;
import java.time.Instant;
import java.util.*;

public class Scheduler {
    private final Random random = new Random();
    private final Queue<PCB> realTimeProcesses = new ArrayDeque<>();
    private final Queue<PCB> interactiveProcesses = new ArrayDeque<>();
    private final Queue<PCB> backgroundProcesses = new ArrayDeque<>();
    private final HashMap<Instant, PCB> sleepingProcesses = new HashMap<>();
    public PCB currentPCB = null;

    public Scheduler() {
        Timer timer = new Timer();
        TimerTask interrupt = new TimerTask() {
            @Override
            public void run() {
                if (currentPCB != null && currentPCB.getUserlandProcess() != null)
                    currentPCB.getUserlandProcess().requestStop();
            }
        };
        // Schedule an interrupt to occur every 250ms and request the currentProcess to stop
        timer.scheduleAtFixedRate(interrupt, 0L, 250L);
    }

    public int createProcess(UserlandProcess userlandProcess, Priority priority) {
        PCB pcb = new PCB(userlandProcess, priority);
        switch (priority) {
            case REAL_TIME -> realTimeProcesses.add(pcb);
            case INTERACTIVE -> interactiveProcesses.add(pcb);
            case BACKGROUND -> backgroundProcesses.add(pcb);
        }
        // Starts the first process
        if (currentPCB == null)
            switchProcess();
        return pcb.getPID();
    }

    public void switchProcess() {
        // First, check to see if any sleeping process are eligible to run now
        wakeUpEligibleProcesses();
        // Move current process to the end of the list as long as it
        // exists and hasn't finished yet
        if (currentPCB != null && !currentPCB.isDone())
            addToQueue(currentPCB);
        currentPCB = getQueueToRun().poll();
    }

    public void sleep(int milliseconds) {
        // take the current time and add milliseconds to it
        // put process into map time -> process
        Instant timeToWake = Clock.systemUTC().instant().plusMillis(milliseconds);
        sleepingProcesses.put(timeToWake, currentPCB);
        // DON'T call switchProcess because this process should not be added to a queue until
        // ready to wake, just run a new process
        currentPCB = getQueueToRun().poll();
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
        switch (pcb.getPriority()) {
            case REAL_TIME -> realTimeProcesses.add(pcb);
            case INTERACTIVE -> interactiveProcesses.add(pcb);
            case BACKGROUND -> backgroundProcesses.add(pcb);
        }
    }
}
