package januari2026.vlezna;// package midterms.january;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final List<Flight> flightsList;

    public Passenger(String id, List<Flight> flightsList) {
        this.id = id;
        this.flightsList = flightsList;
    }

    public int totalMiles() {
        return flightsList.stream()
                .mapToInt(Flight::getMiles)
                .sum();
    }

    public String getId() {
        return id;
    }

    public List<Flight> getFlightsList() {
        return flightsList;
    }

    @Override
    public String toString() {
        // Passenger [p1] totalMiles [2000] destinations [2]
        return String.format("Passenger [%s] totalMiles [%d] destinations [%d]\n", id, totalMiles(), flightsList.size());

    }
}

public class FlightRewardsEvaluator {
    private final Map<String, Passenger> passengerMap = new HashMap<>();

    public FlightRewardsEvaluator() {
    }

    public void loadFlights(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\s++");

            String passengerId = parts[0];

            List<Flight> flightList = new ArrayList<>();

            for (int i = 1; i < parts.length; i++) {
                String[] flightParts = parts[i].split(":");
                String destination = flightParts[0];
                int miles = Integer.parseInt(flightParts[1]);
                flightList.add(new Flight(destination, miles));
            }
            passengerMap.put(passengerId, new Passenger(passengerId, flightList));
        }
    }

    public void printPassengers(OutputStream os) {
        PrintWriter pw = new PrintWriter(os);
        passengerMap.values().stream()
                .sorted(Comparator.comparingInt(Passenger::totalMiles).reversed()
                        .thenComparing(Passenger::getId))
                .forEach(pw::print);
        ;
        pw.flush();
    }

    public Map<String, Integer> groupByDestination() {
        return passengerMap.values().stream()
                .flatMap(p -> p.getFlightsList().stream())
                .collect(Collectors.groupingBy(
                        Flight::getDestination,
                        TreeMap::new,
                        Collectors.summingInt(i -> 1)
                ));
    }
//    public Map<String, Integer> groupByDestination() {
//        return passengerMap.values().stream()
//                .flatMap(p -> p.getFlightsList().stream())
//                .collect(Collectors.toMap(
//                        Flight::getDestination,
//                        f -> 1,
//                        Integer::sum,
//                        TreeMap::new
//                ));
//    }
//    public Map<String, Long> groupByDestination() {
//        return passengerMap.values().stream()
//                .flatMap(p -> p.getFlightsList().stream())
//                .collect(Collectors.groupingBy(
//                        Flight::getDestination,
//                        TreeMap::new,
//                        Collectors.counting()
//                ));
//    }


    static void wtf(Scanner sc) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream("data.txt"));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.equals("---")) {
                break;
            }
            pw.println(line);
        }
        pw.flush();
    }


    public static void main(String[] args) throws Exception {
        FlightRewardsEvaluator evaluator = new FlightRewardsEvaluator();

        Scanner sc = new Scanner(System.in);
        wtf(sc);

        evaluator.loadFlights(new FileInputStream("data.txt"));

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));

        String command = sc.nextLine();
        switch (command) {
            case "PRINT":
                evaluator.printPassengers(System.out);
                break;

            case "GROUP":
                evaluator.groupByDestination().forEach((dest, cnt) ->
                        pw.printf("Destination [%s] passengers [%d]%n", dest, cnt));
                pw.flush();
                break;

            default:
                pw.println("Invalid command");
                pw.flush();
                break;
        }
    }

}
