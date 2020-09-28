package kr.syeyoung.webbrowser.cef;

import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WebDisplaySchemeHandler implements CefResourceHandler {
    private byte[] data_;
    private String mime_type_;
    private int offset_ = 0;

    public synchronized boolean processRequest(CefRequest var1, CefCallback var2) {
        boolean var3 = false;
        String var4 = var1.getURL();
        String var5;
        if (var4.contains("welcome")) {
            mime_type_ = "text/html";
            loadContent("/kr/syeyoung/webbrowser/res/welcome.html");
            offset_ = 0;
        } else if (var4.contains("opensource")) {
            mime_type_ = "text/html";
            loadContent("/kr/syeyoung/webbrowser/res/opensource.html");
            offset_ = 0;
        }

        if (var3) {
            var2.Continue();
            return true;
        } else {
            return false;
        }
    }

    public void getResponseHeaders(CefResponse var1, IntRef var2, StringRef var3) {
        var1.setMimeType(this.mime_type_);
        var1.setStatus(200);
        var2.set(this.data_.length);
    }

    public synchronized boolean readResponse(byte[] var1, int var2, IntRef var3, CefCallback var4) {
        boolean var5 = false;
        if (this.offset_ < this.data_.length) {
            int var6 = Math.min(var2, this.data_.length - this.offset_);
            System.arraycopy(this.data_, this.offset_, var1, 0, var6);
            this.offset_ += var6;
            var3.set(var6);
            var5 = true;
        } else {
            this.offset_ = 0;
            var3.set(0);
        }

        return var5;
    }

    @Override
    public void cancel() {

    }

    private boolean loadContent(String var1) {
        InputStream var2 = this.getClass().getResourceAsStream(var1);
        if (var2 != null) {
            try {
                ByteArrayOutputStream var3 = new ByteArrayOutputStream();
                boolean var4 = true;

                int var6;
                while((var6 = var2.read()) >= 0) {
                    var3.write(var6);
                }

                this.data_ = var3.toByteArray();
                return true;
            } catch (IOException var5) {
            }
        }

        return false;
    }
}
