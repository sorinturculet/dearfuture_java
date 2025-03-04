package cz.dearfuture.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CapsuleTest {
    private Capsule futureCapsule;
    private Capsule pastCapsule;

    @BeforeEach
    void setUp() {
        // Capsule that should still be locked (with color)
        futureCapsule = new Capsule(1, "Future Capsule", "This is locked",
                LocalDateTime.now().plusDays(5), "Reminder", "#3498DB"); // Blue Capsule

        // Capsule that should already be unlocked (with color)
        pastCapsule = new Capsule(2, "Past Capsule", "This is unlocked",
                LocalDateTime.now().minusDays(1), "Event", "#2ECC71"); // Green Capsule
    }

    @Test
    void testCapsuleIsLocked() {
        assertFalse(futureCapsule.isUnlocked(), "Capsule should be locked.");
        assertEquals("This capsule is locked!", futureCapsule.getMessage());
    }

    @Test
    void testCapsuleIsUnlocked() {
        assertFalse(pastCapsule.isOpened(), "Capsule should not be opened initially.");

        pastCapsule.openCapsule();

        assertTrue(pastCapsule.isOpened(), "Capsule should be marked as opened after calling openCapsule().");
        assertEquals("This is unlocked", pastCapsule.getMessage(), "Opened capsule should reveal its message.");
    }

    @Test
    void testOpenCapsule() {
        futureCapsule.openCapsule();
        assertFalse(futureCapsule.isOpened(), "Capsule should not open if locked.");

        pastCapsule.openCapsule();
        assertTrue(pastCapsule.isOpened(), "Capsule should be opened.");
    }

    @Test
    void testDeleteCapsule() {
        futureCapsule.deleteCapsule();
        assertTrue(futureCapsule.isDeleted(), "Capsule should be marked as deleted.");
        assertNotNull(futureCapsule.getDeletedAt(), "DeletedAt timestamp should be set.");
    }

    @Test
    void testRestoreCapsule() {
        futureCapsule.deleteCapsule();
        futureCapsule.restoreCapsule();
        assertFalse(futureCapsule.isDeleted(), "Capsule should be restored.");
        assertNull(futureCapsule.getDeletedAt(), "DeletedAt should be reset.");
    }

    @Test
    void testCapsuleColorAssignment() {
        assertEquals("#3498DB", futureCapsule.getColor(), "Future capsule should have blue color.");
        assertEquals("#2ECC71", pastCapsule.getColor(), "Past capsule should have green color.");
    }
}
