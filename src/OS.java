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
        currentCall = CallType.CREATE_PROCESS;

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
        currentCall = CallType.SWITCH_PROCESS;

        switchToKernel();
    }

    public static void sleep(int milliseconds) {
        parameters.clear();
        parameters.add(milliseconds);
        currentCall = CallType.SLEEP;

        switchToKernel();
    }

    public static int open(String s) {
        parameters.clear();
        parameters.add(s);
        currentCall = CallType.OPEN;

        switchToKernel();
        return (int) returnValue;
    }

    public static void close(int id) {
        parameters.clear();
        parameters.add(id);
        currentCall = CallType.CLOSE;

        switchToKernel();
    }

    public static byte[] read(int id, int size) {
        parameters.clear();
        parameters.add(id);
        parameters.add(size);
        currentCall = CallType.READ;

        switchToKernel();
        return (byte[]) returnValue;
    }

    public static int write(int id, byte[] data) {
        parameters.clear();
        parameters.add(id);
        parameters.add(data);
        currentCall = CallType.WRITE;

        switchToKernel();
        return (int) returnValue;
    }

    public static void seek(int id, int to) {
        parameters.clear();
        parameters.add(id);
        parameters.add(to);
        currentCall = CallType.SEEK;

        switchToKernel();
    }

    public static int getCurrentPID() {
        parameters.clear();
        currentCall = CallType.CURRENT_PID;
        switchToKernel();
        return (int) returnValue;
    }

    public static int getPIDByName(String name) {
        parameters.clear();
        parameters.add(name);
        currentCall = CallType.PID_BY_NAME;
        switchToKernel();
        return (int) returnValue;
    }

    public static void sendKernelMessage(KernelMessage message) {
        parameters.clear();
        parameters.add(message);
        currentCall = CallType.SEND_MESSAGE;
        switchToKernel();
    }

    public static KernelMessage waitForMessage() {
        parameters.clear();
        currentCall = CallType.WAIT_FOR_MESSAGE;
        switchToKernel();
        return (KernelMessage) returnValue;
    }

    public static void getMapping(int virtualPageNumber) {
        parameters.clear();
        parameters.add(virtualPageNumber);
        currentCall = CallType.GET_MAPPING;
        switchToKernel();
    }

    public static int allocateMemory(int size) {
        // Size must be multiple of 1024, if not fail allocation
        if (size % 1024 != 0)
            return -1;

        parameters.clear();
        parameters.add(size);
        currentCall = CallType.ALLOCATE;
        switchToKernel();
        return (int) OS.returnValue;
    }

    public static boolean freeMemory(int pointer, int size) {
        // Pointer and size must be multiple of 1024, if not fail free
        if (size % 1024 != 0 || pointer % 1024 != 0)
            return false;

        parameters.clear();
        parameters.add(pointer);
        parameters.add(size);
        currentCall = CallType.FREE;
        switchToKernel();
        return (boolean) OS.returnValue;
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
