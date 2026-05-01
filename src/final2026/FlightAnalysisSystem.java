package final2026;

import java.util.*;
import java.util.stream.Collectors;

class Flight {
    private final String destination;
    private final int miles;

    public Flight(String destination, int miles) {
        this.destination = destination;
        this.miles = miles;
    }

    public String getDestination() {
        return destination;
    }

    public int getMiles() {
        return miles;
    }
}

class Passenger {
    private final String id;
    private final List<Flight> flights;

    public Passenger(String id) {
        this.id = id;
        this.flights = new ArrayList<>();
    }

    public void addFlight(String dest, int miles) {
        this.flights.add(new Flight(dest, miles));
    }

    public String getId() {
        return id;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public int getTotalMiles() {
        return flights.stream()
                .mapToInt(Flight::getMiles)
                .sum();
    }

    public long getDestCount() {
        return flights.stream()
                .map(Flight::getDestination)
                .distinct()
                .count();
    }

    @Override
    public String toString() {
        return String.format("[%s] Total Miles: [%d] Destinations: [%d]",
                id, getTotalMiles(), getDestCount());
    }
}

public class FlightAnalysisSystem {
    public static void main(String[] args) {
        Map<String, Passenger> passengerMap = new HashMap<>();

        // dummy data
        Passenger p1 = new Passenger("P10");
        p1.addFlight("Skopje", 500);
        p1.addFlight("Berlin", 200);

        Passenger p2 = new Passenger("P05");
        p2.addFlight("London", 1500);
        p2.addFlight("Skopje", 400);

        Passenger p3 = new Passenger("P02");
        p3.addFlight("Rome", 500);
        p3.addFlight("Skopje", 300);

        passengerMap.put(p1.getId(), p1);
        passengerMap.put(p2.getId(), p2);
        passengerMap.put(p3.getId(), p3);

        System.out.println("--- Sorted Passengers ---");
        passengerMap.values().stream()
                .sorted(Comparator
                        .comparing(Passenger::getTotalMiles).reversed()
                        .thenComparing(Passenger::getId))
                .forEach(System.out::println);

        Map<String, Integer> destinationMap = passengerMap.values().stream()
                .flatMap(d -> d.getFlights().stream())
                .collect(Collectors.groupingBy(
                        Flight::getDestination,
                        Collectors.summingInt(i -> 1)
                ));

        System.out.println("\n--- Destination Popularity Map ---");
        destinationMap.forEach((city, count) ->
                System.out.println(city + ": " + count));
    }
}