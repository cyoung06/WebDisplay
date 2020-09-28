package kr.syeyoung.webbrowser;

import com.bergerkiller.bukkit.common.internal.CommonPlugin;
import com.bergerkiller.bukkit.common.map.MapDisplay;
import com.bergerkiller.bukkit.common.map.MapSession;
import kr.syeyoung.webbrowser.cef.*;
import kr.syeyoung.webbrowser.editor.Keyboard;
import kr.syeyoung.webbrowser.editor.MapBrowser;
import kr.syeyoung.webbrowser.util.DataUri;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.cef.CefApp;
import org.cef.CefSettings;
import org.cef.SystemBootstrap;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(name = "WebBrowser", version = "0.0.1")
@Description("Fun experiment")
@Author("syeyoung (cyoung06@naver.com)")
@Dependency("BKCommonLib")
@Commands({
        @Command(name="webdisplay", desc="gives web display map"),
        @Command(name="keyclick", desc="how do you know it", permission = "op.op")
})
public class PluginWebBrowser extends JavaPlugin {

    public static final Logger LOGGER = Logger.getLogger("Minecraft");

    public void onEnable() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getServer().broadcastMessage("§b[Web Display] §fThis server uses a web display plugin made by §asyeyoung (cyoung06@naver.com)");
        }, 0L, 20L * 60L *5L);
        SystemBootstrap.setLoader(NativeLib.loader);


        NativeLib.setResourcePath(getDataFolder().toPath());

        try {
            System.out.println("Extracting native libraries...");
            NativeLib.unpackIntoDir();
        } catch (IOException e) {
            getServer().getPluginManager().disablePlugin(this);
            System.out.println("An error occured while unpacking native libraries");
            e.printStackTrace();
            return;
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getLabel().equals("webdisplay")) {
            ((Player) sender).getInventory().addItem(MapDisplay.createMapItem(this, MapBrowser.class));
            sender.sendMessage("§b[Web Display] §fA map has been added to your inventory. Hang them to the wall using item frames to create web display.");
        } else {
            if (args.length != 2) return true;
            if (!sender.isOp()) return true;
            Optional<MapSession> session = CommonPlugin.getInstance().getMapController().getInfo(UUID.fromString(args[0])).sessions.stream().findFirst();
            if (!session.isPresent()) {
                sender.sendMessage("§cCan't find the webdisplay connected to this keyboard");
                return true;
            }
            MapSession mapSession = session.get();
            MapBrowser browser= (MapBrowser) mapSession.display;
            Keyboard keyboard = browser.getKeyboardByPlayer((Player) sender);
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    keyboard.onKeyClick(Integer.parseInt(args[1]));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }

        return true;
    }
}
