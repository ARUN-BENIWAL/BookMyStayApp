import java.io.*;
import java.util.*;

public class BookMyStayApp {

    static class InvalidBookingException extends Exception {
        InvalidBookingException(String msg) {
            super(msg);
        }
    }

    static class RoomInventory {
        private Map<String, Integer> rooms = new HashMap<>();
        private final String FILE = "inventory.txt";

        RoomInventory() {
            loadInventory();
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
            System.out.println("\nCurrent Inventory:");
            for (String k : rooms.keySet()) {
                System.out.println(k + ": " + rooms.get(k));
            }
        }

        void loadInventory() {
            try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    rooms.put(parts[0], Integer.parseInt(parts[1]));
                }
                System.out.println("Inventory loaded from file.");
            } catch (Exception e) {
                System.out.println("No existing inventory data found. Starting fresh.");
                rooms.put("Single", 5);
                rooms.put("Double", 3);
                rooms.put("Suite", 2);
            }
        }

        void saveInventory() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE))) {
                for (String k : rooms.keySet()) {
                    bw.write(k + "," + rooms.get(k));
                    bw.newLine();
                }
                System.out.println("Inventory saved successfully.");
            } catch (IOException e) {
                System.out.println("Error saving inventory.");
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
        String guest, type;

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
                    if (!inventory.isAvailable(type))
                        throw new InvalidBookingException("Room not available");
                    inventory.bookRoom(type);
                }

                history.add(new Booking(name, type));
                System.out.println("Booking confirmed: " + name + " -> " + type);

            } catch (InvalidBookingException e) {
                System.out.println("Booking failed: " + e.getMessage());
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

        ConcurrentBookingProcessor(BookingService s) {
            service = s;
        }

        public void run() {
            Random r = new Random();
            for (int i = 0; i < 5; i++) {
                service.createBooking(
                        names[r.nextInt(names.length)],
                        types[r.nextInt(types.length)]
                );
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }
        }
    }

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        ReservationValidator validator = new ReservationValidator();
        BookingService service = new BookingService(history, inventory, validator);

        System.out.println("\n=== Concurrent Booking Simulation ===\n");

        Thread t1 = new Thread(new ConcurrentBookingProcessor(service));
        Thread t2 = new Thread(new ConcurrentBookingProcessor(service));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("Thread error");
        }

        inventory.showInventory();

        inventory.saveInventory();
    }
}