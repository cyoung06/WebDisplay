package kr.syeyoung.webbrowser.editor;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.events.map.MapStatusEvent;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapSessionMode;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import kr.syeyoung.webbrowser.PluginWebBrowser;
import kr.syeyoung.webbrowser.cef.*;
import kr.syeyoung.webbrowser.editor.components.AddressBar;
import kr.syeyoung.webbrowser.editor.components.BrowserRenderer;
import kr.syeyoung.webbrowser.editor.components.StatusBar;
import kr.syeyoung.webbrowser.util.DataUri;
import lombok.Getter;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapBrowser extends MapDisplay {
    @Getter
    private AddressBar addressBar;
    private StatusBar statusBar;
    private BrowserRenderer renderer;

    private CefClient cefClient;
    private String errorMsg_ = "";
    @Getter
    private CefBrowser cefBrowser;


    @Override
    public void onAttached() {
        clearWidgets();
        setSessionMode(MapSessionMode.FOREVER);
        setGlobal(true);

        setupBrowser();

        addressBar = new AddressBar(cefBrowser);
        addressBar.setBounds(0,0,getWidth(),addressBar.getHeight());
        addWidget(addressBar);
        statusBar = new StatusBar();
        statusBar.setBounds(0,getHeight()-statusBar.getHeight(),getWidth(),statusBar.getHeight());
        addWidget(statusBar);
        renderer = new BrowserRenderer(cefBrowser);
        renderer.setBounds(0, addressBar.getHeight(), getWidth(), getHeight() - statusBar.getHeight() - addressBar.getHeight());
        addWidget(renderer);
        cefBrowser.createImmediately();

        // Focus code
//        testFrame.setLayout(new BorderLayout());
//        testFrame.add(cefBrowser.getUIComponent());
//        testFrame.setVisible(true);
    }

    public void setupBrowser() {
        if (cefBrowser != null && cefClient != null) return;

        cefClient = PluginWebBrowser.getPlugin(PluginWebBrowser.class).getCefApp().createClient();
        cefClient.addContextMenuHandler(new ContextMenuHandler(null));
        cefClient.addDragHandler(new DragHandler());
        cefClient.addJSDialogHandler(new JSDialogHandler());
        cefClient.addKeyboardHandler(new KeyboardHandler());
        cefClient.addRequestHandler(new RequestHandler(null));

        CefMessageRouter msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new MessageRouterHandler(), true);
        msgRouter.addHandler(new MessageRouterHandlerEx(cefClient), false);
        cefClient.addMessageRouter(msgRouter);

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                addressBar.setAddress(browser, url);
            }
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
//                setTitle(title);
            }
            @Override
            public void onStatusMessage(CefBrowser browser, String value) {
                statusBar.setStatusText(value);
            }
        });
        cefClient.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                                             boolean canGoBack, boolean canGoForward) {
                addressBar.update(browser, isLoading, canGoBack, canGoForward);
                statusBar.setIsInProgress(isLoading);
//
                if (!isLoading && !errorMsg_.isEmpty()) {
                    browser.loadURL(DataUri.create("text/html", errorMsg_));
                    errorMsg_ = "";
                }
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode,
                                    String errorText, String failedUrl) {
                if (errorCode != ErrorCode.ERR_NONE && errorCode != ErrorCode.ERR_ABORTED) {
                    errorMsg_ = "<html><head>";
                    errorMsg_ += "<title>Error while loading</title>";
                    errorMsg_ += "</head><body>";
                    errorMsg_ += "<h1>" + errorCode + "</h1>";
                    errorMsg_ += "<h3>Failed to load " + failedUrl + "</h3>";
                    errorMsg_ += "<p>" + (errorText == null ? "" : errorText) + "</p>";
                    errorMsg_ += "</body></html>";
                    browser.stopLoad();
                }
            }
        });
        // Create the browser.
        CefBrowser browser = cefClient.createBrowser(
                "http://www.google.com", true, false, null);
        this.cefBrowser = browser;
    }


    @Override
    public void onLeftClick(MapClickEvent event) {
        onClick(event);
    }

    @Override
    public void onRightClick(MapClickEvent event) {
        onClick(event);
    }

    private void onClick(MapClickEvent event) {
        event.setCancelled(true);
        if (!event.getPlayer().isOp()) {
            return;
        }

        try {
            if ((lastTouch + 75) > System.currentTimeMillis()) {
                return;
            }
            lastTouch = System.currentTimeMillis();
            if (prioritized != null && System.currentTimeMillis() < priortizedBefore) {

                if (prioritized instanceof MapClickListener) {
                    ((MapClickListener) prioritized).onClick(event);
                }

                if (!prioritized.isFocused()) {
                    prioritized.focus();
                } else {
                    prioritized.activate();
                }

                return;
            }

            int x = event.getX();
            int y = event.getY();
            MapWidget theWidget = null;
            for (MapWidget widget : this.getWidgets()) {
                theWidget = findSpecificWidgetWithin(widget, x, y);
                if (theWidget != null) break;
            }


            if (theWidget == null) return;

            if (theWidget instanceof MapClickListener) {
                ((MapClickListener) theWidget).onClick(event);
            }

            if (!theWidget.isFocused()) {
                theWidget.focus();
            } else {
                theWidget.activate();
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    private boolean withIn(MapWidget widget, int x, int y) {
        int childX = widget.getAbsoluteX();
        if (childX > x || x > (childX + widget.getWidth())) return false;
        int childY = widget.getAbsoluteY();
        if (childY > y || y > (childY + widget.getHeight())) return false;
        return true;
    }

    private MapWidget prioritized = null;
    private long priortizedBefore = System.currentTimeMillis();

    private long lastTouch = System.currentTimeMillis();

    @Override
    public void onStatusChanged(MapStatusEvent event) {
        super.onStatusChanged(event);

        if (event.getName().equals("PRIORITIZE")) {
            if (!(event.getArgument() instanceof Object[])) return;
            Object[] argument = (Object[]) event.getArgument();
            if (argument.length != 2) return;
            if (!(argument[0] instanceof MapWidget)) return;
            MapWidget widget = (MapWidget) argument[0];
            long time = (long) argument[1];

            this.prioritized = widget;
            this.priortizedBefore = time;
        }
    }

    private MapWidget findSpecificWidgetWithin(MapWidget widget, int x, int y) {
        if (!withIn(widget, x, y)) return null;
        List<MapWidget> widgetList = new ArrayList<>();
        widgetList.addAll(widget.getWidgets());
        Collections.reverse(widgetList);
        for (MapWidget children : widgetList) {
            if (withIn(children, x,y)) {
                MapWidget theWidget = findSpecificWidgetWithin(children, x,y);
                if (theWidget == null && children.isFocusable())
                    return children;
                else
                    return theWidget;
            }
        }
        return widget != null && widget.isFocusable() ? widget : null;
    }
}
