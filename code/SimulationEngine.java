import java.io.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

public class SimulationEngine {

    private FlightManagement flightManagement;
    private DepartureFlightManager departureManager;
    private ArrivalFlightManager arrivalManager;
    private WeatherManager weatherManager;

    private int boardings = 0;
    private int arrivals = 0;
    private int missedChecks = 0;

    public SimulationEngine(FlightManagement fm, DepartureFlightManager dm,
                            ArrivalFlightManager am, WeatherManager wm) {
        this.flightManagement = fm;
        this.departureManager = dm;
        this.arrivalManager = am;
        this.weatherManager = wm;
    }

    public void run(LocalDateTime startTime, LocalDateTime endTime, int stepMinutes) {
        // Clear output files before simulation
        clearFile("departedFlights.txt");
        clearFile("arrivedFlights.txt");
        clearFile("cancelledBookings.txt");

        // Enable simulation mode
        weatherManager.setSimulationMode(true);
        flightManagement.setSimulationMode(true);

        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    🚀 SIMULATION ENGINE STARTED 🚀                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println("   Start time : " + startTime);
        System.out.println("   End time   : " + endTime);
        System.out.println("   Step       : " + stepMinutes + " minute(s)\n");

        LocalDateTime simTime = startTime;
        int lastMissedCheckMinute = -1;
        int lastFreeResourcesMinute = -1;
        boolean firstEvent = true;

        System.out.println("📋 SIMULATION LOG (only showing important events):\n");

        while (simTime.isBefore(endTime)) {
            // Free resources every 5 minutes
            if (simTime.getMinute() % 5 == 0 && simTime.getMinute() != lastFreeResourcesMinute) {
                flightManagement.freeResources(simTime);
                lastFreeResourcesMinute = simTime.getMinute();
            }

            // Process scheduled flights
            List<Flight> flights = flightManagement.getFlights();
            for (Flight f : flights) {
                String status = f.getStatus().toUpperCase();
                LocalDateTime actionTime = f.getScheduledActionTime();

                if (status.equals("DEPART-SCHEDULED") && !simTime.isBefore(actionTime)) {
                    if (firstEvent) {
                        firstEvent = false;
                    }
                    System.out.println("[" + simTime + "] ✈️  Boarding " + f.getFlightInstanceId());
                    departureManager.prepareBoarding(f.getFlightInstanceId(), simTime);
                    boardings++;
                }
                else if (status.equals("ARRIVE-SCHEDULED") && !simTime.isBefore(actionTime)) {
                    if (firstEvent) {
                        firstEvent = false;
                    }
                    System.out.println("[" + simTime + "] 🛬 Processing arrival " + f.getFlightInstanceId());
                    arrivalManager.processArrival(f.getFlightInstanceId(), simTime);
                    arrivals++;
                }
            }

            // Check missed flights every 10 minutes
            if (simTime.getMinute() % 10 == 0 && simTime.getMinute() != lastMissedCheckMinute) {
                flightManagement.processGoodWeatherWaitingFlights(simTime);
                lastMissedCheckMinute = simTime.getMinute();
                missedChecks++;
            }

            simTime = simTime.plusMinutes(stepMinutes);
        }

        // Final cleanup
        flightManagement.freeResources(endTime);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🏁 SIMULATION COMPLETED\n");

        // Print report
        printReport(startTime, endTime);

        // Turn off simulation mode
        weatherManager.setSimulationMode(false);
        flightManagement.setSimulationMode(false);
    }

    private void clearFile(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("");
        } catch (IOException e) {
            // File may not exist, that's fine
        }
    }

    private void printReport(LocalDateTime start, LocalDateTime end) {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     📊 SIMULATION REPORT 📊                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-30s : %-40s ║\n", "Simulation Period", start + " → " + end);
        System.out.printf("║ %-30s : %-40d ║\n", "Boarding calls", boardings);
        System.out.printf("║ %-30s : %-40d ║\n", "Arrival process calls", arrivals);
        System.out.printf("║ %-30s : %-40d ║\n", "Missed flight checks", missedChecks);
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");

        // Count flights from current flights.txt
        List<Flight> flights = flightManagement.getFlights();
        int departed = 0, delayed = 0, holding = 0, boarding = 0, arriving = 0;
        List<String> delayedIds = new ArrayList<>();
        List<String> holdingIds = new ArrayList<>();

        for (Flight f : flights) {
            String status = f.getStatus().toUpperCase();
            if (status.equals("DEPARTED")) {
                departed++;
            } else if (status.equals("DELAYED")) {
                delayed++;
                delayedIds.add(f.getFlightInstanceId());
            } else if (status.equals("HOLDING")) {
                holding++;
                holdingIds.add(f.getFlightInstanceId());
            } else if (status.equals("BOARDING")) {
                boarding++;
            } else if (status.equals("ARRIVING")) {
                arriving++;
            }
        }

        // Count diverted flights from arrivedFlights.txt
        int diverted = 0;
        List<String> divertedIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("arrivedFlights.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 9 && parts[8].equalsIgnoreCase("DIVERTED")) {
                    diverted++;
                    if (parts.length >= 2) divertedIds.add(parts[1]);
                }
            }
        } catch (IOException e) {
            // File may not exist
        }

        // Count cancelled flights from departedFlights.txt
        int cancelled = 0;
        List<String> cancelledIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("departedFlights.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 9 && parts[8].equalsIgnoreCase("CANCELLED")) {
                    cancelled++;
                    if (parts.length >= 2) cancelledIds.add(parts[1]);
                }
            }
        } catch (IOException e) {
            // File may not exist
        }

        System.out.printf("║ %-30s : %-40d ║\n", "Flights that departed", departed);
        System.out.printf("║ %-30s : %-40d ║\n", "Flights delayed (active)", delayed);
        System.out.printf("║ %-30s : %-40d ║\n", "Flights holding (active)", holding);
        System.out.printf("║ %-30s : %-40d ║\n", "Flights diverted", diverted);
        System.out.printf("║ %-30s : %-40d ║\n", "Flights cancelled", cancelled);
        System.out.printf("║ %-30s : %-40d ║\n", "Flights currently boarding", boarding);
        System.out.printf("║ %-30s : %-40d ║\n", "Flights currently arriving", arriving);
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");

        // Print details if there are any
        if (!delayedIds.isEmpty()) {
            System.out.println("\n📋 Delayed flights: " + String.join(", ", delayedIds));
        }
        if (!holdingIds.isEmpty()) {
            System.out.println("📋 Holding flights: " + String.join(", ", holdingIds));
        }
        if (!divertedIds.isEmpty()) {
            System.out.println("📋 Diverted flights: " + String.join(", ", divertedIds));
        }
        if (!cancelledIds.isEmpty()) {
            System.out.println("📋 Cancelled flights: " + String.join(", ", cancelledIds));
        }

        System.out.println("\n✅ Simulation finished.\n");
    }
}