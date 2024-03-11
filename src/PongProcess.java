import java.nio.ByteBuffer;

public class PongProcess extends UserlandProcess {
    @Override
    public void main() {
        int pid = OS.getCurrentPID();
        System.out.printf("I'm pong (PID %d)!%n", pid);
        // Attempt to find the Ping process
        int pingPID = -1;
        while(pingPID == -1) {
            pingPID = OS.getPIDByName("PingProcess");
            cooperate();
        }

        int increment;

        while (true) {
            KernelMessage received = OS.waitForMessage();
            increment = ByteBuffer.wrap(received.getData()).getInt();
            System.out.printf("PONG: from %d to %d | %d%n", received.getSenderPID(), received.getReceiverPID(), increment);
            increment++;
            KernelMessage message = new KernelMessage(pid, pingPID, 0, ByteBuffer.allocate(4).putInt(increment).array());
            OS.sendKernelMessage(message);
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}
            cooperate();
        }
    }
}
