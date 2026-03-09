package fi.dy.masa.minihud.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.minihud.MiniHUD;
import fi.dy.masa.minihud.Reference;

/**
 * Player Highlighter config: list of player names to outline (multiplayer).
 * Uses same distance and line width as block highlighter.
 */
public final class PlayerHighlighterConfigs {
    public static final String CONFIG_FILE_NAME = "player_highlighter.json";
    private static final String KEY = Reference.MOD_ID + ".config.player_highlighter";

    public static final ConfigBoolean ENABLED = new ConfigBoolean("enabled", true).apply(KEY);
    public static final ConfigInteger HIGHLIGHT_DISTANCE = new ConfigInteger("highlightDistance", 64, 1, 128).apply(KEY);

    public static final List<IConfigBase> OPTIONS = List.of(ENABLED, HIGHLIGHT_DISTANCE);

    private static final List<PlayerEntry> ENTRIES = new ArrayList<>();

    public static List<PlayerEntry> getEntries() {
        return ENTRIES;
    }

    public static PlayerEntry addEntry() {
        PlayerEntry e = new PlayerEntry("", true, 100, 200, 255, 255);
        ENTRIES.add(e);
        return e;
    }

    public static PlayerEntry addEntry(String playerName) {
        PlayerEntry e = new PlayerEntry(playerName != null ? playerName : "", true, 100, 200, 255, 255);
        ENTRIES.add(e);
        return e;
    }

    public static void removeEntry(int index) {
        if (index >= 0 && index < ENTRIES.size())
            ENTRIES.remove(index);
    }

    public static final class PlayerEntry {
        private String playerName;
        private boolean enabled;
        private int colorR, colorG, colorB, colorA;

        public PlayerEntry(String playerName, boolean enabled, int r, int g, int b, int a) {
            this.playerName = playerName != null ? playerName : "";
            this.enabled = enabled;
            this.colorR = Math.max(0, Math.min(255, r));
            this.colorG = Math.max(0, Math.min(255, g));
            this.colorB = Math.max(0, Math.min(255, b));
            this.colorA = Math.max(0, Math.min(255, a));
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String name) {
            this.playerName = name != null ? name : "";
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean v) {
            this.enabled = v;
        }

        public int getColorR() { return colorR; }
        public void setColorR(int v) { this.colorR = Math.max(0, Math.min(255, v)); }
        public int getColorG() { return colorG; }
        public void setColorG(int v) { this.colorG = Math.max(0, Math.min(255, v)); }
        public int getColorB() { return colorB; }
        public void setColorB(int v) { this.colorB = Math.max(0, Math.min(255, v)); }
        public int getColorA() { return colorA; }
        public void setColorA(int v) { this.colorA = Math.max(0, Math.min(255, v)); }

        public int getColorArgb() {
            return (colorA << 24) | (colorR << 16) | (colorG << 8) | colorB;
        }

        public void setColorArgb(int argb) {
            colorA = (argb >> 24) & 0xFF;
            colorR = (argb >> 16) & 0xFF;
            colorG = (argb >> 8) & 0xFF;
            colorB = argb & 0xFF;
        }
    }

    public static void loadFromFile() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);
        if (!Files.exists(configFile) || !Files.isReadable(configFile))
            return;
        JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
        if (element == null || !element.isJsonObject())
            return;
        JsonObject root = element.getAsJsonObject();
        ConfigUtils.readConfigBase(root, "PlayerHighlighter", OPTIONS);
        ENTRIES.clear();
        JsonElement arrEl = root.get("Entries");
        if (arrEl != null && arrEl.isJsonArray()) {
            for (JsonElement el : arrEl.getAsJsonArray()) {
                if (!el.isJsonObject())
                    continue;
                JsonObject o = el.getAsJsonObject();
                String name = o.has("playerName") ? o.get("playerName").getAsString() : "";
                boolean enabled = !o.has("enabled") || o.get("enabled").getAsBoolean();
                int r = o.has("r") ? o.get("r").getAsInt() : 100;
                int g = o.has("g") ? o.get("g").getAsInt() : 200;
                int b = o.has("b") ? o.get("b").getAsInt() : 255;
                int a = o.has("a") ? o.get("a").getAsInt() : 255;
                ENTRIES.add(new PlayerEntry(name, enabled, r, g, b, a));
            }
        }
    }

    public static void saveToFile() {
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir))
            FileUtils.createDirectoriesIfMissing(dir);
        if (!Files.isDirectory(dir)) {
            MiniHUD.LOGGER.error("PlayerHighlighterConfigs: config directory does not exist");
            return;
        }
        JsonObject root = new JsonObject();
        ConfigUtils.writeConfigBase(root, "PlayerHighlighter", OPTIONS);
        JsonArray arr = new JsonArray();
        for (PlayerEntry e : ENTRIES) {
            JsonObject o = new JsonObject();
            o.addProperty("playerName", e.getPlayerName());
            o.addProperty("enabled", e.isEnabled());
            o.addProperty("r", e.getColorR());
            o.addProperty("g", e.getColorG());
            o.addProperty("b", e.getColorB());
            o.addProperty("a", e.getColorA());
            arr.add(o);
        }
        root.add("Entries", arr);
        JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
    }
}
