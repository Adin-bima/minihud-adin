package fi.dy.masa.minihud.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.wrappers.TextFieldType;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.minihud.config.PlayerHighlighterConfigs;
import fi.dy.masa.minihud.gui.widgets.WidgetPlayerListEntry;
import fi.dy.masa.minihud.gui.widgets.WidgetListPlayerNames;

public class GuiPlayerHighlighterPlayerList extends GuiListBase<String, WidgetPlayerListEntry, WidgetListPlayerNames>
        implements ISelectionListener<String> {

    /** When >= 0, set this entry's player name. When -1, add a new entry with chosen name. */
    private final int entryIndex;

    public GuiPlayerHighlighterPlayerList() {
        this(-1);
    }

    public GuiPlayerHighlighterPlayerList(int entryIndex) {
        super(10, 56);
        this.entryIndex = entryIndex;
        this.title = StringUtils.translate("minihud.gui.player_highlighter.pick_player_title");
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
        this.addLabel(x, y, -1, 12, 0xFFFFFFFF, StringUtils.translate("minihud.gui.player_highlighter.add_by_name"));
        x += this.getStringWidth(StringUtils.translate("minihud.gui.player_highlighter.add_by_name")) + 6;
        GuiTextFieldGeneric nameField = new GuiTextFieldGeneric(x, y - 2, Math.min(180, this.getScreenWidth() - x - 80), 16, Minecraft.getInstance().font);
        nameField.setMaxLength(64);
        nameField.setValueWrapper("");
        this.addTextField(nameField, (tf) -> true, TextFieldType.STRING);
        int btnX = x + nameField.getWidth() + 4;
        fi.dy.masa.malilib.gui.button.ButtonGeneric addBtn = new fi.dy.masa.malilib.gui.button.ButtonGeneric(btnX, y - 2, 60, 18, StringUtils.translate("minihud.gui.player_highlighter.add"));
        this.addButton(addBtn, (b, mb) -> {
            String name = nameField.getValueWrapper();
            if (name != null && !name.isBlank()) {
                applyName(name.trim());
            }
        });
    }

    private static List<String> getPlayersInWorld(Minecraft mc) {
        if (mc.level == null || mc.player == null)
            return new ArrayList<>();
        double r = 128;
        AABB box = new AABB(
                mc.player.getX() - r, mc.player.getY() - r, mc.player.getZ() - r,
                mc.player.getX() + r, mc.player.getY() + r, mc.player.getZ() + r);
        return mc.level.getEntitiesOfClass(Player.class, box).stream()
                .filter(p -> p != mc.player)
                .map(p -> p.getName().getString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private void applyName(String name) {
        if (entryIndex >= 0 && entryIndex < PlayerHighlighterConfigs.getEntries().size())
            PlayerHighlighterConfigs.getEntries().get(entryIndex).setPlayerName(name);
        else
            PlayerHighlighterConfigs.addEntry(name);
        fi.dy.masa.minihud.renderer.OverlayRendererPlayerHighlighter.INSTANCE.invalidate();
        GuiBase.openGui(new GuiPlayerHighlighter());
    }

    @Override
    public void onSelectionChange(@Nullable String entry) {
        if (entry != null)
            applyName(entry);
    }

    @Override
    protected WidgetListPlayerNames createListWidget(int listX, int listY) {
        List<String> names = getPlayersInWorld(Minecraft.getInstance());
        return new WidgetListPlayerNames(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), this, names);
    }
}
