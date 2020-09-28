package kr.syeyoung.webbrowser.editor;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.events.map.MapStatusEvent;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapSessionMode;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.webbrowser.CefAppCreator;
import kr.syeyoung.webbrowser.PluginWebBrowser;
import kr.syeyoung.webbrowser.cef.*;
import kr.syeyoung.webbrowser.editor.components.AddressBar;
import kr.syeyoung.webbrowser.editor.components.BrowserRenderer;
import kr.syeyoung.webbrowser.editor.components.StatusBar;
import kr.syeyoung.webbrowser.editor.components.Tab;
import kr.syeyoung.webbrowser.util.DataUri;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefFileDialogCallback;
import org.cef.handler.CefDialogHandler;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;

import java.util.*;

public class MapBrowser extends MapDisplay {

    @Getter
    private CefClient cefClient;
    private String errorMsg_ = "";

    private List<Tab> tabs = new ArrayList<>();
    @Getter
    private Tab activeTab;

    @Getter
    private Map<Player, Keyboard> keyboardMap = new HashMap<>();

    public Keyboard getKeyboardByPlayer(Player p) {
        return getKeyboardMap().get(p);
    }

    private MapWidgetButton createNew = new MapWidgetButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            setText("+");
        }

        @Override
        public void onActivate() {
            addTab(new Tab(MapBrowser.this));
        }
    };

    @Override
    public void onAttached() {
        clearWidgets();
        setSessionMode(MapSessionMode.FOREVER);
        setGlobal(true);

        createCefClient();
        resizeTabs();

        addTab(new Tab(this));
        addTab(new Tab(this));
    }

    public void setActivatedTab(Tab t) {
        activeTab.setActive(false);
        t.setActive(true);
        activeTab = t;

        removeWidget(activeTab);
        addWidget(t);
        t.setBounds(0, 20, getWidth(), getHeight() - 20);
    }

    public void closeTab(Tab t) {
        tabs.remove(t);
        removeWidget(t.getHeader());

        if (t == activeTab)
            setActivatedTab(tabs.get(0));

        resizeTabs();
    }

    public void addTab(Tab t) {
        tabs.add(t);
        addWidget(t.getHeader());
        resizeTabs();
        if (activeTab == null) setActivatedTab(t);
    }

    public void resizeTabs() {
        int eachSize = Math.min(300, (getWidth() - 20) / tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).getHeader().setBounds(i * eachSize, 0, eachSize, 20);
        }
        createNew.setBounds(tabs.size() * eachSize, 0, 20, 20);
    }

    public void createCefClient() {
        cefClient = CefAppCreator.getInstance().getCefApp().createClient();
        cefClient.addContextMenuHandler(new ContextMenuHandler(null));
        cefClient.addDragHandler(new DragHandler());
        cefClient.addJSDialogHandler(new JSDialogHandler());
        cefClient.addKeyboardHandler(new KeyboardHandler());
        cefClient.addRequestHandler(new RequestHandler(null));

        CefMessageRouter msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new MessageRouterHandler(), true);
        msgRouter.addHandler(new MessageRouterHandlerEx(cefClient), false);
        cefClient.addMessageRouter(msgRouter);
        cefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser cefBrowser, CefFrame cefFrame, String url, String target_frame_name) {
                System.out.println(url + " - " +target_frame_name);
                return false;
            }
        });

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                tabs.stream().filter(t -> t.getCefBrowser() == browser).findFirst().get().getAddressBar().setAddress(browser, url);
            }
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                tabs.stream().filter(t -> t.getCefBrowser() == browser).findFirst().get().setTitle(title);
            }
            @Override
            public void onStatusMessage(CefBrowser browser, String value) {

                tabs.stream().filter(t -> t.getCefBrowser() == browser).findFirst().get().getStatusBar().setStatusText(value);
            }
        });
        cefClient.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                                             boolean canGoBack, boolean canGoForward) {
                tabs.stream().filter(t -> t.getCefBrowser() == browser).findFirst().get().getAddressBar().update(browser, isLoading, canGoBack, canGoForward);
                tabs.stream().filter(t -> t.getCefBrowser() == browser).findFirst().get().getStatusBar().setIsInProgress(isLoading);
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
