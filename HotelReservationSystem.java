import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

// ------------------- ENUM & MODEL CLASSES -------------------

enum RoomType {
    STANDARD,
    DELUXE,
    SUITE
}

class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private RoomType type;
    private double pricePerNight;
    private int capacity;

    public Room(int id, RoomType type, double pricePerNight, int capacity) {
        this.id = id;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }

    public RoomType getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", type=" + type +
                ", pricePerNight=" + pricePerNight +
                ", capacity=" + capacity +
                '}';
    }
}

class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reservationId;
    private String guestName;
    private String guestPhone;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut; // exclusive
    private double totalPrice;
    private String paymentMethod;
    private boolean cancelled;

    public Reservation(String reservationId, String guestName, String guestPhone,
                       Room room, LocalDate checkIn, LocalDate checkOut,
                       double totalPrice, String paymentMethod) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.cancelled = false;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId='" + reservationId + '\'' +
                ", guestName='" + guestName + '\'' +
                ", guestPhone='" + guestPhone + '\'' +
                ", roomId=" + room.getId() +
                ", roomType=" + room.getType() +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", totalPrice=" + totalPrice +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", cancelled=" + cancelled +
                '}';
    }
}

class HotelData implements Serializable {
    private static final long serialVersionUID = 1L;
    List<Room> rooms;
    List<Reservation> reservations;

    public HotelData(List<Room> rooms, List<Reservation> reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }
}

// ------------------- MAIN SYSTEM CLASS -------------------

public class HotelReservationSystem {

    private static final String DATA_FILE = "hotel-data.dat";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static List<Room> rooms = new ArrayList<Room>();
    private static List<Reservation> reservations = new ArrayList<Reservation>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadData();
        if (rooms.isEmpty()) {
            seedDefaultRooms();
        }

        int choice;
        do {
            printMenu();
            choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    searchAvailableRooms();
                    break;
                case 2:
                    makeReservation();
                    break;
                case 3:
                    cancelReservation();
                    break;
                case 4:
                    viewReservationDetails();
                    break;
                case 5:
                    listAllReservations();
                    break;
                case 6:
                    listAllRooms();
                    break;
                case 7:
                    saveData();
                    System.out.println("Data saved. Exiting... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println();
        } while (choice != 7);
    }

    // ------------------- MENU -------------------

    private static void printMenu() {
        System.out.println("========== HOTEL RESERVATION SYSTEM ==========");
        System.out.println("1. Search Available Rooms");
        System.out.println("2. Make a Reservation");
        System.out.println("3. Cancel a Reservation");
        System.out.println("4. View Reservation Details");
        System.out.println("5. List All Reservations");
        System.out.println("6. List All Rooms");
        System.out.println("7. Save & Exit");
        System.out.println("==============================================");
    }

    // ------------------- CORE FEATURES -------------------

    private static void searchAvailableRooms() {
        System.out.println("---- Search Available Rooms ----");
        RoomType type = readRoomType();
        LocalDate checkIn = readDate("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkOut = readDate("Enter check-out date (yyyy-MM-dd): ");

        if (!checkDatesValid(checkIn, checkOut)) {
            System.out.println("Invalid dates. Check-out must be after check-in.");
            return;
        }

        List<Room> available = getAvailableRooms(type, checkIn, checkOut);
        if (available.isEmpty()) {
            System.out.println("No available rooms found for given type and dates.");
        } else {
            System.out.println("Available rooms:");
            for (Room r : available) {
                System.out.println("ID: " + r.getId() + " | Type: " + r.getType() +
                        " | Price/Night: " + r.getPricePerNight() +
                        " | Capacity: " + r.getCapacity());
            }
        }
    }

    private static void makeReservation() {
        System.out.println("---- Make a Reservation ----");
        System.out.print("Enter guest name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Enter guest phone: ");
        String phone = scanner.nextLine().trim();

        RoomType type = readRoomType();
        LocalDate checkIn = readDate("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkOut = readDate("Enter check-out date (yyyy-MM-dd): ");

        if (!checkDatesValid(checkIn, checkOut)) {
            System.out.println("Invalid dates. Check-out must be after check-in.");
            return;
        }

        List<Room> available = getAvailableRooms(type, checkIn, checkOut);
        if (available.isEmpty()) {
            System.out.println("No available rooms for selected type and dates.");
            return;
        }

        System.out.println("Available rooms of type " + type + ":");
        for (Room r : available) {
            System.out.println("ID: " + r.getId() + " | Price/Night: " + r.getPricePerNight() +
                    " | Capacity: " + r.getCapacity());
        }

        int roomId = getIntInput("Enter room ID to book: ");
        Room selectedRoom = null;
        for (Room r : available) {
            if (r.getId() == roomId) {
                selectedRoom = r;
                break;
            }
        }

        if (selectedRoom == null) {
            System.out.println("Invalid room ID or room not available.");
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = nights * selectedRoom.getPricePerNight();

        System.out.println("---- Payment Simulation ----");
        System.out.println("Total amount: " + totalPrice);
        System.out.print("Enter payment method (Card/UPI/Cash): ");
        String paymentMethod = scanner.nextLine().trim();
        System.out.print("Enter dummy transaction reference (anything): ");
        String txn = scanner.nextLine().trim();
        System.out.println("Processing payment with reference: " + txn + " ...");
        System.out.println("Payment successful (simulated).");

        String reservationId = generateReservationId();
        Reservation reservation = new Reservation(
                reservationId,
                name,
                phone,
                selectedRoom,
                checkIn,
                checkOut,
                totalPrice,
                paymentMethod
        );

        reservations.add(reservation);
        saveData();

        System.out.println("Reservation successful!");
        System.out.println("Your Reservation ID: " + reservationId);
    }

    private static void cancelReservation() {
        System.out.println("---- Cancel Reservation ----");
        System.out.print("Enter Reservation ID: ");
        String id = scanner.nextLine().trim();

        Reservation res = findReservationById(id);
        if (res == null) {
            System.out.println("Reservation not found.");
            return;
        }

        if (res.isCancelled()) {
            System.out.println("Reservation is already cancelled.");
            return;
        }

        System.out.println("Found reservation: ");
        System.out.println(res);
        System.out.print("Are you sure you want to cancel? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y") || confirm.equals("yes")) {
            res.cancel();
            saveData();
            System.out.println("Reservation cancelled successfully.");
        } else {
            System.out.println("Cancellation aborted.");
        }
    }

    private static void viewReservationDetails() {
        System.out.println("---- View Reservation Details ----");
        System.out.print("Enter Reservation ID: ");
        String id = scanner.nextLine().trim();

        Reservation res = findReservationById(id);
        if (res == null) {
            System.out.println("Reservation not found.");
            return;
        }

        System.out.println("Reservation details:");
        System.out.println(res);
    }

    private static void listAllReservations() {
        System.out.println("---- All Reservations ----");
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        for (Reservation r : reservations) {
            System.out.println(r);
        }
    }

    private static void listAllRooms() {
        System.out.println("---- All Rooms ----");
        for (Room r : rooms) {
            System.out.println(r);
        }
    }

    // ------------------- AVAILABILITY LOGIC -------------------

    private static List<Room> getAvailableRooms(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        List<Room> result = new ArrayList<Room>();
        for (Room room : rooms) {
            if (room.getType() != type) continue;
            if (isRoomAvailable(room, checkIn, checkOut)) {
                result.add(room);
            }
        }
        return result;
    }

    private static boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation res : reservations) {
            if (res.isCancelled()) continue;
            if (res.getRoom().getId() != room.getId()) continue;

            // Overlap logic
            if (!(checkOut.compareTo(res.getCheckIn()) <= 0 ||
                  checkIn.compareTo(res.getCheckOut()) >= 0)) {
                return false;
            }
        }
        return true;
    }

    // ------------------- HELPERS -------------------

    private static RoomType readRoomType() {
        while (true) {
            System.out.println("Select room type:");
            System.out.println("1. STANDARD");
            System.out.println("2. DELUXE");
            System.out.println("3. SUITE");
            int opt = getIntInput("Enter choice: ");
            switch (opt) {
                case 1:
                    return RoomType.STANDARD;
                case 2:
                    return RoomType.DELUXE;
                case 3:
                    return RoomType.SUITE;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private static LocalDate readDate(String message) {
        while (true) {
            System.out.print(message);
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }

    private static boolean checkDatesValid(LocalDate checkIn, LocalDate checkOut) {
        return checkOut.isAfter(checkIn);
    }

    private static String generateReservationId() {
        return "R" + System.currentTimeMillis();
    }

    private static Reservation findReservationById(String id) {
        for (Reservation r : reservations) {
            if (r.getReservationId().equalsIgnoreCase(id)) {
                return r;
            }
        }
        return null;
    }

    private static int getIntInput(String message) {
        while (true) {
            System.out.print(message);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    // ------------------- DATA PERSISTENCE -------------------

    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            Object obj = ois.readObject();
            if (obj instanceof HotelData) {
                HotelData data = (HotelData) obj;
                if (data.rooms != null) {
                    rooms = data.rooms;
                }
                if (data.reservations != null) {
                    reservations = data.reservations;
                }
                System.out.println("Loaded " + rooms.size() + " rooms and "
                        + reservations.size() + " reservations from file.");
            }
        } catch (IOException e) {
            System.out.println("Could not load existing data (IO error). Starting with defaults.");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load existing data (class error). Starting with defaults.");
        } finally {
            if (ois != null) {
                try { ois.close(); } catch (IOException ignored) {}
            }
        }
    }

    private static void saveData() {
        HotelData data = new HotelData(rooms, reservations);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE));
            oos.writeObject(data);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        } finally {
            if (oos != null) {
                try { oos.close(); } catch (IOException ignored) {}
            }
        }
    }

    private static void seedDefaultRooms() {
        rooms.add(new Room(101, RoomType.STANDARD, 2000, 2));
        rooms.add(new Room(102, RoomType.STANDARD, 2200, 2));
        rooms.add(new Room(201, RoomType.DELUXE, 3500, 3));
        rooms.add(new Room(202, RoomType.DELUXE, 3800, 3));
        rooms.add(new Room(301, RoomType.SUITE, 6000, 4));
        rooms.add(new Room(302, RoomType.SUITE, 6500, 4));
        saveData();
    }
}
