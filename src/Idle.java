public class Idle extends UserlandProcess {

    public void main() {
        while (true) {
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException interruptedException) {
                System.out.println("Thread interruption: " + interruptedException.getMessage());
            }
            cooperate();
        }
    }
}
