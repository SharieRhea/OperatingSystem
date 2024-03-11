import java.nio.ByteBuffer;

public class PingProcess extends UserlandProcess {

    @Override
    public void main() {
        int pid = OS.getCurrentPID();
        System.out.printf("I'm ping (PID %d)!%n", pid);
        // Attempt to find the Pong process
        int pongPID = -1;
        while(pongPID == -1) {
            pongPID = OS.getPIDByName("PongProcess");
            cooperate();
        }

        int increment = 0;

        while (true) {
            KernelMessage message = new KernelMessage(pid, pongPID, 0, ByteBuffer.allocate(4).putInt(increment).array());
            OS.sendKernelMessage(message);
            KernelMessage received = OS.waitForMessage();
            increment = ByteBuffer.wrap(received.getData()).getInt();
            System.out.printf("PING: from %d to %d | %d%n", received.getSenderPID(), received.getReceiverPID(), increment);
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}
            cooperate();
        }
    }
}
