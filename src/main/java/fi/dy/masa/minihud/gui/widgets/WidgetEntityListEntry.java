package fi.dy.masa.minihud.gui.widgets;

import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.resources.Identifier;

public class WidgetEntityListEntry extends WidgetListEntryBase<Identifier> {
    private final Identifier entityId;
    private final boolean isOdd;

    public WidgetEntityListEntry(int x, int y, int width, int height, boolean isOdd,
            Identifier entityId, int listIndex, WidgetListEntityIds parent) {
        super(x, y, width, height, entityId, listIndex);
        this.entityId = entityId;
        this.isOdd = isOdd;
    }

    @Override
    public void render(GuiContext ctx, int mouseX, int mouseY, boolean selected) {
        if (selected || this.isMouseOver(mouseX, mouseY))
            fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, this.x, this.y, this.width, this.height, 0x70FFFFFF);
        else if (this.isOdd)
            fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, this.x, this.y, this.width, this.height, 0x20FFFFFF);
        else
            fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, this.x, this.y, this.width, this.height, 0x50FFFFFF);
        int color = selected ? 0xFFFFFFFF : (this.isOdd ? 0xFFB0B0B0 : 0xFF808080);
        this.drawString(ctx, this.x + 4, this.y + 4, color, this.entityId.toString());
        super.render(ctx, mouseX, mouseY, selected);
    }
}
