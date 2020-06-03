package kr.syeyoung.webbrowser.editor;

import com.bergerkiller.bukkit.common.events.map.MapClickEvent;

public interface MapClickListener {
    /**
     * return true to disable the automatic thing that display will do
     * @param event
     * @return
     */
    public boolean onClick(MapClickEvent event);
}
