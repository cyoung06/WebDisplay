package kr.syeyoung.webbrowser.editor.widgets;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetSubmitText;
import kr.syeyoung.webbrowser.PluginWebBrowser;
import kr.syeyoung.webbrowser.editor.MapClickListener;
import kr.syeyoung.webbrowser.editor.PromptInputPls;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class MapWidgetTextField extends MapWidget implements MapClickListener {

    private boolean allowColor = false;
    private MapFont font;


    public MapWidgetTextField(boolean color) {
        setFocusable(true);
        this.allowColor = color;
        this.font = MapFont.MINECRAFT;
        setSize(100,16);
        setClipParent(true);
    }

    public void setFont(MapFont font) {
        this.font = font;
    }

    @Getter
    private String value = "";

    public void setValue(String value) {
        this.value = value;
        this.coloredText.setText(value);
        invalidate();;
    }

    MapWidgetSubmitText anvilWidget = new MapWidgetSubmitText() {
        @Override
        public void onAccept(String str) {
            value = allowColor ? ChatColor.translateAlternateColorCodes('&', str) : str;
            coloredText.setText(value);
            MapWidgetTextField.this.setFocusable(true);
            MapWidgetTextField.this.invalidate();
            MapWidgetTextField.this.onValueUpdated();

        }
        public void onCancel() {
            MapWidgetTextField.this.setFocusable(true);
            MapWidgetTextField.this.invalidate();
            MapWidgetTextField.this.onValueUpdated();
        }
    };
    MapWidgetColoredText coloredText = new MapWidgetColoredText();

    @Override
    public void onAttached() {
        super.onAttached();
        clearWidgets();
        anvilWidget.setDescription("enter name");
        coloredText.setBounds(2,2,getWidth() -4, getHeight() -4);
        coloredText.setDefaultColor(ChatColor.BLACK);
        addWidget(anvilWidget);
        addWidget(coloredText);
    }

    @Override
    public void onBoundsChanged() {
        super.onBoundsChanged();
        coloredText.setBounds(2,2,getWidth() -4, getHeight() -4);
    }

    public void onValueUpdated() {

    }
    @Override
    public void onDraw() {
        try {
            this.view.fillRectangle(0, 0, getWidth(), getHeight(), MapColorPalette.COLOR_WHITE);
            byte color = 0;
            if (isFocused()) {
                color = MapColorPalette.getColor(75, 75, 75);
            } else {
                color = MapColorPalette.getColor(150, 150, 150);
            }

            this.view.drawRectangle(0, 0, getWidth(), getHeight(), color);
        } catch (Exception e){}
    }

    private ConversationContext conversationContext;

    @Override
    public void onActivate() {
        setFocusable(false);
        Conversation conversation = new ConversationFactory(PluginWebBrowser.getPlugin(PluginWebBrowser.class)).withModality(true)
                .withEscapeSequence("/quit")
                .withInitialSessionData(new HashMap() {{
                    put("curr", value);
                    put("callback", new Runnable() {
                        @Override
                        public void run() {
                            String str = (String) conversationContext.getSessionData("curr");
                            value = allowColor ? ChatColor.translateAlternateColorCodes('&', str) : str;
                            coloredText.setText(value);
                            MapWidgetTextField.this.setFocusable(true);
                            MapWidgetTextField.this.invalidate();
                            MapWidgetTextField.this.onValueUpdated();
                        }
                    });
                }})
                .withFirstPrompt(new PromptInputPls())
                .buildConversation(lastClicker);
        conversation.begin();
        conversationContext = conversation.getContext();
//                .begin();
//        anvilWidget.activate();
    }

    private Player lastClicker;

    @Override
    public boolean onClick(MapClickEvent event) {
        lastClicker = event.getPlayer();
        return false;
    }
}
