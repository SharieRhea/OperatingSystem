public class GoodbyeWorld extends UserlandProcess {

    public void main() {
        while (true) {
            System.out.println("Goodbye world!");
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
