package kr.syeyoung.webbrowser.editor.components;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import kr.syeyoung.webbrowser.editor.MapClickListener;
import kr.syeyoung.webbrowser.editor.widgets.MapWidgetFontSupportButton;
import kr.syeyoung.webbrowser.util.NanumFont;

public class TabHeader extends MapWidget implements MapClickListener {
    private Tab tab;

    private MapWidgetButton button = new MapWidgetButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            setText("X");
            setFocusable(true);
            setClipParent(true);
        }

        @Override
        public void onActivate() {
            tab.delete();
        }
    };

    public TabHeader(Tab tab) {
        this.tab = tab;
        setFocusable(true);
        setClipParent(true);

        button.setBounds(getWidth() - 30, 0, 30, 30);
        addWidget(button);
    }

    @Override
    public void onBoundsChanged() {
        button.setBounds(getWidth() - 30, 0, 30, 30);
    }

    @Override
    public void onDraw() {
        this.view.fillRectangle(0,0,getWidth(), getHeight(), tab.isActive() ? MapColorPalette.COLOR_GREEN : MapColorPalette.COLOR_WHITE);
        this.view.draw(NanumFont.BigMapNanumFont, 5, 5, MapColorPalette.COLOR_BLACK, tab.getTitle());
    }

    @Override
    public boolean onClick(MapClickEvent event) {
        if (!tab.isActive())
        tab.getMapBrowser().setActivatedTab(tab);
        return false;
    }
}
