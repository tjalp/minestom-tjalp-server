package net.tjalp.peach.peel.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A [JsonConfig] can be used to instantiate
 * parameters in a class without having
 * to manually read the keys and values
 */
public class JsonConfig<T> implements Configurable {

    private File file;
    private Class<T> type;
    private Gson gson;
    private T data;

    public JsonConfig(File file, Class<T> type) {
        this(file, type, true);
    }

    public JsonConfig(File file, Class<T> type, boolean create) {
        this.file = file;
        this.type = type;

        if (file.exists()) {
            load();
        } else {
            try {
                data = type.getConstructor().newInstance();
            } catch (Exception err) {
                throw new IllegalStateException("Failed to instantiate config", err);
            }

            if (create) {
                save(null);
            }
        }
    }

    /**
     * Reload the config from disk and replace the
     * current config.
     */
    private void load() {
        try {
            data = gson.fromJson(Files.newBufferedReader(file.toPath()), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the current config to disk
     *
     * @param output The output file, null for the input file
     */
    private void save(File output) {
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(data);
        File outFile = output != null ? output : file;

        try {
            outFile.createNewFile();
            Files.write(file.toPath(), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (data instanceof Configurable) ((Configurable) data).onSave();
    }
}
