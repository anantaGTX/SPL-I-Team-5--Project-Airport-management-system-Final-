import java.time.LocalDateTime;
import java.util.List;

public class Passenger {
    private String ticketId;
    private String passengerName;
    private String flightInstanceId;
    private String origin;
    private String destination;
    private LocalDateTime journeyDateTime; // Current departure time
    private LocalDateTime checkInStartTime; // When passenger can check in (departure - 2 hours)
    private boolean hasCheckedIn;
    private boolean boardingPassIssued;
    private String gateId;
    private List<String> seats;
    private int totalPrice;

    // Full constructor
    public Passenger(String ticketId, String passengerName, String flightInstanceId,
                     String origin, String destination,
                     LocalDateTime journeyDateTime, LocalDateTime checkInStartTime,
                     boolean hasCheckedIn, boolean boardingPassIssued, String gateId,
                     List<String> seats, int totalPrice) {
        this.ticketId = ticketId;
        this.passengerName = passengerName;
        this.flightInstanceId = flightInstanceId;
        this.origin = origin;
        this.destination = destination;
        this.journeyDateTime = journeyDateTime;
        this.checkInStartTime = checkInStartTime;
        this.hasCheckedIn = hasCheckedIn;
        this.boardingPassIssued = boardingPassIssued;
        this.gateId = gateId;
        this.seats = seats;
        this.totalPrice = totalPrice;
    }

    // Getters
    public String getTicketId() { return ticketId; }
    public String getPassengerName() { return passengerName; }
    public String getFlightInstanceId() { return flightInstanceId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getJourneyDateTime() { return journeyDateTime; }
    public LocalDateTime getCheckInStartTime() { return checkInStartTime; }
    public boolean hasCheckedIn() { return hasCheckedIn; }
    public boolean isBoardingPassIssued() { return boardingPassIssued; }
    public String getGateId() { return gateId; }
    public List<String> getSeats() { return seats; }
    public int getTotalPrice() { return totalPrice; }

    // Setters
    public void setJourneyDateTime(LocalDateTime newJourneyTime) {
        this.journeyDateTime = newJourneyTime;
    }

    public void setCheckInStartTime(LocalDateTime newCheckInStart) {
        this.checkInStartTime = newCheckInStart;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.hasCheckedIn = checkedIn;
    }

    public void issueBoardingPass(String gateId) {
        this.boardingPassIssued = true;
        this.gateId = gateId;
    }

    // NEW: Setter for seats (for partial cancellation)
    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    // NEW: Setter for total price (for partial cancellation)
    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Convert to file line
    public String toFileString() {
        String seatStr = seats != null ? String.join(";", seats) : "";
        return ticketId + "," + passengerName + "," + flightInstanceId + "," +
                origin + "," + destination + "," + journeyDateTime + "," +
                checkInStartTime + "," + hasCheckedIn + "," + boardingPassIssued + "," + gateId +
                "," + seatStr + "," + totalPrice;
    }
}