import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class FlightManagement {

    // ============================================================
    // FIELDS
    // ============================================================
    private List<Flight> flights;
    private String flightFilePath = "flights.txt";
    private GateManagement gateManagement;
    private RunwayManagement runwayManagement;
    private WeatherManager weatherManager;
    private DepartureFlightManager departureManager;
    private ArrivalFlightManager arrivalManager;

    private boolean simulationMode = false;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    public FlightManagement(GateManagement gm, RunwayManagement rm) {
        this.flights = new ArrayList<>();
        this.gateManagement = gm;
        this.runwayManagement = rm;
        loadFlightsFromFile();
    }

    // ============================================================
    // MANAGER SETTERS & GETTERS
    // ============================================================
    public void setWeatherManager(WeatherManager wm) {
        this.weatherManager = wm;
    }

    public void setDepartureManager(DepartureFlightManager dm) {
        this.departureManager = dm;
    }

    public void setArrivalManager(ArrivalFlightManager am) {
        this.arrivalManager = am;
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setSimulationMode(boolean mode) {
        this.simulationMode = mode;
    }

    public boolean isSimulationMode() {
        return simulationMode;
    }

    // ============================================================
    // FILE OPERATIONS
    // ============================================================
    private void loadFlightsFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(flightFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                Flight f = new Flight(
                        p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]),
                        p[4], p[5], LocalDateTime.parse(p[6]), LocalDateTime.parse(p[7]),
                        LocalDateTime.parse(p[8]), p[9], p[10], p[11]);
                flights.add(f);
            }

            // NEW: Fix gate and runway assignments after loading flights
            gateManagement.fixGateAssignments(flights);
            runwayManagement.fixRunwayAssignments(flights);

        } catch (IOException e) {
            System.out.println("Error loading flights: " + e.getMessage());
        }
    }

    public void saveFlightsToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(flightFilePath))) {
            for (Flight f : flights)
                bw.write(f.toFileString() + "\n");
        } catch (IOException e) {
            System.out.println("Error saving flights: " + e.getMessage());
        }
    }

    // ============================================================
    // FLIGHT LOOKUP
    // ============================================================
    public Flight FindFlightByInstanceId(String id) {
        for (Flight f : flights)
            if (f.getFlightInstanceId().equals(id))
                return f;
        return null;
    }

    // ============================================================
    // GATE & RUNWAY ASSIGNMENT / FREEDOM
    // ============================================================
    public boolean assignGate(Flight f) {
        if (f == null)
            return false;
        String gate = gateManagement.assignGate(f.getFlightInstanceId());
        if (gate == null)
            return false;
        f.setGateId(gate);
        saveFlightsToFile();
        return true;
    }

    public boolean assignRunway(Flight f) {
        if (f == null)
            return false;
        String runway = runwayManagement.assignRunway(f.getFlightInstanceId());
        if (runway == null)
            return false;
        f.setRunwayId(runway);
        saveFlightsToFile();
        return true;
    }

    public void freeGate(Flight f) {
        if (f == null || f.getGateId() == null || f.getGateId().equals("-"))
            return;
        if (gateManagement.freeGate(f.getFlightInstanceId())) {
            f.setGateId("-");
            saveFlightsToFile();
            System.out.println("Gate freed for flight: " + f.getFlightInstanceId());
        }
    }

    public void freeRunway(Flight f) {
        if (f == null || f.getRunwayId() == null || f.getRunwayId().equals("-"))
            return;
        if (runwayManagement.freeRunway(f.getFlightInstanceId())) {
            f.setRunwayId("-");
            saveFlightsToFile();
            System.out.println("Runway freed for flight: " + f.getFlightInstanceId());
        }
    }

    public void updateFlightStatus(Flight f, String newStatus) {
        if (f != null) {
            f.setStatus(newStatus);
            saveFlightsToFile();
        }
    }

    // ============================================================
    // DISPLAY METHODS
    // ============================================================
    public void displayFlights() {
        for (Flight f : flights)
            displayFlightDetails(f);
    }

    public void displayFlightDetails(Flight f) {
        if (f == null) {
            System.out.println("Flight not found.");
            return;
        }
        System.out.println("Flight Number: " + f.getFlightNumber());
        System.out.println("Flight Instance ID: " + f.getFlightInstanceId());
        System.out.println("Origin: " + f.getOrigin());
        System.out.println("Destination: " + f.getDestination());
        System.out.println("Departure: " + f.getDepartDateTime());
        System.out.println("Arrival: " + f.getArrivalDateTime());
        System.out.println("Action Time: " + f.getScheduledActionTime());
        System.out.println("Status: " + f.getStatus());
        System.out.println("Gate: " + f.getGateId());
        System.out.println("Runway: " + f.getRunwayId());
        System.out.println("Passenger Count: " + f.getPassengerCount());
        System.out.println("----------------------------------------------------");
    }

    public void displayFlightsByStatus(String status) {
        boolean found = false;
        for (Flight f : flights) {
            if (f.getStatus().equalsIgnoreCase(status)) {
                displayFlightDetails(f);
                found = true;
            }
        }
        if (!found)
            System.out.println("No flights found with status: " + status);
    }

    public void displayFlightByID(String id) {
        for (Flight f : flights) {
            if (f.getFlightInstanceId().equalsIgnoreCase(id)) {
                displayFlightDetails(f);
                return;
            }
        }
        System.out.println("Flight with ID " + id + " not found.");
    }

    public int getAvailableSeats(String id) {
        Flight f = FindFlightByInstanceId(id);
        return (f != null) ? f.getSeatsLeft() : 0;
    }

    public List<Flight> getFlightsByDestinationAndDate(String dest, LocalDate date) {
        List<Flight> result = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getDestination().equalsIgnoreCase(dest) &&
                    f.getDepartDateTime().toLocalDate().equals(date))
                result.add(f);
        }
        return result;
    }

    public void populateOccupiedSeatsFromPassengers(List<Passenger> passengers) {
        for (Passenger p : passengers) {
            Flight f = FindFlightByInstanceId(p.getFlightInstanceId());
            if (f != null)
                f.addOccupiedSeats(p.getSeats());
        }
    }

    // ============================================================
    // HELPER METHODS (Flight Type Identification)
    // ============================================================
    private boolean isDeparture(Flight f) {
        return "Dhaka".equalsIgnoreCase(f.getOrigin());
    }

    private boolean isArrival(Flight f) {
        return "Dhaka".equalsIgnoreCase(f.getDestination());
    }

    private LocalDateTime getBoardingTime(Flight f) {
        return f.getScheduledActionTime();
    }

    private LocalDateTime getProcessTime(Flight f) {
        return f.getScheduledActionTime();
    }

    // ============================================================
    // RESOURCE MANAGEMENT
    // ============================================================
    public void freeResources(LocalDateTime currentTime) {
        if (departureManager != null)
            departureManager.checkAndDepartFlights(currentTime);
        if (arrivalManager != null)
            arrivalManager.checkAndCompleteArrivals(currentTime);
    }

    private LocalDateTime getEarliestFreeTimeForOneResource(LocalDateTime currentTime) {
        if (gateManagement.hasFreeGate() && runwayManagement.hasFreeRunway())
            return currentTime;

        LocalDateTime earliestGateFree = null, earliestRunwayFree = null;
        for (Flight f : flights) {
            if (f.getStatus().equalsIgnoreCase("BOARDING") ||
                    f.getStatus().equalsIgnoreCase("ARRIVING")) {
                LocalDateTime freeTime = f.getStatus().equalsIgnoreCase("BOARDING")
                        ? f.getDepartDateTime().plusMinutes(20)
                        : f.getArrivalDateTime().plusMinutes(20);
                if (earliestGateFree == null || freeTime.isBefore(earliestGateFree))
                    earliestGateFree = freeTime;
                if (earliestRunwayFree == null || freeTime.isBefore(earliestRunwayFree))
                    earliestRunwayFree = freeTime;
            }
        }

        LocalDateTime result = currentTime;
        if (earliestGateFree != null && earliestGateFree.isAfter(result))
            result = earliestGateFree;
        if (earliestRunwayFree != null && earliestRunwayFree.isAfter(result))
            result = earliestRunwayFree;
        return result.plusMinutes(1);
    }

    /**
     * Find the earliest time when enough ADDITIONAL gates and runways become free
     * to accommodate a batch of 'capacity' flights.
     * <p>
     * This accounts for already free resources and only waits for additional ones.
     *
     * @param currentTime The current simulation time
     * @param capacity    Number of flights to schedule simultaneously
     *                    (gates/runways needed)
     * @return The earliest time when enough resources are free (with 1 minute
     *         buffer)
     */
    private LocalDateTime getEarliestFreeTimeForAllResources(LocalDateTime currentTime, int capacity) {
        // Count currently free resources
        int freeGates = gateManagement.getFreeGateCount();
        int freeRunways = runwayManagement.getFreeRunwayCount();

        // Calculate how many MORE we need
        int gatesNeeded = capacity - freeGates;
        int runwaysNeeded = capacity - freeRunways;

        // If we already have enough, return current time
        if (gatesNeeded <= 0 && runwaysNeeded <= 0) {
            return currentTime;
        }

        // Collect all occupation end times from current flights
        List<LocalDateTime> gateFreeTimes = new ArrayList<>();
        List<LocalDateTime> runwayFreeTimes = new ArrayList<>();

        for (Flight f : flights) {
            if (f.getStatus().equalsIgnoreCase("BOARDING") ||
                    f.getStatus().equalsIgnoreCase("ARRIVING")) {
                LocalDateTime freeTime = f.getStatus().equalsIgnoreCase("BOARDING")
                        ? f.getDepartDateTime().plusMinutes(20)
                        : f.getArrivalDateTime().plusMinutes(20);
                gateFreeTimes.add(freeTime);
                runwayFreeTimes.add(freeTime);
            }
        }

        // Sort times
        gateFreeTimes.sort((a, b) -> a.compareTo(b));
        runwayFreeTimes.sort((a, b) -> a.compareTo(b));

        // Find when we have enough ADDITIONAL gates free
        LocalDateTime gateReady = currentTime;
        if (gatesNeeded > 0 && gateFreeTimes.size() >= gatesNeeded) {
            gateReady = gateFreeTimes.get(gatesNeeded - 1);
        }

        // Find when we have enough ADDITIONAL runways free
        LocalDateTime runwayReady = currentTime;
        if (runwaysNeeded > 0 && runwayFreeTimes.size() >= runwaysNeeded) {
            runwayReady = runwayFreeTimes.get(runwaysNeeded - 1);
        }

        // Need both, so take the later time
        LocalDateTime result = currentTime;
        if (gateReady.isAfter(result))
            result = gateReady;
        if (runwayReady.isAfter(result))
            result = runwayReady;

        return result.plusMinutes(1);
    }

    // ============================================================
    // FLIGHT DIVERT / CANCEL
    // ============================================================
    private void divertFlight(Flight f) {
        f.setStatus("DIVERTED");
        if (arrivalManager != null)
            arrivalManager.saveDivertedFlight(f);
        flights.remove(f);
        System.out.println("Flight " + f.getFlightInstanceId() + " DIVERTED (delay > 1 hour)");
    }

    private void cancelFlight(Flight f) {
        f.setStatus("CANCELLED");
        if (departureManager != null)
            departureManager.saveCancelledFlight(f);
        flights.remove(f);
        System.out.println("Flight " + f.getFlightInstanceId() + " CANCELLED (delay > 7 hours)");
    }

    // ============================================================
    // BATCH SCHEDULING (CORE LOGIC)
    // ============================================================
    private void batchSchedule(List<Flight> list, LocalDateTime startTime, int gates) {
        LocalDateTime slot = startTime;
        int idx = 0;

        while (idx < list.size()) {
            List<Flight> batch = new ArrayList<>();
            int count = 0;

            while (count < gates && idx < list.size()) {
                Flight f = list.get(idx++);
                if (isArrival(f)) {
                    LocalDateTime arrival = slot.plusMinutes(30);
                    if (Duration.between(f.getOriginalArrivalDateTime(), arrival).toMinutes() >= 60) {
                        divertFlight(f);
                        continue;
                    }
                    f.setScheduledActionTime(slot);
                    f.setArrivalDateTime(arrival);
                    f.setLastUpdatedTime(slot);
                    f.setStatus("HOLDING");
                    System.out.println(
                            "Flight " + f.getFlightInstanceId() + " HOLDING, rescheduled to arrive at " + arrival);
                } else {
                    LocalDateTime depart = slot.plusHours(1);
                    LocalDateTime arrival = depart.plusMinutes(f.getFlightDurationMinutes());
                    if (Duration.between(f.getOriginalDepartDateTime(), depart).toMinutes() >= 7 * 60) {
                        cancelFlight(f);
                        continue;
                    }
                    f.setScheduledActionTime(slot);
                    f.setDepartDateTime(depart);
                    f.setArrivalDateTime(arrival);
                    f.setLastUpdatedTime(slot);
                    f.setStatus("DELAYED");
                    System.out.println(
                            "Flight " + f.getFlightInstanceId() + " DELAYED, rescheduled to depart at " + depart);
                }
                batch.add(f);
                count++;
            }

            if (count > 0) {
                boolean hasDeparture = false;
                for (Flight f : batch)
                    if (isDeparture(f)) {
                        hasDeparture = true;
                        break;
                    }
                slot = slot.plusMinutes(hasDeparture ? 81 : 51);
            }
        }
        saveFlightsToFile();
    }

    // ============================================================
    // BAD WEATHER HANDLER
    // ============================================================
    public void handleBadWeather(LocalDateTime currentTime, LocalDateTime stormEnd) {
        System.out.println("Bad weather detected. Rescheduling all flights after " + stormEnd);
        freeResources(currentTime);
        List<Flight> arrivals = new ArrayList<>(), departures = new ArrayList<>();
        for (Flight f : flights) {
            String s = f.getStatus().toUpperCase();
            if (s.equals("DEPARTED") || s.equals("ARRIVED") || s.equals("DIVERTED") || s.equals("CANCELLED")
                    || s.equals("ARRIVING") || s.equals("BOARDING"))
                continue;
            if (isArrival(f))
                arrivals.add(f);
            else
                departures.add(f);
        }

        arrivals.sort((a, b) -> getProcessTime(a).compareTo(getProcessTime(b)));
        departures.sort((a, b) -> getBoardingTime(a).compareTo(getBoardingTime(b)));

        List<Flight> all = new ArrayList<>();
        all.addAll(arrivals);
        all.addAll(departures);

        int capacity = Math.min(gateManagement.getTotalGateCount(), runwayManagement.getTotalRunwayCount());

        // Calculate start time based on resources becoming free AFTER storm
        LocalDateTime start = getEarliestFreeTimeForAllResources(stormEnd, capacity);

        // ============================================================
        // FIX: Ensure we never schedule flights before storm ends
        // ============================================================
        if (start.isBefore(stormEnd)) {
            start = stormEnd;
            System.out.println("   Adjusted start time to storm end: " + stormEnd);
        }

        batchSchedule(all, start, capacity);
        System.out.println("All flights rescheduled after storm.");
    }

    // ============================================================
    // GOOD WEATHER - PREPARE BOARDING
    // ============================================================
    public void processGoodWeatherPrepareBoarding(String flightId, LocalDateTime now) {
        freeResources(now);

        Flight current = FindFlightByInstanceId(flightId);
        if (current == null) {
            System.out.println("Flight not found.");
            return;
        }
        if (!isDeparture(current)) {
            System.out.println("Flight is not a departure.");
            return;
        }

        List<Flight> holding = new ArrayList<>(), delayed = new ArrayList<>();

        // Find missed flights (excluding current)
        for (Flight f : flights) {
            if (f.getFlightInstanceId().equals(flightId))
                continue;
            String s = f.getStatus().toUpperCase();
            if (s.equals("DEPARTED") || s.equals("ARRIVED") || s.equals("DIVERTED") || s.equals("CANCELLED")
                    || s.equals("ARRIVING") || s.equals("BOARDING"))
                continue;
            if (isDeparture(f)) {
                if (now.isAfter(getBoardingTime(f).plusHours(1)))
                    delayed.add(f);
            } else {
                if (now.isAfter(getProcessTime(f).plusMinutes(30)))
                    holding.add(f);
            }
        }

        // Handle current flight
        Flight present = null;
        if (now.isAfter(getBoardingTime(current).plusHours(1))) {
            delayed.add(current);
        } else {
            present = current;
        }

        // Sort lists by scheduled time (FCFS)
        holding.sort((a, b) -> getProcessTime(a).compareTo(getProcessTime(b)));
        delayed.sort((a, b) -> getBoardingTime(a).compareTo(getBoardingTime(b)));

        int missed = holding.size() + delayed.size();

        // ============================================================
        // CASE A: No missed flights
        // ============================================================
        if (missed == 0) {
            assignGate(current);
            assignRunway(current);
            current.setStatus("BOARDING");
            saveFlightsToFile();
            System.out.println("Flight " + current.getFlightInstanceId() + " is now BOARDING at gate " +
                    current.getGateId() + " using runway " + current.getRunwayId());
            return;
        }

        // ============================================================
        // Determine capacity and gates
        // ============================================================
        int capacity = Math.min(gateManagement.getTotalGateCount(), runwayManagement.getTotalRunwayCount());
        int gates = (missed <= 3) ? 1 : capacity; // FIXED: changed from <=3 to <=2

        // ============================================================
        // Determine start time based on gates
        // ============================================================
        LocalDateTime start;
        if (gates == 1) {
            start = getEarliestFreeTimeForOneResource(now);
        } else {
            start = getEarliestFreeTimeForAllResources(now, capacity);
        }

        // ============================================================
        // CASE B: No HOLDING flights (arrivals) - present can be processed separately
        // ============================================================
        if (holding.isEmpty()) {
            // Process present flight first (if exists)
            if (present != null) {
                // Check if resources are available NOW
                if (gateManagement.hasFreeGate() && runwayManagement.hasFreeRunway()) {
                    // Resources available - board present flight immediately
                    assignGate(present);
                    assignRunway(present);
                    present.setStatus("BOARDING");
                    saveFlightsToFile();
                    System.out.println("Flight " + present.getFlightInstanceId() + " is now BOARDING at gate " +
                            present.getGateId() + " using runway " + present.getRunwayId());

                    // Recalculate start time after present flight took resources
                    if (gates == 1) {
                        start = getEarliestFreeTimeForOneResource(now);
                    } else {
                        start = getEarliestFreeTimeForAllResources(now, capacity);
                    }
                } else {
                    // Resources not available - need to wait
                    LocalDateTime earliestFree = getEarliestFreeTimeForOneResource(now);
                    System.out.println("Gate/Runway not free. Present flight will board at: " + earliestFree);

                    // Schedule present flight first, then delayed
                    List<Flight> finalSchedule = new ArrayList<>();
                    finalSchedule.add(present);
                    finalSchedule.addAll(delayed);
                    batchSchedule(finalSchedule, earliestFree, gates);
                    saveFlightsToFile();
                    System.out.println("Flights rescheduled successfully.");
                    return;
                }
            }

            // Batch schedule delayed flights
            if (!delayed.isEmpty()) {
                batchSchedule(delayed, start, gates);
                System.out.println("Delayed flights rescheduled successfully.");
            }
            return;
        }

        // ============================================================
        // CASE C: HOLDING flights exist - full priority schedule
        // ============================================================
        List<Flight> finalSchedule = new ArrayList<>();
        finalSchedule.addAll(holding); // HOLDING arrivals first
        if (present != null) {
            finalSchedule.add(present); // PRESENT flight second
        }
        finalSchedule.addAll(delayed); // DELAYED flights last

        batchSchedule(finalSchedule, start, gates);
        System.out.println("Flights rescheduled successfully.");
    }

    // ============================================================
    // GOOD WEATHER - PROCESS ARRIVAL
    // ============================================================
    public void processGoodWeatherProcessArrival(String flightId, LocalDateTime now) {
        freeResources(now);

        Flight current = FindFlightByInstanceId(flightId);
        if (current == null) {
            System.out.println("Flight not found.");
            return;
        }
        if (!isArrival(current)) {
            System.out.println("Flight is not an arrival.");
            return;
        }

        List<Flight> holding = new ArrayList<>(), delayed = new ArrayList<>();

        // Find missed flights (excluding current)
        for (Flight f : flights) {
            if (f.getFlightInstanceId().equals(flightId))
                continue;
            String s = f.getStatus().toUpperCase();
            if (s.equals("DEPARTED") || s.equals("ARRIVED") || s.equals("DIVERTED") || s.equals("CANCELLED")
                    || s.equals("ARRIVING") || s.equals("BOARDING"))
                continue;
            if (isDeparture(f)) {
                if (now.isAfter(getBoardingTime(f).plusHours(1)))
                    delayed.add(f);
            } else {
                if (now.isAfter(getProcessTime(f).plusMinutes(30)))
                    holding.add(f);
            }
        }

        // Handle current flight
        Flight present = null;
        if (now.isAfter(getProcessTime(current).plusMinutes(30))) {
            holding.add(current);
        } else {
            present = current;
        }

        // Sort lists by scheduled time (FCFS)
        holding.sort((a, b) -> getProcessTime(a).compareTo(getProcessTime(b)));
        delayed.sort((a, b) -> getBoardingTime(a).compareTo(getBoardingTime(b)));

        int missed = holding.size() + delayed.size();

        // ============================================================
        // CASE A: No missed flights
        // ============================================================
        if (missed == 0) {
            assignGate(current);
            assignRunway(current);
            current.setStatus("ARRIVING");
            saveFlightsToFile();
            System.out.println("Flight " + current.getFlightInstanceId() + " is now ARRIVING at gate " +
                    current.getGateId() + " using runway " + current.getRunwayId());
            return;
        }

        // ============================================================
        // Determine capacity and gates
        // ============================================================
        int capacity = Math.min(gateManagement.getTotalGateCount(), runwayManagement.getTotalRunwayCount());
        int gates = (missed <= 3) ? 1 : capacity;

        // ============================================================
        // Determine start time based on gates
        // ============================================================
        LocalDateTime start;
        if (gates == 1) {
            start = getEarliestFreeTimeForOneResource(now);
        } else {
            start = getEarliestFreeTimeForAllResources(now, capacity);
        }

        // ============================================================
        // Build schedule list
        // ============================================================
        List<Flight> finalSchedule = new ArrayList<>();
        finalSchedule.addAll(holding); // All HOLDING flights first (arrivals)
        if (present != null) {
            finalSchedule.add(present); // PRESENT flight second (arrival within window)
        }
        finalSchedule.addAll(delayed); // DELAYED flights last (departures)

        batchSchedule(finalSchedule, start, gates);
        System.out.println("Flights rescheduled successfully.");
    }

    // ============================================================
    // GOOD WEATHER - PROCESS WAITING FLIGHTS (No Current Flight)
    // ============================================================
    public void processGoodWeatherWaitingFlights(LocalDateTime now) {
        freeResources(now);

        List<Flight> holding = new ArrayList<>(), delayed = new ArrayList<>();

        for (Flight f : flights) {
            String s = f.getStatus().toUpperCase();
            if (s.equals("DEPARTED") || s.equals("ARRIVED") || s.equals("DIVERTED") || s.equals("CANCELLED")
                    || s.equals("ARRIVING") || s.equals("BOARDING"))
                continue;
            if (isDeparture(f)) {
                if (now.isAfter(getBoardingTime(f).plusHours(1)))
                    delayed.add(f);
            } else {
                if (now.isAfter(getProcessTime(f).plusMinutes(30)))
                    holding.add(f);
            }
        }

        // Sort lists by scheduled time (FCFS)
        holding.sort((a, b) -> getProcessTime(a).compareTo(getProcessTime(b)));
        delayed.sort((a, b) -> getBoardingTime(a).compareTo(getBoardingTime(b)));

        int missed = holding.size() + delayed.size();
        if (missed == 0) {
            // Only print in manual mode, not in simulation
            if (!simulationMode) {
                System.out.println("No missed flights found.");
            }
            return;
        }

        int capacity = Math.min(gateManagement.getTotalGateCount(), runwayManagement.getTotalRunwayCount());
        int gates = (missed <= 2) ? 1 : capacity;

        LocalDateTime start;
        if (gates == 1) {
            start = getEarliestFreeTimeForOneResource(now);
        } else {
            start = getEarliestFreeTimeForAllResources(now, capacity);
        }

        List<Flight> finalSchedule = new ArrayList<>();
        finalSchedule.addAll(holding);
        finalSchedule.addAll(delayed);

        batchSchedule(finalSchedule, start, gates);

        // Only print success message in manual mode
        if (!simulationMode) {
            System.out.println("Waiting flights rescheduled successfully.");
        }
    }
}