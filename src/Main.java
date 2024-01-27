public class Main {

    public static void main(String[] args) {
        OS.startup(new HelloWorld());
        OS.createProcess(new GoodbyeWorld());
    }
}
