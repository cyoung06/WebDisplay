package kr.syeyoung.webbrowser.editor.widgets;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapEventPropagation;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import lombok.Getter;
import lombok.Setter;

public class MapWidgetScrollBar extends MapWidget {
    @Getter
    private int min = 0;
    @Getter
    private int max = 1;
    @Getter
    @Setter
    private int thumbSize = 1;
    @Getter
    private double currentValue = 0;

    public void setMin(int min) {
        this.min = min;
        this.currentValue = Math.max(currentValue, min);
        invalidate();
    }

    public void setMax(int max) {
        this.max = max;
        this.currentValue = Math.min(currentValue, max - thumbSize);
        invalidate();
    }

    @Getter
    @Setter
    private ScrollBarDirection direction = ScrollBarDirection.VERTICAL;
    @Getter
    @Setter
    private boolean buttons = true;

    private boolean dragStart = false;
    private double startPos = 0;
    private long lastDragTime;

    public MapWidgetScrollBar() {
        setFocusable(true);
        setClipParent(true);
    }

    private MapWidgetButton upButton = new MapWidgetFontSupportButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText(direction == ScrollBarDirection.VERTICAL ? "▲" : "◀");
        }

        @Override
        public void onActivate() {
            currentValue = Math.max(min, currentValue - 1);
            onUpdate();
            MapWidgetScrollBar.this.invalidate();
        }
    };
    private MapWidgetButton downButton = new MapWidgetFontSupportButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText(direction == ScrollBarDirection.VERTICAL ? "▼" : "▶");
        }

        @Override
        public void onActivate() {
            currentValue = Math.min(max - thumbSize, currentValue + 1);
            onUpdate();
            MapWidgetScrollBar.this.invalidate();
        }
    };

    @Override
    public void onAttached() {
        super.onAttached();

        int depth = direction == ScrollBarDirection.VERTICAL ? getWidth() : getHeight();

        if (buttons) {
            upButton.setBounds(0,0, depth, depth);
            addWidget(upButton);
            downButton.setBounds(direction == ScrollBarDirection.VERTICAL ? 0 : getWidth() - depth,
                                direction == ScrollBarDirection.HORIZONTAL ? 0 : getHeight() - depth, depth, depth);
            addWidget(downButton);
        }
    }

    public void onUpdate() {

    }

    @Override
    public void onDraw() {
        this.view.fillRectangle(0,0,getWidth(),getHeight(), MapColorPalette.COLOR_WHITE);

        byte color;
        if (isFocused()) {
            color = MapColorPalette.getColor(75,75,75);
        } else {
            color = MapColorPalette.getColor(150,150,150);
        }

        int depth = direction == ScrollBarDirection.VERTICAL ? getWidth() : getHeight();
        int buttonDepths = upButton.getWidth() + downButton.getWidth();

        if (this.direction == ScrollBarDirection.VERTICAL) {
            int thePos = (int) ((currentValue - min) * (getHeight() - buttonDepths) / (max - min));
            this.view.fillRectangle(0,thePos + depth, depth,
                    (int) Math.min(getHeight() - buttonDepths - thePos, thumbSize * (getHeight() - buttonDepths) / (max - min)), color);
        } else {
            int thePos = (int) ((currentValue - min) * (getWidth() - buttonDepths) / (max - min));
            this.view.fillRectangle(thePos + depth,0,
                    (int) Math.min(getWidth() - buttonDepths - thePos, thumbSize * (getWidth() - buttonDepths) / (max - min)), depth, color);
        }

        this.view.drawRectangle(0,0,getWidth(),getHeight(), MapColorPalette.getColor(75,75,75));
    }


    private void onClick(MapClickEvent event) {
        if ((lastDragTime + 500) < System.currentTimeMillis()) {
            dragStart = false;
            startPos = 0;
        }
        if (!dragStart) {
            int x = getAbsoluteX(), y = getAbsoluteY();
            if (!(event.getX() > x && event.getY() > y && event.getX() < (x + getWidth()) && event.getY() < (y + getHeight()))) return;

            int length = direction == ScrollBarDirection.VERTICAL ? getHeight() : getWidth();
            int depth = upButton.getWidth() + downButton.getWidth();
            int relX = event.getX() - x, relY = event.getY() - y;
            int relPos = direction == ScrollBarDirection.VERTICAL ? relY : relX;

            if (relPos <= depth || relPos >= (length - depth)) return; // BUTTONS

            int relrelPos = relPos - upButton.getWidth();
            int relLen = length - depth;
            startPos = (relrelPos* (max - min) / relLen) + min;
            dragStart = true;
            lastDragTime = System.currentTimeMillis();
            sendStatusChange(MapEventPropagation.UPSTREAM, "PRIORITIZE", new Object[] {this, lastDragTime + 500});
        } else {
            int x = getAbsoluteX(), y = getAbsoluteY();

            int length = direction == ScrollBarDirection.VERTICAL ? getHeight() : getWidth();
            int depth = upButton.getWidth() + downButton.getWidth();
            int relX = event.getX() - x, relY = event.getY() - y;
            int relPos = direction == ScrollBarDirection.VERTICAL ? relY : relX;
            int relrelPos = relPos - upButton.getWidth();
            int relLen = length - depth;

            double currentPos = (relrelPos* (max - min) / relLen) + min;
            lastDragTime = System.currentTimeMillis();
            currentValue = Math.max(Math.min(currentPos - startPos + currentValue, max - thumbSize), min);
            startPos = currentPos;
            MapWidgetScrollBar.this.invalidate();
            onUpdate();
            sendStatusChange(MapEventPropagation.DOWNSTREAM, "PRIORITIZE", new Object[] {this, lastDragTime + 500});
        }
    }

    public static enum ScrollBarDirection {
        HORIZONTAL, VERTICAL
    }
    @Override
    public void onLeftClick(MapClickEvent event) {

        super.onLeftClick(event);
        if (event.getPlayer().isOp())
            onClick(event);
    }

    @Override
    public void onRightClick(MapClickEvent event) {
        super.onRightClick(event);
        if (event.getPlayer().isOp())
            onClick(event);
    }

    @Override
    public void onActivate() {}
}
