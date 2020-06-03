package kr.syeyoung.webbrowser.editor.components;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetText;
import kr.syeyoung.webbrowser.util.NanumFont;
import org.cef.browser.CefBrowser;

public class StatusBar  extends MapWidget {
    private MapWidgetText status_field_ = new MapWidgetText();
    private boolean progressing = false;
    public StatusBar() {
        setFocusable(true);
        setClipParent(true);
        setSize(getWidth(), 30);
    }

    @Override
    public void onAttached() {
        status_field_.setBounds(127,5,getWidth()-127,20);
        status_field_.setFont(NanumFont.BigMapNanumFont);
        status_field_.setColor(MapColorPalette.COLOR_WHITE);
        addWidget(status_field_);
    }

    public void setIsInProgress(boolean inProgress) {
        progressing = inProgress;
        xPos = -50;
        if (!inProgress) invalidate();
    }

    public void setStatusText(String text) {
        status_field_.setText(text);
    }
    private int xPos = -50;
    public void onDraw() {
        this.view.fillRectangle(0,0,getWidth(),getHeight(),MapColorPalette.getColor(75,75,75));
        this.view.drawRectangle(4,4,112,22,MapColorPalette.COLOR_BLACK);
    }
    @Override
    public void onTick() {
        super.onTick();
        if (this.progressing) {
            int width = 50;
            if (xPos < 0) width = xPos + 50;
            if (xPos > 65) width = 110 - xPos;

            this.view.fillRectangle(Math.max(5 + xPos, 5),5,width,20, MapColorPalette.COLOR_BLUE);
            xPos++;
            if (xPos == 110) xPos = -50;
        }
    }
}
