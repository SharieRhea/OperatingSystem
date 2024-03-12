public class SenderProcess extends UserlandProcess {

    @Override
    public void main() {
        int pid = OS.getCurrentPID();
        System.out.printf("I'm a sender (PID %d)!%n", pid);
        // Attempt to find the Pong process
        int receiverPID = -1;
        while(receiverPID == -1) {
            receiverPID = OS.getPIDByName("ReceiverProcess");
            cooperate();
        }

        while (true) {
            KernelMessage message = new KernelMessage(pid, receiverPID, 0, null);
            OS.sendKernelMessage(message);
            KernelMessage received = OS.waitForMessage();
            System.out.printf("Sender: %s%n", received.toString());
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}
            cooperate();
        }
    }
}
