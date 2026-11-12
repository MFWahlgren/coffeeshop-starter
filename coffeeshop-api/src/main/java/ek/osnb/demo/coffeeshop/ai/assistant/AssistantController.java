package ek.osnb.demo.coffeeshop.ai.assistant;

import ek.osnb.demo.coffeeshop.ai.tools.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {
    private final ChatClient chatClient;

    AssistantController(ChatClient.Builder builder, TimeTools timeTools, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultTools(timeTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @PostMapping("/chat")
    AssistantResponse chat (Authentication auth, @RequestBody AssistantRequest request){
        String reply = chatClient.prompt()
                .user(request.message())
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        auth.getName()
                ))
                .call()
                .content();
        return new AssistantResponse(reply);
    }
}
