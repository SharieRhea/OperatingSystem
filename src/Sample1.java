public class Sample1 extends UserlandProcess {

    public void main() {
        while (true) {
            System.out.println("I'm process 1! Going to sleep...");
            OS.sleep(500);
            System.out.println("Process 1 woke up!");
            cooperate();
        }
    }
}
