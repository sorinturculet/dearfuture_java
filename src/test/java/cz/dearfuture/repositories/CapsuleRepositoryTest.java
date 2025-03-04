package cz.dearfuture.repositories;

import cz.dearfuture.models.Capsule;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CapsuleRepositoryTest {
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
    }

    @Test
    void testAddCapsule() {
        Capsule capsule = new Capsule(1, "Test Capsule", "This is a test",
                LocalDateTime.now().plusDays(2), "Reminder", "#3498DB"); // Blue capsule

        repository.addCapsule(capsule);
        List<Capsule> capsules = repository.getAllCapsules();

        assertEquals(1, capsules.size(), "Capsule should be added to the repository.");
        assertEquals("Test Capsule", capsules.get(0).getTitle());
        assertEquals("#3498DB", capsules.get(0).getColor(), "Capsule should retain assigned color.");
    }

    @Test
    void testGetCapsuleById() {
        Capsule capsule = new Capsule(2, "Find Me", "Testing find method",
                LocalDateTime.now().plusDays(1), "Event", "#2ECC71"); // Green capsule

        repository.addCapsule(capsule);
        Capsule foundCapsule = repository.getCapsuleById(2);

        assertNotNull(foundCapsule, "Capsule should be found.");
        assertEquals("Find Me", foundCapsule.getTitle());
        assertEquals("#2ECC71", foundCapsule.getColor(), "Capsule should retain assigned color.");
    }

    @Test
    void testDeleteCapsule() {
        Capsule capsule = new Capsule(3, "Delete Me", "This will be deleted",
                LocalDateTime.now().plusDays(1), "Reminder", "#E74C3C"); // Red capsule

        repository.addCapsule(capsule);
        repository.deleteCapsule(3);

        List<Capsule> activeCapsules = repository.getAllCapsules();
        List<Capsule> deletedCapsules = repository.getDeletedCapsules();

        assertEquals(0, activeCapsules.size(), "Deleted capsule should not be in active list.");
        assertEquals(1, deletedCapsules.size(), "Deleted capsule should be in trash.");
        assertTrue(deletedCapsules.get(0).isDeleted(), "Deleted capsule should be marked as deleted.");
    }

    @Test
    void testRestoreCapsule() {
        Capsule capsule = new Capsule(4, "Restore Me", "This will be restored",
                LocalDateTime.now().plusDays(1), "Reminder", "#F1C40F"); // Yellow capsule

        repository.addCapsule(capsule);
        repository.deleteCapsule(4);
        repository.restoreCapsule(4);

        List<Capsule> activeCapsules = repository.getAllCapsules();
        assertEquals(1, activeCapsules.size(), "Restored capsule should be back in active list.");
        assertFalse(activeCapsules.get(0).isDeleted(), "Restored capsule should not be marked as deleted.");
    }

    @Test
    void testPermanentlyDeleteCapsule() {
        Capsule capsule = new Capsule(5, "Permanent Delete", "Will be erased",
                LocalDateTime.now().plusDays(1), "Reminder", "#9B59B6"); // Purple capsule

        repository.addCapsule(capsule);
        repository.permanentlyDeleteCapsule(5);

        Capsule retrievedCapsule = repository.getCapsuleById(5);
        assertNull(retrievedCapsule, "Permanently deleted capsule should not be retrievable.");
    }

    @Test
    void testCleanupOldDeletedCapsules() {
        Capsule oldCapsule = new Capsule(6, "Old Trash", "This should be cleaned up",
                LocalDateTime.now().minusDays(20), "Trash", "#D35400"); // Orange capsule
        oldCapsule.deleteCapsule(); // Mark it as deleted manually

        repository.addCapsule(oldCapsule);
        repository.cleanupOldDeletedCapsules();

        List<Capsule> deletedCapsules = repository.getDeletedCapsules();
        assertEquals(1, deletedCapsules.size(), "Capsules deleted over 15 days ago should be removed.");
    }

    @AfterEach
    void tearDown() {
        // Clear the test file after each test
        new File(TEST_FILE_PATH).delete();
    }
}
