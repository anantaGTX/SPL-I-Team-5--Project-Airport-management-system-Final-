public class Runway {

    private String runwayId;     // ID of the runway (e.g., R1, R2)
    private boolean isFree;      // True if the runway is free, false if occupied
    private String flightInstanceId;     // Flight currently assigned to this runway, "-" if none

    // Constructor: creates a Runway object with initial state
    public Runway(String runwayId, boolean isFree, String flightInstanceId) {
        this.runwayId = runwayId;
        this.isFree = isFree;
        this.flightInstanceId = flightInstanceId;
    }

    // Getter for runway ID
    public String getRunwayId() {
        return runwayId;
    }

    // Getter for whether runway is free
    public boolean isFree() {
        return isFree;
    }

    // Getter for flight assigned to this runway
    public String getAssignedflightInstanceId() {
        return flightInstanceId;
    }

    // Assigns a flight to this runway
    public void assignFlight(String flightInstanceId) {
        this.flightInstanceId = flightInstanceId;
        this.isFree = false;   // mark runway as occupied
    }

    // Frees the runway after flight departs or is cancelled
    public void freeRunway() {
        this.flightInstanceId = "-";   // no flight assigned
        this.isFree = true;    // mark runway as free
    }

    // Prepares a string to write to a text file
    public String toFileString() {
        return runwayId + "," + isFree + "," + flightInstanceId;
    }
}
