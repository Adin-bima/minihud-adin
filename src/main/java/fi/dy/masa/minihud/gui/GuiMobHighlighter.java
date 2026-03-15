package fi.dy.masa.minihud.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

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
import fi.dy.masa.minihud.config.MobHighlighterConfigs;
import fi.dy.masa.minihud.config.MobHighlighterConfigs.MobEntry;
import fi.dy.masa.minihud.gui.GuiConfigs.ConfigGuiTab;

public class GuiMobHighlighter extends GuiBase {
    private final List<GuiTextFieldGeneric> entityIdFields = new ArrayList<>();

    public GuiMobHighlighter() {
        this.title = StringUtils.translate("minihud.gui.title.mob_highlighter");
    }

    /** Add inline R G B A fields for a mob entry; apply on change. Same layout as Block Highlighter. */
    private int addEntryColorFields(int rowX, int y, MobEntry entry) {
        int fw = 28;
        GuiTextFieldGeneric rF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        rF.setMaxLength(3);
        rF.setValueWrapper(String.valueOf(entry.getColorR()));
        rowX += fw + 2;
        GuiTextFieldGeneric gF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        gF.setMaxLength(3);
        gF.setValueWrapper(String.valueOf(entry.getColorG()));
        rowX += fw + 2;
        GuiTextFieldGeneric bF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        bF.setMaxLength(3);
        bF.setValueWrapper(String.valueOf(entry.getColorB()));
        rowX += fw + 2;
        GuiTextFieldGeneric aF = new GuiTextFieldGeneric(rowX, y, fw, 16, Minecraft.getInstance().font);
        aF.setMaxLength(3);
        aF.setValueWrapper(String.valueOf(entry.getColorA()));
        rowX += fw + 2;
        this.addTextField(rF, (tf) -> { applyEntryColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(gF, (tf) -> { applyEntryColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(bF, (tf) -> { applyEntryColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addTextField(aF, (tf) -> { applyEntryColorFromFields(entry, rF, gF, bF, aF); return true; }, TextFieldType.INTEGER);
        this.addWidget(new WidgetLiveColorSwatch(rowX, y - 1, 18, 18, rF, gF, bF, aF));
        return rowX + 23;
    }

    private void applyEntryColorFromFields(MobEntry entry, GuiTextFieldGeneric rF, GuiTextFieldGeneric gF,
            GuiTextFieldGeneric bF, GuiTextFieldGeneric aF) {
        try {
            entry.setColorR(Math.max(0, Math.min(255, Integer.parseInt(rF.getValueWrapper(), 10))));
            entry.setColorG(Math.max(0, Math.min(255, Integer.parseInt(gF.getValueWrapper(), 10))));
            entry.setColorB(Math.max(0, Math.min(255, Integer.parseInt(bF.getValueWrapper(), 10))));
            entry.setColorA(Math.max(0, Math.min(255, Integer.parseInt(aF.getValueWrapper(), 10))));
            fi.dy.masa.minihud.renderer.OverlayRendererMobHighlighter.INSTANCE.invalidate();
        } catch (NumberFormatException ignored) { }
    }

    @Override
    public void initGui() {
        GuiConfigs.tab = ConfigGuiTab.MOB_HIGHLIGHTER;
        super.initGui();
        this.clearWidgets();
        this.clearButtons();
        entityIdFields.clear();

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
                MobHighlighterConfigs.ENABLED.getBooleanValue());
        this.addButton(enabledBtn, (b, mb) -> {
            MobHighlighterConfigs.ENABLED.setBooleanValue(!MobHighlighterConfigs.ENABLED.getBooleanValue());
            ((ButtonOnOff) b).updateDisplayString(MobHighlighterConfigs.ENABLED.getBooleanValue());
            fi.dy.masa.minihud.renderer.OverlayRendererMobHighlighter.INSTANCE.invalidate();
        });
        y += 22;

        // Render radius (distance in blocks)
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.gui.mob_highlighter.distance"));
        GuiTextFieldGeneric distanceField = new GuiTextFieldGeneric(x + 120, y - 2, 48, 16,
                Minecraft.getInstance().font);
        distanceField.setMaxLength(3);
        distanceField.setValueWrapper(String.valueOf(MobHighlighterConfigs.HIGHLIGHT_DISTANCE.getIntegerValue()));
        this.addTextField(distanceField, (tf) -> {
            try {
                int v = Integer.parseInt(tf.getValueWrapper(), 10);
                v = Math.max(1, Math.min(128, v));
                MobHighlighterConfigs.HIGHLIGHT_DISTANCE.setIntegerValue(v);
                fi.dy.masa.minihud.renderer.OverlayRendererMobHighlighter.INSTANCE.invalidate();
            } catch (NumberFormatException ignored) {
            }
            return true;
        }, TextFieldType.INTEGER);
        y += 22;

        this.addLabel(x, y, -1, 12, 0xFFA0A0A0, StringUtils.translate("minihud.gui.mob_highlighter.uses_line_width"));
        y += 18;

        // Entries: same layout as Block Highlighter (left-aligned) — toggle + id + Pick + swatch + R G B A + Remove
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.gui.mob_highlighter.entries"));
        y += 12;
        List<MobEntry> entries = MobHighlighterConfigs.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            final int idx = i;
            MobEntry e = entries.get(i);
            int rowX = x;
            ButtonOnOff onOff = new ButtonOnOff(rowX, y, 48, false, "minihud.highlighter.gui.on_off", e.isEnabled());
            this.addButton(onOff, (b, mb) -> {
                e.setEnabled(!e.isEnabled());
                ((ButtonOnOff) b).updateDisplayString(e.isEnabled());
                fi.dy.masa.minihud.renderer.OverlayRendererMobHighlighter.INSTANCE.invalidate();
            });
            rowX += 53;
            GuiTextFieldGeneric entityIdField = new GuiTextFieldGeneric(rowX, y, 180, 17, Minecraft.getInstance().font);
            entityIdField.setMaxLength(256);
            entityIdField.setValueWrapper(e.getEntityId());
            this.addTextField(entityIdField, (tf) -> true, TextFieldType.STRING);
            entityIdFields.add(entityIdField);
            rowX += 185;
            ButtonGeneric pickBtn = new ButtonGeneric(rowX, y - 1, 60, 20,
                    StringUtils.translate("minihud.gui.mob_highlighter.pick_entity"));
            this.addButton(pickBtn, (b, mb) -> GuiBase.openGui(new GuiMobHighlighterEntityList(idx)));
            rowX += 65;
            rowX = addEntryColorFields(rowX, y, e);
            ButtonGeneric removeBtn = new ButtonGeneric(rowX, y, 50, 18,
                    StringUtils.translate("minihud.highlighter.gui.remove"));
            this.addButton(removeBtn, (b, mb) -> {
                MobHighlighterConfigs.removeEntry(idx);
                GuiBase.openGui(new GuiMobHighlighter());
            });
            y += 20;
        }

        ButtonGeneric addBtn = new ButtonGeneric(x, y, 140, 20,
                StringUtils.translate("minihud.gui.mob_highlighter.add_mob"));
        this.addButton(addBtn, (b, mb) -> {
            MobHighlighterConfigs.addEntry();
            GuiBase.openGui(new GuiMobHighlighterEntityList(MobHighlighterConfigs.getEntries().size() - 1));
        });
        y += 28;

        // Done / Cancel
        int btnY = 28;
        ButtonGeneric doneBtn = new ButtonGeneric(this.getScreenWidth() - 180, btnY, 80, 20,
                StringUtils.translate("minihud.highlighter.gui.done"));
        this.addButton(doneBtn, (b, mb) -> {
            applyFromGui(distanceField);
            MobHighlighterConfigs.saveToFile();
            GuiBase.openGui(new GuiConfigs());
        });
        ButtonGeneric cancelBtn = new ButtonGeneric(this.getScreenWidth() - 88, btnY, 80, 20,
                StringUtils.translate("minihud.highlighter.gui.cancel"));
        this.addButton(cancelBtn, (b, mb) -> GuiBase.openGui(new GuiConfigs()));
    }

    private void applyFromGui(GuiTextFieldGeneric distanceField) {
        try {
            int d = Integer.parseInt(distanceField.getValueWrapper(), 10);
            MobHighlighterConfigs.HIGHLIGHT_DISTANCE.setIntegerValue(Math.max(1, Math.min(128, d)));
        } catch (NumberFormatException ignored) {
        }
        List<MobEntry> entries = MobHighlighterConfigs.getEntries();
        for (int i = 0; i < entries.size() && i < entityIdFields.size(); i++) {
            String id = entityIdFields.get(i).getValueWrapper();
            if (id != null && !id.isEmpty())
                entries.get(i).setEntityId(id);
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
            Color4f c = new Color4f(r, g, b, a);
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
