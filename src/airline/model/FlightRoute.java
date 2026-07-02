package airline.model;

import airline.datastructure.LinkList;
import airline.datastructure.LinkQueue;

public class FlightRoute {
    public String terminalStation;   // 终点站名
    public String flightNumber;      // 航班号
    public String aircraftNumber;    // 飞机号
    public int flightDay;            // 飞行周日（1=周一, 7=周日）
    public int capacity;             // 乘员定额
    public int remainingTickets;     // 余票量
    public LinkList<Customer> bookedList; // 已订票客户链表
    public LinkQueue<Waiter> waitQueue;         // 等候替补队列

    public FlightRoute(String terminalStation, String flightNumber, String aircraftNumber,
                       int flightDay, int capacity, int remainingTickets) {
        this.terminalStation = terminalStation;
        this.flightNumber = flightNumber;
        this.aircraftNumber = aircraftNumber;
        this.flightDay = flightDay;
        this.capacity = capacity;
        this.remainingTickets = remainingTickets;
        this.bookedList = new LinkList<>();
        this.waitQueue = new LinkQueue<>();
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
        return "终点站: " + terminalStation +
               ", 航班号: " + flightNumber +
               ", 飞机号: " + aircraftNumber +
               ", 飞行日: " + dayOfWeek(flightDay) +
               ", 乘员定额: " + capacity +
               ", 余票量: " + remainingTickets;
    }
}