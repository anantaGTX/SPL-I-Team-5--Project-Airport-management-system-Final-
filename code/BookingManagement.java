import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.*;

public class BookingManagement {

    // ============================================================
    // FIELDS
    // ============================================================
    private FlightManagement flightManagement;
    private PassengerManagement passengerManagement;
    private Scanner scanner;

    private final int WINDOW_SEAT_PRICE = 500;
    private final int MIDDLE_SEAT_PRICE = 300;
    private final double CANCELLATION_REFUND_PERCENTAGE = 0.5;
    private final int CANCELLATION_HOURS_THRESHOLD = 6;

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    // Constructor without shared scanner (legacy)
    public BookingManagement(FlightManagement fm, PassengerManagement pm) {
        this.flightManagement = fm;
        this.passengerManagement = pm;
        this.scanner = new Scanner(System.in);
    }

    // NEW: Constructor with shared scanner for integration
    public BookingManagement(FlightManagement fm, PassengerManagement pm, Scanner scanner) {
        this.flightManagement = fm;
        this.passengerManagement = pm;
        this.scanner = scanner;
    }

    // ============================================================
    // MAIN BOOKING FLOW (Admin/Generic)
    // ============================================================
    public void startBooking() {
        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                    ✈️  BOOKING MANAGEMENT  ✈️                    ║" + RESET);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);

        System.out.print("\n📍 Enter destination: ");
        String destination = scanner.nextLine();

        System.out.print("📅 Enter travel date (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        LocalDate date;
        try {
            date = LocalDate.parse(dateInput, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            System.out.println(RED + "\n❌ Invalid date format." + RESET);
            return;
        }

        List<Flight> availableFlights = flightManagement.getFlightsByDestinationAndDate(destination, date);
        if (availableFlights.isEmpty()) {
            System.out.println(RED + "\n❌ No flights available for " + destination + " on " + date + RESET);
            return;
        }

        displayAvailableFlights(availableFlights);

        System.out.print("\n✈️ Enter the number of the flight you want to book: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        Flight selectedFlight = getSelectedFlight(availableFlights, choice);
        if (selectedFlight == null) return;

        if (selectedFlight.getSeatsLeft() == 0) {
            System.out.println(RED + "❌ No seats left on this flight." + RESET);
            return;
        }

        displayFlightDetails(selectedFlight);

        SeatLayout seatLayout = new SeatLayout(selectedFlight);
        List<String> selectedSeats = new ArrayList<>();
        int totalPrice = 0;

        while (true) {
            System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
            System.out.println(CYAN + "║                      🪑 SEAT SELECTION 🪑                      ║" + RESET);
            System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
            seatLayout.displayLayout(selectedSeats);

            System.out.print("\n🪑 Enter seats to select (comma-separated, e.g., A1,B2) or leave empty to skip: ");
            String seatInput = scanner.nextLine().trim();
            if (!seatInput.isEmpty()) {
                String[] seatChoices = seatInput.split(",");
                for (String s : seatChoices) {
                    s = s.trim().toUpperCase();

                    if (!seatLayout.isValidSeat(s)) {
                        System.out.println(RED + "   ❌ Seat " + s + " does not exist." + RESET);
                        continue;
                    }

                    if (!selectedSeats.contains(s) && seatLayout.isAvailable(s)) {
                        selectedSeats.add(s);
                        char seatCol = s.charAt(0);
                        int seatPrice = (seatCol == 'A' || seatCol == 'F') ? WINDOW_SEAT_PRICE : MIDDLE_SEAT_PRICE;
                        totalPrice += seatPrice;
                        System.out.println(GREEN + "   ✅ Seat " + s + " added. Price: ৳" + seatPrice + RESET);
                    } else {
                        System.out.println(RED + "   ❌ Seat " + s + " is not available or already selected." + RESET);
                    }
                }
            }

            seatLayout.displayLayout(selectedSeats);
            System.out.println(GREEN + "\n💰 Total price so far: ৳" + totalPrice + RESET);

            System.out.println("\n📋 Options:");
            System.out.println("   1. Select more seats");
            System.out.println("   2. Cancel a selected seat");
            System.out.println("   3. Proceed to confirmation");
            System.out.print("\n👉 Enter choice: ");
            int action = scanner.nextInt();
            scanner.nextLine();

            if (action == 1) {
                continue;
            } else if (action == 2) {
                System.out.print("🗑️ Enter seat to cancel: ");
                String cancelSeat = scanner.nextLine().trim().toUpperCase();
                if (selectedSeats.remove(cancelSeat)) {
                    char seatCol = cancelSeat.charAt(0);
                    totalPrice -= (seatCol == 'A' || seatCol == 'F') ? WINDOW_SEAT_PRICE : MIDDLE_SEAT_PRICE;
                    System.out.println(GREEN + "   ✅ Seat " + cancelSeat + " cancelled." + RESET);
                } else {
                    System.out.println(RED + "   ❌ Seat not in selection." + RESET);
                }
            } else if (action == 3) {
                break;
            } else {
                System.out.println(RED + "   ❌ Invalid choice." + RESET);
            }
        }

        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                  📝 BOOKING CONFIRMATION 📝                  ║" + RESET);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);

        System.out.print("\n👤 Enter passenger name: ");
        String passengerName = scanner.nextLine();

        displayBookingOverview(passengerName, selectedFlight, selectedSeats, totalPrice);

        System.out.print("\n✅ Confirm booking? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println(RED + "\n❌ Booking cancelled." + RESET);
            return;
        }

        String ticketId = generateTicketID();

        LocalDateTime departureTime = selectedFlight.getDepartDateTime();
        LocalDateTime checkInStartTime = departureTime.minusHours(2);
        LocalDateTime boardingStartTime = selectedFlight.getScheduledActionTime();
        LocalDateTime boardingCloseTime = departureTime.minusMinutes(15);

        Passenger passenger = new Passenger(
                ticketId, passengerName, selectedFlight.getFlightInstanceId(),
                selectedFlight.getOrigin(), selectedFlight.getDestination(),
                departureTime, checkInStartTime,
                false, false, "-",
                selectedSeats, totalPrice
        );

        passengerManagement.addPassenger(passenger);
        selectedFlight.bookSpecificSeats(selectedSeats);
        flightManagement.saveFlightsToFile();

        displayTicket(passenger, selectedFlight, boardingStartTime, boardingCloseTime);
    }

    // ============================================================
    // NEW: BOOKING FOR LOGGED-IN PASSENGER (Integration Method)
    // ============================================================
    public void startBooking(AppUser passenger) {
        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                    ✈️  BOOK A FLIGHT  ✈️                        ║" + RESET);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println(GREEN + "\n👤 Passenger: " + passenger.getFullName() + RESET);

        System.out.print("\n📍 Enter destination: ");
        String destination = scanner.nextLine().trim();

        System.out.print("📅 Enter travel date (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateInput, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            System.out.println(RED + "\n❌ Invalid date format. Use yyyy-MM-dd" + RESET);
            return;
        }

        List<Flight> availableFlights = flightManagement.getFlightsByDestinationAndDate(destination, date);
        if (availableFlights.isEmpty()) {
            System.out.println(RED + "\n❌ No flights available for " + destination + " on " + date + RESET);
            return;
        }

        displayAvailableFlights(availableFlights);

        System.out.print("\n✈️ Enter flight number: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(RED + "\n❌ Invalid selection." + RESET);
            return;
        }

        Flight selectedFlight = getSelectedFlight(availableFlights, choice);
        if (selectedFlight == null) return;

        if (selectedFlight.getSeatsLeft() == 0) {
            System.out.println(RED + "\n❌ No seats left on this flight." + RESET);
            return;
        }

        displayFlightDetails(selectedFlight);

        SeatLayout seatLayout = new SeatLayout(selectedFlight);
        List<String> selectedSeats = new ArrayList<>();
        int totalPrice = 0;

        while (true) {
            System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
            System.out.println(CYAN + "║                      🪑 SEAT SELECTION 🪑                      ║" + RESET);
            System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
            seatLayout.displayLayout(selectedSeats);

            System.out.print("\n🪑 Enter seats to select (comma-separated, e.g., A1,B2) or ENTER to skip: ");
            String seatInput = scanner.nextLine().trim().toUpperCase();
            if (!seatInput.isEmpty()) {
                String[] seatChoices = seatInput.split(",");
                for (String s : seatChoices) {
                    s = s.trim();
                    if (!seatLayout.isValidSeat(s)) {
                        System.out.println(RED + "   ❌ Seat " + s + " does not exist." + RESET);
                        continue;
                    }
                    if (!selectedSeats.contains(s) && seatLayout.isAvailable(s)) {
                        selectedSeats.add(s);
                        int seatPrice = seatLayout.getSeatPrice(s);
                        totalPrice += seatPrice;
                        System.out.println(GREEN + "   ✅ Seat " + s + " added. Price: ৳" + seatPrice + RESET);
                    } else {
                        System.out.println(RED + "   ❌ Seat " + s + " is not available or already selected." + RESET);
                    }
                }
            }

            seatLayout.displayLayout(selectedSeats);
            System.out.println(GREEN + "\n💰 Total price so far: ৳" + totalPrice + RESET);

            System.out.println("\n📋 Options:");
            System.out.println("   1. Select more seats");
            System.out.println("   2. Cancel a selected seat");
            System.out.println("   3. Proceed to confirmation");
            System.out.print("\n👉 Enter choice: ");

            int action;
            try {
                action = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                action = 0;
            }

            if (action == 1) {
                continue;
            } else if (action == 2) {
                System.out.print("🗑️ Enter seat to cancel: ");
                String cancelSeat = scanner.nextLine().trim().toUpperCase();
                if (selectedSeats.remove(cancelSeat)) {
                    totalPrice -= seatLayout.getSeatPrice(cancelSeat);
                    System.out.println(GREEN + "   ✅ Seat " + cancelSeat + " cancelled." + RESET);
                } else {
                    System.out.println(RED + "   ❌ Seat not in selection." + RESET);
                }
            } else if (action == 3) {
                break;
            } else {
                System.out.println(RED + "   ❌ Invalid choice." + RESET);
            }
        }

        if (selectedSeats.isEmpty()) {
            System.out.println(RED + "\n❌ No seats selected. Booking cancelled." + RESET);
            return;
        }

        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                  📝 BOOKING CONFIRMATION 📝                  ║" + RESET);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);

        // Use passenger's name from AppUser
        displayBookingOverview(passenger.getFullName(), selectedFlight, selectedSeats, totalPrice);

        System.out.print("\n✅ Confirm booking? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println(RED + "\n❌ Booking cancelled." + RESET);
            return;
        }

        String ticketId = generateTicketID();

        LocalDateTime departureTime = selectedFlight.getDepartDateTime();
        LocalDateTime checkInStartTime = departureTime.minusHours(2);
        LocalDateTime boardingStartTime = selectedFlight.getScheduledActionTime();
        LocalDateTime boardingCloseTime = departureTime.minusMinutes(15);

        Passenger passengerObj = new Passenger(
                ticketId, passenger.getFullName(), selectedFlight.getFlightInstanceId(),
                selectedFlight.getOrigin(), selectedFlight.getDestination(),
                departureTime, checkInStartTime,
                false, false, "-",
                selectedSeats, totalPrice
        );

        passengerManagement.addPassenger(passengerObj);
        selectedFlight.bookSpecificSeats(selectedSeats);
        flightManagement.saveFlightsToFile();

        System.out.println(GREEN + "\n✅ Booking confirmed successfully!" + RESET);
        System.out.println("🎫 Ticket ID: " + ticketId);
        System.out.println("📧 Ticket details have been saved.\n");
    }

    // ============================================================
    // DISPLAY HELPER METHODS
    // ============================================================
    private void displayAvailableFlights(List<Flight> flights) {
        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                              ✈️ AVAILABLE FLIGHTS ✈️                                 ║" + RESET);
        System.out.println(CYAN + "╠════╦══════════════════════════╦══════════════╦══════════════╦══════════════════════════╣" + RESET);
        System.out.println(CYAN + "║ No ║ Flight Instance ID       ║ Departure    ║ Departure    ║ Seats Left               ║" + RESET);
        System.out.println(CYAN + "║    ║                          ║ Date         ║ Time         ║                          ║" + RESET);
        System.out.println(CYAN + "╠════╬══════════════════════════╬══════════════╬══════════════╬══════════════════════════╣" + RESET);

        int index = 1;
        for (Flight f : flights) {
            System.out.printf(CYAN + "║ %-2d ║ " + WHITE + "%-24s " + CYAN + "║ " + WHITE + "%-12s " + CYAN + "║ " + WHITE + "%-12s " + CYAN + "║ " + WHITE + "%-22d " + CYAN + "║\n" + RESET,
                    index, f.getFlightInstanceId(),
                    f.getDepartDateTime().toLocalDate(),
                    f.getDepartDateTime().toLocalTime(),
                    f.getSeatsLeft());
            index++;
        }
        System.out.println(CYAN + "╚════╩══════════════════════════╩══════════════╩══════════════╩══════════════════════════╝" + RESET);
    }

    private Flight getSelectedFlight(List<Flight> flights, int choice) {
        if (choice < 1 || choice > flights.size()) {
            System.out.println(RED + "❌ Invalid selection." + RESET);
            return null;
        }
        return flights.get(choice - 1);
    }

    private void displayFlightDetails(Flight flight) {
        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                     ✈️ FLIGHT DETAILS ✈️                      ║" + RESET);
        System.out.println(CYAN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Flight Number", flight.getFlightNumber());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Flight Instance", flight.getFlightInstanceId());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Origin", flight.getOrigin());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Destination", flight.getDestination());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Departure", flight.getDepartDateTime());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Arrival", flight.getArrivalDateTime());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44d " + CYAN + "║\n" + RESET, "Seats Available", flight.getSeatsLeft());
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
    }

    private void displayBookingOverview(String passengerName, Flight flight, List<String> seats, int totalPrice) {
        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                     📋 BOOKING OVERVIEW 📋                     ║" + RESET);
        System.out.println(CYAN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Passenger Name", passengerName);
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Destination", flight.getDestination());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Flight", flight.getFlightInstanceId());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Origin", flight.getOrigin());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Departure Date", flight.getDepartDateTime().toLocalDate());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Departure Time", flight.getDepartDateTime().toLocalTime());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Arrival", flight.getArrivalDateTime());
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + WHITE + "%-44s " + CYAN + "║\n" + RESET, "Seats Selected", String.join(", ", seats));
        System.out.printf(CYAN + "║ " + WHITE + "%-15s " + CYAN + ": " + GREEN + "৳%-43d " + CYAN + "║\n" + RESET, "Total Price", totalPrice);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
    }

    private void displayTicket(Passenger passenger, Flight flight, LocalDateTime boardingStart, LocalDateTime boardingClose) {
        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                                    🎫 BOARDING PASS 🎫                                   ║" + RESET);
        System.out.println(CYAN + "╠════════════════════════════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Ticket ID", passenger.getTicketId());
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Passenger Name", passenger.getPassengerName());
        System.out.println(CYAN + "╠────────────────────────────────────────────────────────────────────────────────────────╣" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Flight", passenger.getFlightInstanceId());
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Route", passenger.getOrigin() + " → " + passenger.getDestination());
        System.out.println(CYAN + "╠────────────────────────────────────────────────────────────────────────────────────────╣" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Departure Date", flight.getDepartDateTime().toLocalDate());
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Departure Time", flight.getDepartDateTime().toLocalTime());
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Arrival Time", flight.getArrivalDateTime().toLocalTime());
        System.out.println(CYAN + "╠────────────────────────────────────────────────────────────────────────────────────────╣" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Check-in Opens", passenger.getCheckInStartTime());
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Boarding Starts", boardingStart);
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Boarding Closes", boardingClose);
        System.out.println(CYAN + "╠────────────────────────────────────────────────────────────────────────────────────────╣" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + WHITE + "%-64s " + CYAN + "║\n" + RESET, "Seat(s)", String.join(", ", passenger.getSeats()));
        System.out.printf(CYAN + "║ " + WHITE + "%-20s " + CYAN + ": " + GREEN + "৳%-63d " + CYAN + "║\n" + RESET, "Total Paid", passenger.getTotalPrice());
        System.out.println(CYAN + "╠════════════════════════════════════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(CYAN + "║                                    IMPORTANT NOTES                                    ║" + RESET);
        System.out.println(CYAN + "║ • Please arrive at the airport at least 2 hours before departure                    ║" + RESET);
        System.out.println(CYAN + "║ • Check-in closes 45 minutes before departure                                      ║" + RESET);
        System.out.println(CYAN + "║ • Boarding closes 15 minutes before departure                                      ║" + RESET);
        System.out.println(CYAN + "║ • Cancellation allowed up to 6 hours before departure (50% refund)                 ║" + RESET);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println(GREEN + "\n🎉 Booking confirmed! Thank you for choosing our airline! 🎉\n" + RESET);
    }

    // ============================================================
    // CANCELLATION SYSTEM
    // ============================================================
    public void cancelBooking(LocalDateTime currentTime) {
        System.out.println("\n" + CYAN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║                  🗑️ TICKET CANCELLATION 🗑️                   ║" + RESET);
        System.out.println(CYAN + "╚════════════════════════════════════════════════════════════════╝" + RESET);

        System.out.print("\n🎫 Enter Ticket ID: ");
        String ticketId = scanner.nextLine().trim();

        Passenger passenger = findPassengerByTicketId(ticketId);
        if (passenger == null) {
            System.out.println(RED + "\n❌ Ticket not found." + RESET);
            return;
        }

        if (passenger.isBoardingPassIssued()) {
            System.out.println(RED + "\n❌ Cannot cancel. Boarding pass already issued." + RESET);
            return;
        }

        Flight flight = flightManagement.FindFlightByInstanceId(passenger.getFlightInstanceId());
        if (flight == null) {
            System.out.println(RED + "\n❌ Flight not found." + RESET);
            return;
        }

        LocalDateTime departureTime = flight.getDepartDateTime();
        long hoursUntilDeparture = Duration.between(currentTime, departureTime).toHours();

        displayCurrentBooking(passenger, flight, hoursUntilDeparture);

        if (hoursUntilDeparture < CANCELLATION_HOURS_THRESHOLD) {
            System.out.println(RED + "\n❌ Cancellation not allowed." + RESET);
            System.out.println("   Cancellations are only allowed up to " + CANCELLATION_HOURS_THRESHOLD + " hours before departure.");
            System.out.println("   Current simulation time is only " + hoursUntilDeparture + " hours before departure.");
            return;
        }

        showCancellationOptions(passenger, flight);
    }

    private void displayCurrentBooking(Passenger passenger, Flight flight, long hoursUntilDeparture) {
        System.out.println("\n📋 Current Booking Details:");
        System.out.println("   Ticket ID: " + passenger.getTicketId());
        System.out.println("   Passenger: " + passenger.getPassengerName());
        System.out.println("   Flight: " + passenger.getFlightInstanceId());
        System.out.println("   Route: " + passenger.getOrigin() + " → " + passenger.getDestination());
        System.out.println("   Departure: " + flight.getDepartDateTime());
        System.out.println("   Hours until departure: " + hoursUntilDeparture);
        System.out.println("   Seats booked: " + String.join(", ", passenger.getSeats()));
        System.out.println("   Total paid: ৳" + passenger.getTotalPrice());
    }

    private void showCancellationOptions(Passenger passenger, Flight flight) {
        List<String> currentSeats = new ArrayList<>(passenger.getSeats());
        int currentPrice = passenger.getTotalPrice();

        System.out.println("\n📋 Cancellation Options:");
        System.out.println("   1. Cancel entire booking (all seats)");
        System.out.println("   2. Cancel specific seat(s)");
        System.out.println("   3. Back to main menu");
        System.out.print("\n👉 Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                cancelEntireBooking(passenger, flight, currentPrice);
                break;
            case 2:
                cancelSpecificSeats(passenger, flight, currentSeats, currentPrice);
                break;
            case 3:
                System.out.println(YELLOW + "\n❌ Cancellation cancelled." + RESET);
                break;
            default:
                System.out.println(RED + "\n❌ Invalid choice." + RESET);
        }
    }

    private void cancelEntireBooking(Passenger passenger, Flight flight, int originalPrice) {
        System.out.println(YELLOW + "\n⚠️ WARNING: You are about to cancel ALL seats for this booking." + RESET);
        System.out.println("   Seats to cancel: " + String.join(", ", passenger.getSeats()));
        System.out.println("   Total refund: ৳" + (int) (originalPrice * CANCELLATION_REFUND_PERCENTAGE));

        System.out.print("\n👉 Confirm cancellation? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();

        if (confirm.equals("Y")) {
            for (String seat : passenger.getSeats()) {
                flight.removeOccupiedSeat(seat);
            }
            flightManagement.saveFlightsToFile();

            passengerManagement.cancelBooking(passenger);

            System.out.println(GREEN + "\n✅ Entire booking cancelled successfully!" + RESET);
            System.out.println("   Refund amount: ৳" + (int) (originalPrice * CANCELLATION_REFUND_PERCENTAGE));
            System.out.println("   Refund will be processed within 5-7 business days.");
        } else {
            System.out.println(YELLOW + "\n❌ Cancellation aborted." + RESET);
        }
    }
    private void cancelSpecificSeats(Passenger passenger, Flight flight, List<String> currentSeats, int currentPrice) {
        System.out.println("\n🪑 Current seats: " + String.join(", ", currentSeats));
        System.out.print("\n🪑 Enter seat(s) to cancel (comma-separated, e.g., A1,B2): ");
        String seatInput = scanner.nextLine().trim().toUpperCase();

        String[] seatsToCancel = seatInput.split(",");
        List<String> cancelledSeats = new ArrayList<>();
        List<String> remainingSeats = new ArrayList<>(currentSeats);
        int refundAmount = 0;

        for (String seat : seatsToCancel) {
            seat = seat.trim();
            if (currentSeats.contains(seat)) {
                cancelledSeats.add(seat);
                remainingSeats.remove(seat);
                char seatCol = seat.charAt(0);
                int seatPrice = (seatCol == 'A' || seatCol == 'F') ? WINDOW_SEAT_PRICE : MIDDLE_SEAT_PRICE;
                refundAmount += seatPrice;
            } else {
                System.out.println(RED + "   ❌ Seat " + seat + " not found in your booking." + RESET);
            }
        }

        if (cancelledSeats.isEmpty()) {
            System.out.println(RED + "\n❌ No valid seats selected for cancellation." + RESET);
            return;
        }

        int actualRefund = (int) (refundAmount * CANCELLATION_REFUND_PERCENTAGE);
        // FIX: Reduce total price by actual refund, not full seat price
        int newTotalPrice = currentPrice - actualRefund;

        System.out.println("\n📋 Cancellation Summary:");
        System.out.println("   Seats to cancel: " + String.join(", ", cancelledSeats));
        System.out.println("   Remaining seats: " + (remainingSeats.isEmpty() ? "None" : String.join(", ", remainingSeats)));
        System.out.println("   Original price: ৳" + currentPrice);
        System.out.println("   Seat price refund (before policy): ৳" + refundAmount);
        System.out.println("   Actual refund (50%): ৳" + actualRefund);
        System.out.println("   New total price: ৳" + newTotalPrice);

        System.out.print("\n👉 Confirm cancellation of these seats? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();

        if (confirm.equals("Y")) {
            for (String seat : cancelledSeats) {
                flight.removeOccupiedSeat(seat);
            }
            flightManagement.saveFlightsToFile();

            passenger.setSeats(remainingSeats);
            passenger.setTotalPrice(newTotalPrice);

            if (remainingSeats.isEmpty()) {
                passengerManagement.cancelBooking(passenger);
                System.out.println(GREEN + "\n✅ All seats cancelled. Booking fully cancelled." + RESET);
            } else {
                passengerManagement.savePassengersToFile();
                System.out.println(GREEN + "\n✅ Seat cancellation successful!" + RESET);
                System.out.println("   Remaining seats: " + String.join(", ", remainingSeats));
                System.out.println("   New total: ৳" + newTotalPrice);
            }
            System.out.println("   Refund amount: ৳" + actualRefund);
            System.out.println("   Refund will be processed within 5-7 business days.");
        } else {
            System.out.println(YELLOW + "\n❌ Cancellation aborted." + RESET);
        }
    }

    private Passenger findPassengerByTicketId(String ticketId) {
        for (Passenger p : passengerManagement.getAllPassengers()) {
            if (p.getTicketId().equals(ticketId)) {
                return p;
            }
        }
        return null;
    }

    // ============================================================
    // TICKET ID GENERATOR
    // ============================================================
    private String generateTicketID() {
        List<Passenger> allPassengers = passengerManagement.getAllPassengers();
        int maxId = 0;

        for (Passenger p : allPassengers) {
            String tid = p.getTicketId().replaceAll("\\D", "");
            try {
                int num = Integer.parseInt(tid);
                if (num > maxId) maxId = num;
            } catch (NumberFormatException ignored) {
            }
        }

        int nextId = maxId + 1;
        return String.format("T%03d", nextId);
    }
}
