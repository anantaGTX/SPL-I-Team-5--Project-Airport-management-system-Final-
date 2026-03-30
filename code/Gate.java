public class Gate {

    private String gateId;
    private boolean isFree;
    private String flightInstanceId;

    public Gate(String gateId, boolean isFree, String flightInstanceId) {
        this.gateId = gateId;
        this.isFree = isFree;
        this.flightInstanceId = flightInstanceId;
    }

    public String getGateId() {
        return gateId;
    }

    public boolean isFree() {
        return isFree;
    }

    public String getAssignedFlightInstanceId() {
        return flightInstanceId;
    }

    public void assignFlight(String flightInstanceId) {
        this.flightInstanceId = flightInstanceId;
        this.isFree = false;
    }

    public void freeGate() {
        this.flightInstanceId = "-";
        this.isFree = true;
    }

    public String toFileString() {
        return gateId + "," + isFree + "," + flightInstanceId;
    }
}
