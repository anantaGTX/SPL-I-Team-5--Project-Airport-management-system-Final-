import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DepartureFlightManager {

    private FlightManagement flightManagement;
    private PassengerManagement passengerManagement;
    private String departedFlightFilePath = "departedFlights.txt";

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";

    public DepartureFlightManager(FlightManagement fm, PassengerManagement pm) {
        this.flightManagement = fm;
        this.passengerManagement = pm;
    }

    public void prepareBoarding(String flightInstanceId, LocalDateTime currentTime) {
        Flight f = flightManagement.FindFlightByInstanceId(flightInstanceId);
        if (f == null) {
            System.out.println(RED + "Flight " + flightInstanceId + " not found." + RESET);
            return;
        }

        if (!"Dhaka".equalsIgnoreCase(f.getOrigin())) {
            System.out.println(RED + "Flight " + flightInstanceId + " is not a departure flight." + RESET);
            return;
        }

        if (f.getStatus().equalsIgnoreCase("BOARDING")) {
            System.out.println(YELLOW + "Flight already in BOARDING status." + RESET);
            return;
        }

        WeatherManager weatherManager = flightManagement.getWeatherManager();
        WeatherType weather = weatherManager.getWeather(currentTime);

        if (weather.isBadWeather()) {
            LocalDateTime stormEndTime = weatherManager.getWeatherEndTime(currentTime);
            flightManagement.handleBadWeather(currentTime, stormEndTime);
            return;
        }

        flightManagement.processGoodWeatherPrepareBoarding(flightInstanceId, currentTime);
    }

    public void checkAndDepartFlights(LocalDateTime currentTime) {
        List<Flight> departedFlights = new ArrayList<>();
        for (Flight f : flightManagement.getFlights()) {
            if (f.getStatus().equalsIgnoreCase("BOARDING")) {
                if (currentTime.isAfter(f.getDepartDateTime().plusMinutes(20))) {
                    f.setStatus("DEPARTED");
                    departedFlights.add(f);
                    System.out.println(CYAN + "✈️ Flight " + f.getFlightInstanceId() + " has DEPARTED." + RESET);

                    if (f.getGateId() != null && !f.getGateId().equals("-")) {
                        flightManagement.freeGate(f);
                    }
                    if (f.getRunwayId() != null && !f.getRunwayId().equals("-")) {
                        flightManagement.freeRunway(f);
                    }

                    if (passengerManagement != null) {
                        passengerManagement.freePassengersOfFlight(f.getFlightInstanceId());
                    }
                    System.out.println("-------------------------------------");
                }
            }
        }

        saveDepartedFlights(departedFlights);
        if (!departedFlights.isEmpty()) {
            flightManagement.getFlights().removeAll(departedFlights);
            flightManagement.saveFlightsToFile();
        }
    }

    public void saveCancelledFlight(Flight f) {
        f.setStatus("CANCELLED");
        if (passengerManagement != null) {
            passengerManagement.freePassengersOfFlight(f.getFlightInstanceId());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(departedFlightFilePath, true))) {
            bw.write(f.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println(RED + "Error saving cancelled flight: " + e.getMessage() + RESET);
        }
        flightManagement.getFlights().remove(f);
        flightManagement.saveFlightsToFile();
        System.out.println(RED + "❌ Flight " + f.getFlightInstanceId() + " CANCELLED." + RESET);
    }

    private void saveDepartedFlights(List<Flight> departedFlights) {
        if (departedFlights.isEmpty()) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(departedFlightFilePath, true))) {
            for (Flight f : departedFlights) {
                bw.write(f.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(RED + "Error saving departed flights: " + e.getMessage() + RESET);
        }
    }

    public void clearDepartedFlightsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(departedFlightFilePath))) {
            bw.write("");
            System.out.println(GREEN + "Departed flights file cleared." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error clearing departed flights file: " + e.getMessage() + RESET);
        }
    }

    public void displayDepartedFlights() {
        try (BufferedReader br = new BufferedReader(new FileReader(departedFlightFilePath))) {
            String line;
            boolean empty = true;
            System.out.println("\n" + CYAN + "╔══════════════════════════════════════════════════════════════╗" + RESET);
            System.out.println(CYAN + "║                   DEPARTED FLIGHTS                           ║" + RESET);
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
                System.out.println(YELLOW + "   No departed flights found." + RESET);
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println(RED + "Error reading departed flights: " + e.getMessage() + RESET);
        }
    }
}