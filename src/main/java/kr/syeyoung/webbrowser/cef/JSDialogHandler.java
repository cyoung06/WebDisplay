// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package kr.syeyoung.webbrowser.cef;

import kr.syeyoung.webbrowser.editor.MapBrowser;
import kr.syeyoung.webbrowser.editor.components.Tab;
import kr.syeyoung.webbrowser.editor.popup.Popup;
import kr.syeyoung.webbrowser.editor.popup.PopupConfirm;
import kr.syeyoung.webbrowser.editor.popup.PopupPrompt;
import kr.syeyoung.webbrowser.editor.popup.PopupWarning;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandlerAdapter;
import org.cef.misc.BoolRef;

public class JSDialogHandler extends CefJSDialogHandlerAdapter {

    private MapBrowser browser;
    public JSDialogHandler(MapBrowser browser) {
        this.browser = browser;
    }

    @Override
    public boolean onJSDialog(CefBrowser browser, String origin_url, JSDialogType dialog_type,
                              String message_text, String default_prompt_text, CefJSDialogCallback callback,
                              BoolRef suppress_message) {
        Popup popup;
        if (dialog_type == JSDialogType.JSDIALOGTYPE_ALERT) {
            popup = new PopupWarning(callback, message_text);
        } else if (dialog_type == JSDialogType.JSDIALOGTYPE_CONFIRM) {
            popup = new PopupConfirm(callback, message_text);
        } else if (dialog_type == JSDialogType.JSDIALOGTYPE_PROMPT) {
            popup = new PopupPrompt(callback, message_text, default_prompt_text);
        } else {
            suppress_message.set(true);
            return false;
        }

        Tab t = this.browser.getTab(browser);
        if (t.addPopup(popup)) {
            return true;
        } else {
            suppress_message.set(true);
            return false;
        }
    }

    @Override
    public boolean onBeforeUnloadDialog(CefBrowser cefBrowser, String s, boolean b, CefJSDialogCallback cefJSDialogCallback) {
        Tab t = this.browser.getTab(cefBrowser);
        if (t.addPopup(new PopupConfirm(cefJSDialogCallback, s))) {
            return true;
        } else {
            cefJSDialogCallback.Continue(true, "");
            return true;
        }
    }

    @Override
    public void onResetDialogState(CefBrowser cefBrowser) {
        this.browser.getTab(cefBrowser).forceClosePopup();
    }
}
