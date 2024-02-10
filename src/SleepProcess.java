public class SleepProcess extends UserlandProcess {

    public void main() {
        while (true) {
            // TODO: figure out how to get rid of the thread.sleep in here, without it we never get to create the next process
            System.out.println("Going to sleep...");
            /*try {
                Thread.sleep(50);
            }
            catch (InterruptedException interruptedException) {
                System.out.println("Thread interruption: " + interruptedException.getMessage());
            }*/
            OS.sleep(500);
            System.out.println("I woke up!");
            cooperate();
        }
    }
}
