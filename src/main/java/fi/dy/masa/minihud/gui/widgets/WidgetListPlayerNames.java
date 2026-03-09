package fi.dy.masa.minihud.gui.widgets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;

public class WidgetListPlayerNames extends WidgetListBase<String, WidgetPlayerListEntry> {
    private final List<String> playerNames;

    public WidgetListPlayerNames(int x, int y, int width, int height,
            @Nullable ISelectionListener<String> selectionListener, List<String> playerNames) {
        super(x, y, width, height, selectionListener);
        this.browserEntryHeight = 16;
        this.playerNames = playerNames != null ? playerNames : Collections.emptyList();
    }

    @Override
    protected Collection<String> getAllEntries() {
        return playerNames;
    }

    @Override
    protected WidgetPlayerListEntry createListEntryWidget(int x, int y, int listIndex, boolean isOdd, String entry) {
        return new WidgetPlayerListEntry(x, y, this.browserEntryWidth, this.getBrowserEntryHeightFor(entry), isOdd, entry, listIndex, this);
    }
}
