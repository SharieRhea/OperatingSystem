public class HelloWorld extends UserlandProcess {

    public void main() {
        while (true) {
            System.out.println("Hello world!");
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException interruptedException) {
                System.out.println("Thread interruption: " + interruptedException.getMessage());
            }
            OS.sleep(1000);
            cooperate();
        }
    }
}
