import java.util.ArrayList;

public class OS {
    public static CallType currentCall;
    public static ArrayList<Object> parameters = new ArrayList<>();
    public static Object returnValue;
    private static Kernel kernel;

    public static int createProcess(UserlandProcess userlandProcess, Priority priority) {
        // Set up shared information between OS and Kernel
        parameters.clear();
        parameters.add(userlandProcess);
        parameters.add(priority);
        currentCall = CallType.CreateProcess;

        switchToKernel();
        // Creating the very first process, wait for the Kernel to finish creating it
        while (kernel.getScheduler().currentPCB == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException interruptedException) {
                System.out.println("Interruption: " + interruptedException.getMessage());
            }
        }
        // This is the pid for the process that was just created
        return (int) returnValue;
    }

    // Overloaded so that the default priority is interactive
    public static int createProcess(UserlandProcess userlandProcess) {
        return createProcess(userlandProcess, Priority.INTERACTIVE);
    }

    public static void switchProcess() {
        // Set up shared information between OS and Kernel
        parameters.clear();
        currentCall = CallType.SwitchProcess;

        switchToKernel();
    }

    public static void sleep(int milliseconds) {
        parameters.clear();
        parameters.add(milliseconds);
        currentCall = CallType.Sleep;

        switchToKernel();
    }

    private static void switchToKernel() {
        kernel.start();
        PCB currentPCB = kernel.getScheduler().currentPCB;
        if (currentPCB != null)
            currentPCB.stop();
    }

    public static void startup(UserlandProcess init, Priority priority) {
        kernel = new Kernel();

        createProcess(new Idle(), Priority.BACKGROUND);
        createProcess(init, priority);
    }

    // Overloaded so that the default priority is interactive
    public static void startup(UserlandProcess init) {
        startup(init, Priority.INTERACTIVE);
    }

    // Used for testing only.
    public static Kernel getKernel() {
        return kernel;
    }
}
