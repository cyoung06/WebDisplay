package kr.syeyoung.webbrowser.editor.components;

import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import kr.syeyoung.webbrowser.CefAppCreator;
import kr.syeyoung.webbrowser.cef.*;
import kr.syeyoung.webbrowser.editor.MapBrowser;
import kr.syeyoung.webbrowser.util.DataUri;
import lombok.Getter;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;

public class Tab extends MapWidget {
    @Getter
    private MapBrowser mapBrowser;

    @Getter
    private AddressBar addressBar;
    @Getter
    private StatusBar statusBar;
    @Getter
    private BrowserRenderer renderer;
    @Getter
    private CefBrowser cefBrowser;

    @Getter
    private TabHeader header;

    @Getter
    private String title;
    @Getter
    private boolean active;

    public void setTitle(String title) {
        this.title = title;
        header.invalidate();
    }

    public void setActive(boolean active) {
        this.active = active;
        header.invalidate();
    }

    public Tab(MapBrowser browser) {
        setFocusable(true);
        setClipParent(true);
        setSize(getWidth(), getHeight());

        this.mapBrowser = browser;

        setupBrowser();


        addressBar = new AddressBar(cefBrowser, mapBrowser);
        addressBar.setBounds(0,0,getWidth(),addressBar.getHeight());
        addWidget(addressBar);
        statusBar = new StatusBar();
        statusBar.setBounds(0,getHeight()-statusBar.getHeight(),getWidth(),statusBar.getHeight());
        addWidget(statusBar);
        renderer = new BrowserRenderer(cefBrowser);
        renderer.setBounds(0, addressBar.getHeight(), getWidth(), getHeight() - statusBar.getHeight() - addressBar.getHeight());
        addWidget(renderer);
        cefBrowser.createImmediately();
    }

    @Override
    public void onBoundsChanged() {
        addressBar.setBounds(0,0,getWidth(),addressBar.getHeight());
        statusBar.setBounds(0,getHeight()-statusBar.getHeight(),getWidth(),statusBar.getHeight());
        renderer.setBounds(0, addressBar.getHeight(), getWidth(), getHeight() - statusBar.getHeight() - addressBar.getHeight());
    }

    public void setupBrowser() {
        if (cefBrowser != null) return;

        // Create the browser.
        CefBrowser browser = mapBrowser.getCefClient().createBrowser(
                "http://www.google.com", true, false, null);
        this.cefBrowser = browser;
    }


    public void delete() {
        mapBrowser.closeTab(this);
        cefBrowser.doClose();
    }
}
