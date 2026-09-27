package net.bl4st.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("elytradrag");

    private static final String FILE_NAME = "elytradrag.properties";

    private static final String DRAG_KEY = "drag";
    private static final String MAXIMUM_FALLDISTANCE_KEY = "maximum-fall-distance-while-slowing-Down";
    private static final String MINIMUM_SPEED_KEY = "minimum-speed-required";

    private static final float DEFAULT_ELYTRA_DRAG = 2.0F;
    private static final float DEFAULT_MAXIMUM_FALLDISTANCE = 15.0F;
    private static final float DEFAULT_MINIMUM_SPEED = 0.1F;

    /**
     * Velocity is multiplied by (1 - 0.05 * drag) every tick,
     * so anything above 20 would reverse the player's direction.
     */
    private static final float MAXIMUM_ELYTRA_DRAG = 20.0F;

    public static float ELYTRA_DRAG = DEFAULT_ELYTRA_DRAG;

    public static float MAXIMUM_FALLDISTANCE = DEFAULT_MAXIMUM_FALLDISTANCE;

    public static float MINIMUM_SPEED = DEFAULT_MINIMUM_SPEED;

    public static void LoadConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        Properties properties = new Properties();

        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.error("Error loading config file {}, using default values", configPath, e);
            }
        }

        ELYTRA_DRAG = readFloat(properties, DRAG_KEY, DEFAULT_ELYTRA_DRAG, 0.0F, MAXIMUM_ELYTRA_DRAG);
        MAXIMUM_FALLDISTANCE = readFloat(properties, MAXIMUM_FALLDISTANCE_KEY, DEFAULT_MAXIMUM_FALLDISTANCE, 0.0F, Float.MAX_VALUE);
        MINIMUM_SPEED = readFloat(properties, MINIMUM_SPEED_KEY, DEFAULT_MINIMUM_SPEED, 0.0F, Float.MAX_VALUE);

        if (!Files.exists(configPath)) {
            SaveDefaultConfig(configPath);
        }
    }

    private static float readFloat(Properties properties, String key, float defaultValue, float min, float max) {
        String value = properties.getProperty(key);
        if (value == null)
            return defaultValue;

        try {
            float parsed = Float.parseFloat(value.trim());
            if (Float.isNaN(parsed))
                throw new NumberFormatException("NaN");
            if (parsed < min || parsed > max) {
                float clamped = Math.max(min, Math.min(max, parsed));
                LOGGER.warn("Config value {}={} is out of range, using {}", key, value, clamped);
                return clamped;
            }
            return parsed;
        } catch (NumberFormatException e) {
            LOGGER.warn("Config value {}={} is not a valid number, using {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private static void SaveDefaultConfig(Path configPath) {
        Properties properties = new Properties();
        properties.setProperty(DRAG_KEY, Float.toString(DEFAULT_ELYTRA_DRAG));
        properties.setProperty(MAXIMUM_FALLDISTANCE_KEY, Float.toString(DEFAULT_MAXIMUM_FALLDISTANCE));
        properties.setProperty(MINIMUM_SPEED_KEY, Float.toString(DEFAULT_MINIMUM_SPEED));

        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "Elytra Drag Configuration");
            }
        } catch (IOException e) {
            LOGGER.error("Error creating default config file {}", configPath, e);
        }
    }
}
