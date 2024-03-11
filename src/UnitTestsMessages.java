import org.junit.Test;

public class UnitTestsMessages {

    @Test
    public void pingPong() throws InterruptedException {
        OS.startup(new PingProcess());
        OS.createProcess(new PongProcess());

        Thread.sleep(10000000);
    }

    @Test
    public void pingPongWithOtherProcessesRunning() throws InterruptedException {
        OS.startup(new PingProcess());
        OS.createProcess(new HelloWorld(), Priority.BACKGROUND);
        OS.createProcess(new GoodbyeWorld(), Priority.BACKGROUND);
        OS.createProcess(new PongProcess());

        Thread.sleep(7000);
    }

    @Test
    public void sendReceive() throws InterruptedException {
        OS.startup(new SenderProcess());
        OS.createProcess(new ReceiverProcess());

        Thread.sleep(5000);
    }

    @Test
    public void pingPongAndSendReceiver() throws InterruptedException {
        // todo: two different sets of messages don't work together for some reason
        OS.startup(new SenderProcess());
        OS.createProcess(new ReceiverProcess());
        OS.createProcess(new PingProcess());
        OS.createProcess(new PongProcess());

        Thread.sleep(1000000000);
    }

    @Test
    public void manyProcesses() throws InterruptedException {
        // todo: class cast exception between sleep and pong (int ending up as return from wait for message)
        OS.startup(new ReceiverProcess());
        OS.createProcess(new PongProcess());
        OS.createProcess(new PingProcess());
        OS.createProcess(new ShortFileProcess());
        OS.createProcess(new RandomFileProcess());
        OS.createProcess(new SleepProcess());
        OS.createProcess(new SenderProcess());

        Thread.sleep(100000);
    }
}
