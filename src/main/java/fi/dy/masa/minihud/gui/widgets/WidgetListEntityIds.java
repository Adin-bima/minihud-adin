package fi.dy.masa.minihud.gui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.resources.Identifier;

import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.minihud.config.MobHighlighterConfigs;

public class WidgetListEntityIds extends WidgetListBase<Identifier, WidgetEntityListEntry> {
    private String filter = "";

    public WidgetListEntityIds(int x, int y, int width, int height, float zLevel,
            @Nullable ISelectionListener<Identifier> selectionListener) {
        super(x, y, width, height, selectionListener);
        this.browserEntryHeight = 16;
    }

    public void setFilter(String s) {
        this.filter = s == null ? "" : s.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public String getFilter() {
        return this.filter;
    }

    @Override
    protected Collection<Identifier> getAllEntries() {
        Set<Identifier> ids = MobHighlighterConfigs.getAllEntityTypeIds();
        List<Identifier> list = new ArrayList<>(ids);
        list.sort(Comparator.comparing(Identifier::toString));
        if (!this.filter.isEmpty()) {
            list = list.stream()
                    .filter(id -> id.toString().toLowerCase(java.util.Locale.ROOT).contains(this.filter))
                    .collect(Collectors.toList());
        }
        return list;
    }

    @Override
    protected WidgetEntityListEntry createListEntryWidget(int x, int y, int listIndex, boolean isOdd, Identifier entry) {
        return new WidgetEntityListEntry(x, y, this.browserEntryWidth, this.getBrowserEntryHeightFor(entry), isOdd, entry, listIndex, this);
    }
}
