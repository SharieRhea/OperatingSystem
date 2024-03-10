import org.junit.Test;

public class UnitTestsMessages {

    @Test
    public void pingPong() throws InterruptedException {
        OS.startup(new PingProcess());
        OS.createProcess(new PongProcess());

        Thread.sleep(5000);
    }
}
