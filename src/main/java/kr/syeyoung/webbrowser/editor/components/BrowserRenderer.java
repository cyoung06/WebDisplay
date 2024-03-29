package kr.syeyoung.webbrowser.editor.components;

import com.bergerkiller.bukkit.common.events.map.MapAction;
import com.bergerkiller.bukkit.common.events.map.MapClickEvent;
import com.bergerkiller.bukkit.common.map.MapColorPalette;
import com.bergerkiller.bukkit.common.map.widgets.MapWidget;
import com.google.common.base.Preconditions;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.GLBuffers;
import com.sun.awt.AWTUtilities;
import com.sun.jmx.remote.internal.ArrayQueue;
import kr.syeyoung.webbrowser.PluginWebBrowser;
import kr.syeyoung.webbrowser.editor.MapClickListener;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserOsr;
import org.cef.callback.CefDragData;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefScreenInfo;
import org.w3c.dom.css.Rect;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

import static com.jogamp.opengl.GL.*;

public class BrowserRenderer extends MapWidget implements CefRenderHandler, MapClickListener {
    private CefBrowserOsr browser;
    private boolean firstRender = false;


    public BrowserRenderer(CefBrowser browser) {
        this.browser = (CefBrowserOsr) browser;
        this.browser.renderHandler = this;
        setFocusable(true);
    }



    @Override
    public void onActivate() {
    }

    @Override
    public void onFocus() {
        super.onFocus();
        browser.setFocus(true);
    }

    @Override
    public void onBlur() {
        super.onBlur();
        browser.setFocus(false);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        viewRect.setSize(getWidth(), getHeight());
        invalidate();
        this.browser.setFocus(true);
        this.browser.wasResized(getWidth(), getHeight());
    }

    @Override
    public void onBoundsChanged() {
        viewRect.setSize(getWidth(), getHeight());
        browser.wasResized(getWidth(), getHeight());
    }

    @Override
    public void onDraw() {
        synchronized (lastFrameData) {
            FrameData fd = lastFrameData;
            ByteBuffer bf = fd.buffer;
            if (fd.width == 0) return;
            label: {
                    for (int y =0; y < fd.height; y++) {
                        for (int x = 0; x < fd.width; x++) {
                            int blue = 0xff & bf.get();
                            int green = 0xff & bf.get();
                            int red = 0xff & bf.get();
                            int alpha = 0xff & bf.get();
                            this.view.drawPixel(x,y,MapColorPalette.getColor(red,green,blue));
                        }
                    }
                    bf.position(0);
            }
        }
    }

    @Override
    public void onTick() {
        EventQueue.invokeLater(() -> {
            if (!sentRelease && System.currentTimeMillis() > lastClick) {
                MouseEvent mouseEvent = new MouseEvent(dummy, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, lastClickEvent.getX(), lastClickEvent.getY(), 0, false, lastClickEvent.getButton());
                sendMouseEvent(mouseEvent);
                sentRelease = true;
                if (!dragging) {
                    mouseEvent = new MouseEvent(dummy, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, lastClickEvent.getX(), lastClickEvent.getY(), 0, false, lastClickEvent.getButton());
                    sendMouseEvent(mouseEvent);
                }
                dragging = false;
            }
        });
        super.onTick();
        synchronized (lastFrameData) {
            FrameData fd = lastFrameData;
            if (fd.written) return;
            ByteBuffer bf = fd.buffer;
            label: {
                for (Rectangle r : fd.rectangles) {
                    for (int y =r.y; y < r.y + r.height; y++) {
                        bf.position((y * fd.width + r.x) * 4);
                        for (int x = r.x; x < r.x + r.width; x++) {
                            int blue = 0xff & bf.get();
                            int green = 0xff & bf.get();
                            int red = 0xff & bf.get();
                            int alpha = 0xff & bf.get();
                            this.view.drawPixel(x,y,MapColorPalette.getColor(red,green,blue));
                        }
                    }
                }
            }
            fd.rectangles.clear();
            fd.written = true;
            if (bf != null)
                bf.position(0);
        }
    }

    public void sendMouseEvent(MouseEvent mouseEvent) {
        browser.sendMouseEvent(mouseEvent);
    }
    
    private static final Point dummyPoint = new Point(0,0);
    public static final Component dummy = new Component() {
        @Override
        public Point getLocationOnScreen() {
            return dummyPoint;
        }
    };

    private Rectangle viewRect = new Rectangle(0,0,300,200);
    private Point screenPoint = new Point(0,0);

    @Override
    public Rectangle getViewRect(CefBrowser cefBrowser) {
        return viewRect;
    }

    @Override
    public boolean getScreenInfo(CefBrowser cefBrowser, CefScreenInfo cefScreenInfo) {
        return browser.getScreenInfo(cefBrowser, cefScreenInfo);
    }

    @Override
    public Point getScreenPoint(CefBrowser cefBrowser, Point viewPoint) {
        Point screenPoint = new Point(this.screenPoint);
        screenPoint.translate(viewPoint.x, viewPoint.y);
        return screenPoint;
    }

    @Override
    public void onPopupShow(CefBrowser cefBrowser, boolean b) {
        PluginWebBrowser.LOGGER.log(Level.FINE, cefBrowser +" / " + cefBrowser.getURL() + " / "+b);
    }

    @Override
    public void onPopupSize(CefBrowser cefBrowser, Rectangle rectangle) {
        PluginWebBrowser.LOGGER.log(Level.FINE, cefBrowser +" / " + cefBrowser.getURL() + " / "+rectangle.toString());
    }

    private long lastClick = -1;
    private MouseEvent lastClickEvent;
    private boolean sentRelease = true;
    private boolean dragging = false;
    private Point lastKnownPoint = new Point(0,0);
    @Override
    public boolean onClick(MapClickEvent event) {
        lastKnownPoint = new Point(event.getX(), event.getY() - 70);
        EventQueue.invokeLater(() -> {
            if (lastClick < System.currentTimeMillis()) {
                int button = 0;
                if (event.getAction() == MapAction.LEFT_CLICK) button = MouseEvent.BUTTON1;
                else if (event.getAction() == MapAction.RIGHT_CLICK) button = MouseEvent.BUTTON2;
                else button = MouseEvent.NOBUTTON;
                MouseEvent mouseEvent = new MouseEvent(dummy, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, event.getX(), event.getY() - 70, 0, false);
                sendMouseEvent(mouseEvent);
                lastClickEvent = new MouseEvent(dummy, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, event.getX(), event.getY() - 70, 0, false, button);
                sendMouseEvent(lastClickEvent);
                lastClick = System.currentTimeMillis() + 250;
                sentRelease = false;
                dragging = false;
            } else {
                int button = 0;
                if (event.getAction() == MapAction.LEFT_CLICK) button = MouseEvent.BUTTON1;
                else if (event.getAction() == MapAction.RIGHT_CLICK) button = MouseEvent.BUTTON2;
                else button = MouseEvent.NOBUTTON;
                MouseEvent mouseEvent = new MouseEvent(dummy, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, event.getX(), event.getY() - 70, 0, false);
                sendMouseEvent(mouseEvent);
                lastClickEvent = new MouseEvent(dummy, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, event.getX(), event.getY() - 70, 0, false, button);
                sendMouseEvent(lastClickEvent);
                lastClick = System.currentTimeMillis() + 250;
                sentRelease = false;
                dragging = true;
            }
        });
        return false;
    }

    public static class FrameData {
        public List<Rectangle> rectangles = new ArrayList<>();
        public ByteBuffer buffer;
        public int width;
        public int height;
        public boolean written;
    }
    FrameData lastFrameData = new FrameData();

    @Override
    public void onPaint(CefBrowser cefBrowser, boolean b, Rectangle[] rectangles, ByteBuffer byteBuffer, int i, int i1) {
        // yay fun stuff
        FrameData fd = lastFrameData;
        synchronized (lastFrameData) {
            }
            fd.rectangles.addAll(Arrays.asList(rectangles));
            int size = (i * i1) << 2;
            if (fd.buffer == null || size != fd.buffer.capacity()) //This only happens when the browser gets resized
                fd.buffer = ByteBuffer.allocate(size);

            fd.buffer.position(0);
            fd.buffer.limit(byteBuffer.limit());
            byteBuffer.position(0);
            fd.buffer.put(byteBuffer);
            fd.buffer.position(0);
            fd.width = i;
            fd.height = i1;
            fd.written = false;
        }
    }

    @Override
    public void onCursorChange(CefBrowser cefBrowser, int i) {

    }

    @Override
    public boolean startDragging(CefBrowser cefBrowser, CefDragData cefDragData, int i, int i1, int i2) {
        return false;
    }

    @Override
    public void updateDragCursor(CefBrowser cefBrowser, int i) {

    }
}
