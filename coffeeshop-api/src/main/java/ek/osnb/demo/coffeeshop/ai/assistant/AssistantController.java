package ek.osnb.demo.coffeeshop.ai.assistant;

import ek.osnb.demo.coffeeshop.ai.tools.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {
    private final ChatClient chatClient;

    AssistantController(ChatClient.Builder builder, TimeTools timeTools) {
        this.chatClient = builder
                .defaultTools(timeTools)
                .build();
    }

    @PostMapping("/chat")
    AssistantResponse chat (@RequestBody AssistantRequest request){
        String reply = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return new AssistantResponse(reply);
    }
}
