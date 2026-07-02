package airline.model;

public class Waiter {
    public String name;
    public int ticketCount;

    public Waiter(String name, int ticketCount) {
        this.name = name;
        this.ticketCount = ticketCount;
    }

    @Override
    public String toString() {
        return "姓名: " + name + ", 需要票数: " + ticketCount;
    }
}