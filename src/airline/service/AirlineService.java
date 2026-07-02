package airline.service;

import airline.model.*;
import airline.datastructure.LinkNode;
import java.time.LocalDate;

public class AirlineService {
    private FlightList flightList;

    public AirlineService() {
        this.flightList = new FlightList(10);
        this.flightList.init();
    }

    public FlightList getFlightList() {
        return flightList;
    }

    // ==================== 航线查询 ====================
    public String queryRoute(String station) {
        FlightRoute route = flightList.searchByStation(station);
        if (route == null) {
            return "无此航线。";
        }
        String nearestDate = calculateNearestFlightDate(route.flightDay);
        return route.toString() + "\n最近航班日期: " + nearestDate;
    }

    private String calculateNearestFlightDate(int flightDay) {
        LocalDate today = LocalDate.now();
        int todayDayOfWeek = today.getDayOfWeek().getValue();
        int daysUntil = (flightDay - todayDayOfWeek + 7) % 7;
        if (daysUntil == 0) {
            daysUntil = 7;
        }
        return today.plusDays(daysUntil).toString();
    }

    // ==================== 客票预订 ====================
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

        if (route.remainingTickets >= count) {
            Customer customer = new Customer(name, count, cabinClass);
            route.bookedList.insert(customer);
            route.remainingTickets -= count;
            return "订票成功！\n客户: " + name + ", 票数: " + count
                    + ", 舱位: " + customer.getCabinName()
                    + "\n当前余票: " + route.remainingTickets;
        } else {
            return "余票不足！当前余票: " + route.remainingTickets + "，您需要: " + count + " 张";
        }
    }

    // ==================== 加入等候队列 ====================
    public String joinWaitQueue(String flightNum, int count, String name) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) {
            return "航班不存在！";
        }
        Waiter waiter = new Waiter(name, count);
        route.waitQueue.push(waiter);
        return "已加入等候队列，当前队列长度: " + route.waitQueue.size();
    }

    // ==================== 推荐其他航班 ====================
    public String recommendOtherFlights(FlightRoute except, int needTickets) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < flightList.getCount(); i++) {
            FlightRoute r = flightList.getRoute(i);
            if (r != except && r.remainingTickets >= needTickets) {
                sb.append(r.toString()).append("\n");
                found = true;
            }
        }
        if (!found) {
            sb.append("（暂无可满足需求的航班）");
        }
        return sb.toString();
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
        boolean deleted = route.bookedList.delete(name);
        if (!deleted) {
            return "退票失败！";
        }

        route.remainingTickets += releasedTickets;
        StringBuilder sb = new StringBuilder();
        sb.append("退票成功！客户: ").append(name)
          .append("，释放票数: ").append(releasedTickets)
          .append("\n当前余票: ").append(route.remainingTickets);

        // 处理等候队列 FIFO
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
            if (route.remainingTickets >= waiter.ticketCount) {
                route.waitQueue.pop();
                Customer customer = new Customer(waiter.name, waiter.ticketCount, 3);
                route.bookedList.insert(customer);
                route.remainingTickets -= waiter.ticketCount;
                substituted++;
                sb.append("替补订票成功: ").append(waiter.name)
                  .append("，票数: ").append(waiter.ticketCount).append("\n");
            } else {
                break;
            }
        }
        if (substituted > 0) {
            sb.append("共替补 ").append(substituted)
              .append(" 位等候客户，当前余票: ").append(route.remainingTickets);
        }
        return sb.toString();
    }

    // ==================== 获取已订票客户信息 ====================
    public String getBookedCustomersInfo(String flightNum) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) return null;
        if (route.bookedList.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        LinkNode<Customer> curr = route.bookedList.getHead();
        while (curr != null) {
            sb.append(curr.data.toString()).append("\n");
            curr = curr.next;
        }
        return sb.toString();
    }

    // ==================== 获取等候队列信息 ====================
    public String getWaitQueueInfo(String flightNum) {
        FlightRoute route = flightList.searchByFlight(flightNum);
        if (route == null) return null;
        if (route.waitQueue.empty()) return "";

        StringBuilder sb = new StringBuilder();
        LinkNode<Waiter> curr = route.waitQueue.getFront();
        while (curr != null) {
            sb.append(curr.data.toString()).append("\n");
            curr = curr.next;
        }
        return sb.toString();
    }

    // ==================== 获取所有航线信息 ====================
    public String getAllRoutesInfo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < flightList.getCount(); i++) {
            sb.append(flightList.getRoute(i).toString()).append("\n");
        }
        return sb.toString();
    }
}