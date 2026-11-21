import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CarRentalSystem {
    private final List<Car> cars = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();
    private int nextCustomerId = 1;

    public CarRentalSystem() {
        cars.add(new Car("C001", "Toyota", "Camry", new BigDecimal("60.00")));
        cars.add(new Car("C002", "Honda", "Accord", new BigDecimal("55.00")));
        cars.add(new Car("C003", "Mahindra", "Thar", new BigDecimal("75.00")));
    }

    public static void main(String[] args) {
        CarRentalSystem system = new CarRentalSystem();
        system.run();
    }

    private void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Welcome to the Car Rental System ===");
            boolean running = true;

            while (running) {
                printMenu();
                int choice = readInt(scanner, "Choose an option: ");

                switch (choice) {
                    case 1:
                        displayAvailableCars();
                        break;
                    case 2:
                        rentCar(scanner);
                        break;
                    case 3:
                        returnCar(scanner);
                        break;
                    case 4:
                        running = false;
                        System.out.println("Thank you for using the Car Rental System.");
                        break;
                    default:
                        System.out.println("Please choose an option from 1 to 4.");
                }
            }
        }
    }

    private void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. View available cars");
        System.out.println("2. Rent a car");
        System.out.println("3. Return a car");
        System.out.println("4. Exit");
    }

    private void displayAvailableCars() {
        System.out.println("\n--- Available Cars ---");
        boolean foundAvailableCar = false;

        for (Car car : cars) {
            if (car.isAvailable()) {
                System.out.printf(
                        "%s | %s %s | $%s per day%n",
                        car.getCarId(),
                        car.getBrand(),
                        car.getModel(),
                        car.getBasePricePerDay().toPlainString());
                foundAvailableCar = true;
            }
        }

        if (!foundAvailableCar) {
            System.out.println("There are currently no cars available.");
        }
    }

    private void rentCar(Scanner scanner) {
        displayAvailableCars();

        System.out.print("\nEnter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty. Rental was not completed.");
            return;
        }

        System.out.print("Enter the ID of the car you want to rent: ");
        String carId = scanner.nextLine().trim();
        Car car = findCarById(carId);
        if (car == null) {
            System.out.println("No car was found with ID " + carId + ".");
            return;
        }
        if (!car.isAvailable()) {
            System.out.println("That car is already rented.");
            return;
        }

        int days = readInt(scanner, "Enter the number of rental days: ");
        if (days <= 0) {
            System.out.println("Rental days must be greater than zero. Rental was not completed.");
            return;
        }

        Customer customer = new Customer("U%03d".formatted(nextCustomerId++), name);
        Rental rental = new Rental(car, customer, days);

        if (!car.rent()) {
            System.out.println("That car is no longer available.");
            return;
        }
        customers.add(customer);
        rentals.add(rental);

        System.out.println("\n--- Rental Confirmed ---");
        System.out.println("Customer: " + customer.getName() + " (" + customer.getCustomerId() + ")");
        System.out.println("Car: " + car.getBrand() + " " + car.getModel() + " (" + car.getCarId() + ")");
        System.out.println("Days: " + rental.getDays());
        System.out.println("Total price: $" + rental.getTotalPrice().toPlainString());
    }

    private void returnCar(Scanner scanner) {
        System.out.print("\nEnter the ID of the car you want to return: ");
        String carId = scanner.nextLine().trim();

        Rental activeRental = findRentalByCarId(carId);
        if (activeRental == null) {
            System.out.println("No active rental was found for car ID " + carId + ".");
            return;
        }

        Car car = activeRental.getCar();
        car.returnCar();
        rentals.remove(activeRental);
        System.out.println("Car " + car.getCarId() + " has been returned successfully.");
    }

    private Car findCarById(String carId) {
        for (Car car : cars) {
            if (car.getCarId().equalsIgnoreCase(carId)) {
                return car;
            }
        }
        return null;
    }

    private Rental findRentalByCarId(String carId) {
        for (Rental rental : rentals) {
            if (rental.getCar().getCarId().equalsIgnoreCase(carId)) {
                return rental;
            }
        }
        return null;
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}

class Car {
    private final String carId;
    private final String brand;
    private final String model;
    private final BigDecimal basePricePerDay;
    private boolean isAvailable;

    public Car(String carId, String brand, String model, BigDecimal basePricePerDay) {
        if (carId == null || carId.isBlank()
                || brand == null || brand.isBlank()
                || model == null || model.isBlank()
                || basePricePerDay == null || basePricePerDay.signum() < 0) {
            throw new IllegalArgumentException("Car details and a non-negative daily price are required.");
        }

        this.carId = carId;
        this.brand = brand;
        this.model = model;
        this.basePricePerDay = basePricePerDay;
        this.isAvailable = true;
    }

    public String getCarId() {
        return carId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public BigDecimal getBasePricePerDay() {
        return basePricePerDay;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean rent() {
        if (!isAvailable) {
            return false;
        }
        isAvailable = false;
        return true;
    }

    public void returnCar() {
        isAvailable = true;
    }
}

class Customer {
    private final String customerId;
    private final String name;

    public Customer(String customerId, String name) {
        if (customerId == null || customerId.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer ID and name are required.");
        }
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }
}

class Rental {
    private final Car car;
    private final Customer customer;
    private final int days;

    public Rental(Car car, Customer customer, int days) {
        if (car == null || customer == null || days <= 0) {
            throw new IllegalArgumentException("A car, customer, and positive rental duration are required.");
        }
        this.car = car;
        this.customer = customer;
        this.days = days;
    }

    public Car getCar() {
        return car;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getDays() {
        return days;
    }

    public BigDecimal getTotalPrice() {
        return car.getBasePricePerDay().multiply(BigDecimal.valueOf(days));
    }
}
