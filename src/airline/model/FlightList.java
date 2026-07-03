package airline.model;

public class FlightList {
    private FlightRoute[] routes;
    private int count;

    public FlightList(int capacity) {
        this.routes = new FlightRoute[capacity];
        this.count = 0;
    }

    public void init() {
        // 参数: 起始站, 终点站, 航班号, 飞机号, 飞行日,
        //       头等定额, 头等余票, 商务定额, 商务余票, 经济定额, 经济余票
        addRoute(new FlightRoute("上海", "北京", "CA1111", "B1111", 1,
                20, 15, 50, 40, 80, 65));
        addRoute(new FlightRoute("北京", "上海", "MU2222", "B2222", 3,
                60, 60, 60, 60, 115, 100));
        addRoute(new FlightRoute("北京", "上海", "MU3333", "B3333", 3,
                60, 60, 60, 60, 115, 100));
        addRoute(new FlightRoute("北京", "广州", "CZ3333", "A3333", 5,
                20, 2, 50, 3, 110, 0));
        addRoute(new FlightRoute("上海", "深圳", "ZH1234", "A1234", 7,
                20, 20, 50, 50, 90, 90));
        addRoute(new FlightRoute("广州", "成都", "CA5678", "B5678", 2,
                20, 0, 40, 0, 80, 0));
        addRoute(new FlightRoute("北京", "深圳", "HU7788", "B9900", 4,
                20, 10, 50, 15, 100, 25));
        addRoute(new FlightRoute("上海", "广州", "FM6688", "A5566", 6,
                20, 10, 60, 30, 110, 40));
        addRoute(new FlightRoute("成都", "北京", "3U8801", "B3301", 1,
                20, 15, 50, 35, 90, 50));
        addRoute(new FlightRoute("杭州", "广州", "CZ3801", "A3801", 2,
                20, 10, 50, 20, 105, 30));
        addRoute(new FlightRoute("深圳", "上海", "ZH9101", "B9101", 3,
                25, 25, 60, 60, 100, 100));
        addRoute(new FlightRoute("南京", "成都", "MU2801", "A2801", 4,
                15, 5, 45, 10, 90, 15));
        addRoute(new FlightRoute("武汉", "北京", "CZ6601", "B6601", 5,
                20, 0, 50, 0, 95, 0));
        addRoute(new FlightRoute("北京", "西安", "HU7601", "A7601", 6,
                20, 15, 50, 30, 110, 45));
        addRoute(new FlightRoute("上海", "昆明", "MU5801", "B5801", 7,
                20, 20, 50, 50, 100, 100));
        addRoute(new FlightRoute("广州", "厦门", "MF8301", "A8301", 1,
                15, 5, 45, 5, 95, 10));
        addRoute(new FlightRoute("重庆", "上海", "3U8901", "B8901", 2,
                20, 10, 60, 25, 110, 40));
        addRoute(new FlightRoute("西安", "北京", "MU2101", "A2101", 3,
                20, 0, 50, 0, 90, 0));
        addRoute(new FlightRoute("厦门", "成都", "MF8401", "B8401", 4,
                15, 10, 45, 20, 85, 25));
        addRoute(new FlightRoute("青岛", "广州", "SC4601", "A4601", 5,
                20, 15, 50, 40, 105, 55));
        addRoute(new FlightRoute("昆明", "深圳", "MU5802", "B5802", 6,
                20, 5, 45, 15, 85, 20));
        addRoute(new FlightRoute("北京", "杭州", "CA1701", "A1701", 7,
                20, 20, 60, 60, 120, 120));
    }

    private void addRoute(FlightRoute route) {
        if (count < routes.length) {
            routes[count++] = route;
        }
    }

    public FlightRoute searchByStation(String name) {
        for (int i = 0; i < count; i++) {
            if (routes[i].dest.equals(name)) {
                return routes[i];
            }
        }
        return null;
    }

    public FlightRoute[] searchByStationAll(String name) {
        int matchCount = 0;
        for (int i = 0; i < count; i++) {
            if (routes[i].dest.equals(name)) {
                matchCount++;
            }
        }
        FlightRoute[] result = new FlightRoute[matchCount];
        int idx = 0;
        for (int i = 0; i < count; i++) {
            if (routes[i].dest.equals(name)) {
                result[idx++] = routes[i];
            }
        }
        return result;
    }

    public FlightRoute searchByFlight(String num) {
        for (int i = 0; i < count; i++) {
            if (routes[i].flightNo.equals(num)) {
                return routes[i];
            }
        }
        return null;
    }

    public FlightRoute[] searchByRoute(String origin, String destination) {
        int matchCount = 0;
        for (int i = 0; i < count; i++) {
            if (routes[i].origin.equals(origin)
                    && routes[i].dest.equals(destination)) {
                matchCount++;
            }
        }
        FlightRoute[] result = new FlightRoute[matchCount];
        int idx = 0;
        for (int i = 0; i < count; i++) {
            if (routes[i].origin.equals(origin)
                    && routes[i].dest.equals(destination)) {
                result[idx++] = routes[i];
            }
        }
        return result;
    }

    public int getCount() {
        return count;
    }

    public FlightRoute getRoute(int index) {
        if (index >= 0 && index < count) {
            return routes[index];
        }
        return null;
    }
}