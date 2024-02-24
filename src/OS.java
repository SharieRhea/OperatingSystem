import java.util.ArrayList;

public class OS {
    public static CallType currentCall;
    public static ArrayList<Object> parameters = new ArrayList<>();
    public static Object returnValue;
    private static Kernel kernel;

    public static void startup(UserlandProcess init, Priority priority) {
        kernel = new Kernel();

        createProcess(new Idle(), Priority.BACKGROUND);
        createProcess(init, priority);
    }

    // Overloaded so that the default priority is interactive
    public static void startup(UserlandProcess init) {
        startup(init, Priority.INTERACTIVE);
    }

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

    public static int open(String s) {
        parameters.clear();
        parameters.add(s);
        currentCall = CallType.Open;

        switchToKernel();
        return (int) returnValue;
    }

    public static void close(int id) {
        parameters.clear();
        parameters.add(id);
        currentCall = CallType.Close;

        switchToKernel();
    }

    public static byte[] read(int id, int size) {
        parameters.clear();
        parameters.add(id);
        parameters.add(size);
        currentCall = CallType.Read;

        switchToKernel();
        return (byte[]) returnValue;
    }

    public static int write(int id, byte[] data) {
        parameters.clear();
        parameters.add(id);
        parameters.add(data);
        currentCall = CallType.Write;

        switchToKernel();
        return (int) returnValue;
    }

    public static void seek(int id, int to) {
        parameters.clear();
        parameters.add(id);
        parameters.add(to);
        currentCall = CallType.Seek;

        switchToKernel();
    }

    private static void switchToKernel() {
        kernel.start();
        PCB currentPCB = kernel.getScheduler().currentPCB;
        if (currentPCB != null)
            currentPCB.stop();
    }

    public static Kernel getKernel() {
        return kernel;
    }
}
