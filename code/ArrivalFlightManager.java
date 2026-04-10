import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArrivalFlightManager {

    private FlightManagement flightManagement;
    private String arrivedFlightFilePath = "arrivedFlights.txt";
    private List<Flight> arrivedFlights;

    // ANSI Color Codes
    private static final String RESET = "";
    private static final String GREEN = "";
    private static final String YELLOW = "";
    private static final String RED = "";
    private static final String CYAN = "";

    public ArrivalFlightManager(FlightManagement fm) {
        this.flightManagement = fm;
        this.arrivedFlights = new ArrayList<>();
    }

    public void processArrival(String flightInstanceId, LocalDateTime currentTime) {
        Flight f = flightManagement.FindFlightByInstanceId(flightInstanceId);
        if (f == null) {
            System.out.println(RED + "Flight " + flightInstanceId + " not found." + RESET);
            return;
        }

        if (!"Dhaka".equalsIgnoreCase(f.getDestination())) {
            System.out.println(RED + "Flight " + flightInstanceId + " is not an arrival flight." + RESET);
            return;
        }

        if (f.getStatus().equalsIgnoreCase("ARRIVING") || f.getStatus().equalsIgnoreCase("ARRIVED")) {
            System.out.println(YELLOW + "Flight already in " + f.getStatus() + " status." + RESET);
            return;
        }

        WeatherManager weatherManager = flightManagement.getWeatherManager();
        WeatherType weather = weatherManager.getWeather(currentTime);

        if (weather.isBadWeather()) {
            LocalDateTime stormEndTime = weatherManager.getWeatherEndTime(currentTime);
            flightManagement.handleBadWeather(currentTime, stormEndTime);
            return;
        }

        flightManagement.processGoodWeatherProcessArrival(flightInstanceId, currentTime);
    }

    public void checkAndCompleteArrivals(LocalDateTime currentTime) {
        List<Flight> completedArrivals = new ArrayList<>();
        for (Flight f : flightManagement.getFlights()) {
            if (f.getStatus().equalsIgnoreCase("ARRIVING")) {
                if (!currentTime.isBefore(f.getArrivalDateTime().plusMinutes(20))) {
                    flightManagement.updateFlightStatus(f, "ARRIVED");
                    arrivedFlights.add(f);
                    completedArrivals.add(f);
                    System.out.println(CYAN + "🛬 Flight " + f.getFlightInstanceId() + " has ARRIVED at gate " +
                            f.getGateId() + " using runway " + f.getRunwayId() + "." + RESET);

                    if (f.getGateId() != null && !f.getGateId().equals("-")) {
                        flightManagement.freeGate(f);
                    }
                    if (f.getRunwayId() != null && !f.getRunwayId().equals("-")) {
                        flightManagement.freeRunway(f);
                    }
                    System.out.println("-------------------------------------");
                }
            }
        }
        saveAllArrivedFlights(arrivedFlights);
        if (!completedArrivals.isEmpty()) {
            flightManagement.getFlights().removeAll(completedArrivals);
            flightManagement.saveFlightsToFile();
        }
    }

    public void saveDivertedFlight(Flight f) {
        f.setStatus("DIVERTED");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arrivedFlightFilePath, true))) {
            bw.write(f.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println(RED + "Error saving diverted flight: " + e.getMessage() + RESET);
        }
        flightManagement.getFlights().remove(f);
        flightManagement.saveFlightsToFile();
        System.out.println(RED + "🔄 Flight " + f.getFlightInstanceId() + " DIVERTED." + RESET);
    }

    public void saveAllArrivedFlights(List<Flight> arrivedFlights) {
        if (arrivedFlights.isEmpty()) return;

        // FIX: Read existing entries to avoid duplicates
        List<String> existingIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(arrivedFlightFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 1) {
                    existingIds.add(parts[1]);
                }
            }
        } catch (IOException e) {
            // File may not exist, ignore
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arrivedFlightFilePath, true))) {
            for (Flight f : arrivedFlights) {
                if (!existingIds.contains(f.getFlightInstanceId())) {
                    bw.write(f.toFileString());
                    bw.newLine();
                }
            }
            System.out.println(GREEN + "All arrived flights saved to " + arrivedFlightFilePath + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error saving arrived flights: " + e.getMessage() + RESET);
        }
        arrivedFlights.clear();
    }
    public void clearArrivedFlightsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arrivedFlightFilePath))) {
            bw.write("");
            System.out.println(GREEN + "Arrived flights file cleared." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error clearing arrived flights file: " + e.getMessage() + RESET);
        }
    }

    public void displayArrivedFlights() {
        try (BufferedReader br = new BufferedReader(new FileReader(arrivedFlightFilePath))) {
            String line;
            boolean empty = true;
            System.out.println("\n" + CYAN + "╔══════════════════════════════════════════════════════════════╗" + RESET);
            System.out.println(CYAN + "║                    ARRIVED FLIGHTS                            ║" + RESET);
            System.out.println(CYAN + "╚══════════════════════════════════════════════════════════════╝" + RESET);
            while ((line = br.readLine()) != null) {
                empty = false;
                String[] parts = line.split(",");
                System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
                System.out.println("   Flight Number: " + parts[0]);
                System.out.println("   Flight Instance: " + parts[1]);
                System.out.println("   Route: " + parts[4] + " → " + parts[5]);
                System.out.println("   Departure: " + parts[6]);
                System.out.println("   Arrival: " + parts[7]);
                System.out.println("   Status: " + parts[8]);
                System.out.println("   Gate: " + (parts[9].equals("-") ? "—" : parts[9]));
                System.out.println("   Runway: " + (parts[10].equals("-") ? "—" : parts[10]));
            }
            if (empty) {
                System.out.println(YELLOW + "   No arrived flights found." + RESET);
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println(RED + "Error reading arrived flights: " + e.getMessage() + RESET);
        }
    }

    public List<Flight> getArrivedFlights() {
        return new ArrayList<>(arrivedFlights);
    }
}
