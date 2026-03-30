import java.util.List;

public class ExploreService {
    private FlightManagement flightManagement;

    public ExploreService(FlightManagement flightManagement) {
        this.flightManagement = flightManagement;
    }

    public void displayAllFlightsForPassengers() {
        List<Flight> flights = flightManagement.getFlights();

        if (flights.isEmpty()) {
            System.out.println("No flights available.");
            return;
        }

        System.out.println("\n====================================================");
        System.out.println("                  AVAILABLE FLIGHTS");
        System.out.println("====================================================");

        for (Flight f : flights) {
            printPassengerFlightCard(f);
        }
    }

    public void searchByDestination(String destination) {
        List<Flight> flights = flightManagement.getFlights();
        boolean found = false;

        System.out.println("\n=========== SEARCH RESULT: " + destination.toUpperCase() + " ===========");

        for (Flight f : flights) {
            if (f.getDestination().equalsIgnoreCase(destination)) {
                printPassengerFlightCard(f);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No flights found for destination: " + destination);
        }
    }

    public void searchByFlightId(String flightId) {
        Flight f = flightManagement.FindFlightByInstanceId(flightId);

        if (f == null) {
            System.out.println("Flight not found.");
            return;
        }

        System.out.println("\n================ FLIGHT DETAILS ================");
        printPassengerFlightCard(f);
    }

    private void printPassengerFlightCard(Flight f) {
        int seatsLeft = f.getSeatCapacity() - f.getPassengerCount();

        System.out.println("----------------------------------------------------");
        System.out.println("[ " + f.getFlightNumber() + " ]  " + f.getOrigin() + " -> " + f.getDestination());
        System.out.println("Flight Instance ID : " + f.getFlightInstanceId());
        System.out.println("Departure          : " + f.getDepartDateTime());
        System.out.println("Arrival            : " + f.getArrivalDateTime());
        System.out.println("Status             : " + f.getStatus());
        System.out.println("Seats Left         : " + seatsLeft);
        System.out.println("----------------------------------------------------");
    }
}