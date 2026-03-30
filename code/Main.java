import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";

    // Global scanner (shared across all classes to avoid input issues)
    private static Scanner globalScanner;

    // Store managers as static fields so all methods can access them
    private static ExploreService exploreService;
    private static FlightManagement flightManager;
    private static PassengerManagement passengerManager;
    private static BookingManagement bookingManager;
    private static DepartureFlightManager departureManager;
    private static ArrivalFlightManager arrivalManager;
    private static GateManagement gateManager;
    private static RunwayManagement runwayManager;
    private static WeatherManager weatherManager;
    private static AuthManager authManager;

    public static void main(String[] args) {
        globalScanner = new Scanner(System.in);

        // ============================================================
        // INITIALIZE ALL MANAGERS
        // ============================================================
        gateManager = new GateManagement();
        runwayManager = new RunwayManagement();
        flightManager = new FlightManagement(gateManager, runwayManager);
        passengerManager = new PassengerManagement(flightManager);
        flightManager.populateOccupiedSeatsFromPassengers(passengerManager.getAllPassengers());
        weatherManager = new WeatherManager();
        authManager = new AuthManager();
        exploreService = new ExploreService(flightManager);

        departureManager = new DepartureFlightManager(flightManager, passengerManager);
        arrivalManager = new ArrivalFlightManager(flightManager);

        // BookingManagement with shared scanner
        bookingManager = new BookingManagement(flightManager, passengerManager, globalScanner);

        // Set references in FlightManagement
        flightManager.setWeatherManager(weatherManager);
        flightManager.setDepartureManager(departureManager);
        flightManager.setArrivalManager(arrivalManager);

        // ============================================================
        // MAIN APPLICATION LOOP
        // ============================================================
        boolean exit = false;

        while (!exit) {
            clearScreen();
            printLogo();
            printHeader("AIRPORT MANAGEMENT SYSTEM");
            System.out.println("  " + CYAN + "1" + RESET + ". Explore Flights");
            System.out.println("  " + CYAN + "2" + RESET + ". Passenger Sign Up");
            System.out.println("  " + CYAN + "3" + RESET + ". Passenger Sign In");
            System.out.println("  " + CYAN + "4" + RESET + ". Admin Login");
            System.out.println("  " + YELLOW + "0" + RESET + ". Exit");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    exploreFlightsMenu();
                    break;
                case "2":
                    passengerSignUp();
                    break;
                case "3":
                    AppUser passenger = passengerSignIn();
                    if (passenger != null) {
                        passengerPortal(passenger);
                    }
                    break;
                case "4":
                    AppUser admin = adminLogin();
                    if (admin != null) {
                        adminMenu();
                    }
                    break;
                case "0":
                    exit = true;
                    System.out.println(GREEN + "\n  Thank you for using Airport Management System. Goodbye!" + RESET);
                    break;
                default:
                    System.out.println(RED + "  Invalid choice. Please try again." + RESET);
                    waitForEnter();
            }
        }
        globalScanner.close();
    }

    // ============================================================
    // EXPLORE FLIGHTS (Guest)
    // ============================================================
    private static void exploreFlightsMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("EXPLORE FLIGHTS");
            System.out.println("  " + CYAN + "1" + RESET + ". View All Flights");
            System.out.println("  " + CYAN + "2" + RESET + ". Search by Destination");
            System.out.println("  " + CYAN + "3" + RESET + ". Search by Flight ID");
            System.out.println("  " + YELLOW + "0" + RESET + ". Back to Main Menu");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    exploreService.displayAllFlightsForPassengers();
                    waitForEnter();
                    break;
                case "2":
                    System.out.print("\n  Enter destination: ");
                    String dest = globalScanner.nextLine().trim();
                    exploreService.searchByDestination(dest);
                    waitForEnter();
                    break;
                case "3":
                    System.out.print("\n  Enter Flight Instance ID: ");
                    String fid = globalScanner.nextLine().trim();
                    exploreService.searchByFlightId(fid);
                    waitForEnter();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    // ============================================================
    // PASSENGER AUTHENTICATION
    // ============================================================
    private static void passengerSignUp() {
        clearScreen();
        printHeader("PASSENGER SIGN UP");

        System.out.print("  Enter Full Name: ");
        String fullName = globalScanner.nextLine().trim();

        System.out.print("  Enter Username (min 4 chars, letters/numbers/_.): ");
        String username = globalScanner.nextLine().trim();

        System.out.print("  Enter Email (gmail.com, yahoo.com, outlook.com only): ");
        String email = globalScanner.nextLine().trim();

        System.out.print("  Enter Password (min 8 chars, upper/lower/number/special): ");
        String password = globalScanner.nextLine().trim();

        if (authManager.passengerSignup(fullName, username, email, password)) {
            System.out.println(GREEN + "\n  ✅ Sign up successful! You can now log in." + RESET);
        }
        waitForEnter();
    }

    private static AppUser passengerSignIn() {
        clearScreen();
        printHeader("PASSENGER SIGN IN");

        System.out.print("  Enter Username: ");
        String username = globalScanner.nextLine().trim();

        System.out.print("  Enter Password: ");
        String password = globalScanner.nextLine().trim();

        AppUser user = authManager.authenticate(username, password, "PASSENGER");
        if (user != null) {
            System.out.println(GREEN + "\n  ✅ Welcome back, " + user.getFullName() + "!" + RESET);
            waitForEnter();
            return user;
        } else {
            System.out.println(RED + "\n  ❌ Invalid username or password." + RESET);
            waitForEnter();
            return null;
        }
    }

    // ============================================================
    // PASSENGER PORTAL
    // ============================================================
    private static void passengerPortal(AppUser passenger) {
        boolean logout = false;
        while (!logout) {
            clearScreen();
            printHeader("PASSENGER PORTAL");
            System.out.println("  " + GREEN + "Welcome, " + passenger.getFullName() + "!" + RESET);
            System.out.println();
            System.out.println("  " + CYAN + "1" + RESET + ". Explore Flights");
            System.out.println("  " + CYAN + "2" + RESET + ". Book a Flight");
            System.out.println("  " + CYAN + "3" + RESET + ". Check-in");
            System.out.println("  " + CYAN + "4" + RESET + ". Get Boarding Pass");
            System.out.println("  " + CYAN + "5" + RESET + ". Cancel Booking");
            System.out.println("  " + YELLOW + "0" + RESET + ". Logout");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    exploreService.displayAllFlightsForPassengers();
                    waitForEnter();
                    break;
                case "2":
                    bookingManager.startBooking(passenger);
                    waitForEnter();
                    break;
                case "3":
                    checkInPassenger();
                    break;
                case "4":
                    processBoardingPass();
                    break;
                case "5":
                    cancelBooking();
                    break;
                case "0":
                    logout = true;
                    System.out.println(YELLOW + "\n  Logging out..." + RESET);
                    waitForEnter();
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    // ============================================================
    // PASSENGER SELF-SERVICE METHODS
    // ============================================================
    private static void checkInPassenger() {
        clearScreen();
        printHeader("CHECK-IN");
        System.out.print("  Enter Ticket ID: ");
        String ticketId = globalScanner.nextLine().trim();

        System.out.print("  Enter current simulation time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime simTime = getUserSimulationTime();
        if (simTime != null) {
            passengerManager.checkIn(ticketId, simTime);
        }
        waitForEnter();
    }

    private static void processBoardingPass() {
        clearScreen();
        printHeader("BOARDING PASS");
        System.out.print("  Enter Ticket ID: ");
        String ticketId = globalScanner.nextLine().trim();

        System.out.print("  Enter current simulation time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime simTime = getUserSimulationTime();
        if (simTime != null) {
            passengerManager.processBoarding(ticketId, simTime);
        }
        waitForEnter();
    }

    private static void cancelBooking() {
        clearScreen();
        printHeader("CANCEL BOOKING");
        System.out.print("  Enter current simulation time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime simTime = getUserSimulationTime();
        if (simTime != null) {
            bookingManager.cancelBooking(simTime);
        }
        waitForEnter();
    }

    // ============================================================
    // ADMIN LOGIN
    // ============================================================
    private static AppUser adminLogin() {
        clearScreen();
        printHeader("ADMIN LOGIN");

        System.out.print("  Enter Username: ");
        String username = globalScanner.nextLine().trim();

        System.out.print("  Enter Password: ");
        String password = globalScanner.nextLine().trim();

        AppUser admin = authManager.authenticate(username, password, "ADMIN");
        if (admin != null) {
            System.out.println(GREEN + "\n  ✅ Welcome Admin, " + admin.getFullName() + "!" + RESET);
            waitForEnter();
            return admin;
        } else {
            System.out.println(RED + "\n  ❌ Invalid admin credentials." + RESET);
            waitForEnter();
            return null;
        }
    }

    // ============================================================
    // ADMIN MENU (Full Management)
    // ============================================================
    private static void adminMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("ADMIN CONTROL PANEL");
            System.out.println("  " + CYAN + "1" + RESET + ". Flight Management");
            System.out.println("  " + CYAN + "2" + RESET + ". Gate Management");
            System.out.println("  " + CYAN + "3" + RESET + ". Runway Management");
            System.out.println("  " + CYAN + "4" + RESET + ". Passenger Management");
            System.out.println("  " + CYAN + "5" + RESET + ". Weather Management");
            System.out.println("  " + CYAN + "6" + RESET + ". Booking Management");
            System.out.println("  " + CYAN + "7" + RESET + ". Run Simulation");
            System.out.println("  " + YELLOW + "0" + RESET + ". Logout");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    flightManagementMenu();
                    break;
                case "2":
                    gateManagementMenu();
                    break;
                case "3":
                    runwayManagementMenu();
                    break;
                case "4":
                    passengerManagementMenu();
                    break;
                case "5":
                    weatherManager.showWeatherMenu();
                    waitForEnter();
                    break;
                case "6":
                    bookingManager.startBooking();
                    waitForEnter();
                    break;
                case "7":
                    runSimulation();
                    break;
                case "0":
                    back = true;
                    System.out.println(YELLOW + "\n  Logging out from admin panel..." + RESET);
                    waitForEnter();
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    // ============================================================
    // ADMIN SUB-MENUS
    // ============================================================
    private static void flightManagementMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("FLIGHT MANAGEMENT");
            System.out.println("  " + CYAN + "1" + RESET + ". Display all flights");
            System.out.println("  " + CYAN + "2" + RESET + ". Display flight by Status");
            System.out.println("  " + CYAN + "3" + RESET + ". Display flight by Flight Instance ID");
            System.out.println("  " + CYAN + "4" + RESET + ". Departure Flights Menu");
            System.out.println("  " + CYAN + "5" + RESET + ". Arrival Flights Menu");
            System.out.println("  " + CYAN + "6" + RESET + ". Process Waiting Flights");
            System.out.println("  " + YELLOW + "0" + RESET + ". Back to Admin Panel");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    flightManager.displayFlights();
                    waitForEnter();
                    break;
                case "2":
                    System.out.print("  Enter status: ");
                    String status = globalScanner.nextLine();
                    flightManager.displayFlightsByStatus(status);
                    waitForEnter();
                    break;
                case "3":
                    System.out.print("  Enter Flight Instance ID: ");
                    String fid = globalScanner.nextLine();
                    flightManager.displayFlightByID(fid);
                    waitForEnter();
                    break;
                case "4":
                    departureMenu();
                    break;
                case "5":
                    arrivalMenu();
                    break;
                case "6":
                    System.out.print("  Enter simulation time (yyyy-MM-ddTHH:mm): ");
                    LocalDateTime waitTime = getUserSimulationTime();
                    if (waitTime != null) {
                        flightManager.processGoodWeatherWaitingFlights(waitTime);
                    }
                    waitForEnter();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    private static void departureMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("DEPARTURE FLIGHTS");
            System.out.println("  " + CYAN + "1" + RESET + ". Prepare Boarding");
            System.out.println("  " + CYAN + "2" + RESET + ". Complete Departure");
            System.out.println("  " + CYAN + "3" + RESET + ". Show Departed List");
            System.out.println("  " + CYAN + "4" + RESET + ". Clear Departed Flights File");
            System.out.println("  " + YELLOW + "0" + RESET + ". Back");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();
            LocalDateTime currentTime;

            switch (input) {
                case "1":
                    System.out.print("  Enter Flight Instance ID: ");
                    String fid = globalScanner.nextLine();
                    System.out.print("  Enter current simulation time: ");
                    currentTime = getUserSimulationTime();
                    if (currentTime != null) departureManager.prepareBoarding(fid, currentTime);
                    waitForEnter();
                    break;
                case "2":
                    System.out.print("  Enter current simulation time: ");
                    currentTime = getUserSimulationTime();
                    if (currentTime != null) departureManager.checkAndDepartFlights(currentTime);
                    waitForEnter();
                    break;
                case "3":
                    departureManager.displayDepartedFlights();
                    waitForEnter();
                    break;
                case "4":
                    departureManager.clearDepartedFlightsFile();
                    waitForEnter();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    private static void arrivalMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("ARRIVAL FLIGHTS");
            System.out.println("  " + CYAN + "1" + RESET + ". Process Arrival");
            System.out.println("  " + CYAN + "2" + RESET + ". Complete Arrival");
            System.out.println("  " + CYAN + "3" + RESET + ". Show Arrived Flights");
            System.out.println("  " + CYAN + "4" + RESET + ". Clear Arrived Flights File");
            System.out.println("  " + YELLOW + "0" + RESET + ". Back");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();
            LocalDateTime currentTime;

            switch (input) {
                case "1":
                    System.out.print("  Enter Flight Instance ID: ");
                    String fid = globalScanner.nextLine();
                    System.out.print("  Enter current simulation time: ");
                    currentTime = getUserSimulationTime();
                    if (currentTime != null) arrivalManager.processArrival(fid, currentTime);
                    waitForEnter();
                    break;
                case "2":
                    System.out.print("  Enter current simulation time: ");
                    currentTime = getUserSimulationTime();
                    if (currentTime != null) arrivalManager.checkAndCompleteArrivals(currentTime);
                    waitForEnter();
                    break;
                case "3":
                    arrivalManager.displayArrivedFlights();
                    waitForEnter();
                    break;
                case "4":
                    arrivalManager.clearArrivedFlightsFile();
                    waitForEnter();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    private static void gateManagementMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("GATE MANAGEMENT");
            System.out.println("  " + CYAN + "1" + RESET + ". Display all gates");
            System.out.println("  " + CYAN + "2" + RESET + ". Assign gate manually");
            System.out.println("  " + CYAN + "3" + RESET + ". Free a gate manually");
            System.out.println("  " + YELLOW + "0" + RESET + ". Back");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    gateManager.displayGates();
                    waitForEnter();
                    break;
                case "2":
                    System.out.print("  Enter Flight Instance ID: ");
                    String fid = globalScanner.nextLine();
                    String assigned = gateManager.assignGate(fid);
                    if (assigned != null) {
                        System.out.println(GREEN + "  Gate assigned: " + assigned + RESET);
                    } else {
                        System.out.println(RED + "  No free gate available" + RESET);
                    }
                    waitForEnter();
                    break;
                case "3":
                    System.out.print("  Enter Flight ID to free gate: ");
                    boolean freed = gateManager.freeGate(globalScanner.nextLine());
                    System.out.println(freed ? GREEN + "  Gate freed." + RESET : RED + "  No gate matched." + RESET);
                    waitForEnter();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    private static void runwayManagementMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("RUNWAY MANAGEMENT");
            System.out.println("  " + CYAN + "1" + RESET + ". Display all runways");
            System.out.println("  " + CYAN + "2" + RESET + ". Assign runway manually");
            System.out.println("  " + CYAN + "3" + RESET + ". Free a runway manually");
            System.out.println("  " + YELLOW + "0" + RESET + ". Back");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    runwayManager.displayRunways();
                    waitForEnter();
                    break;
                case "2":
                    System.out.print("  Enter Flight Instance ID: ");
                    String fid = globalScanner.nextLine();
                    String assigned = runwayManager.assignRunway(fid);
                    if (assigned != null) {
                        System.out.println(GREEN + "  Runway assigned: " + assigned + RESET);
                    } else {
                        System.out.println(RED + "  No free runway available" + RESET);
                    }
                    waitForEnter();
                    break;
                case "3":
                    System.out.print("  Enter Flight ID to free runway: ");
                    boolean freed = runwayManager.freeRunway(globalScanner.nextLine());
                    System.out.println(freed ? GREEN + "  Runway freed." + RESET : RED + "  No runway matched." + RESET);
                    waitForEnter();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    private static void passengerManagementMenu() {
        boolean back = false;
        while (!back) {
            clearScreen();
            printHeader("PASSENGER MANAGEMENT");
            System.out.println("  " + CYAN + "1" + RESET + ". Display all passengers");
            System.out.println("  " + CYAN + "2" + RESET + ". Display cancelled bookings");
            System.out.println("  " + YELLOW + "0" + RESET + ". Back");
            printSeparator();
            System.out.print("  Enter choice: ");

            String input = globalScanner.nextLine().trim();

            switch (input) {
                case "1":
                    passengerManager.displayPassengers();
                    waitForEnter();
                    break;
                case "2":
                    passengerManager.displayCancelledBookings();
                    waitForEnter();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "  Invalid choice." + RESET);
                    waitForEnter();
            }
        }
    }

    // ============================================================
    // SIMULATION
    // ============================================================
    private static void runSimulation() {
        clearScreen();
        printHeader("SIMULATION SETUP");

        System.out.print("  Enter start time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime start = getUserSimulationTime();
        if (start == null) return;

        System.out.print("  Enter end time (yyyy-MM-ddTHH:mm): ");
        LocalDateTime end = getUserSimulationTime();
        if (end == null) return;

        System.out.print("  Enter time step in minutes (1 recommended): ");
        int step;
        try {
            step = Integer.parseInt(globalScanner.nextLine());
        } catch (NumberFormatException e) {
            step = 1;
        }

        SimulationEngine engine = new SimulationEngine(flightManager, departureManager, arrivalManager, weatherManager);
        engine.run(start, end, step);

        System.out.print("\n  Press Enter to continue...");
        globalScanner.nextLine();
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================
    private static void clearScreen() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (Exception e) {
            for (int i = 0; i < 30; i++) System.out.println();
        }
    }

    private static void printLogo() {
        System.out.println(CYAN + "  █████╗ ██╗██████╗ ██████╗  ██████╗ ██████╗ ████████╗");
        System.out.println("  ██╔══██╗██║██╔══██╗██╔══██╗██╔═══██╗██╔══██╗╚══██╔══╝");
        System.out.println("  ███████║██║██████╔╝██████╔╝██║   ██║██████╔╝   ██║   ");
        System.out.println("  ██╔══██║██║██╔══██╗██╔══██╗██║   ██║██╔══██╗   ██║   ");
        System.out.println("  ██║  ██║██║██║  ██║██║  ██║╚██████╔╝██║  ██║   ██║   ");
        System.out.println("  ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝   " + RESET);
    }

    private static void printHeader(String title) {
        int width = 50;
        int padding = (width - title.length()) / 2;
        System.out.println("\n" + CYAN + "╔" + "═".repeat(width) + "╗" + RESET);
        System.out.println(CYAN + "║" + RESET + " ".repeat(padding) + BOLD + title + RESET + " ".repeat(width - title.length() - padding) + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚" + "═".repeat(width) + "╝" + RESET);
    }

    private static void printSeparator() {
        System.out.println(CYAN + "  " + "─".repeat(40) + RESET);
    }

    private static void waitForEnter() {
        System.out.print("\n  " + YELLOW + "Press Enter to continue..." + RESET);
        globalScanner.nextLine();
    }

    private static LocalDateTime getUserSimulationTime() {
        String input = globalScanner.nextLine();
        try {
            return LocalDateTime.parse(input, formatter);
        } catch (DateTimeParseException e) {
            System.out.println(RED + "  Invalid format. Example: 2026-01-26T14:30" + RESET);
            return null;
        }
    }
}