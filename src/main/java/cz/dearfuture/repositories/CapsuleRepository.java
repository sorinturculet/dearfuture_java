package cz.dearfuture.repositories;

import com.google.gson.*;
import cz.dearfuture.models.Capsule;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles storing, retrieving, and managing Capsule objects using a JSON file.
 */
public class CapsuleRepository {
    private final String filePath;
    private List<Capsule> capsules;

    /**
     * Constructs a repository for managing capsules stored in a JSON file.
     *
     * @param filePath The file path where capsules are stored.
     */
    public CapsuleRepository(String filePath) {
        this.filePath = filePath;
        this.capsules = loadCapsulesFromFile();
    }

    /**
     * Loads capsules from the JSON storage file.
     *
     * @return A list of capsules loaded from the file.
     */
    private List<Capsule> loadCapsulesFromFile() {
        try {
            if (!Files.exists(Paths.get(filePath))) {
                return new ArrayList<>();
            }
            String json = Files.readString(Paths.get(filePath));
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            Capsule[] capsuleArray = gson.fromJson(json, Capsule[].class);
            return capsuleArray != null ? new ArrayList<>(Arrays.asList(capsuleArray)) : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Saves the current list of capsules to the JSON storage file.
     */
    private void saveCapsulesToFile() {
        try (Writer writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .setPrettyPrinting()
                    .create();
            gson.toJson(capsules, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all active (non-deleted) capsules.
     *
     * @return A list of non-deleted capsules.
     */
    public List<Capsule> getAllCapsules() {
        return capsules.stream()
                .filter(capsule -> !capsule.isDeleted())
                .toList();
    }

    /**
     * Retrieves a capsule by its ID if it is not deleted.
     *
     * @param id The ID of the capsule to retrieve.
     * @return The capsule with the given ID, or {@code null} if not found.
     */
    public Capsule getCapsuleById(int id) {
        return capsules.stream()
                .filter(capsule -> capsule.getId() == id && !capsule.isDeleted())
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds a new capsule to the repository and saves it to storage.
     *
     * @param capsule The capsule to be added.
     */
    public void addCapsule(Capsule capsule) {
        capsules.add(capsule);
        saveCapsulesToFile();
    }

    /**
     * Moves a capsule to the trash (soft delete).
     *
     * @param id The ID of the capsule to delete.
     */
    public void deleteCapsule(int id) {
        for (Capsule c : capsules) {
            if (c.getId() == id) {
                c.deleteCapsule();
                saveCapsulesToFile();
                return;
            }
        }
    }

    /**
     * Permanently removes a capsule from storage.
     *
     * @param id The ID of the capsule to be permanently deleted.
     */
    public void permanentlyDeleteCapsule(int id) {
        capsules.removeIf(capsule -> capsule.getId() == id);
        saveCapsulesToFile();
    }

    /**
     * Restores a capsule from the trash.
     *
     * @param id The ID of the capsule to restore.
     */
    public void restoreCapsule(int id) {
        for (Capsule c : capsules) {
            if (c.getId() == id) {
                c.restoreCapsule();
                saveCapsulesToFile();
                return;
            }
        }
    }

    /**
     * Retrieves all capsules that are currently in the trash.
     *
     * @return A list of deleted (trashed) capsules.
     */
    public List<Capsule> getDeletedCapsules() {
        return capsules.stream()
                .filter(Capsule::isDeleted)
                .toList();
    }

    /**
     * Removes permanently deleted capsules that have been in trash for more than 15 days.
     */
    public void cleanupOldDeletedCapsules() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(15);
        capsules.removeIf(capsule -> capsule.isDeleted() && capsule.getDeletedAt().isBefore(threshold));
        saveCapsulesToFile();
    }
}

/**
 * A custom adapter to handle serialization and deserialization of {@link LocalDateTime} in JSON.
 */
class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    /**
     * Serializes a LocalDateTime object into JSON format.
     *
     * @param dateTime The LocalDateTime object to serialize.
     * @param type     The type of the object.
     * @param context  The serialization context.
     * @return The serialized JSON element.
     */
    @Override
    public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(dateTime.toString());
    }

    /**
     * Deserializes a JSON element into a LocalDateTime object.
     *
     * @param json     The JSON element to deserialize.
     * @param type     The type of the object.
     * @param context  The deserialization context.
     * @return The deserialized LocalDateTime object.
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        return LocalDateTime.parse(json.getAsString());
    }
}
