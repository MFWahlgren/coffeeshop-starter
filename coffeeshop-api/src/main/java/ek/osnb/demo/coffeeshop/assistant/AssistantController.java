package ek.osnb.demo.coffeeshop.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {
    private final ChatClient chatClient;

    AssistantController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping("/chat")
    String chat (@RequestBody AssistantRequest request){
        return chatClient.prompt()
                .user(request.message())
                .call()
                .content();
    }
}
