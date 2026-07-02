package airline.model;

import airline.datastructure.LinkList;
import airline.datastructure.LinkQueue;

public class FlightRoute {
    public String originStation;       // 起始站名
    public String terminalStation;     // 终点站名
    public String flightNumber;        // 航班号
    public String aircraftNumber;      // 飞机号
    public int flightDay;              // 飞行周日（1=周一, 7=周日）

    // 各舱位定额
    public int firstClassCapacity;
    public int businessCapacity;
    public int economyCapacity;

    // 各舱位余票
    public int firstClassRemaining;
    public int businessRemaining;
    public int economyRemaining;

    public LinkList<Customer> bookedList;
    public LinkQueue<Waiter> waitQueue;

    public FlightRoute(String originStation, String terminalStation, String flightNumber,
                       String aircraftNumber, int flightDay,
                       int firstClassCapacity, int firstClassRemaining,
                       int businessCapacity, int businessRemaining,
                       int economyCapacity, int economyRemaining) {
        this.originStation = originStation;
        this.terminalStation = terminalStation;
        this.flightNumber = flightNumber;
        this.aircraftNumber = aircraftNumber;
        this.flightDay = flightDay;
        this.firstClassCapacity = firstClassCapacity;
        this.firstClassRemaining = firstClassRemaining;
        this.businessCapacity = businessCapacity;
        this.businessRemaining = businessRemaining;
        this.economyCapacity = economyCapacity;
        this.economyRemaining = economyRemaining;
        this.bookedList = new LinkList<>();
        this.waitQueue = new LinkQueue<>();
    }

    /** 获取指定舱位的余票量 */
    public int getRemainingByCabin(int cabinClass) {
        switch (cabinClass) {
            case 1: return firstClassRemaining;
            case 2: return businessRemaining;
            case 3: return economyRemaining;
            default: return 0;
        }
    }

    /** 获取指定舱位的定额 */
    public int getCapacityByCabin(int cabinClass) {
        switch (cabinClass) {
            case 1: return firstClassCapacity;
            case 2: return businessCapacity;
            case 3: return economyCapacity;
            default: return 0;
        }
    }

    /** 扣减指定舱位余票 */
    public void reduceRemaining(int cabinClass, int count) {
        switch (cabinClass) {
            case 1: firstClassRemaining -= count; break;
            case 2: businessRemaining -= count; break;
            case 3: economyRemaining -= count; break;
        }
    }

    /** 增加指定舱位余票 */
    public void increaseRemaining(int cabinClass, int count) {
        switch (cabinClass) {
            case 1: firstClassRemaining += count; break;
            case 2: businessRemaining += count; break;
            case 3: economyRemaining += count; break;
        }
    }

    /** 总余票量 */
    public int totalRemaining() {
        return firstClassRemaining + businessRemaining + economyRemaining;
    }

    /** 总定额 */
    public int totalCapacity() {
        return firstClassCapacity + businessCapacity + economyCapacity;
    }

    public static String dayOfWeek(int day) {
        switch (day) {
            case 1: return "周一";
            case 2: return "周二";
            case 3: return "周三";
            case 4: return "周四";
            case 5: return "周五";
            case 6: return "周六";
            case 7: return "周日";
            default: return "未知";
        }
    }

    @Override
    public String toString() {
        return originStation + " → " + terminalStation +
               ", 航班号: " + flightNumber +
               ", 飞机号: " + aircraftNumber +
               ", 飞行日: " + dayOfWeek(flightDay) +
               ", 头等舱: " + firstClassRemaining + "/" + firstClassCapacity +
               ", 商务舱: " + businessRemaining + "/" + businessCapacity +
               ", 经济舱: " + economyRemaining + "/" + economyCapacity;
    }
}