// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package kr.syeyoung.webbrowser.cef;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefAuthCallback;
import org.cef.callback.CefRequestCallback;
import org.cef.handler.CefLoadHandler.ErrorCode;
import org.cef.handler.CefRequestHandler;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.handler.CefResourceRequestHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;
import tests.detailed.dialog.CertErrorDialog;
import tests.detailed.dialog.PasswordDialog;
import tests.detailed.handler.ResourceHandler;
import tests.detailed.handler.ResourceSetErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Vector;

public class RequestHandler extends CefResourceRequestHandlerAdapter implements CefRequestHandler {
    private final Frame owner_;

    public RequestHandler(Frame owner) {
        owner_ = owner;
    }

    @Override
    public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request,
                                  boolean user_gesture, boolean is_redirect) {
        return false;
    }

    @Override
    public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame,
                                                               CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator,
                                                               BoolRef disableDefaultHandling) {
        return this;
    }

    @Override
    public boolean onBeforeResourceLoad(CefBrowser browser, CefFrame frame, CefRequest request) {
        return false;
    }

    @Override
    public CefResourceHandler getResourceHandler(
            CefBrowser browser, CefFrame frame, CefRequest request) {
        return null;
    }

    @Override
    public boolean getAuthCredentials(CefBrowser browser, String origin_url, boolean isProxy,
                                      String host, int port, String realm, String scheme, CefAuthCallback callback) {
        SwingUtilities.invokeLater(new PasswordDialog(owner_, callback));
        return true;
    }

    @Override
    public boolean onQuotaRequest(
            CefBrowser browser, String origin_url, long new_size, CefRequestCallback callback) {
        return false;
    }

    @Override
    public boolean onCertificateError(CefBrowser browser, ErrorCode cert_error, String request_url,
                                      CefRequestCallback callback) {
        SwingUtilities.invokeLater(new CertErrorDialog(owner_, cert_error, request_url, callback));
        return true;
    }

    @Override
    public void onPluginCrashed(CefBrowser browser, String pluginPath) {
        System.out.println("Plugin " + pluginPath + "CRASHED");
    }

    @Override
    public void onRenderProcessTerminated(CefBrowser browser, TerminationStatus status) {
        System.out.println("render process terminated: " + status);
    }
}
