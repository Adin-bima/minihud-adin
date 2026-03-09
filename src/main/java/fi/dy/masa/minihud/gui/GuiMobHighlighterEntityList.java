package fi.dy.masa.minihud.gui;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.wrappers.TextFieldType;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.minihud.config.MobHighlighterConfigs;
import fi.dy.masa.minihud.gui.widgets.WidgetEntityListEntry;
import fi.dy.masa.minihud.gui.widgets.WidgetListEntityIds;

public class GuiMobHighlighterEntityList extends GuiListBase<Identifier, WidgetEntityListEntry, WidgetListEntityIds>
        implements ISelectionListener<Identifier> {

    /** When >= 0, set this entry's entity ID. When -1, add a new entry with chosen entity type. */
    private final int entryIndex;

    public GuiMobHighlighterEntityList() {
        this(-1);
    }

    public GuiMobHighlighterEntityList(int entryIndex) {
        super(10, 56);
        this.entryIndex = entryIndex;
        this.title = StringUtils.translate("minihud.gui.mob_highlighter.pick_entity_title");
    }

    @Override
    protected int getBrowserWidth() {
        return this.getScreenWidth() - 20;
    }

    @Override
    protected int getBrowserHeight() {
        return this.getScreenHeight() - this.getListY() - 6;
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = 10;
        int y = 28;
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.gui.mob_highlighter.search"));
        x += this.getStringWidth(StringUtils.translate("minihud.gui.mob_highlighter.search")) + 6;
        GuiTextFieldGeneric searchField = new GuiTextFieldGeneric(x, y - 2, Math.min(220, this.getScreenWidth() - x - 10), 16, Minecraft.getInstance().font);
        searchField.setMaxLength(128);
        searchField.setValueWrapper("");
        this.addTextField(searchField, (tf) -> {
            this.getListWidget().setFilter(tf.getValueWrapper());
            this.getListWidget().refreshEntries();
            return true;
        }, TextFieldType.STRING);
    }

    @Override
    public void onSelectionChange(@Nullable Identifier entry) {
        if (entry != null) {
            if (entryIndex >= 0 && entryIndex < MobHighlighterConfigs.getEntries().size())
                MobHighlighterConfigs.getEntries().get(entryIndex).setEntityId(entry.toString());
            else
                MobHighlighterConfigs.addEntry().setEntityId(entry.toString());
            GuiBase.openGui(new GuiMobHighlighter());
        }
    }

    @Override
    protected WidgetListEntityIds createListWidget(int listX, int listY) {
        return new WidgetListEntityIds(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), 0, this);
    }
}
