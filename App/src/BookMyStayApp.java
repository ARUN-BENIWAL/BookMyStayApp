import java.util.*;

public class BookMyStayApp {

    static class Room {
        int roomNumber;
        String type;
        boolean isAvailable;

        Room(int roomNumber, String type, boolean isAvailable) {
            this.roomNumber = roomNumber;
            this.type = type;
            this.isAvailable = isAvailable;
        }
    }

    static class Booking {
        String guestName;
        int roomNumber;
        String roomType;

        Booking(String guestName, int roomNumber, String roomType) {
            this.guestName = guestName;
            this.roomNumber = roomNumber;
            this.roomType = roomType;
        }
    }

    static class BookingHistory {
        List<Booking> bookings;

        BookingHistory() {
            bookings = new ArrayList<>();
        }

        void addBooking(Booking booking) {
            bookings.add(booking);
        }

        List<Booking> getAllBookings() {
            return bookings;
        }
    }

    static class BookingRepository {
        private BookingHistory history;

        BookingRepository(BookingHistory history) {
            this.history = history;
        }

        void saveBooking(Booking booking) {
            history.addBooking(booking);
        }

        List<Booking> fetchBookings() {
            return history.getAllBookings();
        }
    }

    static class BookingService {
        private BookingRepository repository;

        BookingService(BookingRepository repository) {
            this.repository = repository;
        }

        void createBooking(String name, int roomNumber, String type) {
            Booking booking = new Booking(name, roomNumber, type);
            repository.saveBooking(booking);
        }

        List<Booking> getBookings() {
            return repository.fetchBookings();
        }
    }

    static class BookingReportService {
        void generateReport(List<Booking> bookings) {
            System.out.println("Booking History and Reporting");
            for (Booking b : bookings) {
                System.out.println(b.guestName + " booked room " + b.roomNumber + " - " + b.roomType);
            }
        }
    }

    public static void main(String[] args) {
        BookingHistory history = new BookingHistory();
        BookingRepository repository = new BookingRepository(history);
        BookingService service = new BookingService(repository);
        BookingReportService reportService = new BookingReportService();

        service.createBooking("Aditi", 101, "Suite");
        service.createBooking("Karan", 102, "Suite");

        List<Booking> bookings = service.getBookings();

        reportService.generateReport(bookings);
    }
}