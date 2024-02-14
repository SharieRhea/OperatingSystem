public class SleepProcess extends UserlandProcess {

    public void main() {
        while (true) {
            System.out.println("Going to sleep...");
            OS.sleep(500);
            System.out.println("I woke up!");
            cooperate();
        }
    }
}
