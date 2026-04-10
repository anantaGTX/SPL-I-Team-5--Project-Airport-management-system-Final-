import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private String flightNumber;
    private String flightInstanceId;
    private int seatCapacity;
    private int passengerCount;
    private String origin;
    private String destination;
    private LocalDateTime departDateTime;
    private LocalDateTime arrivalDateTime;
    private LocalDateTime scheduledActionTime; // Boarding time for departures, Process time for arrivals
    private String status;
    private String gateId;
    private String runwayId;
    private List<String> occupiedSeats;

    // Original times for delay calculation (not saved to file, derived on load)
    private LocalDateTime originalDepartDateTime;
    private LocalDateTime originalArrivalDateTime;
    private LocalDateTime originalScheduledActionTime;
    private LocalDateTime lastUpdatedTime;

    // Constructor for loading from file
    public Flight(String flightNumber, String flightInstanceId, int seatCapacity, int passengerCount,
            String origin, String destination, LocalDateTime departDateTime,
            LocalDateTime arrivalDateTime, LocalDateTime scheduledActionTime,
            String status, String gateId, String runwayId) {
        this.flightNumber = flightNumber;
        this.flightInstanceId = flightInstanceId;
        this.seatCapacity = seatCapacity;
        this.passengerCount = passengerCount;
        this.origin = origin;
        this.destination = destination;
        this.departDateTime = departDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.scheduledActionTime = scheduledActionTime;
        this.status = status;
        this.gateId = gateId;
        this.runwayId = runwayId;
        occupiedSeats = new ArrayList<>();

        // Set original times
        this.originalDepartDateTime = departDateTime;
        this.originalArrivalDateTime = arrivalDateTime;
        this.originalScheduledActionTime = scheduledActionTime;
        this.lastUpdatedTime = scheduledActionTime;

        this.generateInstanceId(this.flightNumber, this.departDateTime);
    }

    // Generate instance ID
    public void generateInstanceId(String flightNumber, LocalDateTime departDateTime) {
        if (!"-".equals(this.flightInstanceId)) {
            return;
        }
        this.flightInstanceId = flightNumber + "-" + departDateTime.toString().replace("T", "-");
    }

    // =======================
    // Getters
    // =======================
    public String getFlightNumber() {
        return flightNumber;
    }

    public String getFlightInstanceId() {
        return flightInstanceId;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartDateTime() {
        return departDateTime;
    }

    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public LocalDateTime getScheduledActionTime() {
        return scheduledActionTime;
    }

    public String getStatus() {
        return status;
    }

    public String getGateId() {
        return gateId;
    }

    public String getRunwayId() {
        return runwayId;
    }

    public List<String> getOccupiedSeats() {
        return occupiedSeats;
    }

    // Original times
    public LocalDateTime getOriginalDepartDateTime() {
        return originalDepartDateTime;
    }

    public LocalDateTime getOriginalArrivalDateTime() {
        return originalArrivalDateTime;
    }

    public LocalDateTime getOriginalScheduledActionTime() {
        return originalScheduledActionTime;
    }

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    // =======================
    // Setters
    // =======================
    public void setStatus(String status) {
        this.status = status;
    }

    public void setGateId(String gateId) {
        this.gateId = gateId;
    }

    public void setRunwayId(String runwayId) {
        this.runwayId = runwayId;
    }

    public void setDepartDateTime(LocalDateTime dt) {
        this.departDateTime = dt;
    }

    public void setArrivalDateTime(LocalDateTime dt) {
        this.arrivalDateTime = dt;
    }

    public void setScheduledActionTime(LocalDateTime st) {
        this.scheduledActionTime = st;
    }

    public void setLastUpdatedTime(LocalDateTime lt) {
        this.lastUpdatedTime = lt;
    }
    public void setPassengerCount(int count) { this.passengerCount = count;}

    // Helper methods
    public int getTotalSeats() {
        return seatCapacity;
    }

    public int getSeatsLeft() {
        return seatCapacity - occupiedSeats.size();
    }

    public long getFlightDurationMinutes() {
        return Duration.between(originalDepartDateTime, originalArrivalDateTime).toMinutes();
    }

    public void addOccupiedSeats(List<String> seats) {
        for (String seat : seats) {
            if (!occupiedSeats.contains(seat)) {
                occupiedSeats.add(seat);
                passengerCount++;
            }
        }
    }

    public boolean isSeatOccupied(String seat) {
        return occupiedSeats.contains(seat);
    }

    public void removeOccupiedSeat(String seat) {
        // Check if the seat is actually occupied
        if (occupiedSeats.contains(seat)) {
            occupiedSeats.remove(seat);
            passengerCount--;
        }
        // If seat not found, do nothing (no change to passengerCount)
    }

    public boolean bookSpecificSeats(List<String> seats) {
        for (String s : seats) {
            if (isSeatOccupied(s) || s == null || s.isEmpty()) {
                return false;
            }
        }
        occupiedSeats.addAll(seats);
        passengerCount += seats.size();
        return true;
    }

    // Convert to file line – includes scheduledActionTime
    public String toFileString() {
        String seatStr = String.join(";", occupiedSeats);
        return flightNumber + "," + flightInstanceId + "," + seatCapacity + "," + passengerCount + "," +
                origin + "," + destination + "," + departDateTime + "," + arrivalDateTime + "," +
                scheduledActionTime + "," + status + "," + gateId + "," + runwayId + "," + seatStr;
    }
}
