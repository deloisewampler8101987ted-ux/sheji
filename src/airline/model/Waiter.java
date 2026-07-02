package airline.model;

public class Waiter {
    public String name;
    public int ticketCount;
    public int cabinClass; // 1=头等舱, 2=商务舱, 3=经济舱

    public Waiter(String name, int ticketCount, int cabinClass) {
        this.name = name;
        this.ticketCount = ticketCount;
        this.cabinClass = cabinClass;
    }

    @Override
    public String toString() {
        String cabin = "经济舱";
        if (cabinClass == 1) cabin = "头等舱";
        else if (cabinClass == 2) cabin = "商务舱";
        return "姓名: " + name + ", 需要票数: " + ticketCount + ", 舱位: " + cabin;
    }
}