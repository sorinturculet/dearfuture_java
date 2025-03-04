package cz.dearfuture.main;

import cz.dearfuture.models.Capsule;
import cz.dearfuture.repositories.CapsuleRepository;
import cz.dearfuture.services.CapsuleService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The main entry point for the Dear Future time capsule application.
 * Provides a command-line interface for creating, viewing, opening,
 * deleting, and restoring time capsules.
 */
public class DearFutureApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String FILE_PATH = "data/capsules.json"; // Define the storage file
    private static final CapsuleRepository repository = new CapsuleRepository(FILE_PATH);
    private static final CapsuleService service = new CapsuleService(repository);

    public static void main(String[] args) {
        while (true) {
            showMenu();
            int choice = getUserChoice(0, 10);
            switch (choice) {
                case 1 -> createCapsule();
                case 2 -> viewCapsules();
                case 3 -> openCapsule();
                case 4 -> deleteCapsule();
                case 5 -> restoreCapsule();
                case 6 -> permanentlyDeleteCapsule();
                case 7 -> exportCapsules();
                case 8 -> importCapsules();
                case 9 -> showStatistics();
                case 10 -> sortCapsules();
                case 0 -> exitApp();
            }

        }
    }

    /**
     * Displays the main menu with capsule colors.
     */
    private static void showMenu() {
        System.out.println("\n" + Capsule.BLUE + "=== Dear Future - Time Capsule App ===" + Capsule.RESET);
        System.out.println(Capsule.CYAN + "1. Create a Capsule" + Capsule.RESET);
        System.out.println(Capsule.GREEN + "2. View Capsules" + Capsule.RESET);
        System.out.println(Capsule.YELLOW + "3. Open a Capsule" + Capsule.RESET);
        System.out.println(Capsule.RED + "4. Delete a Capsule" + Capsule.RESET);
        System.out.println(Capsule.PURPLE + "5. Restore a Capsule" + Capsule.RESET);
        System.out.println(Capsule.RED + "6. Permanently Delete a Capsule" + Capsule.RESET);
        System.out.println(Capsule.CYAN + "7. Export Capsules" + Capsule.RESET);
        System.out.println(Capsule.YELLOW + "8. Import Capsules" + Capsule.RESET);
        System.out.println(Capsule.GREEN + "9. View Statistics" + Capsule.RESET);
        System.out.println(Capsule.CYAN + "10. Sort Capsules" + Capsule.RESET);
        System.out.println(Capsule.WHITE + "0. Exit" + Capsule.RESET);

        System.out.print("Choose an option: ");
    }



    /**
     * Ensures valid user input within a range.
     */
    private static int getUserChoice(int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= min && choice <= max) return choice;
                System.out.println(Capsule.RED + "Invalid choice! Please select between " + min + " and " + max + "." + Capsule.RESET);
            } catch (NumberFormatException e) {
                System.out.println(Capsule.RED + "Invalid input! Please enter a number." + Capsule.RESET);
            }
        }
    }

    /**
     * Creates a new time capsule with color selection.
     */
    private static void createCapsule() {
        System.out.println("\n" + Capsule.CYAN + "=== Create a New Capsule ===" + Capsule.RESET);

        System.out.print("Enter Capsule Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter Capsule Message: ");
        String message = scanner.nextLine().trim();

        System.out.print("Enter Capsule Category (Event, Reminder, Reflection): ");
        String category = scanner.nextLine().trim();

        System.out.print("Enter Unlock Date (YYYY-MM-DD HH:MM): ");
        LocalDateTime unlockDate = parseDateTime(scanner.nextLine().trim());

        if (unlockDate == null) {
            System.out.println(Capsule.RED + "Invalid date format! Capsule not created." + Capsule.RESET);
            return;
        }

        //  Select a capsule color
        String color = selectCapsuleColor();

        int capsuleId = service.generateUniqueCapsuleId();

        Capsule capsule = new Capsule(capsuleId, title, message, unlockDate, category, color);
        if (service.addCapsule(capsule)) {
            System.out.println(color + "Capsule created successfully!" + Capsule.RESET);
        } else {
            System.out.println(Capsule.RED + "Failed to create capsule. Ensure fields are not empty." + Capsule.RESET);
        }
    }

    /**
     * Allows the user to select a capsule color.
     *
     * @return The selected hex color code.
     */
    private static String selectCapsuleColor() {
        System.out.println("\n" + Capsule.BLUE + "Select a Capsule Color:" + Capsule.RESET);
        System.out.println("1. 🔴 Red    (#E74C3C)");
        System.out.println("2. 🔵 Blue   (#3498DB)");
        System.out.println("3. 🟢 Green  (#2ECC71)");
        System.out.println("4. 🟡 Yellow (#F1C40F)");
        System.out.println("5. 🟣 Purple (#9B59B6)");
        System.out.println("6. 🔶 Orange (#D35400)");
        System.out.println("7. ⚪ White  (#FFFFFF) (Default)");
        System.out.print("Choose a color (1-7): ");

        int choice = getUserChoice(1, 7);
        return switch (choice) {
            case 1 -> "#E74C3C"; // Red
            case 2 -> "#3498DB"; // Blue
            case 3 -> "#2ECC71"; // Green
            case 4 -> "#F1C40F"; // Yellow
            case 5 -> "#9B59B6"; // Purple
            case 6 -> "#D35400"; // Orange
            default -> "#FFFFFF"; // Default White
        };
    }
    /**
     * Displays capsules by type (Locked, Archived, or Trash).
     */
    private static void viewCapsules() {
        System.out.println("\n" + Capsule.CYAN + "=== View Capsules ===" + Capsule.RESET);
        System.out.println("1. 🔒 Locked Capsules");
        System.out.println("2. 📂 Archived Capsules");
        System.out.println("3. 🗑️ Trash Capsules");
        System.out.print("Choose an option: ");

        int choice = getUserChoice(1, 3);
        List<Capsule> capsules;

        switch (choice) {
            case 1 -> {
                System.out.println("\n" + Capsule.YELLOW + "🔒 Viewing Locked Capsules" + Capsule.RESET);
                capsules = service.getLockedCapsules();
            }
            case 2 -> {
                System.out.println("\n" + Capsule.GREEN + "📂 Viewing Archived Capsules" + Capsule.RESET);
                capsules = service.getArchivedCapsules();
            }
            case 3 -> {
                System.out.println("\n" + Capsule.RED + "🗑️ Viewing Trash Capsules" + Capsule.RESET);
                capsules = service.getDeletedCapsules();
            }
            default -> {
                System.out.println(Capsule.RED + "Invalid choice!" + Capsule.RESET);
                return;
            }
        }

        if (capsules.isEmpty()) {
            System.out.println(Capsule.YELLOW + "No capsules found in this category." + Capsule.RESET);
            return;
        }

        paginateCapsules(capsules);
    }
    /**
     * Paginates through a list of capsules.
     *
     * @param capsules The list of capsules to display.
     */
    private static void paginateCapsules(List<Capsule> capsules) {
        int page = 0;
        int pageSize = 3;

        while (true) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, capsules.size());

            System.out.println("\n" + Capsule.CYAN + "=== Page " + (page + 1) + " ===" + Capsule.RESET);
            for (int i = start; i < end; i++) {
                System.out.println(capsules.get(i));
            }

            System.out.println("\nN - Next Page | P - Previous Page | Q - Quit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("n") && end < capsules.size()) page++;
            else if (choice.equals("p") && page > 0) page--;
            else if (choice.equals("q")) break;
        }
    }


    /**
     * Opens a capsule by ID.
     */
    private static void openCapsule() {
        System.out.print("Enter Capsule ID to Open: ");
        int id = getUserChoice(1, Integer.MAX_VALUE);
        System.out.println(service.openCapsule(id));
    }

    /**
     * Moves a capsule to Trash (soft delete).
     */
    private static void deleteCapsule() {
        System.out.print("Enter Capsule ID to Move to Trash: ");
        int id = getUserChoice(1, Integer.MAX_VALUE);
        service.deleteCapsule(id);
        System.out.println(Capsule.YELLOW + "Capsule moved to Trash." + Capsule.RESET);
    }

    /**
     * Restores a capsule from Trash.
     */
    private static void restoreCapsule() {
        System.out.print("Enter Capsule ID to Restore: ");
        int id = getUserChoice(1, Integer.MAX_VALUE);
        service.restoreCapsule(id);
        System.out.println(Capsule.GREEN + "Capsule restored." + Capsule.RESET);
    }

    /**
     * Permanently deletes a capsule.
     */
    private static void permanentlyDeleteCapsule() {
        System.out.print("Enter Capsule ID to Permanently Delete: ");
        int id = getUserChoice(1, Integer.MAX_VALUE);
        service.permanentlyDeleteCapsule(id);
        System.out.println(Capsule.RED + "Capsule permanently deleted." + Capsule.RESET);
    }

    /**
     * Parses user input into {@link LocalDateTime}.
     */
    private static LocalDateTime parseDateTime(String input) {
        try {
            return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * Allows the user to export capsules to a file.
     */
    private static void exportCapsules() {
        System.out.println("\n" + Capsule.CYAN + "=== Export Capsules ===" + Capsule.RESET);
        System.out.print("Enter file path (e.g., data/export.json or data/export.csv): ");
        String filePath = scanner.nextLine().trim();
        System.out.print("Choose format (json/csv): ");
        String format = scanner.nextLine().trim().toLowerCase();

        if (!format.equals("json") && !format.equals("csv")) {
            System.out.println(Capsule.RED + "Invalid format! Please choose 'json' or 'csv'." + Capsule.RESET);
            return;
        }

        service.exportCapsulesToFile(filePath, format);
    }
    /**
     * Allows the user to import capsules from a file.
     */
    private static void importCapsules() {
        System.out.println("\n" + Capsule.CYAN + "=== Import Capsules ===" + Capsule.RESET);
        System.out.print("Enter file path to import from (e.g., data/export.json or data/export.csv): ");
        String filePath = scanner.nextLine().trim();

        System.out.print("Choose format (json/csv): ");
        String format = scanner.nextLine().trim().toLowerCase();

        if (!format.equals("json") && !format.equals("csv")) {
            System.out.println(Capsule.RED + "Invalid format! Please choose 'json' or 'csv'." + Capsule.RESET);
            return;
        }

        service.importCapsulesFromFile(filePath, format);
    }
    /**
     * Displays capsule statistics and insights.
     */
    private static void showStatistics() {
        System.out.println("\n" + Capsule.CYAN + "=== Capsule Statistics & Insights ===" + Capsule.RESET);
        Map<String, String> stats = service.calculateStatistics();

        for (Map.Entry<String, String> entry : stats.entrySet()) {
            System.out.println(Capsule.GREEN + entry.getKey() + ": " + Capsule.RESET + entry.getValue());
        }
    }

    /**
     * Allows the user to sort capsules.
     */
    private static void sortCapsules() {
        System.out.println("\n" + Capsule.CYAN + "=== Sort Capsules ===" + Capsule.RESET);
        System.out.println("1. Title A-Z");
        System.out.println("2. Title Z-A");
        System.out.println("3. Unlock Soonest");
        System.out.println("4. Unlock Latest");
        System.out.println("5. Created Newest");
        System.out.println("6. Created Oldest");
        System.out.println("7. Category A-Z");
        System.out.println("8. Category Z-A");
        System.out.print("Choose a sorting option: ");

        int choice = getUserChoice(1, 8);

        String sortOption = switch (choice) {
            case 1 -> "Title A-Z";
            case 2 -> "Title Z-A";
            case 3 -> "Unlock Soonest";
            case 4 -> "Unlock Latest";
            case 5 -> "Created Newest";
            case 6 -> "Created Oldest";
            case 7 -> "Category A-Z";
            case 8 -> "Category Z-A";
            default -> "Title A-Z";
        };

        List<Capsule> sortedCapsules = service.sortCapsules(sortOption);
        paginateCapsules(sortedCapsules);
    }

    /**
     * Exits the application.
     */
    private static void exitApp() {
        System.out.println(Capsule.GREEN + "Goodbye!" + Capsule.RESET);
        System.exit(0);
    }
}
