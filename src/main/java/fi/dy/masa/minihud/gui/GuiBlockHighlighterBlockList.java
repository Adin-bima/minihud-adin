package fi.dy.masa.minihud.gui;

import javax.annotation.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.wrappers.TextFieldType;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.minihud.config.HighlighterConfigs;
import fi.dy.masa.minihud.gui.widgets.WidgetBlockListEntry;
import fi.dy.masa.minihud.gui.widgets.WidgetListBlockIds;

public class GuiBlockHighlighterBlockList extends GuiListBase<Identifier, WidgetBlockListEntry, WidgetListBlockIds>
        implements ISelectionListener<Identifier> {
    /**
     * When >= 0, set this custom entry's block ID. When -1, add a new entry with
     * chosen block.
     */
    private final int customEntryIndex;

    public GuiBlockHighlighterBlockList() {
        this(-1);
    }

    public GuiBlockHighlighterBlockList(int customEntryIndex) {
        super(10, 56);
        this.customEntryIndex = customEntryIndex;
        this.title = StringUtils.translate("minihud.highlighter.gui.pick_block_title");
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
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.highlighter.gui.search"));
        x += this.getStringWidth(StringUtils.translate("minihud.highlighter.gui.search")) + 6;
        GuiTextFieldGeneric searchField = new GuiTextFieldGeneric(x, y - 2,
                Math.min(220, this.getScreenWidth() - x - 10), 16, Minecraft.getInstance().font);
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
            if (customEntryIndex >= 0 && customEntryIndex < HighlighterConfigs.getCustomEntries().size())
                HighlighterConfigs.getCustomEntries().get(customEntryIndex).setBlockId(entry.toString());
            else
                HighlighterConfigs.addCustomEntry().setBlockId(entry.toString());
            GuiBase.openGui(new GuiBlockHighlighter());
        }
    }

    @Override
    protected WidgetListBlockIds createListWidget(int listX, int listY) {
        return new WidgetListBlockIds(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), 0, this);
    }
}
