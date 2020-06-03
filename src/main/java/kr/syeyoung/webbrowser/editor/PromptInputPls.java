package kr.syeyoung.webbrowser.editor;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class PromptInputPls implements Prompt {
    @Override
    public String getPromptText(ConversationContext context) {
        return "새 주소를 입력해주세요:: 취소는 /quit :: 현제주소: "+context.getSessionData("curr");
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
