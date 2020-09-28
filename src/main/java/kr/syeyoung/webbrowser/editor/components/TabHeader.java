package kr.syeyoung.webbrowser.editor.components;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import kr.syeyoung.webbrowser.editor.MapClickListener;
import kr.syeyoung.webbrowser.editor.widgets.MapWidgetFontSupportButton;
import kr.syeyoung.webbrowser.util.NanumFont;

public class TabHeader extends MapWidget {
    private Tab tab;

    private MapWidgetButton button = new MapWidgetButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            setText("X");
        }

        @Override
        public void onActivate() {
            tab.delete();
        }
    };

    public TabHeader(Tab tab) {
        this.tab = tab;
        setFocusable(true);
        setEnabled(false);
        setClipParent(true);

        button.setBounds(getWidth() - 20, 0, 20, 20);
        addWidget(button);
    }

    @Override
    public void onBoundsChanged() {
        button.setBounds(getWidth() - 20, 0, 20, 20);
    }

    @Override
    public void onDraw() {
        this.view.fillRectangle(0,0,getWidth(), getHeight(), tab.isActive() ? MapColorPalette.COLOR_GREEN : MapColorPalette.COLOR_WHITE);
        this.view.draw(NanumFont.NormalMapNanumFont, 0, 0, MapColorPalette.COLOR_BLACK, tab.getTitle());
    }

    @Override
    public void onActivate() {
        tab.getMapBrowser().setActivatedTab(tab);
    }
}
