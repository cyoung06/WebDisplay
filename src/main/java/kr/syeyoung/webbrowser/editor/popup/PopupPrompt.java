package kr.syeyoung.webbrowser.editor.popup;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetText;
import kr.syeyoung.webbrowser.editor.widgets.MapWidgetFontSupportButton;
import kr.syeyoung.webbrowser.editor.widgets.MapWidgetTextField;
import kr.syeyoung.webbrowser.util.NanumFont;
import org.cef.callback.CefJSDialogCallback;

public class PopupPrompt extends Popup {
    private String message;
    private String defaultMsg;

    public PopupPrompt(CefJSDialogCallback callback, String message, String defaultMsg) {
        super(callback);
        this.message = message;
        this.defaultMsg = defaultMsg;
        setDepthOffset(10);
        setSize(400,180);
    }

    private MapWidgetTextField nameField = new MapWidgetTextField(true);
    private MapWidgetButton okButton = new MapWidgetFontSupportButton() {
        @Override
        public void onActivate() {
            close();
        }
    };

    @Override
    public void onAttached() {
        super.onAttached();

        MapWidgetText text = new MapWidgetText();
        text.setBounds(10,40, getWidth(), 20);
        text.setFont(NanumFont.BigMapNanumFont);
        text.setAlignment(MapFont.Alignment.MIDDLE);
        text.setColor(MapColorPalette.COLOR_BLACK);
        text.setText(message);

        okButton.setBounds(10, getHeight() - 50, getWidth() - 20, 40);
        okButton.setText("확인");

        nameField.setBounds(10, getHeight()-90, getWidth()-20, 30);
        nameField.setValue(defaultMsg);

        addWidget(text);
        addWidget(okButton);
        addWidget(nameField);
    }

    @Override
    public void onPopupClosed() {
        callback.Continue(true, nameField.getValue());
    }
}
