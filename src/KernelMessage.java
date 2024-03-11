public class KernelMessage {
    private int senderPID;
    private int receiverPID;
    private int type;
    private byte[] data;

    public KernelMessage(int sender, int receiver, int type, byte[] data) {
        senderPID = sender;
        receiverPID = receiver;
        this.type = type;
        this.data = data;
    }

    // Copy constructor used for creating a copy for the receiver
    public KernelMessage(KernelMessage message) {
        senderPID = message.senderPID;
        receiverPID = message.receiverPID;
        type = message.type;
        data = message.data;
    }

    public void setSenderPID(int pid) {
        senderPID = pid;
    }

    public int getReceiverPID() {
        return receiverPID;
    }

    public int getSenderPID() {
        return senderPID;
    }

    public byte[] getData() {
        return data;
    }
}
