public class ReceiverProcess extends UserlandProcess {

    @Override
    public void main() {
        int pid = OS.getCurrentPID();
        System.out.printf("I'm a receiver (PID %d)!%n", pid);
        // Attempt to find the Pong process
        int senderPID = -1;
        while(senderPID == -1) {
            senderPID = OS.getPIDByName("SenderProcess");
            cooperate();
        }

        while (true) {
            KernelMessage received = OS.waitForMessage();
            System.out.printf("Receiver: %s%n", received.toString());
            KernelMessage message = new KernelMessage(pid, senderPID, 0, null);
            OS.sendKernelMessage(message);
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}
            cooperate();
        }
    }
}
