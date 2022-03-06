package io.github.galaxyvn.ggarden.utils;

import io.github.galaxyvn.ggarden.GalaxyPlugin;
import io.github.galaxyvn.ggarden.config.CommentedConfigurationSection;
import io.github.galaxyvn.ggarden.config.CommentedFileConfiguration;
import io.github.galaxyvn.ggarden.config.GalaxySettingSection;
import io.github.galaxyvn.ggarden.config.GalaxySettingValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class GGardenUtils {

    public static final String GRADIENT = "<g:#8A2387:#431ff2:#1169cf>";
    public static final String PREFIX = "&7[" + GRADIENT + "GGarden&7] ";

    public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<Class<?>, Class<?>>() {{
        this.put(boolean.class, Boolean.class);
        this.put(byte.class, Byte.class);
        this.put(char.class, Character.class);
        this.put(double.class, Double.class);
        this.put(float.class, Float.class);
        this.put(int.class, Integer.class);
        this.put(long.class, Long.class);
        this.put(short.class, Short.class);
        this.put(void.class, Void.class);
    }};

    private static Logger logger;

    private GGardenUtils() {

    }

    /**
     * @return the Logger for GGarden
     */
    public static Logger getLogger() {
        if (logger == null) {
            logger= new Logger("GGarden", null) { };
            logger.setParent(Bukkit.getLogger());
            logger.setLevel(Level.ALL);
        }
        return logger;
    }

    /**
     * Checks if a String contains any values for a yaml value that need to be quoted
     *
     * @param string The string to check
     * @return true if any special characters need to be escaped, otherwise false
     */
    public static boolean containsConfigSpecialCharacters(String string) {
        for (char c : string.toCharArray()) {
            // Range taken from SnakeYAML's Emitter.java
            if (!(c == '\n' || (0x20 <= c && c <= 0x7E)) &&
                    (c == 0x85 || (c >= 0xA0 && c <= 0xD7FF)
                            || (c >= 0xE000 && c <= 0xFFFD)
                            || (c >= 0x10000 && c <= 0x10FFFF))) {
                return true;
            }
        }
        return false;
    }

    public static void recursivelyWriteGalaxySettingValues(CommentedFileConfiguration fileConfiguration, GalaxySettingValue settingValue) {
        recursivelyWriteGalaxySettingValues(fileConfiguration, fileConfiguration, settingValue);
    }

    private static void recursivelyWriteGalaxySettingValues(CommentedFileConfiguration baseConfiguration, CommentedConfigurationSection currentSection, GalaxySettingValue settingValue) {
        String key = settingValue.getKey();
        Object defaultValue = settingValue.getDefaultValue();
        String[] comments = settingValue.getComments();

        String keyPath = currentSection.getCurrentPath() == null ? key : currentSection.getCurrentPath() + "." + key;

        if (defaultValue instanceof GalaxySettingSection) {
            baseConfiguration.addPathedComments(keyPath, comments);
            currentSection = currentSection.createSection(key);

            GalaxySettingSection settingSection = (GalaxySettingSection) defaultValue;
            for (GalaxySettingValue value : settingSection.getValues())
                recursivelyWriteGalaxySettingValues(baseConfiguration, currentSection, value);
        } else {
            baseConfiguration.set(keyPath, defaultValue, comments);
        }
    }

    /**
     * Gets an Object as a numerical value
     *
     * @param value The Object to cast
     * @return The Object as a numerical value
     * @throws ClassCastException when the value is not a numerical value
     */
    public static double getNumber(Object value) {
        if (value instanceof Integer) {
            return (int) value;
        } else if (value instanceof Short) {
            return (short) value;
        } else if (value instanceof Byte) {
            return (byte) value;
        } else if (value instanceof Float) {
            return (float) value;
        }

        return (double) value;
    }

    /**
     * Checks if there is an update available
     *
     * @param latest The latest version of the plugin from Spigot
     * @param current The currently installed version of the plugin
     * @return true if available, otherwise false
     */
    public static boolean isUpdateAvailable(String latest, String current) {
        if (latest == null || current == null)
            return false;

        // Break versions into individual numerical pieces separated by periods
        int[] latestSplit = Arrays.stream(latest.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray();
        int[] currentSplit = Arrays.stream(current.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray();

        // Make sure both arrays are the same length
        if (latestSplit.length > currentSplit.length) {
            currentSplit = Arrays.copyOf(currentSplit, latestSplit.length);
        } else if (currentSplit.length > latestSplit.length) {
            latestSplit = Arrays.copyOf(latestSplit, currentSplit.length);
        }

        // Compare pieces from most significant to least significant
        for (int i = 0; i < latestSplit.length; i++) {
            if (latestSplit[i] > currentSplit[i]) {
                return true;
            } else if (currentSplit[i] > latestSplit[i]) {
                break;
            }
        }

        return false;
    }

    /**
     * @return true if GalaxyGarden has been relocated properly, otherwise false
     */
    public static boolean isRelocated() {
        String defaultPackage = new String(new byte[]{'i', 'o', '.', 'g', 'i', 't', 'h', 'u', 'b', '.', 'g', 'a', 'l', 'a', 'x', 'y', 'v', 'n', '.', 'g', 'g', 'a', 'r', 'd', 'e', 'n'});
        return !GalaxyPlugin.class.getPackage().getName().equals(defaultPackage);
    }

    /**
     * Gets a primitive class as its wrapper counterpart
     *
     * @param clazz The class to get the wrapped class of
     * @return The wrapped class, or the same class if not primitive
     */
    public static Class<?> getPrimitiveAsWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ? PRIMITIVE_TO_WRAPPER.get(clazz) : clazz;
    }

    /**
     * Sends a GGarden message to a recipient
     */
    public static void sendMessage(CommandSender recipient, String message) {
        recipient.sendMessage(HexUtils.colorify(PREFIX + message));
    }

    /**
     * Sends a GGarden message to a recipient
     */
    public static void sendMessage(CommandSender recipient, String message, StringPlaceholders placeholders) {
        sendMessage(recipient, placeholders.apply(message));
    }
}
