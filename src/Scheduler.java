import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private LinkedList<UserlandProcess> processes = new LinkedList<>();
    private Timer timer;
    public UserlandProcess currentProcess = null;
    private int pidCounter = 0;

    public Scheduler() {
        timer = new Timer();
        TimerTask interrupt = new TimerTask() {
            @Override
            public void run() {
                if (currentProcess != null)
                    currentProcess.requestStop();
            }
        };
        // Schedule an interrupt to occur every 250ms and request the currentProcess to stop
        timer.scheduleAtFixedRate(interrupt, 0L, 250L);
    }

    public int createProcess(UserlandProcess up) {
        processes.add(up);
        // Starts the first process
        if (currentProcess == null)
            switchProcess();
        pidCounter++;
        return pidCounter;
    }

    public void switchProcess() {
        // Move current process to the end of the list as long as it
        // exists and hasn't finished yet
        if (currentProcess != null && !currentProcess.isDone()) {
            processes.add(currentProcess);
            processes.removeFirst();
        }
        currentProcess = processes.getFirst();
    }
}
