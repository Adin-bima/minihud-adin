package fi.dy.masa.minihud.config;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.minihud.MiniHUD;
import fi.dy.masa.minihud.Reference;

/**
 * Block Highlighter config: persisted to config/highlighter.json.
 * Loaded on startup from Configs.loadFromFile(); saved when user clicks Done in
 * Block Highlighter GUI.
 */
public final class HighlighterConfigs {
    public static final String CONFIG_FILE_NAME = "highlighter.json";
    private static final String KEY = Reference.MOD_ID + ".config.highlighter";

    public static final ConfigBoolean ENABLED = new ConfigBoolean("enabled", true).apply(KEY);
    public static final ConfigDouble LINE_WIDTH = new ConfigDouble("lineWidth", 3.0, 0.5, 8.0).apply(KEY);
    /** Max distance in blocks to scan and highlight (default 10). */
    public static final ConfigInteger HIGHLIGHT_DISTANCE = new ConfigInteger("highlightDistance", 10, 1, 128).apply(KEY);

    public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(ENABLED, LINE_WIDTH, HIGHLIGHT_DISTANCE);

    /**
     * Mutable list of custom block entries (each has blockId, enabled, fill,
     * outline, color).
     */
    private static final List<CustomEntry> CUSTOM_ENTRIES = new ArrayList<>();

    public static List<CustomEntry> getCustomEntries() {
        return CUSTOM_ENTRIES;
    }

    public static CustomEntry addCustomEntry() {
        CustomEntry e = new CustomEntry("minecraft:stone", true, false, true, 255, 165, 0, 128);
        CUSTOM_ENTRIES.add(e);
        return e;
    }

    public static void removeCustomEntry(int index) {
        if (index >= 0 && index < CUSTOM_ENTRIES.size())
            CUSTOM_ENTRIES.remove(index);
    }

    /** One custom block highlighter entry. */
    public static final class CustomEntry {
        private String blockId;
        private boolean enabled;
        private boolean drawFill;
        private boolean drawOutline;
        private int colorR, colorG, colorB, colorA;

        public CustomEntry(String blockId, boolean enabled, boolean drawFill, boolean drawOutline, int r, int g, int b,
                int a) {
            this.blockId = blockId != null ? blockId : "minecraft:stone";
            this.enabled = enabled;
            this.drawFill = drawFill;
            this.drawOutline = drawOutline;
            this.colorR = Math.max(0, Math.min(255, r));
            this.colorG = Math.max(0, Math.min(255, g));
            this.colorB = Math.max(0, Math.min(255, b));
            this.colorA = Math.max(0, Math.min(255, a));
        }

        public String getBlockId() {
            return blockId;
        }

        public void setBlockId(String id) {
            this.blockId = id != null ? id : "minecraft:stone";
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean v) {
            this.enabled = v;
        }

        public boolean isDrawFill() {
            return drawFill;
        }

        public void setDrawFill(boolean v) {
            this.drawFill = v;
        }

        public boolean isDrawOutline() {
            return drawOutline;
        }

        public void setDrawOutline(boolean v) {
            this.drawOutline = v;
        }

        public int getColorR() {
            return colorR;
        }

        public void setColorR(int v) {
            this.colorR = Math.max(0, Math.min(255, v));
        }

        public int getColorG() {
            return colorG;
        }

        public void setColorG(int v) {
            this.colorG = Math.max(0, Math.min(255, v));
        }

        public int getColorB() {
            return colorB;
        }

        public void setColorB(int v) {
            this.colorB = Math.max(0, Math.min(255, v));
        }

        public int getColorA() {
            return colorA;
        }

        public void setColorA(int v) {
            this.colorA = Math.max(0, Math.min(255, v));
        }

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

    /** Ore preset toggles and block IDs. */
    public enum OrePreset {
        COAL("coal", "minihud.highlighter.preset.coal", 0x80404040, "minecraft:coal_ore",
                "minecraft:deepslate_coal_ore"),
        IRON("iron", "minihud.highlighter.preset.iron", 0x80A0A0A0, "minecraft:iron_ore",
                "minecraft:deepslate_iron_ore"),
        COPPER("copper", "minihud.highlighter.preset.copper", 0x80B87333, "minecraft:copper_ore",
                "minecraft:deepslate_copper_ore"),
        GOLD("gold", "minihud.highlighter.preset.gold", 0x80FFD700, "minecraft:gold_ore",
                "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore"),
        REDSTONE("redstone", "minihud.highlighter.preset.redstone", 0x80CC0000, "minecraft:redstone_ore",
                "minecraft:deepslate_redstone_ore"),
        EMERALD("emerald", "minihud.highlighter.preset.emerald", 0x8050C878, "minecraft:emerald_ore",
                "minecraft:deepslate_emerald_ore"),
        LAPIS("lapis", "minihud.highlighter.preset.lapis", 0x801E90FF, "minecraft:lapis_ore",
                "minecraft:deepslate_lapis_ore"),
        DIAMOND("diamond", "minihud.highlighter.preset.diamond", 0x804EE2EC, "minecraft:diamond_ore",
                "minecraft:deepslate_diamond_ore"),
        QUARTZ("quartz", "minihud.highlighter.preset.quartz", 0x80E8E8E8, "minecraft:nether_quartz_ore"),
        ANCIENT_DEBRIS("ancient_debris", "minihud.highlighter.preset.ancient_debris", 0x80443A3B,
                "minecraft:ancient_debris");

        public static final ImmutableList<OrePreset> VALUES = ImmutableList.copyOf(values());

        private final String id;
        private final String translationKey;
        private final int defaultColorArgb;
        private final ImmutableList<Identifier> blockIds;
        private ConfigBoolean toggle;
        private ConfigBoolean drawFill;
        private ConfigBoolean drawOutline;
        /** -1 = use default color; otherwise ARGB override. */
        private ConfigInteger colorOverride;

        OrePreset(String id, String translationKey, int defaultColorArgb, String... blockIdStrings) {
            this.id = id;
            this.translationKey = translationKey;
            this.defaultColorArgb = defaultColorArgb;
            ImmutableList.Builder<Identifier> b = ImmutableList.builder();
            for (String s : blockIdStrings)
                b.add(Identifier.parse(s));
            this.blockIds = b.build();
        }

        public String getId() {
            return id;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public int getDefaultColorArgb() {
            return defaultColorArgb;
        }

        public ImmutableList<Identifier> getBlockIds() {
            return blockIds;
        }

        public ConfigBoolean getToggle() {
            return toggle;
        }

        public void setToggle(ConfigBoolean t) {
            this.toggle = t;
        }

        public ConfigBoolean getDrawFill() {
            return drawFill;
        }

        public void setDrawFill(ConfigBoolean v) {
            this.drawFill = v;
        }

        public ConfigBoolean getDrawOutline() {
            return drawOutline;
        }

        public void setDrawOutline(ConfigBoolean v) {
            this.drawOutline = v;
        }

        public ConfigInteger getColorOverride() {
            return colorOverride;
        }

        public void setColorOverride(ConfigInteger v) {
            this.colorOverride = v;
        }

        /** Resolved ARGB: override if set, else default. */
        public int getColorArgb() {
            int v = colorOverride.getIntegerValue();
            return v >= 0 ? v : getDefaultColorArgb();
        }
    }

    static {
        for (OrePreset p : OrePreset.VALUES) {
            p.setToggle(new ConfigBoolean("preset_" + p.getId(), false).apply(KEY));
            p.setDrawFill(new ConfigBoolean("preset_" + p.getId() + "_drawFill", false).apply(KEY));
            p.setDrawOutline(new ConfigBoolean("preset_" + p.getId() + "_drawOutline", true).apply(KEY));
            p.setColorOverride(
                    new ConfigInteger("preset_" + p.getId() + "_color", -1, -1, Integer.MAX_VALUE).apply(KEY));
        }
    }

    public static ImmutableList<IConfigBase> getPresetOptions(OrePreset p) {
        return ImmutableList.of(p.getToggle(), p.getDrawFill(), p.getDrawOutline(), p.getColorOverride());
    }

    public static void loadFromFile() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);
        if (!Files.exists(configFile) || !Files.isReadable(configFile))
            return;
        JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
        if (element == null || !element.isJsonObject())
            return;
        JsonObject root = element.getAsJsonObject();
        ConfigUtils.readConfigBase(root, "Highlighter", OPTIONS);
        for (OrePreset p : OrePreset.VALUES)
            ConfigUtils.readConfigBase(root, "Preset_" + p.getId(), getPresetOptions(p));

        CUSTOM_ENTRIES.clear();
        JsonElement arrEl = root.get("CustomEntries");
        if (arrEl != null && arrEl.isJsonArray()) {
            for (JsonElement el : arrEl.getAsJsonArray()) {
                if (!el.isJsonObject())
                    continue;
                JsonObject o = el.getAsJsonObject();
                String blockId = o.has("blockId") ? o.get("blockId").getAsString() : "minecraft:stone";
                boolean enabled = !o.has("enabled") || o.get("enabled").getAsBoolean();
                boolean drawFill = o.has("drawFill") && o.get("drawFill").getAsBoolean();
                boolean drawOutline = !o.has("drawOutline") || o.get("drawOutline").getAsBoolean();
                int r = o.has("r") ? o.get("r").getAsInt() : 255;
                int g = o.has("g") ? o.get("g").getAsInt() : 165;
                int b = o.has("b") ? o.get("b").getAsInt() : 0;
                int a = o.has("a") ? o.get("a").getAsInt() : 128;
                CUSTOM_ENTRIES.add(new CustomEntry(blockId, enabled, drawFill, drawOutline, r, g, b, a));
            }
        } else if (root.has("customBlockId")) {
            String blockId = root.get("customBlockId").getAsString();
            boolean enabled = !root.has("customEnabled") || root.get("customEnabled").getAsBoolean();
            boolean drawFill = root.has("customDrawFill") && root.get("customDrawFill").getAsBoolean();
            boolean drawOutline = !root.has("customDrawOutline") || root.get("customDrawOutline").getAsBoolean();
            int r = root.has("customColorR") ? root.get("customColorR").getAsInt() : 255;
            int g = root.has("customColorG") ? root.get("customColorG").getAsInt() : 165;
            int b = root.has("customColorB") ? root.get("customColorB").getAsInt() : 0;
            int a = root.has("customColorA") ? root.get("customColorA").getAsInt() : 128;
            CUSTOM_ENTRIES.add(new CustomEntry(blockId, enabled, drawFill, drawOutline, r, g, b, a));
        }
    }

    public static void saveToFile() {
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir))
            FileUtils.createDirectoriesIfMissing(dir);
        if (!Files.isDirectory(dir)) {
            MiniHUD.LOGGER.error("HighlighterConfigs: config directory does not exist");
            return;
        }
        JsonObject root = new JsonObject();
        ConfigUtils.writeConfigBase(root, "Highlighter", OPTIONS);
        for (OrePreset p : OrePreset.VALUES)
            ConfigUtils.writeConfigBase(root, "Preset_" + p.getId(), getPresetOptions(p));
        JsonArray customArr = new JsonArray();
        for (CustomEntry e : CUSTOM_ENTRIES) {
            JsonObject o = new JsonObject();
            o.addProperty("blockId", e.getBlockId());
            o.addProperty("enabled", e.isEnabled());
            o.addProperty("drawFill", e.isDrawFill());
            o.addProperty("drawOutline", e.isDrawOutline());
            o.addProperty("r", e.getColorR());
            o.addProperty("g", e.getColorG());
            o.addProperty("b", e.getColorB());
            o.addProperty("a", e.getColorA());
            customArr.add(o);
        }
        root.add("CustomEntries", customArr);
        JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
    }

    /** All non-air block IDs for "pick from list" GUI. */
    public static Set<Identifier> getAllBlockIds() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(b -> !b.defaultBlockState().isAir())
                .map(b -> BuiltInRegistries.BLOCK.getKey(b))
                .collect(Collectors.toSet());
    }

    @Nullable
    public static Block getBlockFromId(String id) {
        try {
            Identifier rl = Identifier.parse(id);
            return BuiltInRegistries.BLOCK.get(rl).map(h -> h.value()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
