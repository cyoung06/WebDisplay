package kr.syeyoung.webbrowser.editor.popup;

import com.bergerkiller.bukkit.common.events.map.MapKeyEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapPlayerInput;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import lombok.Getter;
import org.cef.callback.CefJSDialogCallback;

public abstract class Popup extends MapWidget {

    protected CefJSDialogCallback callback;
    @Getter
    private boolean closed = false;
    public Popup(CefJSDialogCallback callback) {
        setFocusable(true);
        setSize(100,100);
        this.callback = callback;
    }

    @Override
    public void onAttached() {
        super.onAttached();
        this.activate();

        mapWidgetButton.setBounds(getWidth()-24, 0, 24, 24);
        mapWidgetButton.setText("X");
        setDepthOffset(100);
        addWidget(mapWidgetButton);
    }

    private MapWidgetButton mapWidgetButton = new MapWidgetButton() {
        @Override
        public void onActivate() {
            Popup.this.close();
        }
    };

    @Override
    public void onDraw() {
        super.onDraw();
        this.view.fillRectangle(0,0, getWidth(),getHeight(), MapColorPalette.COLOR_WHITE);
        this.view.drawRectangle(0,0,getWidth(),getHeight(), MapColorPalette.COLOR_BLUE);

    }

    public void close() {
        removeWidget();
        onPopupClosed();
        closed = true;
    }

    @Override
    public void onKeyPressed(MapKeyEvent event) {
        if (event.getKey() == MapPlayerInput.Key.BACK) {
            close();
        } else {
            super.onKeyPressed(event);
        }
    }

    public abstract void onPopupClosed();
}
