public class NecessaryFreeProcess extends UserlandProcess {

    public void main() {
        int pointer = OS.allocateMemory(81920);
        System.out.printf("Allocated 80 pages, address %d.\n", pointer);
        int pointer2 = OS.allocateMemory(30720);
        System.out.printf("Tried to allocate 30 pages, address %d.\n", pointer2);
        OS.freeMemory(pointer, 81920);
        pointer2 = OS.allocateMemory(30720);
        System.out.printf("After freeing 80 pages, allocated 30 pages, address %d.\n", pointer2);
        int pointer3 = OS.allocateMemory(30720);
        System.out.printf("Additional 30 pages, address %d.\n", pointer3);
        exit();
    }
}
