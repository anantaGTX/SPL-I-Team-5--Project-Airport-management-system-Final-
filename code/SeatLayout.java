import java.util.ArrayList;
import java.util.List;

public class SeatLayout {
    private int totalSeats;
    private int rows;
    private int cols = 6; // A-F
    private List<String> occupiedSeats;

    public SeatLayout(Flight flight) {
        this.totalSeats = flight.getSeatCapacity();
        this.rows = (int) Math.ceil(totalSeats / (double) cols);
        this.occupiedSeats = new ArrayList<>(flight.getOccupiedSeats());
    }

    public int getRows() {
        return rows;
    }

    // Display seat layout with selected seats highlighted
    public void displayLayout(List<String> selectedSeats) {

        System.out.println("\nSeat Legend: [XX]=Occupied  [S]=Selected  (SeatNo)=Available\n");

        final int CELL_WIDTH = 7; // fixed width for every seat cell
        final int ROW_LABEL_WIDTH = 4;

        // Header row
        System.out.printf("%-" + ROW_LABEL_WIDTH + "s", "");
        for (char c = 'A'; c <= 'F'; c++) {
            System.out.printf("%-" + CELL_WIDTH + "s", c);
        }
        System.out.println();

        System.out.println("------------------------------------------------------------");

        for (int r = 1; r <= rows; r++) {

            // Row number
            System.out.printf("%-" + ROW_LABEL_WIDTH + "d", r);

            for (int c = 0; c < cols; c++) {

                String seat = "" + (char) ('A' + c) + r;
                int seatIndex = (r - 1) * cols + c + 1;

                if (seatIndex > totalSeats) {
                    System.out.printf("%-" + CELL_WIDTH + "s", "");
                    continue;
                }

                String display;

                if (occupiedSeats.contains(seat))
                    display = "[XX]";
                else if (selectedSeats.contains(seat))
                    display = "[S]";
                else
                    display = seat;

                System.out.printf("%-" + CELL_WIDTH + "s", display);
            }

            System.out.println();
        }

        System.out.println("\nPricing: Window (A/F) = 500 | Middle (B/C/D/E) = 300\n");
    }

    public boolean isAvailable(String seat) {
        return !occupiedSeats.contains(seat) && isValidSeat(seat);
    }

    public int getSeatPrice(String seat) {
        char c = seat.charAt(0);
        if (c == 'A' || c == 'F')
            return 500;
        else
            return 300;
    }

    public String getSeatType(String seat) {
        char c = seat.charAt(0);
        if (c == 'A' || c == 'F')
            return "Window";
        else
            return "Middle";
    }

    // New method: validate if seat exists within layout
    public boolean isValidSeat(String seat) {
        if (seat.length() < 2)
            return false;
        char col = seat.charAt(0);
        int row;
        try {
            row = Integer.parseInt(seat.substring(1));
        } catch (NumberFormatException e) {
            return false;
        }

        if (col < 'A' || col > 'F')
            return false;

        int colIndex = col - 'A';
        int seatIndex = (row - 1) * cols + colIndex + 1;
        return seatIndex <= totalSeats;
    }
}
