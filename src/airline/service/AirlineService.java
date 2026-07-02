package airline.service;

import airline.model.*;
import airline.datastructure.LinkNode;

public class AirlineService {
    private FlightList flightList;

    public AirlineService() {
        this.flightList = new FlightList(30);
        this.flightList.init();
    }

    public FlightList getFlightList() {
        return flightList;
    }

    public FlightRoute[] queryRouteByStation(String station) {
        return flightList.searchByStationAll(station);
    }

    public FlightRoute[] searchByRoute(String origin, String destination) {
        return flightList.searchByRoute(origin, destination);
    }

    // ==================== 客票预订（按舱位） ====================
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

        int remaining = route.getRemainingByCabin(cabinClass);
        if (remaining >= count) {
            Customer customer = new Customer(name, count, cabinClass);
            route.bookedList.insert(customer);
            route.reduceRemaining(cabinClass, count);
            return "订票成功！\n客户: " + name + ", 票数: " + count
                    + ", 舱位: " + customer.getCabinName()
                    + "\n该舱位剩余: " + route.getRemainingByCabin(cabinClass);
        } else {
            int totalRemaining = route.totalRemaining();
            return "该舱位余票不足！\n" + Customer.getCabinNameStatic(cabinClass)
                    + "剩余: " + remaining + "，您需要: " + count + " 张"
                    + "\n（该航班总余票: " + totalRemaining + "）";
        }
    }

    // ==================== 加入等候队列 ====================
    public String joinWaitQueue(String flightNum, int count, String name, int cabinClass) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) {
            return "航班不存在！";
        }
        Waiter waiter = new Waiter(name, count, cabinClass);
        route.waitQueue.push(waiter);
        int position = route.waitQueue.size();
        return "候补成功！\n客户: " + name + ", 票数: " + count
                + ", 舱位: " + Customer.getCabinNameStatic(cabinClass)
                + "\n当前候补位次: 第 " + position + " 位";
    }

    // ==================== 退票处理 ====================
    public String refundTicket(String flightNum, String name) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) {
            return "航班不存在！";
        }

        Customer customer = route.bookedList.search(name);
        if (customer == null) {
            return "未找到该客户的订票记录！";
        }

        int releasedTickets = customer.ticketCount;
        int cabinClass = customer.cabinClass;
        boolean deleted = route.bookedList.delete(name);
        if (!deleted) {
            return "退票失败！";
        }

        route.increaseRemaining(cabinClass, releasedTickets);
        StringBuilder sb = new StringBuilder();
        sb.append("退票成功！客户: ").append(name)
          .append("，释放 ").append(Customer.getCabinNameStatic(cabinClass))
          .append(" ").append(releasedTickets).append(" 张")
          .append("\n该舱位剩余: ").append(route.getRemainingByCabin(cabinClass));

        String subResult = processWaitQueue(route);
        if (!subResult.isEmpty()) {
            sb.append("\n").append(subResult);
        }
        return sb.toString();
    }

    private String processWaitQueue(FlightRoute route) {
        StringBuilder sb = new StringBuilder();
        int substituted = 0;
        while (!route.waitQueue.empty()) {
            Waiter waiter = route.waitQueue.peek();
            int remaining = route.getRemainingByCabin(waiter.cabinClass);
            if (remaining >= waiter.ticketCount) {
                route.waitQueue.pop();
                Customer customer = new Customer(waiter.name, waiter.ticketCount, waiter.cabinClass);
                route.bookedList.insert(customer);
                route.reduceRemaining(waiter.cabinClass, waiter.ticketCount);
                substituted++;
                sb.append("替补订票成功: ").append(waiter.name)
                  .append("，").append(Customer.getCabinNameStatic(waiter.cabinClass))
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