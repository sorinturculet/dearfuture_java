package cz.dearfuture.utils;

import cz.dearfuture.models.Capsule;
import cz.dearfuture.models.CapsuleStatus;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for exporting and importing capsules to/from CSV files.
 * Handles encryption and decryption of sensitive data.
 */
public class CapsuleFileUtils {

    /**
     * Exports capsules to a CSV file.
     * Encrypts sensitive data for security.
     *
     * @param capsules The list of capsules to export.
     * @param filePath The destination file path.
     */
    public static void exportCapsulesToFile(List<Capsule> capsules, String filePath) {
        try (Writer writer = new FileWriter(filePath)) {
            // Write CSV header
            writer.write("ID,Title,Message,UnlockDate,Category,Color,Status\n");
            
            for (Capsule capsule : capsules) {
                // Use getRawTitle and getRawMessage instead of getTitle and getMessage
                String encryptedTitle = EncryptionUtil.encrypt(capsule.getRawTitle());
                String encryptedMessage = EncryptionUtil.encrypt(capsule.getRawMessage());
                
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                        capsule.getId(),
                        encryptedTitle,
                        encryptedMessage,
                        capsule.getUnlockDate(),
                        capsule.getCategory(),
                        capsule.getColor(),
                        capsule.getStatus()));
            }
            System.out.println(Capsule.GREEN + "Capsules successfully exported to " + filePath + Capsule.RESET);
        } catch (IOException e) {
            System.out.println(Capsule.RED + "Error exporting capsules: " + e.getMessage() + Capsule.RESET);
        }
    }

    /**
     * Imports capsules from a CSV file.
     * Decrypts sensitive data during import.
     *
     * @param filePath The source file path.
     * @return A list of imported capsules.
     */
    public static List<Capsule> importCapsulesFromFile(String filePath) {
        List<Capsule> importedCapsules = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] data = line.split(",");
                if (data.length >= 7) {
                    try {
                        // Decrypt sensitive data from CSV
                        String decryptedTitle = EncryptionUtil.decrypt(data[1]);
                        String decryptedMessage = EncryptionUtil.decrypt(data[2]);
                        
                        Capsule capsule = new Capsule(
                                Integer.parseInt(data[0]),     // ID
                                decryptedTitle,                // Title (decrypted)
                                decryptedMessage,              // Message (decrypted)
                                LocalDateTime.parse(data[3]),  // Unlock Date
                                data[4],                       // Category
                                data[5]                        // Color
                        );

                        // Set the status based on imported value
                        CapsuleStatus status = CapsuleStatus.valueOf(data[6]);
                        switch (status) {
                            case OPENED -> capsule.openCapsule();
                            case DELETED -> capsule.deleteCapsule();
                            // LOCKED is default state, no action needed
                        }

                        importedCapsules.add(capsule);
                    } catch (Exception e) {
                        System.out.println(Capsule.YELLOW + "Warning: Skipping invalid capsule entry: " + e.getMessage() + Capsule.RESET);
                    }
                }
            }
            System.out.println(Capsule.GREEN + "Capsules successfully imported from " + filePath + Capsule.RESET);
        } catch (IOException | NumberFormatException e) {
            System.out.println(Capsule.RED + "Error importing capsules: " + e.getMessage() + Capsule.RESET);
        }
        return importedCapsules;
    }
}
