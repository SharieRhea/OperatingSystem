public class Sample2 extends UserlandProcess {

    public void main() {
        while (true) {
            System.out.println("I'm process 2! I never sleep!");
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