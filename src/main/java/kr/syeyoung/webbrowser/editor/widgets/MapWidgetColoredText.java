package kr.syeyoung.webbrowser.editor.widgets;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import kr.syeyoung.webbrowser.util.ColorHelper;
import kr.syeyoung.webbrowser.util.NanumFont;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import java.awt.*;

public class MapWidgetColoredText extends MapWidget {
    @Getter
    private boolean autoSize = false;
    @Getter
    private MapFont font;
    @Getter
    private MapFont.Alignment alignment;
    @Getter
    private String text;
    @Getter
    private float fontSize;
    @Getter
    private ChatColor defaultColor = ChatColor.WHITE;

    public void setAlignment(MapFont.Alignment alignment) {
    }

    public void setText(String text) {
        this.text = text;
        calcAutoSize();
        invalidate();
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
        calcAutoSize();
        invalidate();
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        this.font = NanumFont.getNanumFont(fontSize, 0);
        calcAutoSize();
        invalidate();
    }

    public void setDefaultColor(ChatColor color) {
        this.defaultColor = color;
        invalidate();
    }

    public MapWidgetColoredText() {
        this.font = NanumFont.getNanumFont(12.0f, 0);
        this.alignment = MapFont.Alignment.LEFT;
        this.autoSize = false;
        this.defaultColor = ChatColor.WHITE;
        this.text = "";
        this.fontSize = 12.0f;
        setSize(100, 20);
        setClipParent(true);
    }

    @Getter
    private int width_text = 0;

    @Getter
    private int height_text = 0;

    @Override
    public void onDraw() {
        BaseComponent[] baseComponents = TextComponent.fromLegacyText("§r"+this.text, this.defaultColor.asBungee());
        int x = 0;
        int y = 0;
        width_text = height_text = 0;
        int biggestHeight = 0;

        try {
            for (BaseComponent baseComponent : baseComponents) {
                TextComponent textComponent = (TextComponent) baseComponent;
                if (textComponent.getText().equals("\n")) {
                    y += biggestHeight;
                    x = 0;
                    biggestHeight = 0;
                    if (y > height_text)
                        height_text = y;
                    continue;
                }

                byte color = MapColorPalette.getColor(ColorHelper.chatColorToColor(textComponent.getColor()));
                MapFont font = NanumFont.getNanumFont(fontSize, (textComponent.isBold() ? Font.BOLD : 0)
                        | (textComponent.isItalic() ? Font.ITALIC : 0));
                Dimension dimension = this.view.calcFontSize(font, textComponent.getText());
                if (dimension.height > biggestHeight)
                    biggestHeight = dimension.height;
                this.view.draw(font, x, y, color, textComponent.getText());
                if (textComponent.isUnderlined()) {
                    this.view.drawLine(x, dimension.height - 2 + y, dimension.width + x, dimension.height - 2, color);
                }
                if (textComponent.isStrikethrough()) {
                    this.view.drawLine(x, dimension.height / 2 + y, dimension.width + x, dimension.height / 2, color);
                }

                x += dimension.width;
                if (x > width_text)
                    width_text = x;
            }
        } catch (Exception e) {
            System.out.println("Error while writing "+text);
            e.printStackTrace();
        }
    }

    public void onAttached() {
        if (this.autoSize) {
            this.calcAutoSize();
        }
        invalidate();
    }

    private void calcAutoSize() {
        if (this.autoSize && this.view != null) {
            if (this.text != null && !this.text.isEmpty()) {
                this.onDraw();
                this.setSize(width_text, height_text);
            } else {
                this.setSize(0, 0);
            }
        }
    }

}
