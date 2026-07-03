package airline.service;

import airline.model.*;

public class AirlineService {
    private FlightList flightList;

    //构造方法：初始化航班列表，默认容量为30条航线，并调用init()填充初始数据
    public AirlineService() {
        flightList = new FlightList(30);
        flightList.init();
    }

    /**
     * 获取当前航班列表对象
     * @return FlightList 航班列表
     */
    public FlightList getFlightList() {
        return flightList;
    }

    /**
     * 根据站点名称查询所有经过该站点的航线
     * @param station 站点名称
     * @return 匹配的航线数组
     */
    public FlightRoute[] queryRouteByStation(String station) {
        return flightList.searchByStationAll(station);
    }

    /**
     * 根据起点和终点查询直达航线
     * @param origin      起点站名称
     * @param destination 终点站名称
     * @return 匹配的航线数组
     */
    public FlightRoute[] searchByRoute(String origin, String destination) {
        return flightList.searchByRoute(origin, destination);
    }

    // ==================== 客票预订（按舱位） ====================
    /**
     * 按舱位等级预订机票：检查航班是否存在、票数是否有效、舱位是否合法，
     * 若指定舱位余票充足则扣减余票并将客户加入已订票列表
     * @param flightNum  航班号
     * @param count      订票数量
     * @param name       客户姓名
     * @param cabinClass 舱位等级（1-头等舱，2-商务舱，3-经济舱）
     * @return 订票结果描述字符串
     */
    public String bookTicket(String flightNum, int count, String name, int cabinClass) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) {
            return "航班不存在！";
        }
        if (count <= 0) {
            return "订票数量必须大于0！";
        }
        if (cabinClass < 1 || cabinClass > 3) {
            return "无效的舱位等级！";
        }

        int remaining = route.cabinRem(cabinClass);
        if (remaining >= count) {
            Customer customer = new Customer(name, count, cabinClass);
            route.booked.insert(customer);
            route.reduce(cabinClass, count);
            return "订票成功！\n客户: " + name + ", 票数: " + count
                    + ", 舱位: " + customer.cabinName()
                    + "\n该舱位剩余: " + route.cabinRem(cabinClass);
        } else {
            int totalRem = route.totalRem();
            return "该舱位余票不足！\n" + Customer.cabinName(cabinClass)
                    + "剩余: " + remaining + "，您需要: " + count + " 张"
                    + "\n（该航班总余票: " + totalRem + "）";
        }
    }

    // ==================== 加入等候队列 ====================
    /**
     * 将客户加入候补队列：当指定航班舱位余票不足时，客户可选择候补，
     * 后续有退票时会自动按候补顺序替补订票
     * @param flightNum  航班号
     * @param count      需要票数
     * @param name       客户姓名
     * @param cabinClass 舱位等级
     * @return 候补结果描述字符串（含当前候补位次）
     */
    public String joinWaitQueue(String flightNum, int count, String name, int cabinClass) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) {
            return "航班不存在！";
        }
        Waiter waiter = new Waiter(name, count, cabinClass);
        route.queue.push(waiter);
        int position = route.queue.size();
        return "候补成功！\n客户: " + name + ", 票数: " + count
                + ", 舱位: " + Customer.cabinName(cabinClass)
                + "\n当前候补位次: 第 " + position + " 位";
    }

    // ==================== 推荐同目的地其他航线 ====================
    /**
     * 查找到达同一目的地的其他有余票航线
     * @param excludeFlightNum 排除的航班号
     * @param destination      目的地（终点站）
     * @param cabinClass       舱位等级
     * @param needTickets      需要的票数
     * @return 匹配的航线数组
     */
    public FlightRoute[] recommendSameDestination(String excludeFlightNum,
            String destination, int cabinClass, int needTickets) {
        int matchCount = 0;
        for (int i = 0; i < flightList.getCount(); i++) {
            FlightRoute r = flightList.getRoute(i);
            if (!r.flightNo.equals(excludeFlightNum)
                    && r.dest.equals(destination)
                    && r.cabinRem(cabinClass) >= needTickets) {
                matchCount++;
            }
        }
        FlightRoute[] result = new FlightRoute[matchCount];
        int idx = 0;
        for (int i = 0; i < flightList.getCount(); i++) {
            FlightRoute r = flightList.getRoute(i);
            if (!r.flightNo.equals(excludeFlightNum)
                    && r.dest.equals(destination)
                    && r.cabinRem(cabinClass) >= needTickets) {
                result[idx++] = r;
            }
        }
        return result;
    }

    // ==================== 退票处理 ====================
    /**
     * 退票处理：根据航班号和客户姓名查找订票记录，删除该记录并释放对应舱位的余票，
     * 退票后自动处理候补队列，将释放的票按顺序分配给等候客户
     * @param flightNum 航班号
     * @param name      客户姓名
     * @return 退票结果描述字符串（含替补信息）
     */
    public String refundTicket(String flightNum, String name) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) {
            return "航班不存在！";
        }

        Customer customer = route.booked.search(name);
        if (customer == null) {
            return "未找到该客户的订票记录！";
        }

        int releasedTickets = customer.ticketCount;
        int cabinClass = customer.cabinClass;
        boolean deleted = route.booked.delete(name);
        if (!deleted) {
            return "退票失败！";
        }

        route.increase(cabinClass, releasedTickets);
        StringBuilder sb = new StringBuilder();
        sb.append("退票成功！客户: ").append(name)
          .append("，释放 ").append(Customer.cabinName(cabinClass))
          .append(" ").append(releasedTickets).append(" 张")
          .append("\n该舱位剩余: ").append(route.cabinRem(cabinClass));

        String subResult = processWaitQueue(route);
        if (!subResult.isEmpty()) {
            sb.append("\n").append(subResult);
        }
        return sb.toString();
    }

    /**
     * 处理候补队列：退票后自动遍历等候队列，按FIFO顺序将释放的余票分配给候补客户，
     * 若某候补客户所需票数大于当前舱位余票则停止继续分配
     * @param route 航班航线对象
     * @return 替补结果描述字符串
     */
    private String processWaitQueue(FlightRoute route) {
        StringBuilder sb = new StringBuilder();
        int substituted = 0;
        while (!route.queue.empty()) {
            Waiter waiter = route.queue.peek();
            int remaining = route.cabinRem(waiter.cabinClass);
            if (remaining >= waiter.ticketCount) {
                route.queue.pop();
                Customer customer = new Customer(waiter.name, waiter.ticketCount, waiter.cabinClass);
                route.booked.insert(customer);
                route.reduce(waiter.cabinClass, waiter.ticketCount);
                substituted++;
                sb.append("替补订票成功: ").append(waiter.name)
                  .append("，").append(Customer.cabinName(waiter.cabinClass))
                  .append(" ").append(waiter.ticketCount).append(" 张\n");
            } else {
                break;
            }
        }
        if (substituted > 0) {
            sb.append("共替补 ").append(substituted).append(" 位等候客户");
        }
        return sb.toString();
    }
}