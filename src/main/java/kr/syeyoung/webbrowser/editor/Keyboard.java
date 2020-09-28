package kr.syeyoung.webbrowser.editor;

import kr.syeyoung.webbrowser.editor.components.BrowserRenderer;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserOsr;
import sun.awt.AWTAccessor;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.*;

public class Keyboard {
    private boolean isShiftPressed;
    private boolean isCtrlPressed;
    private boolean isAltPressed;
    private boolean isCapsLockPressed;

    private boolean isBound;

    @Getter
    private MapBrowser browser;
    private Player p;
    private UUID uid;

    public Keyboard(MapBrowser browser, UUID uuid, Player p) {
        this.browser = browser;
        this.p = p;
        this.uid = uuid;
    }

    private static final Map<Integer, Character[]> shiftModifier = new HashMap() {{
        this.put(KeyEvent.VK_1, new Character[] {'1', '!'});
        this.put(KeyEvent.VK_2, new Character[] {'2', '@'});
        this.put(KeyEvent.VK_3, new Character[] {'3', '#'});
        this.put(KeyEvent.VK_4, new Character[] {'4', '$'});
        this.put(KeyEvent.VK_5, new Character[] {'5', '%'});
        this.put(KeyEvent.VK_6, new Character[] {'6', '^'});
        this.put(KeyEvent.VK_7, new Character[] {'7', '&'});
        this.put(KeyEvent.VK_8, new Character[] {'8', '*'});
        this.put(KeyEvent.VK_9, new Character[] {'9', '('});
        this.put(KeyEvent.VK_0, new Character[] {'0', ')'});
        this.put(KeyEvent.VK_BACK_QUOTE, new Character[] {'`', '~'});
        this.put(KeyEvent.VK_MINUS, new Character[] {'-', '_'});
        this.put(KeyEvent.VK_UNDERSCORE, new Character[] {'-', '_'});
        this.put(KeyEvent.VK_EQUALS, new Character[] {'=', '+'});
        this.put(KeyEvent.VK_PLUS, new Character[] {'=', '+'});
        this.put(KeyEvent.VK_OPEN_BRACKET, new Character[] {'[', '{'});
        this.put(KeyEvent.VK_CLOSE_BRACKET, new Character[] {']', '}'});
        this.put(KeyEvent.VK_BACK_SLASH, new Character[] {'\\', '|'});
        this.put(KeyEvent.VK_SEMICOLON, new Character[] {';', ':'});
        this.put(KeyEvent.VK_COLON, new Character[] {';', ':'});
        this.put(KeyEvent.VK_QUOTE, new Character[] {'\'', '"'});
        this.put(KeyEvent.VK_QUOTEDBL, new Character[] {'\'', '"'});
        this.put(KeyEvent.VK_COMMA, new Character[] {',', '<'});
        this.put(KeyEvent.VK_PERIOD, new Character[] {'.', '>'});
        this.put(KeyEvent.VK_SLASH, new Character[] {'/', '?'});
        this.put(KeyEvent.VK_ENTER, new Character[] {'\n', '\n'});
        this.put(KeyEvent.VK_SPACE, new Character[] {' ', ' '});
        for (char i ='A'; i <= 'Z'; i++) {
            char c = KeyEvent.getKeyText(i).charAt(0);
            this.put((int) i, new Character[] {Character.toLowerCase(c), c});
        }
    }};

    public void onKeyClick(int keyCode) throws InterruptedException {
//        if (keyCode == 999999999) {
//            isBound = !isBound;
//
//        }

        if (keyCode == KeyEvent.VK_SHIFT) {
            isShiftPressed = !isShiftPressed;
            sendEvent(createKeyEvent(isShiftPressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED, keyCode , KeyEvent.CHAR_UNDEFINED, keyCode, 0, keyCode));
        } else if (keyCode == KeyEvent.VK_CONTROL) {
            isCtrlPressed = !isCtrlPressed;
            sendEvent(createKeyEvent(isShiftPressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED, keyCode , KeyEvent.CHAR_UNDEFINED, keyCode, 0, keyCode));
        } else if (keyCode == KeyEvent.VK_ALT) {
            isAltPressed = !isAltPressed;
            sendEvent(createKeyEvent(isShiftPressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED, keyCode , KeyEvent.CHAR_UNDEFINED, keyCode, 0, keyCode));
        } else if (keyCode == KeyEvent.VK_CAPS_LOCK) {
            isCapsLockPressed = !isCapsLockPressed;
            sendEvent(createKeyEvent(isShiftPressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED, keyCode , KeyEvent.CHAR_UNDEFINED, keyCode, 0, keyCode));
        } else {
            if (keyCode == KeyEvent.VK_ENTER) {
                sendEvent(createKeyEvent(KeyEvent.KEY_PRESSED, keyCode , '\n', 10, 13, 10));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED , '\n', 0,0,0));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_RELEASED, keyCode ,'\n', 10, 13, 10));
            } else if (shiftModifier.containsKey(keyCode)){
                Character[] modifier = shiftModifier.get(keyCode);
                char c = modifier[isShiftPressed | isCapsLockPressed ? 1 : 0];

                sendEvent(createKeyEvent(KeyEvent.KEY_PRESSED, keyCode , c, keyCode, Character.toLowerCase(c), keyCode));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED ,c, 0,0,0));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_RELEASED, keyCode , c, keyCode, Character.toLowerCase(c), keyCode));
            } else if (keyCode == KeyEvent.VK_TAB) {
                sendEvent(createKeyEvent(KeyEvent.KEY_PRESSED, keyCode ,'\t', keyCode, keyCode, keyCode));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED ,'\t', 0,0,0));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_RELEASED, keyCode , '\t', keyCode, keyCode, keyCode));
            } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                sendEvent(createKeyEvent(KeyEvent.KEY_PRESSED, keyCode , '\b', keyCode, keyCode, keyCode));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_TYPED, KeyEvent.VK_UNDEFINED ,'\b', 0,0,0));
                Thread.sleep(50);
                sendEvent(createKeyEvent(KeyEvent.KEY_RELEASED, keyCode , '\b', keyCode, keyCode, keyCode));
            }
            if (isShiftPressed) {
                isShiftPressed = false;
                sendEvent(new KeyEvent(BrowserRenderer.dummy, isShiftPressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED, System.currentTimeMillis(), getMask(),keyCode , KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_STANDARD));
            }
        }
        sendKeyboard();
    }

    private static final Field f;

    static {
        Field semiF = null;
        try {
            semiF = KeyEvent.class.getDeclaredField("scancode");
            semiF.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        f = semiF;
    }

    public KeyEvent createKeyEvent(int event, int keyCode, char keyChar, int extended,int primaryUnicode, int rawCode) {
        KeyEvent ev =  new KeyEvent(BrowserRenderer.dummy, event, System.currentTimeMillis(), getMask(), keyCode, keyChar, event != KeyEvent.KEY_TYPED ? KeyEvent.KEY_LOCATION_STANDARD : KeyEvent.KEY_LOCATION_UNKNOWN);
        AWTAccessor.getKeyEventAccessor().setExtendedKeyCode(ev, extended);
        AWTAccessor.getKeyEventAccessor().setPrimaryLevelUnicode(ev, primaryUnicode);
        AWTAccessor.getKeyEventAccessor().setRawCode(ev, rawCode);

        if (keyChar == '\b' && f != null && event != KeyEvent.KEY_TYPED) {
            try {
                f.setLong(ev,14);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else if (keyChar == '\n' && f != null && event != KeyEvent.KEY_TYPED) {
            try {
                f.setLong(ev,28);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return ev;
    }

    public void sendEvent(KeyEvent event) {
        if (browser.getActiveTab() != null)
            ((CefBrowserOsr)browser.getActiveTab().getCefBrowser()).sendKeyEventWrap(event);
    }

    public int getMask() {
        int result = 0;
        if (isShiftPressed|isCapsLockPressed ) result |= KeyEvent.SHIFT_DOWN_MASK;
        if (isAltPressed) result |= KeyEvent.ALT_DOWN_MASK;
        if (isCtrlPressed) result |= KeyEvent.CTRL_DOWN_MASK;

        return result;
    }

    public void sendKeyboard() {
        List<TextComponent> rows = new ArrayList<>();
        {
            int[] row = new int[]{
                    KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4
                    , KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0
                    , isShiftPressed|isCapsLockPressed  ? KeyEvent.VK_UNDERSCORE : KeyEvent.VK_MINUS, isShiftPressed|isCapsLockPressed  ? KeyEvent.VK_PLUS : KeyEvent.VK_EQUALS
            };
            List<TextComponent> components = new ArrayList<>(row.length + 1);
            for (int i : row) {
                components.add(composeKey(i, null));
            }
            components.add(composeKey(KeyEvent.VK_BACK_SPACE, "BKSPACE"));
            rows.add(spaceSeparatedRow(components.<TextComponent>toArray(new TextComponent[0])));
        }
        {
            int[] row = new int[]{
                    'Q', 'W', 'E', 'R' , 'T', 'Y' ,'U', 'I', 'O', 'P', KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_BACK_SLASH
            };
            List<TextComponent> components = new ArrayList<>(row.length + 1);
            components.add(composeKey(KeyEvent.VK_TAB, "TAB"));
            for (int i : row) {
                components.add(composeKey(i, null));
            }
            rows.add(spaceSeparatedRow(components.<TextComponent>toArray(new TextComponent[0])));
        }
        {
            int[] row = new int[]{
                    'A', 'S', 'D', 'F' , 'G', 'H' ,'J', 'K', 'L', isShiftPressed|isCapsLockPressed ? KeyEvent.VK_COLON :KeyEvent.VK_SEMICOLON
                    , isShiftPressed|isCapsLockPressed ? KeyEvent.VK_QUOTEDBL : KeyEvent.VK_QUOTE
            };
            List<TextComponent> components = new ArrayList<>(row.length + 2);
            components.add(composeKey(KeyEvent.VK_CAPS_LOCK, "CAPS"));
            for (int i : row) {
                components.add(composeKey(i, null));
            }
            components.add(composeKey(KeyEvent.VK_ENTER, "ENTER"));
            rows.add(spaceSeparatedRow(components.<TextComponent>toArray(new TextComponent[0])));
        }
        {
            char[] row = new char[]{
                    'Z', 'X', 'C', 'V' , 'B', 'N' ,'M', KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH
            };
            List<TextComponent> components = new ArrayList<>(row.length + 2);
            components.add(composeKey(KeyEvent.VK_SHIFT, "SHIFT"));
            for (char i : row) {
                components.add(composeKey(i, null));
            }
            components.add(composeKey(KeyEvent.VK_SHIFT, "SHIFT"));
            rows.add(spaceSeparatedRow(components.<TextComponent>toArray(new TextComponent[0])));
        }
        {
            List<TextComponent> components = new ArrayList<>();
            components.add(composeKey(KeyEvent.VK_CONTROL, "CTRL"));
            components.add(composeKey(KeyEvent.VK_ALT, "ALT"));
            components.add(composeKey(KeyEvent.VK_SPACE, "        SPACE        "));
            components.add(composeKey(KeyEvent.VK_ALT, "ALT"));
            components.add(composeKey(KeyEvent.VK_CONTROL, "CTRL"));
//            components.add(bindWASD());
            rows.add(spaceSeparatedRow(components.<TextComponent>toArray(new TextComponent[0])));
        }
        for (int i =0; i <10; i++) p.sendMessage("§f");
        rows.forEach(p.spigot()::sendMessage);
    }

    public TextComponent bindWASD() {
        TextComponent tc = new TextComponent("[ Bind WASD ]");
        tc.setBold(true);
        tc.setColor(isBound ? ChatColor.RED : ChatColor.GREEN);
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] {new TextComponent("Click on this to bind your WASD input to arrow input")}));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/keyclick "+uid.toString()+" 999999999"));
        return tc;
    }

    public TextComponent composeKey(int keyEventKey, String key) {
        TextComponent tc = new TextComponent("["+(key != null ? key : shiftModifier.containsKey(keyEventKey) ? shiftModifier.get(keyEventKey)[isShiftPressed|isCapsLockPressed ? 1:0] : "into the unknown")+ "]");
        tc.setBold(true);
        tc.setColor(((shiftModifier.containsKey(keyEventKey) && (isShiftPressed | isCapsLockPressed))
                || (keyEventKey == KeyEvent.VK_CONTROL && isCtrlPressed)
                || (keyEventKey == KeyEvent.VK_ALT && isAltPressed )
                || (keyEventKey == KeyEvent.VK_CAPS_LOCK && isCapsLockPressed )
                || (keyEventKey == KeyEvent.VK_SHIFT && isShiftPressed)) ? ChatColor.RED : ChatColor.GREEN);
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/keyclick "+uid.toString()+" "+keyEventKey));
        return tc;
    }

    public TextComponent spaceSeparatedRow(TextComponent[] components) {
        TextComponent finalComponent = new TextComponent();
        for (TextComponent textComponent:components) {
            finalComponent.addExtra(textComponent);
            finalComponent.addExtra(" ");
        }
        return finalComponent;
    }
}
