package cz.dearfuture.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a time capsule that stores a message and unlocks at a future date.
 */
public class Capsule {
    // Static ANSI Color Codes (Reusable for UI & Printing)
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[1;31m"; // Red for Deleted
    public static final String GREEN = "\033[1;32m"; // Green for Opened
    public static final String YELLOW = "\033[1;33m"; // Yellow for Locked
    public static final String BLUE = "\033[1;34m"; // Blue for Titles
    public static final String PURPLE = "\033[1;35m"; // Purple for Special
    public static final String CYAN = "\033[1;36m"; // Cyan for Highlights
    public static final String WHITE = "\033[1;37m"; // Default White
    // Fields
    private int id;
    private String title;
    private String message;
    private String color = "#FFFFFF";
    private LocalDateTime unlockDate;
    private String category;
    private LocalDateTime dateCreated;
    private boolean isOpened;
    private boolean isDeleted;
    private LocalDateTime deletedAt;

    /**
     * Constructs a new Capsule.
     *
     * @param id         Unique identifier of the capsule.
     * @param title      Title of the capsule.
     * @param message    Message stored inside the capsule.
     * @param unlockDate The date and time when the capsule can be opened.
     * @param category   Category of the capsule (e.g., Reminder, Reflection, Event).
     */
    public Capsule(int id, String title, String message, LocalDateTime unlockDate, String category,String color) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.unlockDate = unlockDate;
        this.category = category;
        this.dateCreated = LocalDateTime.now();
        this.color = color;
        this.isOpened = false;
        this.isDeleted = false;
        this.deletedAt = null;
    }

    /** @return The unique ID of the capsule. */
    public int getId() { return id; }

    /** @return The title of the capsule. */
    public String getTitle() { return title; }

    /**
     * @return The message stored inside the capsule if it is opened.
     * Otherwise, returns a locked message.
     */
    public String getMessage() { return isOpened ? message : "This capsule is locked!"; }

    /** @return The color of the capsule. */
    public String getColor() { return color; }

    /** @return The unlock date of the capsule. */
    public LocalDateTime getUnlockDate() { return unlockDate; }

    /** @return The category of the capsule. */
    public String getCategory() { return category; }

    /** @return The date when the capsule was created. */
    public LocalDateTime getDateCreated() { return dateCreated; }

    /** @return {@code true} if the capsule has been opened, otherwise {@code false}. */
    public boolean isOpened() { return isOpened; }

    /** @return {@code true} if the capsule has been deleted (moved to Trash), otherwise {@code false}. */
    public boolean isDeleted() { return isDeleted; }

    /** @return The timestamp when the capsule was deleted, or {@code null} if not deleted. */
    public LocalDateTime getDeletedAt() { return deletedAt; }

    /** @param title Sets the title of the capsule. */
    public void setTitle(String title) { this.title = title; }

    /** @param message Sets the message of the capsule. */
    public void setMessage(String message) { this.message = message; }

    /** @param color Sets the color of the capsule. */
    public void setColor(String color) { this.color = color; }

    /** @param unlockDate Sets the unlock date of the capsule. */
    public void setUnlockDate(LocalDateTime unlockDate) { this.unlockDate = unlockDate; }

    /** @param category Sets the category of the capsule. */
    public void setCategory(String category) { this.category = category; }

    /** @param opened Sets whether the capsule is opened. */
    public void setOpened(boolean opened) { this.isOpened = opened; }

    /**
     * Checks if the capsule is unlocked based on the current date and time.
     *
     * @return {@code true} if the capsule is unlocked, otherwise {@code false}.
     */
    public boolean isUnlocked() {
        return LocalDateTime.now().isAfter(unlockDate);
    }

    /**
     * Attempts to open the capsule.
     *
     * @return {@code true} if the capsule is successfully opened, otherwise {@code false}.
     */
    public boolean openCapsule() {
        if (isUnlocked()) {
            isOpened = true;
            return true;
        }
        return false;
    }

    /** Moves the capsule to the Trash (soft delete). */
    public void deleteCapsule() {
        isDeleted = true;
        deletedAt = LocalDateTime.now();
    }

    /** Restores the capsule from the Trash. */
    public void restoreCapsule() {
        isDeleted = false;
        deletedAt = null;
    }

    /**
     * Returns a string representation of the Capsule object.
     *
     * @return A formatted string containing capsule details.
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Determine status color
        String statusColor = isDeleted ? RED : (isOpened ? GREEN : YELLOW);

        // Determine capsule title color (based on predefined colors)
        String capsuleColor = switch (color.toUpperCase()) {
            case "#E74C3C" -> RED;    // Red
            case "#3498DB" -> BLUE;   // Blue
            case "#2ECC71" -> GREEN;  // Green
            case "#F1C40F" -> YELLOW; // Yellow
            case "#9B59B6" -> PURPLE; // Purple
            case "#1ABC9C" -> CYAN;   // Cyan
            default -> WHITE;         // Default White
        };

        return String.format("""
            --------------------------------------
            %sID: %d%s
            %sTitle: %s%s
            %sMessage: %s%s
            Color: %s
            Unlock Date: %s
            Category: %s
            Created On: %s
            Status: %s%s%s
            Deleted: %s
            --------------------------------------
            """,
                statusColor, id, RESET, // ID Color
                capsuleColor, title, RESET, // Title in Capsule's Color
                isOpened ? GREEN : YELLOW, isOpened ? message : "This capsule is locked!", RESET, // Message Color
                color, // Hex Color Code
                unlockDate.format(formatter),
                category,
                dateCreated.format(formatter),
                statusColor, isOpened ? "Opened" : "Locked", RESET, // Status Color
                isDeleted ? RED + "Yes (Deleted at: " + (deletedAt != null ? deletedAt.format(formatter) : "Unknown") + ")" + RESET : "No"
        );
    }

}
