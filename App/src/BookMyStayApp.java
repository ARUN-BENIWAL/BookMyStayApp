import java.util.*;

public class BookMyStayApp {

    static class InvalidBookingException extends Exception {
        InvalidBookingException(String msg) {
            super(msg);
        }
    }

    static class RoomInventory {
        private Map<String, Integer> rooms = new HashMap<>();

        RoomInventory() {
            rooms.put("Single", 2);
            rooms.put("Double", 2);
            rooms.put("Suite", 1);
        }

        synchronized boolean isAvailable(String type) {
            return rooms.getOrDefault(type, 0) > 0;
        }

        synchronized void bookRoom(String type) {
            rooms.put(type, rooms.get(type) - 1);
        }

        synchronized void releaseRoom(String type) {
            rooms.put(type, rooms.get(type) + 1);
        }

        void showInventory() {
            System.out.println("\nRemaining Inventory:");
            for (String key : rooms.keySet()) {
                System.out.println(key + ": " + rooms.get(key));
            }
        }
    }

    static class ReservationValidator {
        void validate(String name, String type, RoomInventory inv)
                throws InvalidBookingException {

            if (name == null || name.isEmpty())
                throw new InvalidBookingException("Invalid name");

            if (!inv.isAvailable(type))
                throw new InvalidBookingException("Room not available");
        }
    }

    static class Booking {
        String guest;
        String type;

        Booking(String g, String t) {
            guest = g;
            type = t;
        }
    }

    static class BookingHistory {
        List<Booking> list = new ArrayList<>();

        synchronized void add(Booking b) {
            list.add(b);
        }
    }

    static class BookingService {
        private BookingHistory history;
        private RoomInventory inventory;
        private ReservationValidator validator;

        BookingService(BookingHistory h, RoomInventory i, ReservationValidator v) {
            history = h;
            inventory = i;
            validator = v;
        }

        void createBooking(String name, String type) {
            try {
                validator.validate(name, type, inventory);

                synchronized (inventory) {
                    if (!inventory.isAvailable(type)) {
                        throw new InvalidBookingException("Room not available");
                    }
                    inventory.bookRoom(type);
                }

                Booking b = new Booking(name, type);
                history.add(b);

                System.out.println("Booking confirmed for " + name + " | Room: " + type);

            } catch (InvalidBookingException e) {
                System.out.println("Booking failed for " + name + ": " + e.getMessage());
            }
        }
    }

    static class CancellationService {
        Stack<Booking> stack = new Stack<>();

        void cancel(Booking b, RoomInventory inv) {
            synchronized (inv) {
                inv.releaseRoom(b.type);
            }
            stack.push(b);
        }
    }

    static class ConcurrentBookingProcessor implements Runnable {

        private BookingService service;
        private String[] names = {"Amit", "Neha", "Raj", "Simran"};
        private String[] types = {"Single", "Double", "Suite"};

        ConcurrentBookingProcessor(BookingService service) {
            this.service = service;
        }

        @Override
        public void run() {
            Random rand = new Random();

            for (int i = 0; i < 5; i++) {
                String name = names[rand.nextInt(names.length)];
                String type = types[rand.nextInt(types.length)];

                service.createBooking(name, type);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted");
                }
            }
        }
    }

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        ReservationValidator validator = new ReservationValidator();
        BookingService service = new BookingService(history, inventory, validator);

        System.out.println("Concurrent Booking Simulation\n");

        Thread t1 = new Thread(new ConcurrentBookingProcessor(service));
        Thread t2 = new Thread(new ConcurrentBookingProcessor(service));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("Thread execution interrupted");
        }

        inventory.showInventory();
    }
}