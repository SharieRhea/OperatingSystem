import java.util.*;

public class Scheduler {
    private final Random random = new Random();
    public PCB currentPCB = null;
    private final Queue<PCB> realTimeProcesses = new ArrayDeque<>();
    private final Queue<PCB> interactiveProcesses = new ArrayDeque<>();
    private final Queue<PCB> backgroundProcesses = new ArrayDeque<>();

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
        // Move current process to the end of the list as long as it
        // exists and hasn't finished yet
        if (currentPCB != null && !currentPCB.isDone()) {
            switch (currentPCB.getPriority()) {
                case REAL_TIME -> realTimeProcesses.add(currentPCB);
                case INTERACTIVE -> interactiveProcesses.add(currentPCB);
                case BACKGROUND -> backgroundProcesses.add(currentPCB);
            }
        }
        currentPCB = getQueueToRun().poll();
    }

    private Queue<PCB> getQueueToRun() {
        // todo: have to account for queues being empty here somewhere
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
}
