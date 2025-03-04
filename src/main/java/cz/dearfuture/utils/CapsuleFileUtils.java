package cz.dearfuture.utils;

import cz.dearfuture.models.Capsule;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for exporting and importing capsules to/from JSON or CSV files.
 */
public class CapsuleFileUtils {

    /**
     * Exports capsules to a file (JSON or CSV format).
     *
     * @param capsules The list of capsules to export.
     * @param filePath The destination file path.
     * @param format   The format ("json" or "csv").
     */
    public static void exportCapsulesToFile(List<Capsule> capsules, String filePath, String format) {
        try (Writer writer = new FileWriter(filePath)) {
            if (format.equalsIgnoreCase("json")) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(capsules, writer);
            } else if (format.equalsIgnoreCase("csv")) {
                for (Capsule capsule : capsules) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s\n",
                            capsule.getId(),
                            capsule.getTitle(),
                            capsule.getMessage(),
                            capsule.getUnlockDate(),
                            capsule.getCategory(),
                            capsule.getColor()));
                }
            }
            System.out.println(Capsule.GREEN + "Capsules successfully exported to " + filePath + Capsule.RESET);
        } catch (IOException e) {
            System.out.println(Capsule.RED + "Error exporting capsules: " + e.getMessage() + Capsule.RESET);
        }
    }

    /**
     * Imports capsules from a file (JSON or CSV format).
     *
     * @param filePath The source file path.
     * @param format   The format ("json" or "csv").
     * @return A list of imported capsules.
     */
    public static List<Capsule> importCapsulesFromFile(String filePath, String format) {
        List<Capsule> importedCapsules = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            if (format.equalsIgnoreCase("json")) {
                Gson gson = new Gson();
                importedCapsules = gson.fromJson(reader, new TypeToken<List<Capsule>>() {}.getType());
            } else if (format.equalsIgnoreCase("csv")) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    Capsule capsule = new Capsule(
                            Integer.parseInt(data[0]), // ID
                            data[1], // Title
                            data[2], // Message
                            LocalDateTime.parse(data[3]), // Unlock Date
                            data[4], // Category
                            data[5]  // Color
                    );
                    importedCapsules.add(capsule);
                }
            }
            System.out.println(Capsule.GREEN + "Capsules successfully imported from " + filePath + Capsule.RESET);
        } catch (IOException | NumberFormatException e) {
            System.out.println(Capsule.RED + "Error importing capsules: " + e.getMessage() + Capsule.RESET);
        }
        return importedCapsules;
    }
}
