package kr.syeyoung.webbrowser;

import kr.syeyoung.webbrowser.cef.AppHandler;
import org.cef.CefApp;
import org.cef.CefSettings;

import java.util.Arrays;
import java.util.Collections;

public class CefAppCreator {

    private static final CefAppCreator instance = new CefAppCreator();

    public static CefAppCreator getInstance() {
        return instance;
    }

    private static CefApp cefApp;
    //    @Override
    private void createCefAPP() {
        if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {
            CefSettings settings = new CefSettings();
            settings.windowless_rendering_enabled = true;
            settings.command_line_args_disabled = false;
            settings.browser_subprocess_path = NativeLib.getSubProcessPath();
            settings.resources_dir_path = NativeLib.getResourcesPath();
            settings.locales_dir_path = NativeLib.getLocalesPath();
//            settings.resources_dir_path =
            settings.background_color = settings.new ColorType(100, 255, 242, 211);
            cefApp = CefApp.getInstance(new String[]{"--mute-audio=true", "--mute-audio", "-mute-audio"}, settings);

            CefApp.CefVersion version = cefApp.getVersion();
            System.out.println("Using:\n" + version);

            CefApp.addAppHandler(new AppHandler(new String[0]));
        } else {
            cefApp = CefApp.getInstance();
        }
    }

    public CefApp getCefApp() {
        if (cefApp == null || cefApp.getState() != CefApp.CefAppState.INITIALIZED)
            createCefAPP();
        return cefApp;
    }
}
