package kr.syeyoung.webbrowser.editor.widgets;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import kr.syeyoung.webbrowser.editor.MapClickListener;
import kr.syeyoung.webbrowser.util.NanumFont;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.awt.*;

public class MapWidgetFontSupportButton extends MapWidgetButton implements MapClickListener {

    @Getter
    @Setter
    private MapFont<Character> font = NanumFont.BigMapNanumFont;

    @Getter
    private Player lastClicker;

    public MapWidgetFontSupportButton() {
        super();
        setClipParent(true);
    }

    public void onDraw() {
        try {
            byte textColor;
            byte textShadowColor;
            if (!this.isEnabled()) {
                textColor = MapColorPalette.getColor(160, 160, 160);
                textShadowColor = 0;
            } else if (this.isFocused()) {
                textColor = MapColorPalette.getColor(255, 255, 160);
                textShadowColor = MapColorPalette.getColor(63, 63, 40);
            } else {
                textColor = MapColorPalette.getColor(224, 224, 224);
                textShadowColor = MapColorPalette.getColor(56, 56, 56);
            }

            if (this.isShowBorder()) {
                fillBackground(this.view.getView(1, 1, this.getWidth() - 2, this.getHeight() - 2), this.isEnabled(), this.isFocused());
                this.view.drawRectangle(0, 0, this.getWidth(), this.getHeight(), (byte) 119);
            } else {
                fillBackground(this.view, this.isEnabled(), this.isFocused());
            }

            int iconX;
            if (!this.getText().isEmpty()) {
                Dimension textSize = this.view.calcFontSize(font, getText());
                iconX = (this.getWidth() - textSize.width) / 2;
                int textY = (this.getHeight() - textSize.height) / 2;
                this.view.setAlignment(MapFont.Alignment.LEFT);
                if (textShadowColor != 0) {
                    this.view.draw(font, iconX + 1, textY + 1, textShadowColor, getText());
                }

                this.view.draw(font, iconX, textY, textColor, getText());
            }

            if (this.getIcon() != null) {
                int iconY = (this.getHeight() - this.getIcon().getHeight()) / 2;
                iconX = this.getWidth() - iconY - this.getIcon().getWidth();
                this.view.draw(this.getIcon(), iconX, iconY);
            }
        } catch (Exception e) {}
    }

    @Override
    public boolean onClick(MapClickEvent event) {
        lastClicker = event.getPlayer();
        return true;
    }
}
