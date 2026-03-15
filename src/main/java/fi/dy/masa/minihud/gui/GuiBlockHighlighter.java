package fi.dy.masa.minihud.gui;

import java.util.ArrayList;
import java.util.List;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ButtonOnOff;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.wrappers.TextFieldType;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import fi.dy.masa.minihud.config.HighlighterConfigs;
import fi.dy.masa.minihud.config.HighlighterConfigs.CustomEntry;
import fi.dy.masa.minihud.config.HighlighterConfigs.OrePreset;
import fi.dy.masa.minihud.gui.GuiConfigs.ConfigGuiTab;
import net.minecraft.client.Minecraft;

public class GuiBlockHighlighter extends GuiBase {
    private final List<GuiTextFieldGeneric> customBlockIdFields = new ArrayList<>();

    public static void setPendingSelectCustomIndex(int index) {
        // No-op; kept for compatibility when returning from block list
    }

    public GuiBlockHighlighter() {
        this.title = StringUtils.translate("minihud.gui.title.block_highlighter");
    }

    private static String presetDisplayName(OrePreset p) {
        return StringUtils.translate(p.getTranslationKey()).replace(": %s", "").trim();
    }

    /** Add inline R G B A fields for a preset; apply on change. */
    private int addPresetColorFields(int rowX, int y, OrePreset preset) {
        int argb = preset.getColorArgb();
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF, a = (argb >> 24) & 0xFF;
        int fw = 28;
        GuiTextFieldGeneric rF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        rF.setMaxLength(3);
        rF.setValueWrapper(String.valueOf(r));
        rowX += fw + 2;
        GuiTextFieldGeneric gF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        gF.setMaxLength(3);
        gF.setValueWrapper(String.valueOf(g));
        rowX += fw + 2;
        GuiTextFieldGeneric bF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        bF.setMaxLength(3);
        bF.setValueWrapper(String.valueOf(b));
        rowX += fw + 2;
        GuiTextFieldGeneric aF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        aF.setMaxLength(3);
        aF.setValueWrapper(String.valueOf(a));
        rowX += fw + 2;
        this.addTextField(rF, (tf) -> { applyPresetColorFromFields(preset, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(gF, (tf) -> { applyPresetColorFromFields(preset, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(bF, (tf) -> { applyPresetColorFromFields(preset, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(aF, (tf) -> { applyPresetColorFromFields(preset, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addWidget(new WidgetLiveColorSwatch(rowX, y - 1, 18, 18, rF, gF, bF, aF));
        return rowX + 23;
    }

    private void applyPresetColorFromFields(OrePreset preset, GuiTextFieldGeneric rF, GuiTextFieldGeneric gF,
            GuiTextFieldGeneric bF, GuiTextFieldGeneric aF) {
        try {
            int rv = Math.max(0, Math.min(255, Integer.parseInt(rF.getValueWrapper(), 10)));
            int gv = Math.max(0, Math.min(255, Integer.parseInt(gF.getValueWrapper(), 10)));
            int bv = Math.max(0, Math.min(255, Integer.parseInt(bF.getValueWrapper(), 10)));
            int av = Math.max(0, Math.min(255, Integer.parseInt(aF.getValueWrapper(), 10)));
            preset.getColorOverride().setIntegerValue((av << 24) | (rv << 16) | (gv << 8) | bv);
            fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
        } catch (NumberFormatException ignored) { }
    }

    /** Add inline R G B A fields for a custom entry; apply on change. */
    private int addCustomColorFields(int rowX, int y, CustomEntry entry) {
        GuiTextFieldGeneric rF = new GuiTextFieldGeneric(rowX, y, 28, 16, Minecraft.getInstance().font);
        rF.setMaxLength(3);
        rF.setValueWrapper(String.valueOf(entry.getColorR()));
        rowX += 30;
        GuiTextFieldGeneric gF = new GuiTextFieldGeneric(rowX, y, 28, 16, Minecraft.getInstance().font);
        gF.setMaxLength(3);
        gF.setValueWrapper(String.valueOf(entry.getColorG()));
        rowX += 30;
        GuiTextFieldGeneric bF = new GuiTextFieldGeneric(rowX, y, 28, 16, Minecraft.getInstance().font);
        bF.setMaxLength(3);
        bF.setValueWrapper(String.valueOf(entry.getColorB()));
        rowX += 30;
        GuiTextFieldGeneric aF = new GuiTextFieldGeneric(rowX, y, 28, 16, Minecraft.getInstance().font);
        aF.setMaxLength(3);
        aF.setValueWrapper(String.valueOf(entry.getColorA()));
        rowX += 30;
        this.addTextField(rF, (tf) -> { applyCustomColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(gF, (tf) -> { applyCustomColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(bF, (tf) -> { applyCustomColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(aF, (tf) -> { applyCustomColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addWidget(new WidgetLiveColorSwatch(rowX, y - 1, 18, 18, rF, gF, bF, aF));
        return rowX + 23;
    }

    private void applyCustomColorFromFields(CustomEntry entry, GuiTextFieldGeneric rF, GuiTextFieldGeneric gF,
            GuiTextFieldGeneric bF, GuiTextFieldGeneric aF) {
        try {
            entry.setColorR(Math.max(0, Math.min(255, Integer.parseInt(rF.getValueWrapper(), 10))));
            entry.setColorG(Math.max(0, Math.min(255, Integer.parseInt(gF.getValueWrapper(), 10))));
            entry.setColorB(Math.max(0, Math.min(255, Integer.parseInt(bF.getValueWrapper(), 10))));
            entry.setColorA(Math.max(0, Math.min(255, Integer.parseInt(aF.getValueWrapper(), 10))));
            fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
        } catch (NumberFormatException ignored) { }
    }

    @Override
    public void initGui() {
        GuiConfigs.tab = ConfigGuiTab.BLOCK_HIGHLIGHTER;
        super.initGui();
        this.clearWidgets();
        this.clearButtons();
        customBlockIdFields.clear();

        int x = 10;
        int y = 26;

        // Tab row
        for (ConfigGuiTab tab : ConfigGuiTab.values()) {
            if (tab == ConfigGuiTab.ALL)
                continue;
            if (tab == ConfigGuiTab.BLOCK_HIGHLIGHTER && !fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.isBlockHighlighterVisible())
                continue;
            int w = this.getStringWidth(tab.getDisplayName()) + 10;
            if (x >= this.getScreenWidth() - w - 10) {
                x = 10;
                y += 22;
            }
            ButtonGeneric btn = new ButtonGeneric(x, y, w, 20, tab.getDisplayName());
            btn.setEnabled(GuiConfigs.tab != tab);
            this.addButton(btn, new TabListener(tab));
            x += w + 2;
        }
        y += 22;  // clear tab row (20px) + gap
        x = 10;

        // Master enabled and distance: below tab row, left-aligned under Generic
        ButtonOnOff enabledBtn = new ButtonOnOff(x, y, -1, false, "minihud.highlighter.gui.enabled",
                HighlighterConfigs.ENABLED.getBooleanValue());
        this.addButton(enabledBtn, (b, mb) -> {
            HighlighterConfigs.ENABLED.setBooleanValue(!HighlighterConfigs.ENABLED.getBooleanValue());
            ((ButtonOnOff) b).updateDisplayString(HighlighterConfigs.ENABLED.getBooleanValue());
            fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
        });
        y += 22;

        // Highlight distance (blocks)
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.highlighter.gui.distance"));
        GuiTextFieldGeneric distanceField = new GuiTextFieldGeneric(x + 120, y - 2, 48, 16, Minecraft.getInstance().font);
        distanceField.setMaxLength(3);
        distanceField.setValueWrapper(String.valueOf(HighlighterConfigs.HIGHLIGHT_DISTANCE.getIntegerValue()));
        this.addTextField(distanceField, (tf) -> {
            try {
                int v = Integer.parseInt(tf.getValueWrapper(), 10);
                v = Math.max(1, Math.min(128, v));
                HighlighterConfigs.HIGHLIGHT_DISTANCE.setIntegerValue(v);
                fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
            } catch (NumberFormatException ignored) { }
            return true;
        }, TextFieldType.INTEGER);
        y += 22;

        // ---- Ores: per item = toggle + name + swatch + R G B A + fill + outline ----
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.highlighter.gui.ores_section"));
        y += 12;
        for (OrePreset p : OrePreset.VALUES) {
            final OrePreset preset = p;
            int rowX = x;
            ButtonOnOff onOff = new ButtonOnOff(rowX, y, 48, false, "minihud.highlighter.gui.on_off",
                    preset.getToggle().getBooleanValue());
            this.addButton(onOff, (b, mb) -> {
                preset.getToggle().setBooleanValue(!preset.getToggle().getBooleanValue());
                ((ButtonOnOff) b).updateDisplayString(preset.getToggle().getBooleanValue());
                fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
            });
            rowX += 53;
            this.addLabel(rowX, y + 2, 220, 12, 0xFFFFFFFF, presetDisplayName(preset));
            rowX += 225;
            rowX = addPresetColorFields(rowX, y, preset);
            ButtonOnOff fillBtn = new ButtonOnOff(rowX, y, 72, false, "minihud.highlighter.gui.draw_fill",
                    preset.getDrawFill().getBooleanValue());
            this.addButton(fillBtn, (b, mb) -> {
                preset.getDrawFill().setBooleanValue(!preset.getDrawFill().getBooleanValue());
                ((ButtonOnOff) b).updateDisplayString(preset.getDrawFill().getBooleanValue());
                fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
            });
            rowX += 77;
            ButtonOnOff outlineBtn = new ButtonOnOff(rowX, y, 72, false, "minihud.highlighter.gui.draw_outline",
                    preset.getDrawOutline().getBooleanValue());
            this.addButton(outlineBtn, (b, mb) -> {
                preset.getDrawOutline().setBooleanValue(!preset.getDrawOutline().getBooleanValue());
                ((ButtonOnOff) b).updateDisplayString(preset.getDrawOutline().getBooleanValue());
                fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
            });
            y += 20;
        }
        y += 4;

        // ---- Custom blocks: toggle + block id + Pick + swatch + R G B A + fill + outline + Remove ----
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.highlighter.gui.custom_blocks"));
        y += 12;
        List<CustomEntry> customEntries = HighlighterConfigs.getCustomEntries();
        for (int i = 0; i < customEntries.size(); i++) {
            final int idx = i;
            CustomEntry e = customEntries.get(i);
            int rowX = x;
            ButtonOnOff onOff = new ButtonOnOff(rowX, y, 48, false, "minihud.highlighter.gui.on_off", e.isEnabled());
            this.addButton(onOff, (b, mb) -> {
                e.setEnabled(!e.isEnabled());
                ((ButtonOnOff) b).updateDisplayString(e.isEnabled());
                fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
            });
            rowX += 53;
            GuiTextFieldGeneric blockIdField = new GuiTextFieldGeneric(rowX, y, 180, 17, Minecraft.getInstance().font);
            blockIdField.setMaxLength(256);
            blockIdField.setValueWrapper(e.getBlockId());
            this.addTextField(blockIdField, (tf) -> true, TextFieldType.STRING);
            customBlockIdFields.add(blockIdField);
            rowX += 185;
            ButtonGeneric pickBtn = new ButtonGeneric(rowX, y - 1, 60, 20,
                    StringUtils.translate("minihud.highlighter.gui.pick_block"));
            this.addButton(pickBtn, (b, mb) -> GuiBase.openGui(new GuiBlockHighlighterBlockList(idx)));
            rowX += 65;
            rowX = addCustomColorFields(rowX, y, e);
            ButtonOnOff fillBtn = new ButtonOnOff(rowX, y, 72, false, "minihud.highlighter.gui.draw_fill",
                    e.isDrawFill());
            this.addButton(fillBtn, (b, mb) -> {
                e.setDrawFill(!e.isDrawFill());
                ((ButtonOnOff) b).updateDisplayString(e.isDrawFill());
                fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
            });
            rowX += 77;
            ButtonOnOff outlineBtn = new ButtonOnOff(rowX, y, 72, false, "minihud.highlighter.gui.draw_outline",
                    e.isDrawOutline());
            this.addButton(outlineBtn, (b, mb) -> {
                e.setDrawOutline(!e.isDrawOutline());
                ((ButtonOnOff) b).updateDisplayString(e.isDrawOutline());
                fi.dy.masa.minihud.renderer.OverlayRendererBlockHighlighter.INSTANCE.invalidate();
            });
            rowX += 77;
            ButtonGeneric removeBtn = new ButtonGeneric(rowX, y, 50, 18,
                    StringUtils.translate("minihud.highlighter.gui.remove"));
            this.addButton(removeBtn, (b, mb) -> {
                HighlighterConfigs.removeCustomEntry(idx);
                GuiBase.openGui(new GuiBlockHighlighter());
            });
            y += 20;
        }

        ButtonGeneric addBtn = new ButtonGeneric(x, y, 120, 20,
                StringUtils.translate("minihud.highlighter.gui.add_custom_block"));
        this.addButton(addBtn, (b, mb) -> {
            HighlighterConfigs.addCustomEntry();
            GuiBase.openGui(new GuiBlockHighlighterBlockList(HighlighterConfigs.getCustomEntries().size() - 1));
        });
        y += 28;

        // Done / Cancel
        int btnY = 28;
        ButtonGeneric doneBtn = new ButtonGeneric(this.getScreenWidth() - 180, btnY, 80, 20,
                StringUtils.translate("minihud.highlighter.gui.done"));
        this.addButton(doneBtn, (b, mb) -> {
            applyFromGui(distanceField);
            HighlighterConfigs.saveToFile();
            GuiBase.openGui(new GuiConfigs());
        });
        ButtonGeneric cancelBtn = new ButtonGeneric(this.getScreenWidth() - 88, btnY, 80, 20,
                StringUtils.translate("minihud.highlighter.gui.cancel"));
        this.addButton(cancelBtn, (b, mb) -> GuiBase.openGui(new GuiConfigs()));
    }

    private void applyFromGui(GuiTextFieldGeneric distanceField) {
        try {
            int d = Integer.parseInt(distanceField.getValueWrapper(), 10);
            HighlighterConfigs.HIGHLIGHT_DISTANCE.setIntegerValue(Math.max(1, Math.min(128, d)));
        } catch (NumberFormatException ignored) { }
        List<CustomEntry> entries = HighlighterConfigs.getCustomEntries();
        for (int i = 0; i < entries.size() && i < customBlockIdFields.size(); i++) {
            String id = customBlockIdFields.get(i).getValueWrapper();
            if (id != null && !id.isEmpty())
                entries.get(i).setBlockId(id);
        }
    }

    private static final class TabListener implements IButtonActionListener {
        private final ConfigGuiTab tab;

        TabListener(ConfigGuiTab tab) {
            this.tab = tab;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            GuiConfigs.tab = tab;
            if (tab == ConfigGuiTab.BLOCK_HIGHLIGHTER)
                GuiBase.openGui(new GuiBlockHighlighter());
            else if (tab == ConfigGuiTab.MOB_HIGHLIGHTER)
                GuiBase.openGui(new GuiMobHighlighter());
            else if (tab == ConfigGuiTab.PLAYER_HIGHLIGHTER)
                GuiBase.openGui(new GuiPlayerHighlighter());
            else
                GuiBase.openGui(new GuiConfigs());
        }
    }

    private static class WidgetColorSwatch extends fi.dy.masa.malilib.gui.widgets.WidgetBase {
        private final int colorArgb;

        WidgetColorSwatch(int x, int y, int width, int height, int colorArgb) {
            super(x, y, width, height);
            this.colorArgb = colorArgb;
        }

        @Override
        public void render(fi.dy.masa.malilib.render.GuiContext ctx, int mouseX, int mouseY, boolean selected) {
            RenderUtils.drawRect(ctx, this.x, this.y, this.width, this.height, 0xFF000000);
            float a = ((colorArgb >> 24) & 0xFF) / 255f;
            float r = ((colorArgb >> 16) & 0xFF) / 255f;
            float g = ((colorArgb >> 8) & 0xFF) / 255f;
            float b = (colorArgb & 0xFF) / 255f;
            fi.dy.masa.malilib.util.data.Color4f c = new Color4f(r, g, b, a);
            RenderUtils.drawRect(ctx, this.x + 1, this.y + 1, this.width - 2, this.height - 2, c.intValue);
        }
    }

    /** Swatch that reads R G B A from the four text fields each frame so preview updates as user types. */
    private static class WidgetLiveColorSwatch extends fi.dy.masa.malilib.gui.widgets.WidgetBase {
        private final GuiTextFieldGeneric rF, gF, bF, aF;

        WidgetLiveColorSwatch(int x, int y, int width, int height,
                GuiTextFieldGeneric rF, GuiTextFieldGeneric gF, GuiTextFieldGeneric bF, GuiTextFieldGeneric aF) {
            super(x, y, width, height);
            this.rF = rF;
            this.gF = gF;
            this.bF = bF;
            this.aF = aF;
        }

        private int getArgbFromFields() {
            try {
                int r = Math.max(0, Math.min(255, Integer.parseInt(rF.getValueWrapper(), 10)));
                int g = Math.max(0, Math.min(255, Integer.parseInt(gF.getValueWrapper(), 10)));
                int b = Math.max(0, Math.min(255, Integer.parseInt(bF.getValueWrapper(), 10)));
                int a = Math.max(0, Math.min(255, Integer.parseInt(aF.getValueWrapper(), 10)));
                return (a << 24) | (r << 16) | (g << 8) | b;
            } catch (NumberFormatException e) {
                return 0xFF000000;
            }
        }

        @Override
        public void render(fi.dy.masa.malilib.render.GuiContext ctx, int mouseX, int mouseY, boolean selected) {
            int colorArgb = getArgbFromFields();
            RenderUtils.drawRect(ctx, this.x, this.y, this.width, this.height, 0xFF000000);
            float a = ((colorArgb >> 24) & 0xFF) / 255f;
            float r = ((colorArgb >> 16) & 0xFF) / 255f;
            float g = ((colorArgb >> 8) & 0xFF) / 255f;
            float b = (colorArgb & 0xFF) / 255f;
            Color4f c = new Color4f(r, g, b, a);
            RenderUtils.drawRect(ctx, this.x + 1, this.y + 1, this.width - 2, this.height - 2, c.intValue);
        }
    }
}
