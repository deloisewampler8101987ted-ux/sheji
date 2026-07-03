package airline.model;

public class Customer implements Comparable<Customer> {
    public String name;
    public int ticketCount;
    public int cabinClass; // 1=头等舱, 2=商务舱, 3=经济舱

    public Customer(String name, int ticketCount, int cabinClass) {
        this.name = name;
        this.ticketCount = ticketCount;
        this.cabinClass = cabinClass;
    }

    @Override
    public int compareTo(Customer other) {
        return this.name.compareTo(other.name);
    }

    public String cabinName() {
        return cabinName(cabinClass);
    }

    public static String cabinName(int cabinClass) {
        switch (cabinClass) {
            case 1: return "头等舱";
            case 2: return "商务舱";
            case 3: return "经济舱";
            default: return "未知";
        }
    }

    @Override
    public String toString() {
        return "姓名: " + name + ", 订票数: " + ticketCount + ", 舱位: " + cabinName();
    }
}