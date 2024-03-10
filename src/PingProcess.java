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
            KernelMessage message = new KernelMessage(pid, pongPID, 0, new byte[] {0});
            OS.sendKernelMessage(message);
            KernelMessage received = OS.waitForMessage();
            System.out.printf("PING: from %d to %d%n", received.getSenderPID(), received.getReceiverPID());
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}
            cooperate();
        }
    }
}
