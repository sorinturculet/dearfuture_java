package cz.dearfuture.services;

import cz.dearfuture.models.Capsule;
import cz.dearfuture.repositories.CapsuleRepository;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CapsuleServiceTest {
    private CapsuleService service;
    private CapsuleRepository repository;
    private final String TEST_FILE_PATH = "data/test_capsules.json"; // Separate test file

    @BeforeAll
    void setupTestFile() {
        // Ensure the test directory exists
        File testFile = new File(TEST_FILE_PATH);
        testFile.getParentFile().mkdirs();
    }

    @BeforeEach
    void setUp() {
        repository = new CapsuleRepository(TEST_FILE_PATH);
        service = new CapsuleService(repository);
    }

    @Test
    void testAddCapsule() {
        Capsule capsule = new Capsule(1, "Test Capsule", "This is a test message",
                LocalDateTime.now().plusDays(2), "Reminder", "#3498DB"); // Blue capsule

        boolean added = service.addCapsule(capsule);
        List<Capsule> capsules = service.getLockedCapsules();

        assertTrue(added, "Capsule should be successfully added.");
        assertEquals(1, capsules.size(), "Capsule should be stored in the repository.");
        assertEquals("Test Capsule", capsules.get(0).getTitle());
        assertEquals("#3498DB", capsules.get(0).getColor(), "Capsule should retain assigned color.");
    }

    @Test
    void testOpenCapsule_Locked() {
        Capsule capsule = new Capsule(2, "Locked Capsule", "Secret Message",
                LocalDateTime.now().plusDays(2), "Event", "#E74C3C"); // Red capsule

        service.addCapsule(capsule);
        String result = service.openCapsule(2);

        assertEquals("This capsule is still locked!", result, "Locked capsule should not open.");
        assertFalse(capsule.isOpened(), "Capsule should remain unopened.");
    }

    @Test
    void testOpenCapsule_Unlocked() {
        Capsule capsule = new Capsule(3, "Unlocked Capsule", "This is visible",
                LocalDateTime.now().minusDays(1), "Event", "#2ECC71"); // Green capsule

        service.addCapsule(capsule);
        String result = service.openCapsule(3);

        assertEquals("This is visible", result, "Unlocked capsule should reveal message.");
        assertTrue(capsule.isOpened(), "Capsule should be marked as opened.");
    }

    @Test
    void testDeleteCapsule() {
        Capsule capsule = new Capsule(4, "Trash Me", "Move this to trash",
                LocalDateTime.now().plusDays(1), "Reminder", "#F1C40F"); // Yellow capsule

        service.addCapsule(capsule);
        service.deleteCapsule(4);

        List<Capsule> activeCapsules = service.getLockedCapsules();
        List<Capsule> deletedCapsules = service.getDeletedCapsules();

        assertEquals(0, activeCapsules.size(), "Deleted capsule should not be in active list.");
        assertEquals(1, deletedCapsules.size(), "Deleted capsule should be in trash.");
        assertTrue(deletedCapsules.get(0).isDeleted(), "Deleted capsule should be marked as deleted.");
    }

    @Test
    void testRestoreCapsule() {
        Capsule capsule = new Capsule(5, "Restore Me", "Bring me back",
                LocalDateTime.now().plusDays(1), "Reminder", "#9B59B6"); // Purple capsule

        service.addCapsule(capsule);
        service.deleteCapsule(5);
        service.restoreCapsule(5);

        List<Capsule> activeCapsules = service.getLockedCapsules();
        assertEquals(1, activeCapsules.size(), "Restored capsule should be back in active list.");
        assertFalse(activeCapsules.get(0).isDeleted(), "Restored capsule should not be marked as deleted.");
    }

    @Test
    void testPermanentlyDeleteCapsule() {
        Capsule capsule = new Capsule(6, "Permanent Delete", "Will be erased forever",
                LocalDateTime.now().plusDays(1), "Reminder", "#D35400"); // Orange capsule

        service.addCapsule(capsule);
        service.permanentlyDeleteCapsule(6);

        List<Capsule> activeCapsules = service.getLockedCapsules();
        assertEquals(0, activeCapsules.size(), "Permanently deleted capsule should not exist anymore.");
    }

    @Test
    void testCleanupOldDeletedCapsules() {
        Capsule oldCapsule = new Capsule(7, "Old Trash", "Should be auto-deleted",
                LocalDateTime.now().minusDays(20), "Trash", "#E74C3C"); // Red capsule
        oldCapsule.deleteCapsule(); // Move to trash manually

        service.addCapsule(oldCapsule);
        service.cleanupOldDeletedCapsules();

        List<Capsule> deletedCapsules = service.getDeletedCapsules();
        assertEquals(1, deletedCapsules.size(), "Capsules deleted over 15 days ago should be removed.");
    }

    @AfterEach
    void tearDown() {
        // Clear the test file after each test
        new File(TEST_FILE_PATH).delete();
    }
}
