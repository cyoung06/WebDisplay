package kr.syeyoung.webbrowser.editor;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.events.map.MapStatusEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapFont;
import com.bergerkiller.bukkit.common.map.MapSessionMode;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.bergerkiller.bukkit.common.map.widgets.MapWidgetButton;
import com.bergerkiller.bukkit.common.nbt.CommonTag;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.nbt.CommonTagList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.webbrowser.CefAppCreator;
import kr.syeyoung.webbrowser.PluginWebBrowser;
import kr.syeyoung.webbrowser.cef.*;
import kr.syeyoung.webbrowser.editor.components.AddressBar;
import kr.syeyoung.webbrowser.editor.components.BrowserRenderer;
import kr.syeyoung.webbrowser.editor.components.StatusBar;
import kr.syeyoung.webbrowser.editor.components.Tab;
import kr.syeyoung.webbrowser.editor.popup.Popup;
import kr.syeyoung.webbrowser.util.DataUri;
import kr.syeyoung.webbrowser.util.NanumFont;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefFileDialogCallback;
import org.cef.handler.*;
import org.cef.network.CefRequest;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    public static final String DEFAULT_URL = "webdisplay://welcome";

    private MapWidgetButton createNew = new MapWidgetButton() {
        @Override
        public void onAttached() {
            super.onAttached();
            setFocusable(true);
            setText("+");
        }

        @Override
        public void onActivate() {
            addTab(new Tab(MapBrowser.this, DEFAULT_URL));
        }
    };


    @Override
    public void onTick() {
        if (getWidth() < 384 || getHeight() < 128) {
            getTopLayer().fillRectangle(0,0,getWidth(),getHeight(), MapColorPalette.COLOR_WHITE);
            getTopLayer().draw(MapFont.MINECRAFT, 5,5,MapColorPalette.COLOR_RED, "Display Too Small!");
            drawTextInMiddleWithAdoquateSpacingWithin(getTopLayer(), "The smallest size web display can operate on is 3x1 maps (recommended smallest size 4x3 maps)", 5, 20, getWidth() - 10, getHeight() / 2);
            drawTextInMiddleWithAdoquateSpacingWithin(getTopLayer(), "Please put and connect webdisplays in itemframes to see content", 5, getHeight() / 2 + 20, getWidth() - 10, getHeight() / 2 - 25);
        }
    }

    public void drawTextInMiddleWithAdoquateSpacingWithin(Layer layer, String str, int x, int y, int totalWidth, int totalHeight) {
        String[] words = str.split(" ");
        List<String> linesStr = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for (String word : words) {
            int width2 = word.length() * 6 - 1;
            if (width2 + width > totalWidth) {
                width = width2;
                linesStr.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(" ").append(word);
                width += width2;
            }

            width += 6;
        }

        linesStr.add(currentLine.toString());
        int startY = y + ((totalHeight - (linesStr.size() * 9 - 1)) / 2);
        for (int i = 0; i < linesStr.size(); i++ ) {
            String lines = linesStr.get(i).trim();
            int localWidth = lines.length() * 6 - 1;

            int startX = x + (totalWidth - localWidth) / 2;

            layer.draw(MapFont.MINECRAFT, startX, startY + i * 9, MapColorPalette.COLOR_BLACK, lines);
        }
    }

    @Override
    public void onAttached() {
        clearWidgets();

        setSessionMode(MapSessionMode.FOREVER);
        setGlobal(true);
        setUpdateWithoutViewers(true);

        if (cefClient == null)
            createCefClient();


        if (getWidth() >= 384 && getHeight() >= 128) {
            addWidget(createNew);
            resizeTabs();

            if (tabs.size() == 0) {
                CommonTagCompound tabs = properties.get("tabs", CommonTagCompound.class);
                if (tabs != null) {
                    CommonTagList taglist = tabs.getValue("urls", CommonTagList.class);

                    for (CommonTag url : taglist)
                        addTab(new Tab(this, url.getData(String.class)));
                } else {
                    addTab(new Tab(this, DEFAULT_URL));
                }
            } else {
                for (Tab t:tabs) {
                    addWidget(t.getHeader());
                }
                addWidget(activeTab);
                if (activeTab != null) activeTab.setBounds(0, 30, getWidth(), getHeight() - 30);

                resizeTabs();
            }
        }
    }

    public void setActivatedTab(Tab t) {
        if (activeTab != null)
            activeTab.setActive(false);
        t.setActive(true);

        if (activeTab != null)
            removeWidget(activeTab);

        activeTab = t;
        addWidget(t);
        t.setBounds(0, 30, getWidth(), getHeight() - 30);
    }

    public void closeTab(Tab t) {
        tabs.remove(t);
        removeWidget(t.getHeader());

        if (t == activeTab && tabs.size() != 0)
            setActivatedTab(tabs.get(0));
        else if (t == activeTab && tabs.size() == 0) {
            removeWidget(activeTab);
            activeTab = null;
            addTab(new Tab(this, DEFAULT_URL));
        }
        resizeTabs();
    }

    public void addTab(Tab t) {
        tabs.add(t);
        addWidget(t.getHeader());
        resizeTabs();
        if (activeTab == null) setActivatedTab(t);
    }

    public void resizeTabs() {

        int eachSize = 0;
        if (tabs.size() != 0) eachSize = Math.min(300, (getWidth() - 20) / tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).getHeader().setBounds(i * eachSize, 0, eachSize, 30);
        }
        createNew.setBounds(tabs.size() * eachSize, 0, 30, 30);
    }

    public void saveURLS() {
        CommonTagCompound common = new CommonTagCompound();
        common.putListValues("urls", tabs.stream().map(t -> t.getCefBrowser().getURL()).collect(Collectors.toList()));
        properties.set("tabs", common);
    }

    public void createCefClient() {
        cefClient = CefAppCreator.getInstance().getCefApp().createClient();
        cefClient.addContextMenuHandler(new ContextMenuHandler(null));
        cefClient.addJSDialogHandler(new JSDialogHandler(this));
        cefClient.addRequestHandler(new RequestHandler(null));

        CefMessageRouter msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new MessageRouterHandlerEx(cefClient), false);
        cefClient.addMessageRouter(msgRouter);
        cefClient.removeLifeSpanHandler();

        cefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser cefBrowser, CefFrame cefFrame, String url, String target_frame_name) {
                addTab(new Tab(MapBrowser.this, url));
                PluginWebBrowser.LOGGER.log(Level.FINE, url + " / " + target_frame_name);
                return true;
            }
        });

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                tabs.stream().filter(t -> t.getCefBrowser() == browser).findFirst().get().getAddressBar().setAddress(browser, url);

                saveURLS();
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
///
//                if (!isLoading && !errorMsg_.isEmpty()) {
//                    browser.loadURL(DataUri.create("text/html", errorMsg_));
//                    errorMsg_ = "";
//                }
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode,
                                    String errorText, String failedUrl) {

//                if (errorCode != ErrorCode.ERR_NONE && errorCode != ErrorCode.ERR_ABORTED) {
//                    errorMsg_ = "<html><head>";
//                    errorMsg_ += "<title>Error while loading</title>";
//                    errorMsg_ += "</head><body>";
//                    errorMsg_ += "<h1>" + errorCode + "</h1>";
//                    errorMsg_ += "<h3>Failed to load " + failedUrl + "</h3>";
//                    errorMsg_ += "<p>" + (errorText == null ? "" : errorText) + "</p>";
//                    errorMsg_ += "</body></html>";
//                    browser.stopLoad();
//                }
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

    public Tab getTab(CefBrowser browser) {
        return tabs.stream().filter(t -> t.getCefBrowser() == browser).findFirst().get();
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
