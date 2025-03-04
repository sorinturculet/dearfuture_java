package cz.dearfuture.services;

import cz.dearfuture.models.Capsule;
import cz.dearfuture.repositories.CapsuleRepository;
import cz.dearfuture.utils.CapsuleFileUtils;
import cz.dearfuture.utils.MultiThreadedMergeSort;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles business logic for managing capsules, including retrieval, deletion,
 * restoration, and unlocking.
 */
public class CapsuleService {
    private final CapsuleRepository repository;

    /**
     * Constructs a new CapsuleService.
     *
     * @param repository The repository responsible for capsule data storage.
     */
    public CapsuleService(CapsuleRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds a new capsule if the title and message are valid.
     *
     * @param capsule The capsule to be added.
     * @return {@code true} if the capsule was successfully added, otherwise {@code false}.
     */
    public boolean addCapsule(Capsule capsule) {
        if (capsule.getTitle().isBlank() || capsule.getMessage().isBlank()) {
            return false; // Validation failed
        }
        repository.addCapsule(capsule);
        return true;
    }
    /**
     * Generates a unique ID for a new capsule.
     *
     * @return The next available ID.
     */
    public int generateUniqueCapsuleId() {
        List<Capsule> allCapsules = repository.getAllCapsules();
        if (allCapsules.isEmpty()) return 1;

        int maxId = allCapsules.stream()
                .mapToInt(Capsule::getId)
                .max()
                .orElse(0);

        return maxId + 2;
    }

    /**
     * Retrieves all locked (non-opened) capsules.
     *
     * @return A list of locked capsules.
     */
    public List<Capsule> getLockedCapsules() {
        return repository.getAllCapsules().stream()
                .filter(capsule -> !capsule.isOpened()) // Only locked capsules
                .toList();
    }

    /**
     * Retrieves all archived (opened) capsules.
     *
     * @return A list of archived capsules.
     */
    public List<Capsule> getArchivedCapsules() {
        return repository.getAllCapsules().stream()
                .filter(Capsule::isOpened) // Only opened capsules
                .toList();
    }

    /**
     * Retrieves all capsules that are currently in the Trash.
     *
     * @return A list of deleted (trashed) capsules.
     */
    public List<Capsule> getDeletedCapsules() {
        return repository.getDeletedCapsules();
    }

    /**
     * Attempts to open a capsule if it is unlocked.
     *
     * @param id The ID of the capsule to open.
     * @return The capsule's message if successfully opened, otherwise an error message.
     */
    public String openCapsule(int id) {
        Capsule capsule = repository.getCapsuleById(id);
        if (capsule == null) {
            return "Capsule not found.";
        }
        if (capsule.openCapsule()) {
            repository.addCapsule(capsule); // Update repository
            return capsule.getMessage(); // Return the message
        }
        return "This capsule is still locked!";
    }

    /**
     * Moves a capsule to the Trash (soft delete).
     *
     * @param id The ID of the capsule to delete.
     */
    public void deleteCapsule(int id) {
        repository.deleteCapsule(id);
    }

    /**
     * Restores a capsule from the Trash.
     *
     * @param id The ID of the capsule to restore.
     */
    public void restoreCapsule(int id) {
        repository.restoreCapsule(id);
    }

    /**
     * Permanently deletes a capsule, removing it from storage.
     *
     * @param id The ID of the capsule to permanently delete.
     */
    public void permanentlyDeleteCapsule(int id) {
        repository.permanentlyDeleteCapsule(id);
    }

    /**
     * Cleans up capsules that have been in Trash for more than 15 days.
     */
    public void cleanupOldDeletedCapsules() {
        repository.cleanupOldDeletedCapsules();
    }
    /**
     * Exports capsules to a file (JSON or CSV format).
     *
     * @param filePath The destination file path.
     * @param format   The format ("json" or "csv").
     */
    public void exportCapsulesToFile(String filePath, String format) {
        List<Capsule> capsules = repository.getAllCapsules();
        CapsuleFileUtils.exportCapsulesToFile(capsules, filePath, format);
    }

    /**
     * Imports capsules from a file (JSON or CSV format).
     *
     * @param filePath The source file path.
     * @param format   The format ("json" or "csv").
     */
    public void importCapsulesFromFile(String filePath, String format) {
        List<Capsule> importedCapsules = CapsuleFileUtils.importCapsulesFromFile(filePath, format);
        for (Capsule capsule : importedCapsules) {
            repository.addCapsule(capsule);
        }
    }
    /**
     * Calculates statistics for capsules.
     *
     * @return A map containing all statistics.
     */
    public Map<String, String> calculateStatistics() {
        Map<String, String> stats = new LinkedHashMap<>();
        List<Capsule> allCapsules = repository.getAllCapsules();

        // 1. Total Capsules Count 🔢
        int totalCapsules = allCapsules.size();
        int locked = (int) allCapsules.stream().filter(c -> !c.isOpened() && !c.isDeleted()).count();
        int opened = (int) allCapsules.stream().filter(Capsule::isOpened).count();
        int deleted = (int) allCapsules.stream().filter(Capsule::isDeleted).count();
        stats.put("Total Capsules", String.valueOf(totalCapsules));
        stats.put("Locked Capsules", String.valueOf(locked));
        stats.put("Opened Capsules", String.valueOf(opened));
        stats.put("Deleted Capsules", String.valueOf(deleted));

        // 2. Most Common Category
        Map<String, Long> categoryCount = allCapsules.stream()
                .filter(c -> !c.isDeleted())
                .collect(Collectors.groupingBy(Capsule::getCategory, Collectors.counting()));
        String mostCommonCategory = categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        stats.put("Most Common Category", mostCommonCategory);
        stats.put("Category Breakdown", categoryCount.toString());

        // 3. Most Used Capsule Color
        Map<String, Long> colorCount = allCapsules.stream()
                .filter(c -> !c.isDeleted())
                .collect(Collectors.groupingBy(Capsule::getColor, Collectors.counting()));
        String mostUsedColor = colorCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        stats.put("Most Used Color", mostUsedColor);
        stats.put("Color Breakdown", colorCount.toString());

        // 4. Unlock History & Trends
        LocalDateTime now = LocalDateTime.now();
        long last7Days = allCapsules.stream()
                .filter(c -> c.getUnlockDate().isAfter(now.minusDays(7)) && c.getUnlockDate().isBefore(now))
                .count();
        long last30Days = allCapsules.stream()
                .filter(c -> c.getUnlockDate().isAfter(now.minusDays(30)) && c.getUnlockDate().isBefore(now))
                .count();
        long lastYear = allCapsules.stream()
                .filter(c -> c.getUnlockDate().isAfter(now.minusYears(1)) && c.getUnlockDate().isBefore(now))
                .count();
        stats.put("Unlocked Last 7 Days", String.valueOf(last7Days));
        stats.put("Unlocked Last 30 Days", String.valueOf(last30Days));
        stats.put("Unlocked Last Year", String.valueOf(lastYear));

        // 5. Upcoming Unlocks
        long next7Days = allCapsules.stream()
                .filter(c -> c.getUnlockDate().isAfter(now) && c.getUnlockDate().isBefore(now.plusDays(7)))
                .count();
        long next30Days = allCapsules.stream()
                .filter(c -> c.getUnlockDate().isAfter(now) && c.getUnlockDate().isBefore(now.plusDays(30)))
                .count();
        long nextYear = allCapsules.stream()
                .filter(c -> c.getUnlockDate().isAfter(now) && c.getUnlockDate().isBefore(now.plusYears(1)))
                .count();
        stats.put("Upcoming in 7 Days", String.valueOf(next7Days));
        stats.put("Upcoming in 30 Days", String.valueOf(next30Days));
        stats.put("Upcoming in 1 Year", String.valueOf(nextYear));

        return stats;
    }

    /**
     * Sorts capsules based on the chosen sorting option.
     *
     * @param sortOption The sorting option to use.
     * @return A sorted list of capsules.
     */
    public List<Capsule> sortCapsules(String sortOption) {
        List<Capsule> capsules = repository.getAllCapsules();

        Comparator<Capsule> comparator = switch (sortOption) {
            case "Title A-Z" -> Comparator.comparing(Capsule::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "Title Z-A" -> Comparator.comparing(Capsule::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Unlock Soonest" -> Comparator.comparing(Capsule::getUnlockDate);
            case "Unlock Latest" -> Comparator.comparing(Capsule::getUnlockDate).reversed();
            case "Created Newest" -> Comparator.comparing(Capsule::getDateCreated).reversed();
            case "Created Oldest" -> Comparator.comparing(Capsule::getDateCreated);
            case "Category A-Z" -> Comparator.comparing(Capsule::getCategory, String.CASE_INSENSITIVE_ORDER);
            case "Category Z-A" -> Comparator.comparing(Capsule::getCategory, String.CASE_INSENSITIVE_ORDER).reversed();
            default -> (a, b) -> 0;
        };

        return MultiThreadedMergeSort.sort(capsules, comparator);
    }


}
