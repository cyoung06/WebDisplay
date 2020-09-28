package kr.syeyoung.webbrowser.editor.components;

import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetText;
import kr.syeyoung.webbrowser.editor.Keyboard;
import kr.syeyoung.webbrowser.editor.MapBrowser;
import kr.syeyoung.webbrowser.editor.widgets.MapWidgetColoredText;
import kr.syeyoung.webbrowser.editor.widgets.MapWidgetFontSupportButton;
import kr.syeyoung.webbrowser.editor.widgets.MapWidgetTextField;
import kr.syeyoung.webbrowser.util.NanumFont;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserOsr;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AddressBar extends MapWidget {
    private MapWidgetButton backButton_ = new MapWidgetFontSupportButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText("BACK");
            this.setFont(NanumFont.BigMapNanumFont);
        }

        @Override
        public void onActivate() {
            browser_.goBack();
        }
    };
    private MapWidgetButton forwardButton_ = new MapWidgetFontSupportButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText("FORWARD");
            this.setFont(NanumFont.BigMapNanumFont);
        }
        @Override
        public void onActivate() {
            browser_.goForward();
        }
    };
    private MapWidgetButton reloadButton_ = new MapWidgetFontSupportButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText("REFRESH");
            this.setFont(NanumFont.BigMapNanumFont);
        }
        @Override
        public void onActivate() {
            if (reloadButton_.getText().equalsIgnoreCase("REFRESH")) {
                browser_.reloadIgnoreCache();
            } else {
                browser_.stopLoad();
            }
        }
    };
    private MapWidgetTextField address_field_ = new MapWidgetTextField(false) {
        @Override
        public void onValueUpdated() {
            browser_.loadURL(getAddress());
        }
    };
    private MapWidgetButton minusButton_ = new MapWidgetButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText("-");
        }

        @Override
        public void onActivate() {
            browser_.setZoomLevel(--zoomLevel_);
            zoom_label_.setText(new Double(zoomLevel_).toString());
        }
    };
    private MapWidgetButton plusButton_ = new MapWidgetButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText("+");
        }
        @Override
        public void onActivate() {
            browser_.setZoomLevel(++zoomLevel_);
            zoom_label_.setText(new Double(zoomLevel_).toString());
        }
    };
    private MapWidgetButton keyboard = new MapWidgetFontSupportButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            this.setText("KEYBOARD");
            this.setFont(NanumFont.BigMapNanumFont);
        }
        @Override
        public void onActivate() {
            if (!mapBrowser.getKeyboardMap().containsKey(getLastClicker()))
                mapBrowser.getKeyboardMap().put(getLastClicker(), new Keyboard(mapBrowser,getDisplay().getMapInfo().uuid, getLastClicker()));
            mapBrowser.getKeyboardMap().get(getLastClicker()).sendKeyboard();
        }
    };
    private MapWidgetText zoom_label_ = new MapWidgetText();
    private double zoomLevel_ = 0;
    private CefBrowser browser_;

    private MapBrowser mapBrowser;


    public AddressBar(CefBrowser browser, MapBrowser browser2) {
        setFocusable(true);
        setClipParent(true);
        browser_ = browser;
        this.mapBrowser = browser2;
        setSize(getWidth(), 40);
    }

    @Override
    public void onAttached() {
        addWidget(backButton_); backButton_.setBounds(5,5, 50,30);
        addWidget(forwardButton_); forwardButton_.setBounds(55,5,60,30);
        addWidget(reloadButton_); reloadButton_.setBounds(115, 5, 70, 30);
        addWidget(address_field_); address_field_.setBounds(185, 5, getWidth()-330 , 30);
        addWidget(minusButton_); minusButton_.setBounds(getWidth()-145, 5, 30, 30);
        addWidget(plusButton_); plusButton_.setBounds(getWidth()-85, 5, 30, 30);
        addWidget(zoom_label_); zoom_label_.setBounds(getWidth()-115, 5, 30, 30); zoom_label_.setText(zoomLevel_+""); zoom_label_.setFont(NanumFont.BigMapNanumFont);
        addWidget(keyboard); keyboard.setBounds(getWidth()-55, 5, 50, 30);
        address_field_.setPrompt("Type the url to navigate");
    }

    @Override
    public void onDraw() {
        super.onDraw();
        this.view.fillRectangle(0,0,getWidth(),getHeight(), MapColorPalette.getColor(75,75,75));
        this.view.fillRectangle(getWidth()-115,5,30,30, MapColorPalette.COLOR_WHITE);
    }

    @Override
    public void onDetached() {
        clearWidgets();
    }


    public void update(
            CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
        if (browser == browser_) {
            backButton_.setEnabled(canGoBack);
            forwardButton_.setEnabled(canGoForward);
            reloadButton_.setText(isLoading ? "STOP" : "REFRESH");
        }
    }

    public String getAddress() {
        String address = address_field_.getValue();
        // If the URI format is unknown "new URI" will throw an
        // exception. In this case we interpret the value of the
        // address field as search request. Therefore we simply add
        // the "search" scheme.
        try {
            address = address.replaceAll(" ", "%20");
            URI test = new URI(address);
            if (test.getScheme() != null) return address;
            if (test.getHost() != null && test.getPath() != null) return address;
            String specific = test.getSchemeSpecificPart();
            if (specific.indexOf('.') == -1)
                throw new URISyntaxException(specific, "No dot inside domain");
        } catch (URISyntaxException e1) {
            address = "search://" + address;
        }
        return address;
    }

    public void setAddress(CefBrowser browser, String address) {
        if (browser == browser_) address_field_.setValue(address);
    }

    public MapWidgetTextField getAddressField() {
        return address_field_;
    }
}
