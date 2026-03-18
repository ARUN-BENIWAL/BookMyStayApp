import java.util.*;

public class BookMyStayApp {

    static class InvalidBookingException extends Exception {
        InvalidBookingException(String message) {
            super(message);
        }
    }

    static class RoomInventory {
        private Map<String, Integer> rooms;

        RoomInventory() {
            rooms = new HashMap<>();
            rooms.put("Single", 5);
            rooms.put("Double", 3);
            rooms.put("Suite", 2);
        }

        boolean isAvailable(String type) {
            return rooms.getOrDefault(type, 0) > 0;
        }

        void bookRoom(String type) {
            rooms.put(type, rooms.get(type) - 1);
        }

        void releaseRoom(String type) {
            rooms.put(type, rooms.get(type) + 1);
        }

        int getAvailability(String type) {
            return rooms.get(type);
        }
    }

    static class ReservationValidator {
        void validate(String guestName, String roomType, RoomInventory inventory)
                throws InvalidBookingException {

            if (guestName == null || guestName.isEmpty()) {
                throw new InvalidBookingException("Guest name cannot be empty");
            }

            if (!inventory.isAvailable(roomType)) {
                throw new InvalidBookingException("Room not available");
            }
        }
    }

    static class Booking {
        String guestName;
        String roomType;

        Booking(String guestName, String roomType) {
            this.guestName = guestName;
            this.roomType = roomType;
        }
    }

    static class BookingHistory {
        List<Booking> bookings = new ArrayList<>();

        void addBooking(Booking booking) {
            bookings.add(booking);
        }

        void removeBooking(Booking booking) {
            bookings.remove(booking);
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

        Booking createBooking(String name, String type) throws InvalidBookingException {
            validator.validate(name, type, inventory);
            Booking booking = new Booking(name, type);
            inventory.bookRoom(type);
            history.addBooking(booking);
            return booking;
        }

        void cancelBooking(Booking booking) {
            history.removeBooking(booking);
            inventory.releaseRoom(booking.roomType);
        }
    }

    static class CancellationService {
        Stack<Booking> cancelledBookings = new Stack<>();

        void cancel(Booking booking, BookingService service) {
            service.cancelBooking(booking);
            cancelledBookings.push(booking);
        }

        void showCancellationHistory() {
            System.out.println("Cancelled Booking History:");
            for (Booking b : cancelledBookings) {
                System.out.println(b.guestName + " - " + b.roomType);
            }
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        RoomInventory inventory = new RoomInventory();
        ReservationValidator validator = new ReservationValidator();
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(history, validator, inventory);
        CancellationService cancelService = new CancellationService();

        try {
            System.out.print("Enter guest name: ");
            String name = sc.nextLine();

            System.out.print("Enter room type (Single/Double/Suite): ");
            String type = sc.nextLine();

            Booking booking = bookingService.createBooking(name, type);

            System.out.println("Booking confirmed successfully for " + type);

            System.out.print("Do you want to cancel booking? (yes/no): ");
            String choice = sc.nextLine();

            if (choice.equalsIgnoreCase("yes")) {
                cancelService.cancel(booking, bookingService);

                System.out.println("Booking cancelled successfully. Inventory restored for room type: " + type);

                cancelService.showCancellationHistory();

                System.out.println("Updated " + type + " Room Availability: " + inventory.getAvailability(type));
            }

        } catch (InvalidBookingException e) {
            System.out.println("Booking failed: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}