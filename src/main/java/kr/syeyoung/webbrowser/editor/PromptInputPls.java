package kr.syeyoung.webbrowser.editor;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class PromptInputPls implements Prompt {
    private String ask;
    public PromptInputPls(String ask) {
        this.ask = ask;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return ask+"\n\nTo cancel, type 'quit'";
    }

    @Override
    public boolean blocksForInput(ConversationContext context) {
        return true;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        context.setSessionData("curr", input);
        ((Runnable)context.getSessionData("callback")).run();
        return Prompt.END_OF_CONVERSATION;
    }
}
