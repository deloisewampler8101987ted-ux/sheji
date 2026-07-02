package airline.model;

public class FlightList {
    private FlightRoute[] routes;
    private int count;

    public FlightList(int capacity) {
        this.routes = new FlightRoute[capacity];
        this.count = 0;
    }

    public void init() {
        addRoute(new FlightRoute("北京", "CA1111", "B1111", 1, 150, 120));
        addRoute(new FlightRoute("上海", "MU2222", "B2222", 3, 200, 185));
        addRoute(new FlightRoute("广州", "CZ3333", "A3333", 5, 180, 5));
        addRoute(new FlightRoute("深圳", "ZH1234", "A1234", 7, 160, 160));
        addRoute(new FlightRoute("成都", "CA5678", "B5678", 2, 140, 0));
    }

    private void addRoute(FlightRoute route) {
        if (count < routes.length) {
            routes[count++] = route;
        }
    }

    public FlightRoute searchByStation(String name) {
        for (int i = 0; i < count; i++) {
            if (routes[i].terminalStation.equals(name)) {
                return routes[i];
            }
        }
        return null;
    }

    public FlightRoute searchByFlight(String num) {
        for (int i = 0; i < count; i++) {
            if (routes[i].flightNumber.equals(num)) {
                return routes[i];
            }
        }
        return null;
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