public class PongProcess extends UserlandProcess {
    @Override
    public void main() {
        int pid = OS.getCurrentPID();
        System.out.printf("I'm pong (PID %d)!%n", pid);
        // Attempt to find the Pong process
        int pingPID = -1;
        while(pingPID == -1) {
            pingPID = OS.getPIDByName("PingProcess");
            cooperate();
        }

        while (true) {
            KernelMessage received = OS.waitForMessage();
            System.out.printf("PONG: from %d to %d%n", received.getSenderPID(), received.getReceiverPID());
            KernelMessage message = new KernelMessage(pid, pingPID, 0, new byte[0]);
            OS.sendKernelMessage(message);
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}
            cooperate();
        }
    }
}
