import java.util.ArrayList;

public class OS {
    private static Kernel kernel;
    public static CallType currentCall;
    public static ArrayList<Object> parameters = new ArrayList<>();
    public static Object returnValue;

    public static int createProcess(UserlandProcess up) {
        // Set up shared information between OS and Kernel
        parameters.clear();
        parameters.add(up);
        currentCall = CallType.CreateProcess;
        kernel.start();

        UserlandProcess currentProcess = kernel.getScheduler().currentProcess;
        if (currentProcess != null) {
            currentProcess.stop();
        }
        else {
            // Creating the very first process, wait for the Kernel to finish creating it
            while (kernel.getScheduler().currentProcess == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException interruptedException) {
                    System.out.println("Interruption: " + interruptedException.getMessage());
                }
            }
        }
        // This is the pid for the process that was just created
        return (int) returnValue;
    }

    public static void switchProcess() {
        // Set up shared information between OS and Kernel
        parameters.clear();
        currentCall = CallType.SwitchProcess;
        kernel.start();

        UserlandProcess currentProcess = kernel.getScheduler().currentProcess;
        if (currentProcess != null) {
            currentProcess.stop();
        }
    }

    public static void startup(UserlandProcess init) {
        kernel = new Kernel();

        createProcess(init);
        createProcess(new Idle());
    }
}
