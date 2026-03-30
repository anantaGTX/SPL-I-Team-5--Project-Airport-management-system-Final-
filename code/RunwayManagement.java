import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RunwayManagement {

    private static final String RUNWAY_FILE = "runways.txt";
    private List<Runway> runwayList;

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public RunwayManagement() {
        runwayList = new ArrayList<>();
        loadRunwaysFromFile();
    }

    private void loadRunwaysFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(RUNWAY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String runwayId = data[0];
                boolean isFree = Boolean.parseBoolean(data[1]);
                String flightInstanceId = data[2];
                runwayList.add(new Runway(runwayId, isFree, flightInstanceId));
            }
        } catch (IOException e) {
            System.out.println(RED + "Error loading runway data." + RESET);
        }
    }

    private void saveRunwaysToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUNWAY_FILE))) {
            for (Runway runway : runwayList) {
                bw.write(runway.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(RED + "Error saving runway data." + RESET);
        }
    }

    public String assignRunway(String flightInstanceId) {
        for (Runway runway : runwayList) {
            if (runway.isFree()) {
                runway.assignFlight(flightInstanceId);
                saveRunwaysToFile();
                return runway.getRunwayId();
            }
        }
        return null;
    }

    public boolean freeRunway(String flightInstanceId) {
        for (Runway runway : runwayList) {
            if (!runway.isFree() && runway.getAssignedflightInstanceId().equals(flightInstanceId)) {
                runway.freeRunway();
                saveRunwaysToFile();
                return true;
            }
        }
        return false;
    }

    public void displayRunways() {
        if (runwayList.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️ No runways found." + RESET);
            return;
        }

        // Calculate column widths dynamically
        int col1 = 10; // Runway ID (fixed)
        int col2 = 10; // Status (fixed)
        int col3 = 35; // Assigned Flight (default, will adjust)

        // Find max length for assigned flight
        for (Runway runway : runwayList) {
            String assigned = runway.isFree() ? "—" : runway.getAssignedflightInstanceId();
            if (assigned.length() > col3) col3 = assigned.length();
        }
        col3 = Math.max(col3, 20); // Minimum 20

        int totalWidth = col1 + col2 + col3 + 11; // 11 for borders and spaces

        // Header
        System.out.println("\n" + CYAN + "╔" + "═".repeat(totalWidth - 2) + "╗" + RESET);
        System.out.println(CYAN + "║" + RESET + centerText("RUNWAY MANAGEMENT", totalWidth - 2) + CYAN + "║" + RESET);
        System.out.println(CYAN + "╠" + "═".repeat(col1 + 2) + "╦" + "═".repeat(col2 + 2) + "╦" + "═".repeat(col3 + 2) + "╣" + RESET);
        System.out.printf(CYAN + "║ " + RESET + BOLD + "%-" + col1 + "s " + CYAN + "║ " + RESET + BOLD + "%-" + col2 + "s " + CYAN + "║ " + RESET + BOLD + "%-" + col3 + "s " + CYAN + "║" + RESET + "\n",
                "Runway ID", "Status", "Assigned Flight");
        System.out.println(CYAN + "╠" + "═".repeat(col1 + 2) + "╬" + "═".repeat(col2 + 2) + "╬" + "═".repeat(col3 + 2) + "╣" + RESET);

        // Data rows
        for (Runway runway : runwayList) {
            String status = runway.isFree() ? (GREEN + "FREE" + RESET) : (RED + "OCCUPIED" + RESET);
            String assigned = runway.isFree() ? "—" : runway.getAssignedflightInstanceId();
            System.out.printf(CYAN + "║ " + RESET + "%-" + col1 + "s " + CYAN + "║ " + RESET + "%-" + col2 + "s " + CYAN + "║ " + RESET + "%-" + col3 + "s " + CYAN + "║" + RESET + "\n",
                    runway.getRunwayId(), status, assigned);
        }

        System.out.println(CYAN + "╚" + "═".repeat(col1 + 2) + "╩" + "═".repeat(col2 + 2) + "╩" + "═".repeat(col3 + 2) + "╝" + RESET);
        System.out.println(GREEN + "\n✅ Total runways: " + runwayList.size() + RESET + "\n");
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - text.length() - padding));
    }

    public boolean hasFreeRunway() {
        for (Runway runway : runwayList) {
            if (runway.isFree()) return true;
        }
        return false;
    }

    public int getTotalRunwayCount() { return runwayList.size(); }

    public int getFreeRunwayCount() {
        int count = 0;
        for (Runway runway : runwayList) {
            if (runway.isFree()) count++;
        }
        return count;
    }

    public List<Runway> getRunwayList() { return runwayList; }

    public void fixRunwayAssignments(List<Flight> flights) {
        System.out.println(CYAN + "Fixing runway assignments..." + RESET);
        int fixedCount = 0;

        for (Runway runway : runwayList) {
            runway.freeRunway();
        }

        for (Flight f : flights) {
            String status = f.getStatus().toUpperCase();
            String runwayId = f.getRunwayId();
            if (runwayId != null && !runwayId.equals("-") &&
                    (status.equals("BOARDING") || status.equals("ARRIVING"))) {
                for (Runway runway : runwayList) {
                    if (runway.getRunwayId().equals(runwayId) && runway.isFree()) {
                        runway.assignFlight(f.getFlightInstanceId());
                        fixedCount++;
                        break;
                    }
                }
            }
        }

        if (fixedCount > 0) {
            saveRunwaysToFile();
            System.out.println(GREEN + "Fixed " + fixedCount + " runway assignments." + RESET);
        } else {
            System.out.println(YELLOW + "No runway fixes needed." + RESET);
        }
    }
}