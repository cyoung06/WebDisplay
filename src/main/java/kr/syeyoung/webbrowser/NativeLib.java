package kr.syeyoung.webbrowser;

import org.bukkit.Bukkit;
import org.cef.OS;
import org.cef.SystemBootstrap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Native;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NativeLib {
    private static Path RESOURCE_PATH;

    public static void setResourcePath(Path RESOURCE_PATH) {
        NativeLib.RESOURCE_PATH = RESOURCE_PATH;
    }

    public static void unpackIntoDir() throws IOException {

        if (RESOURCE_PATH == null) throw new IllegalStateException("Resource not set");

        String nativelibName = getLibName();

        Path target = RESOURCE_PATH.resolve(nativelibName);
        if (target.toFile().exists()) return;
        Files.createDirectories(target);


        try (ZipInputStream zipIn = new ZipInputStream(NativeLib.class.getResourceAsStream("/kr/syeyoung/webbrowser/res/nativelibs/"+nativelibName+".zip"))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                final Path toPath = target.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else {
                    Files.copy(zipIn, toPath);
                }
            }
        }
    }

    private static String getLibName() {
        boolean is64bit = "64".equals(System.getProperty("sun.arch.data.model"));
        if (OS.isWindows() && is64bit)
            return "win64";
        else if (OS.isWindows() && !is64bit)
            return "win32";
        else if (OS.isLinux() && is64bit)
            return "linux64";
        else if (OS.isLinux() && !is64bit)
            return "linux32";
        else
            throw new IllegalStateException("No native libs supporting this version");
    }

    public static String getSubProcessPath() {
        String nativelibName = getLibName();

        File dir = RESOURCE_PATH.resolve(nativelibName).resolve(OS.isWindows() ? "jcef_helper.exe" : OS.isLinux() ? "jcef_helper" : "../Frameworks/jcef Helper.app/Contents/MacOS/jcef Helpe").toFile();
        return dir.getAbsolutePath();
    }

    static {
        boolean is64bit = "64".equals(System.getProperty("sun.arch.data.model"));
        if (OS.isLinux() && is64bit) System.load(System.getProperty("java.home") + "/lib/amd64/libjawt.so");
        else if (OS.isLinux() && !is64bit) System.load(System.getProperty("java.home") + "/lib/i386/libjawt.so");
    }

    static SystemBootstrap.Loader loader = new SystemBootstrap.Loader() {
        @Override
        public void loadLibrary(String s) {
            String nativelibName = getLibName();


            File dir = RESOURCE_PATH.resolve(nativelibName).toFile();
            try {
                if (searchAndLoad(dir, s)) return;
                System.out.println("loading lib... "+s);
                System.loadLibrary(s);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§c================================ WEB DISPLAY WARNING ================================");
                Bukkit.getConsoleSender().sendMessage("§fFailed to load native library "+s+", "+e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        private boolean searchAndLoad(File dir, String s) {
            for (File f: Objects.requireNonNull(dir.listFiles())) {
                if (f.isDirectory()) if (searchAndLoad(f, s)) return true;
                if (OS.isWindows()) {
                    if (f.isFile() && f.getName().startsWith(s)) {
                        System.load(f.getAbsolutePath());
                        return true;
                    }
                } else if (OS.isLinux()) {
                    if (f.isFile() && f.getName().startsWith("lib"+s) && f.getName().endsWith(".so")) {
                        System.out.println("loading... "+f.getAbsolutePath());
                        System.load(f.getAbsolutePath());
                        return true;
                    }
                }
            }
            return false;
        }
    };
}
