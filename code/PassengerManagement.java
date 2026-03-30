import java.io.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PassengerManagement {

    private List<Passenger> passengers;
    private FlightManagement flightManagement;
    private String passengerFilePath = "passengers.txt";
    private String passengerRemovedFilePath = "passengerRemoved.txt";
    private String cancelledBookingsFilePath = "cancelledBookings.txt";

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";

    public PassengerManagement(FlightManagement fm) {
        this.passengers = new ArrayList<>();
        this.flightManagement = fm;
        loadPassengersFromFile();
    }

    // ============================================================
    // FILE OPERATIONS
    // ============================================================
    private void loadPassengersFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(passengerFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 10) continue;

                String ticketId = parts[0];
                String name = parts[1];
                String flightInstanceId = parts[2];
                String origin = parts[3];
                String destination = parts[4];
                LocalDateTime journeyDateTime = LocalDateTime.parse(parts[5]);
                LocalDateTime checkInStartTime = LocalDateTime.parse(parts[6]);
                boolean hasCheckedIn = Boolean.parseBoolean(parts[7]);
                boolean boardingPassIssued = Boolean.parseBoolean(parts[8]);
                String gateId = parts[9];

                List<String> seats = new ArrayList<>();
                if (parts.length > 10 && parts[10] != null && !parts[10].isEmpty()) {
                    String[] seatArray = parts[10].split(";");
                    for (String s : seatArray) seats.add(s.trim());
                }

                int totalPrice = 0;
                if (parts.length > 11 && parts[11] != null && !parts[11].isEmpty()) {
                    try {
                        totalPrice = Integer.parseInt(parts[11]);
                    } catch (NumberFormatException e) {
                        totalPrice = 0;
                    }
                }

                Passenger p = new Passenger(ticketId, name, flightInstanceId, origin, destination,
                        journeyDateTime, checkInStartTime, hasCheckedIn, boardingPassIssued, gateId,
                        seats, totalPrice);
                passengers.add(p);
            }
        } catch (FileNotFoundException e) {
            System.out.println(YELLOW + "Passenger file not found. Creating new file." + RESET);
            savePassengersToFile();
        } catch (IOException e) {
            System.out.println(RED + "Error loading passengers: " + e.getMessage() + RESET);
        }
    }

    public void savePassengersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(passengerFilePath))) {
            for (Passenger p : passengers) {
                bw.write(p.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(RED + "Error saving passengers: " + e.getMessage() + RESET);
        }
    }

    private void saveRemovedPassengers(List<Passenger> removedPassengers) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(passengerRemovedFilePath, true))) {
            for (Passenger p : removedPassengers) {
                bw.write(p.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(RED + "Error saving removed passengers: " + e.getMessage() + RESET);
        }
    }

    public void saveCancelledBooking(Passenger p) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(cancelledBookingsFilePath, true))) {
            bw.write(p.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println(RED + "Error saving cancelled booking: " + e.getMessage() + RESET);
        }
    }

    public void clearRemovedPassengersFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(passengerRemovedFilePath))) {
            bw.write("");
            System.out.println(GREEN + "Removed passengers list cleared." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error clearing removed passengers file: " + e.getMessage() + RESET);
        }
    }

    public void clearCancelledBookingsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(cancelledBookingsFilePath))) {
            bw.write("");
            System.out.println(GREEN + "Cancelled bookings list cleared." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error clearing cancelled bookings file: " + e.getMessage() + RESET);
        }
    }

    private Passenger findPassengerByTicket(String ticketId) {
        for (Passenger p : passengers) {
            if (p.getTicketId().equals(ticketId))
                return p;
        }
        return null;
    }

    public List<Passenger> getAllPassengers() {
        return passengers;
    }

    public void updatePassengersFlightTimes(String flightInstanceId, LocalDateTime newDepartTime) {
        LocalDateTime newCheckInStart = newDepartTime.minusHours(2);
        boolean updated = false;

        for (Passenger p : passengers) {
            if (p.getFlightInstanceId().equals(flightInstanceId)) {
                p.setJourneyDateTime(newDepartTime);
                p.setCheckInStartTime(newCheckInStart);
                updated = true;
            }
        }

        if (updated) {
            savePassengersToFile();
            System.out.println(CYAN + "Updated flight times for passengers of " + flightInstanceId + RESET);
            System.out.println("   New departure: " + newDepartTime);
            System.out.println("   New check-in start: " + newCheckInStart);
        }
    }

    public void cancelBooking(Passenger p) {
        saveCancelledBooking(p);
        passengers.remove(p);
        savePassengersToFile();
        System.out.println(GREEN + "Booking cancelled for passenger: " + p.getPassengerName() + RESET);
    }

    public void checkIn(String ticketId, LocalDateTime currentTime) {
        Passenger p = findPassengerByTicket(ticketId);
        if (p == null) {
            System.out.println(RED + "❌ Invalid ticket." + RESET);
            return;
        }

        if (p.isBoardingPassIssued()) {
            System.out.println(YELLOW + "⚠️ Boarding pass already issued." + RESET);
            return;
        }

        if (p.hasCheckedIn()) {
            System.out.println(YELLOW + "⚠️ Already checked in. Please proceed to gate." + RESET);
            return;
        }

        Flight flight = flightManagement.FindFlightByInstanceId(p.getFlightInstanceId());
        if (flight == null) {
            System.out.println(RED + "❌ Flight not found." + RESET);
            return;
        }

        LocalDateTime flightDepartTime = flight.getDepartDateTime();
        LocalDateTime flightBoardingStart = flight.getScheduledActionTime();
        LocalDateTime flightBoardingClose = flightDepartTime.minusMinutes(15);
        LocalDateTime flightCheckInStart = flightDepartTime.minusHours(2);
        LocalDateTime flightCheckInEnd = flightBoardingClose.minusMinutes(30);

        boolean flightTimeChanged = !p.getJourneyDateTime().equals(flightDepartTime);

        if (flightTimeChanged) {
            System.out.println(YELLOW + "⚠️ Flight time changed." + RESET);
            System.out.println("   Original departure: " + p.getJourneyDateTime());
            System.out.println("   New departure: " + flightDepartTime);
            System.out.println("   New check-in starts at: " + flightCheckInStart);
            System.out.println("   New check-in closes at: " + flightCheckInEnd);
            System.out.println("   New boarding starts at: " + flightBoardingStart);
            System.out.println("   New boarding closes at: " + flightBoardingClose);

            p.setJourneyDateTime(flightDepartTime);
            p.setCheckInStartTime(flightCheckInStart);
            savePassengersToFile();
        }

        if (currentTime.isBefore(flightCheckInStart)) {
            System.out.println(RED + "❌ Too early to check in." + RESET);
            System.out.println("   Check-in starts at: " + flightCheckInStart);
            if (flightTimeChanged) {
                System.out.println(YELLOW + "   (Flight was delayed. Please return at the new check-in time.)" + RESET);
            }
            return;
        }

        if (currentTime.isAfter(flightCheckInEnd)) {
            System.out.println(RED + "❌ Sorry! Check-in is closed." + RESET);
            System.out.println("   Check-in closed at: " + flightCheckInEnd);
            System.out.println("   Boarding starts at: " + flightBoardingStart);
            System.out.println("   Boarding closes at: " + flightBoardingClose);
            System.out.println("   You needed 30 minutes to reach the gate.");
            if (flightTimeChanged) {
                System.out.println(YELLOW + "   (Flight was delayed, but you arrived after check-in closed.)" + RESET);
            }
            return;
        }

        p.setCheckedIn(true);
        savePassengersToFile();

        long minutesUntilBoarding = Duration.between(currentTime, flightBoardingStart).toMinutes();
        String gateInfo = flight.getGateId();
        if (gateInfo == null || gateInfo.equals("-")) {
            gateInfo = "Will be assigned at boarding";
        }

        System.out.println(GREEN + "\n✅ Check-in successful!" + RESET);
        System.out.println(CYAN + "┌────────────────────────────────────────────────────────────────────────────┐" + RESET);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Passenger", p.getPassengerName());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Flight", flight.getFlightInstanceId());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Gate", gateInfo);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Boarding Starts", flightBoardingStart);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Boarding Closes", flightBoardingClose);
        System.out.println(CYAN + "└────────────────────────────────────────────────────────────────────────────┘" + RESET);

        if (minutesUntilBoarding <= 0) {
            System.out.println(YELLOW + "   ⚠️ Boarding is already happening! Please go to gate IMMEDIATELY." + RESET);
        } else if (minutesUntilBoarding <= 15) {
            System.out.println(YELLOW + "   ⚠️ Boarding starts in " + minutesUntilBoarding + " minutes. Please go to gate soon." + RESET);
        } else {
            System.out.println(GREEN + "   You have " + minutesUntilBoarding + " minutes until boarding starts." + RESET);
        }
        System.out.println("   Please proceed to gate before boarding closes.\n");
    }

    public void processBoarding(String ticketId, LocalDateTime currentTime) {
        Passenger p = findPassengerByTicket(ticketId);
        if (p == null) {
            System.out.println(RED + "❌ Invalid ticket." + RESET);
            return;
        }

        if (p.isBoardingPassIssued()) {
            System.out.println(YELLOW + "⚠️ Boarding pass already issued." + RESET);
            return;
        }

        if (!p.hasCheckedIn()) {
            System.out.println(RED + "❌ Please check in first at the counter." + RESET);
            return;
        }

        Flight flight = flightManagement.FindFlightByInstanceId(p.getFlightInstanceId());
        if (flight == null) {
            System.out.println(RED + "❌ Flight not found." + RESET);
            return;
        }

        LocalDateTime flightDepartTime = flight.getDepartDateTime();
        LocalDateTime flightBoardingStart = flight.getScheduledActionTime();
        LocalDateTime flightBoardingClose = flightDepartTime.minusMinutes(15);

        boolean flightTimeChanged = !p.getJourneyDateTime().equals(flightDepartTime);

        if (flightTimeChanged) {
            System.out.println(YELLOW + "⚠️ Flight time has changed since you checked in." + RESET);
            System.out.println("   Your original departure: " + p.getJourneyDateTime());
            System.out.println("   New departure: " + flightDepartTime);
            System.out.println("   New boarding starts at: " + flightBoardingStart);
            System.out.println("   New boarding closes at: " + flightBoardingClose);

            p.setJourneyDateTime(flightDepartTime);
            p.setCheckInStartTime(flightDepartTime.minusHours(2));
            savePassengersToFile();

            flightBoardingStart = flight.getScheduledActionTime();
            flightBoardingClose = flightDepartTime.minusMinutes(15);
        }

        if (currentTime.isBefore(flightBoardingStart)) {
            long minutesUntilBoarding = Duration.between(currentTime, flightBoardingStart).toMinutes();
            System.out.println(RED + "❌ Boarding hasn't started yet." + RESET);
            System.out.println("   Boarding starts at: " + flightBoardingStart);
            System.out.println("   Please return in " + minutesUntilBoarding + " minutes.");
            return;
        }

        if (currentTime.isAfter(flightBoardingClose)) {
            System.out.println(RED + "❌ Sorry! Boarding is over." + RESET);
            System.out.println("   Boarding closed at: " + flightBoardingClose);
            System.out.println("   Flight departed at: " + flightDepartTime);
            return;
        }

        if (!"BOARDING".equalsIgnoreCase(flight.getStatus())) {
            System.out.println(RED + "❌ Flight not boarding yet." + RESET);
            System.out.println("   Boarding starts at: " + flightBoardingStart);
            System.out.println("   Current flight status: " + flight.getStatus());
            return;
        }

        String gateId = flight.getGateId();
        if (gateId == null || gateId.equals("-")) {
            flightManagement.assignGate(flight);
            gateId = flight.getGateId();
        }

        p.issueBoardingPass(gateId);
        savePassengersToFile();

        long minutesUntilDeparture = Duration.between(currentTime, flightDepartTime).toMinutes();
        long minutesUntilBoardingCloses = Duration.between(currentTime, flightBoardingClose).toMinutes();

        System.out.println(GREEN + "\n✅ Boarding pass issued!" + RESET);
        System.out.println(CYAN + "┌────────────────────────────────────────────────────────────────────────────┐" + RESET);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Passenger", p.getPassengerName());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Gate", gateId);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Flight", flight.getFlightInstanceId());
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Departure", flightDepartTime);
        System.out.printf(CYAN + "│ " + RESET + "%-20s : " + WHITE + "%-50s" + RESET + CYAN + " │" + RESET + "\n", "Boarding Closes", flightBoardingClose);
        System.out.println(CYAN + "└────────────────────────────────────────────────────────────────────────────┘" + RESET);

        if (minutesUntilBoardingCloses <= 5) {
            System.out.println(RED + "   ⚠️ Boarding closes in " + minutesUntilBoardingCloses + " minutes! Please board immediately." + RESET);
        } else {
            System.out.println(GREEN + "   You have " + minutesUntilDeparture + " minutes until departure." + RESET);
        }
        System.out.println("   Please board the plane now.\n");
    }

    public void freePassengersOfFlight(String flightInstanceId) {
        List<Passenger> toRemove = new ArrayList<>();
        for (Passenger p : passengers) {
            if (p.getFlightInstanceId().equals(flightInstanceId)) {
                toRemove.add(p);
            }
        }

        if (!toRemove.isEmpty()) {
            saveRemovedPassengers(toRemove);
            passengers.removeAll(toRemove);
            savePassengersToFile();
            System.out.println(GREEN + "Freed " + toRemove.size() + " passengers for flight " + flightInstanceId + "." + RESET);
        }
    }

    public void addPassenger(Passenger p) {
        passengers.add(p);
        savePassengersToFile();
    }

    // ============================================================
    // PERFECT TABLE DISPLAY (Calculates column widths dynamically without color codes)
    // ============================================================
    public void displayPassengers() {
        if (passengers.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️ No passengers found." + RESET);
            return;
        }

        System.out.println("\n" + CYAN + "═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════" + RESET);
        System.out.println(CYAN + "                                            PASSENGER MANAGEMENT SYSTEM" + RESET);
        System.out.println(CYAN + "═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════" + RESET);
        System.out.printf(CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + WHITE + "%-20s " + CYAN + "║ " + WHITE + "%-18s " + CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + WHITE + "%-19s " + CYAN + "║ " + WHITE + "%-19s " + CYAN + "║ " + WHITE + "%-4s " + CYAN + "║ " + WHITE + "%-4s " + CYAN + "║ " + WHITE + "%-5s " + CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + WHITE + "%-6s " + CYAN + "║" + RESET + "\n",
                "Ticket", "Name", "Flight", "Origin", "Dest", "Journey DateTime", "Check-in Start", "CkIn", "Brd", "Gate", "Seats", "Price");
        System.out.println(CYAN + "═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════" + RESET);

        for (Passenger p : passengers) {
            String checkedIn = p.hasCheckedIn() ? (GREEN + "✓" + RESET) : (RED + "✗" + RESET);
            String boardingPass = p.isBoardingPassIssued() ? (GREEN + "✓" + RESET) : (RED + "✗" + RESET);
            String seatsStr = p.getSeats().isEmpty() ? "-" : String.join(",", p.getSeats());
            String gateId = p.getGateId().equals("-") ? "—" : p.getGateId();

            System.out.printf(CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + WHITE + "%-20s " + CYAN + "║ " + WHITE + "%-18s " + CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + WHITE + "%-19s " + CYAN + "║ " + WHITE + "%-19s " + CYAN + "║ " + RESET + "%-4s " + CYAN + "║ " + RESET + "%-4s " + CYAN + "║ " + WHITE + "%-5s " + CYAN + "║ " + WHITE + "%-8s " + CYAN + "║ " + GREEN + "%-6s " + CYAN + "║" + RESET + "\n",
                    truncate(p.getTicketId(), 8),
                    truncate(p.getPassengerName(), 20),
                    truncate(p.getFlightInstanceId(), 18),
                    truncate(p.getOrigin(), 8),
                    truncate(p.getDestination(), 8),
                    truncate(p.getJourneyDateTime().toString(), 19),
                    truncate(p.getCheckInStartTime().toString(), 19),
                    checkedIn,
                    boardingPass,
                    gateId,
                    truncate(seatsStr, 8),
                    "৳" + p.getTotalPrice());
        }

        System.out.println(CYAN + "═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════" + RESET);
        System.out.println(GREEN + "\n✅ Total passengers: " + passengers.size() + RESET + "\n");
    }

    // Add this helper method if not already present
    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    private String padRight(String s, int width) {
        if (s == null) s = "";
        // Strip ANSI codes for length calculation
        String plain = s.replaceAll("\u001B\\[[;\\d]*m", "");
        if (plain.length() >= width) return s;
        return s + " ".repeat(width - plain.length());
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }


    public void displayCancelledBookings() {
        try (BufferedReader br = new BufferedReader(new FileReader(cancelledBookingsFilePath))) {
            String line;
            boolean empty = true;
            System.out.println("\n" + YELLOW + "╔══════════════════════════════════════════════════════════════╗" + RESET);
            System.out.println(YELLOW + "║                    CANCELLED BOOKINGS                      ║" + RESET);
            System.out.println(YELLOW + "╚══════════════════════════════════════════════════════════════╝" + RESET);

            while ((line = br.readLine()) != null) {
                empty = false;
                String[] parts = line.split(",");
                if (parts.length >= 12) {
                    System.out.println(CYAN + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + RESET);
                    System.out.println("   Ticket ID: " + parts[0]);
                    System.out.println("   Passenger: " + parts[1]);
                    System.out.println("   Flight: " + parts[2]);
                    System.out.println("   Route: " + parts[3] + " → " + parts[4]);
                    System.out.println("   Seats: " + (parts[9].isEmpty() ? "-" : parts[9]));
                    System.out.println("   Total Paid: ৳" + parts[11]);
                }
            }
            if (empty) {
                System.out.println(YELLOW + "   No cancelled bookings found." + RESET);
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println(YELLOW + "   No cancelled bookings found." + RESET);
        }
    }
}