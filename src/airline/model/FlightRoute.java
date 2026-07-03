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

    /**
     * 构造方法：初始化航线所有属性，包括站点、航班信息、各舱位定额与余票，
     * 并初始化已订票链表和候补队列
     */
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

    /**
     * 获取指定舱位的剩余票数
     * @param cabinClass 舱位等级（1-头等舱，2-商务舱，3-经济舱）
     * @return 该舱位剩余票数，无效舱位返回0
     */
    public int cabinRem(int cabinClass) {
        switch (cabinClass) {
            case 1: return firstRem;
            case 2: return bizRem;
            case 3: return ecoRem;
            default: return 0;
        }
    }

    /**
     * 获取指定舱位的定额（总票数）
     * @param cabinClass 舱位等级（1-头等舱，2-商务舱，3-经济舱）
     * @return 该舱位定额，无效舱位返回0
     */
    public int cabinCap(int cabinClass) {
        switch (cabinClass) {
            case 1: return firstCap;
            case 2: return bizCap;
            case 3: return ecoCap;
            default: return 0;
        }
    }

    /**
     * 扣减指定舱位的余票数量（订票时调用）
     * @param cabinClass 舱位等级
     * @param count      扣减数量
     */
    public void reduce(int cabinClass, int count) {
        switch (cabinClass) {
            case 1: firstRem -= count; break;
            case 2: bizRem -= count; break;
            case 3: ecoRem -= count; break;
        }
    }

    /**
     * 增加指定舱位的余票数量（退票时调用）
     * @param cabinClass 舱位等级
     * @param count      增加数量
     */
    public void increase(int cabinClass, int count) {
        switch (cabinClass) {
            case 1: firstRem += count; break;
            case 2: bizRem += count; break;
            case 3: ecoRem += count; break;
        }
    }

    /**
     * 获取该航班所有舱位的总剩余票数
     * @return 头等舱 + 商务舱 + 经济舱余票之和
     */
    public int totalRem() {
        return firstRem + bizRem + ecoRem;
    }

    /**
     * 获取该航班所有舱位的总票数
     * @return 头等舱 + 商务舱 + 经济舱定额之和
     */
    public int totalCap() {
        return firstCap + bizCap + ecoCap;
    }

    /**
     * 将数字（1-7）转换为中文星期表示
     * @param day 飞行日数字（1=周一 ... 7=周日）
     * @return 中文星期字符串，无效数字返回"未知"
     */
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

    /**
     * 返回航线的完整信息字符串，包含起终点、航班号、飞机号、飞行日及各舱位余票/定额
     * @return 格式化后的航线信息
     */
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