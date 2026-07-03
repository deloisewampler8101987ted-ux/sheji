package airline.model;

import airline.datastructure.LinkList;
import airline.datastructure.LinkQueue;

public class FlightRoute {
    public String origin;       // 起始站
    public String dest;         // 终点站
    public String flightNo;     // 航班号
    public String planeNo;      // 飞机号
    public int day;             // 飞行日（1=周一, 7=周日）

    public int firstCap;        // 头等舱定额
    public int bizCap;          // 商务舱定额
    public int ecoCap;          // 经济舱定额

    public int firstRem;        // 头等舱余票
    public int bizRem;          // 商务舱余票
    public int ecoRem;          // 经济舱余票

    public LinkList<Customer> booked;
    public LinkQueue<Waiter> queue;

    public FlightRoute(String origin, String dest, String flightNo,
                       String planeNo, int day,
                       int firstCap, int firstRem,
                       int bizCap, int bizRem,
                       int ecoCap, int ecoRem) {
        this.origin = origin;
        this.dest = dest;
        this.flightNo = flightNo;
        this.planeNo = planeNo;
        this.day = day;
        this.firstCap = firstCap;
        this.firstRem = firstRem;
        this.bizCap = bizCap;
        this.bizRem = bizRem;
        this.ecoCap = ecoCap;
        this.ecoRem = ecoRem;
        this.booked = new LinkList<>();
        this.queue = new LinkQueue<>();
    }

    public int cabinRem(int cabinClass) {
        switch (cabinClass) {
            case 1: return firstRem;
            case 2: return bizRem;
            case 3: return ecoRem;
            default: return 0;
        }
    }

    public int cabinCap(int cabinClass) {
        switch (cabinClass) {
            case 1: return firstCap;
            case 2: return bizCap;
            case 3: return ecoCap;
            default: return 0;
        }
    }

    public void reduce(int cabinClass, int count) {
        switch (cabinClass) {
            case 1: firstRem -= count; break;
            case 2: bizRem -= count; break;
            case 3: ecoRem -= count; break;
        }
    }

    public void increase(int cabinClass, int count) {
        switch (cabinClass) {
            case 1: firstRem += count; break;
            case 2: bizRem += count; break;
            case 3: ecoRem += count; break;
        }
    }

    public int totalRem() {
        return firstRem + bizRem + ecoRem;
    }

    public int totalCap() {
        return firstCap + bizCap + ecoCap;
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
        return origin + " → " + dest +
               ", 航班号: " + flightNo +
               ", 飞机号: " + planeNo +
               ", 飞行日: " + dayOfWeek(day) +
               ", 头等舱: " + firstRem + "/" + firstCap +
               ", 商务舱: " + bizRem + "/" + bizCap +
               ", 经济舱: " + ecoRem + "/" + ecoCap;
    }
}