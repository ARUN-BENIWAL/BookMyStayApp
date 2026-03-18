import java.util.*;

public class BookMyStayApp {

    static class InvalidBookingException extends Exception {
        InvalidBookingException(String message) {
            super(message);
        }
    }

    static class RoomInventory {
        private Map<Integer, Boolean> rooms;

        RoomInventory() {
            rooms = new HashMap<>();
            rooms.put(101, true);
            rooms.put(102, true);
            rooms.put(103, false);
        }

        boolean isAvailable(int roomNumber) {
            return rooms.getOrDefault(roomNumber, false);
        }
    }

    static class ReservationValidator {

        void validate(String guestName, int roomNumber, RoomInventory inventory)
                throws InvalidBookingException {

            if (guestName == null || guestName.isEmpty()) {
                throw new InvalidBookingException("Guest name cannot be empty");
            }

            if (!inventory.isAvailable(roomNumber)) {
                throw new InvalidBookingException("Room not available");
            }
        }
    }

    static class Booking {
        String guestName;
        int roomNumber;

        Booking(String guestName, int roomNumber) {
            this.guestName = guestName;
            this.roomNumber = roomNumber;
        }
    }

    static class BookingHistory {
        List<Booking> bookings = new ArrayList<>();

        void addBooking(Booking booking) {
            bookings.add(booking);
        }

        List<Booking> getBookings() {
            return bookings;
        }
    }

    static class BookingService {
        private BookingHistory history;
        private ReservationValidator validator;
        private RoomInventory inventory;

        BookingService(BookingHistory history, ReservationValidator validator, RoomInventory inventory) {
            this.history = history;
            this.validator = validator;
            this.inventory = inventory;
        }

        void createBooking(String name, int roomNumber) throws InvalidBookingException {
            validator.validate(name, roomNumber, inventory);
            Booking booking = new Booking(name, roomNumber);
            history.addBooking(booking);
        }
    }

    static class BookingReport {
        void generate(List<Booking> bookings) {
            System.out.println("Booking Validation");
            for (Booking b : bookings) {
                System.out.println("Guest: " + b.guestName + ", Room: " + b.roomNumber);
            }
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Booking Validation");

        RoomInventory inventory = new RoomInventory();
        ReservationValidator validator = new ReservationValidator();
        BookingHistory history = new BookingHistory();
        BookingService service = new BookingService(history, validator, inventory);
        BookingReport report = new BookingReport();

        try {
            System.out.print("Enter guest name: ");
            String name = scanner.nextLine();

            System.out.print("Enter room number: ");
            int room = scanner.nextInt();

            service.createBooking(name, room);

            System.out.println("Booking successful!");

            report.generate(history.getBookings());

        } catch (InvalidBookingException e) {
            System.out.println("Booking failed: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}