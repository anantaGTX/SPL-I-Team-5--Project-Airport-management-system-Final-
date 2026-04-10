import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class WeatherManager {

    private ArrayList<WeatherSlot> weatherSlots = new ArrayList<>();
    private Random random = new Random();
    private Scanner sc = new Scanner(System.in);
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // Simulation mode flag
    private boolean simulationMode = false;

    public WeatherManager() {
        loadWeatherFromFile();
    }

    // ============================================================
    // SIMULATION MODE SETTER/GETTER
    // ============================================================
    public void setSimulationMode(boolean mode) {
        this.simulationMode = mode;
    }

    public boolean isSimulationMode() {
        return simulationMode;
    }

    // ============================================================
    // FILE OPERATIONS
    // ============================================================
    private void loadWeatherFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("weather.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                LocalDateTime start = LocalDateTime.parse(parts[0]);
                LocalDateTime end = LocalDateTime.parse(parts[1]);
                WeatherType type = WeatherType.valueOf(parts[2]);
                weatherSlots.add(new WeatherSlot(start, end, type));
            }
        } catch (IOException e) {
            System.out.println("Weather file not found, starting fresh.");
        }
    }

    // FIX: Overwrite the whole file instead of appending to prevent duplicates
    private void saveWeatherToFile(WeatherSlot slot) {
        // Add to in-memory list
        weatherSlots.add(slot);
        // Save all slots to file
        saveAllWeatherToFile();
    }

    private void saveAllWeatherToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("weather.txt"))) {
            for (WeatherSlot slot : weatherSlots) {
                bw.write(slot.getStartTime() + "," + slot.getEndTime() + "," + slot.getType());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving weather.");
        }
    }

    // ============================================================
    // WEATHER LOOKUP (Main Method)
    // ============================================================

    /**
     * Get the weather for a given time.
     * Weather blocks: HH:01 to (HH+1):00
     * Example: 07:01 to 08:00, 08:01 to 09:00, etc.
     * Times at HH:00 belong to the previous hour's block.
     */
    public WeatherType getWeather(LocalDateTime time) {
        // Find the 1-hour block containing this time
        LocalDateTime blockStart = getBlockStart(time);
        LocalDateTime blockEnd = blockStart.plusHours(1);

        // Check if we already have weather for this block
        for (WeatherSlot slot : weatherSlots) {
            if (slot.getStartTime().equals(blockStart)) {
                return slot.getType();
            }
        }

        // No weather for this block
        if (simulationMode) {
            // Auto-generate random weather without asking user
            return generateRandomWeather(blockStart);
        } else {
            // Manual mode: ask user
            System.out.println("\n⚠️ No weather data for " + blockStart + " to " + blockEnd);
            System.out.println("Weather is needed for flight at " + time);
            return askForWeather(blockStart, blockEnd);
        }
    }

    /**
     * Get the end time of current weather (for storm duration)
     */
    public LocalDateTime getWeatherEndTime(LocalDateTime time) {
        LocalDateTime blockStart = getBlockStart(time);

        for (WeatherSlot slot : weatherSlots) {
            if (slot.getStartTime().equals(blockStart)) {
                return slot.getEndTime();
            }
        }

        // No weather - return block end time
        return blockStart.plusHours(1);
    }

    /**
     * Get the start of the 1-hour block for a given time.
     * Blocks always start at HH:01.
     * FIX: For times at HH:00, the block is from (HH-1):01 to HH:00.
     */
    private LocalDateTime getBlockStart(LocalDateTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        // If minute is 0, this time belongs to the previous hour's block
        if (minute == 0) {
            hour = hour - 1;
            if (hour < 0) hour = 23; // wrap around midnight if needed
        }

        // Block starts at HH:01
        return time.withHour(hour).withMinute(1).withSecond(0).withNano(0);
    }

    // ============================================================
    // WEATHER GENERATION
    // ============================================================

    /**
     * Generate random weather for a 1-hour block (used in simulation mode)
     */
    private WeatherType generateRandomWeather(LocalDateTime blockStart) {
        int value = random.nextInt(100);
        WeatherType type;
        if (value < 60) {
            type = WeatherType.SUNNY;
        } else if (value < 90) {
            type = WeatherType.CLEAR;
        } else {
            type = WeatherType.STORM;
        }

        LocalDateTime blockEnd = blockStart.plusHours(1);
        WeatherSlot slot = new WeatherSlot(blockStart, blockEnd, type);
        // Use the overwrite save method
        saveWeatherToFile(slot);

        System.out.println("   [Auto] Generated " + type + " weather from " + blockStart + " to " + blockEnd);
        return type;
    }

    /**
     * Ask user to set weather for a 1-hour block (manual mode)
     */
    private WeatherType askForWeather(LocalDateTime blockStart, LocalDateTime blockEnd) {
        System.out.println("\n=== Set Weather for " + blockStart + " to " + blockEnd + " ===");
        System.out.println("1. Set manual weather");
        System.out.println("2. Generate random weather");
        System.out.print("Enter choice (1 or 2): ");

        int choice = sc.nextInt();
        sc.nextLine();

        WeatherType type;

        if (choice == 1) {
            System.out.print("Enter weather type (SUNNY, CLEAR, STORM): ");
            String typeStr = sc.nextLine().toUpperCase();
            try {
                type = WeatherType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type. Using SUNNY.");
                type = WeatherType.SUNNY;
            }
        } else {
            type = generateRandomType();
        }

        WeatherSlot slot = new WeatherSlot(blockStart, blockEnd, type);
        saveWeatherToFile(slot);

        System.out.println("\n✅ Weather set: " + type + " from " + blockStart + " to " + blockEnd);
        return type;
    }

    /**
     * Generate random weather type (for manual mode option 2)
     */
    private WeatherType generateRandomType() {
        int value = random.nextInt(100);
        if (value < 60) return WeatherType.SUNNY;
        if (value < 90) return WeatherType.CLEAR;
        return WeatherType.STORM;
    }

    // ============================================================
    // DISPLAY METHODS
    // ============================================================

    /**
     * View all weather slots
     */
    public void viewWeatherSchedule() {
        if (weatherSlots.isEmpty()) {
            System.out.println("No weather slots scheduled yet.");
            return;
        }
        System.out.println("\nWeather Schedule:");
        for (WeatherSlot slot : weatherSlots) {
            System.out.println("   " + slot.getStartTime() + " to " + slot.getEndTime() + " : " + slot.getType());
        }
    }

    /**
     * Admin menu
     */
    public void showWeatherMenu() {
        int choice;
        do {
            System.out.println("\n===== WEATHER MANAGEMENT =====");
            System.out.println("1. View Weather Schedule");
            System.out.println("2. Back");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> viewWeatherSchedule();
                case 2 -> System.out.println("Exiting Weather Menu...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 2);
    }
}
