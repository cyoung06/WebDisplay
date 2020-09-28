package kr.syeyoung.webbrowser.cef;

import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;
import tests.detailed.handler.ClientSchemeHandler;

public class AppHandler extends CefAppHandlerAdapter {
    public AppHandler(String[] args) {
        super(args);
    }

    public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
        // scheme / standard / local / display isolation / secure / cors / csp bypass / fetch
        registrar.addCustomScheme("webdisplay", false, false, false, false, false, false, false);
    }

    public void onContextInitialized() {
        CefApp cefApp = CefApp.getInstance();
        cefApp.registerSchemeHandlerFactory("webdisplay", "", new SchemeHandlerFactory());
    }

    public void stateHasChanged(CefApp.CefAppState state) {
    }

    private static class SchemeHandlerFactory implements CefSchemeHandlerFactory {
        private SchemeHandlerFactory() {
        }

        public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
            return schemeName.equals("webdisplay") ? new WebDisplaySchemeHandler() : null;
        }
    }
}
