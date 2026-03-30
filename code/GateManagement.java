import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GateManagement {

    private static final String GATE_FILE = "gates.txt";
    private List<Gate> gateList;

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public GateManagement() {
        gateList = new ArrayList<>();
        loadGatesFromFile();
    }

    private void loadGatesFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(GATE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String gateId = data[0];
                boolean isFree = Boolean.parseBoolean(data[1]);
                String flightId = data[2];
                gateList.add(new Gate(gateId, isFree, flightId));
            }
        } catch (IOException e) {
            System.out.println(RED + "Error loading gate data." + RESET);
        }
    }

    private void saveGatesToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(GATE_FILE))) {
            for (Gate gate : gateList) {
                bw.write(gate.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(RED + "Error saving gate data." + RESET);
        }
    }

    public String assignGate(String flightInstanceId) {
        for (Gate gate : gateList) {
            if (gate.isFree()) {
                gate.assignFlight(flightInstanceId);
                saveGatesToFile();
                return gate.getGateId();
            }
        }
        return null;
    }

    public boolean freeGate(String flightInstanceId) {
        for (Gate gate : gateList) {
            if (!gate.isFree() && gate.getAssignedFlightInstanceId().equals(flightInstanceId)) {
                gate.freeGate();
                saveGatesToFile();
                return true;
            }
        }
        return false;
    }

    public void displayGates() {
        if (gateList.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️ No gates found." + RESET);
            return;
        }

        // Calculate column widths dynamically
        int col1 = 8;  // Gate ID (fixed)
        int col2 = 10; // Status (fixed)
        int col3 = 35; // Assigned Flight (default, will adjust)

        // Find max length for assigned flight
        for (Gate gate : gateList) {
            String assigned = gate.isFree() ? "—" : gate.getAssignedFlightInstanceId();
            if (assigned.length() > col3) col3 = assigned.length();
        }
        col3 = Math.max(col3, 20); // Minimum 20

        int totalWidth = col1 + col2 + col3 + 11; // 11 for borders and spaces

        // Header
        System.out.println("\n" + CYAN + "╔" + "═".repeat(totalWidth - 2) + "╗" + RESET);
        System.out.println(CYAN + "║" + RESET + centerText("GATE MANAGEMENT", totalWidth - 2) + CYAN + "║" + RESET);
        System.out.println(CYAN + "╠" + "═".repeat(col1 + 2) + "╦" + "═".repeat(col2 + 2) + "╦" + "═".repeat(col3 + 2) + "╣" + RESET);
        System.out.printf(CYAN + "║ " + RESET + BOLD + "%-" + col1 + "s " + CYAN + "║ " + RESET + BOLD + "%-" + col2 + "s " + CYAN + "║ " + RESET + BOLD + "%-" + col3 + "s " + CYAN + "║" + RESET + "\n",
                "Gate ID", "Status", "Assigned Flight");
        System.out.println(CYAN + "╠" + "═".repeat(col1 + 2) + "╬" + "═".repeat(col2 + 2) + "╬" + "═".repeat(col3 + 2) + "╣" + RESET);

        // Data rows
        for (Gate gate : gateList) {
            String status = gate.isFree() ? (GREEN + "FREE" + RESET) : (RED + "OCCUPIED" + RESET);
            String assigned = gate.isFree() ? "—" : gate.getAssignedFlightInstanceId();
            System.out.printf(CYAN + "║ " + RESET + "%-" + col1 + "s " + CYAN + "║ " + RESET + "%-" + col2 + "s " + CYAN + "║ " + RESET + "%-" + col3 + "s " + CYAN + "║" + RESET + "\n",
                    gate.getGateId(), status, assigned);
        }

        System.out.println(CYAN + "╚" + "═".repeat(col1 + 2) + "╩" + "═".repeat(col2 + 2) + "╩" + "═".repeat(col3 + 2) + "╝" + RESET);
        System.out.println(GREEN + "\n✅ Total gates: " + gateList.size() + RESET + "\n");
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - text.length() - padding));
    }

    public boolean hasFreeGate() {
        for (Gate gate : gateList) {
            if (gate.isFree()) return true;
        }
        return false;
    }

    public int getTotalGateCount() { return gateList.size(); }

    public int getFreeGateCount() {
        int count = 0;
        for (Gate gate : gateList) {
            if (gate.isFree()) count++;
        }
        return count;
    }

    public List<Gate> getGateList() { return gateList; }

    public void fixGateAssignments(List<Flight> flights) {
        System.out.println(CYAN + "Fixing gate assignments..." + RESET);
        int fixedCount = 0;

        for (Gate gate : gateList) {
            gate.freeGate();
        }

        for (Flight f : flights) {
            String status = f.getStatus().toUpperCase();
            String gateId = f.getGateId();
            if (gateId != null && !gateId.equals("-") &&
                    (status.equals("BOARDING") || status.equals("ARRIVING"))) {
                for (Gate gate : gateList) {
                    if (gate.getGateId().equals(gateId) && gate.isFree()) {
                        gate.assignFlight(f.getFlightInstanceId());
                        fixedCount++;
                        break;
                    }
                }
            }
        }

        if (fixedCount > 0) {
            saveGatesToFile();
            System.out.println(GREEN + "Fixed " + fixedCount + " gate assignments." + RESET);
        } else {
            System.out.println(YELLOW + "No gate fixes needed." + RESET);
        }
    }
}