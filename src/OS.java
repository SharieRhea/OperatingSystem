import java.util.ArrayList;

public class OS {
    private static Kernel kernel;
    public static CallType currentCall;
    public static ArrayList<Object> parameters = new ArrayList<>();
    public static Object returnValue;

    public static int createProcess(UserlandProcess up) {
        parameters.clear();
        parameters.add(up);
        currentCall = CallType.CreateProcess;
        kernel.start();
        // if scheduler has a currentProcess, call stop on it
        UserlandProcess currentProcess = kernel.getScheduler().currentProcess;
        if (currentProcess != null) {
            currentProcess.stop();
        }
        else {
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException interruptedException) {
                System.out.println("Interruption: " + interruptedException.getMessage());
            }
            /*while (true) {
                try {
                    Thread.sleep(10);
                }
                catch (InterruptedException interruptedException) {
                    System.out.println("Interruption: " + interruptedException.getMessage());
                }
            }*/
        }
        // cast and return the return value
        return (int) returnValue;
    }

    public static void switchProcess() {
        currentCall = CallType.SwitchProcess;
    }

    public static void startup(UserlandProcess init) {
        // creates the kernel()
        kernel = new Kernel();
        // calls createProcess once for init and once for idle
        createProcess(init);
        createProcess(new Idle());
    }
}
