package ek.osnb.demo.coffeeshop.ai.assistant;

import ek.osnb.demo.coffeeshop.ai.tools.OrderTools;
import ek.osnb.demo.coffeeshop.ai.tools.TimeTools;
import ek.osnb.demo.coffeeshop.common.NotFoundException;
import ek.osnb.demo.coffeeshop.customer.CustomerApi;
import ek.osnb.demo.coffeeshop.customer.CustomerInfo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {
    private final ChatClient chatClient;
    private final CustomerApi customers;

    AssistantController(ChatClient.Builder builder, TimeTools timeTools, OrderTools orderTools,
                        ChatMemory chatMemory, CustomerApi customers) {
        this.customers = customers;
        this.chatClient = builder
                .defaultTools(timeTools, orderTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @PostMapping("/chat")
    AssistantResponse chat (Authentication auth, @RequestBody AssistantRequest request){
        CustomerInfo customer = customers.findCustomerByEmail(auth.getName())
                .orElseThrow(() -> new NotFoundException("No customer details saved yet"));

        String reply = chatClient.prompt()
                .user(request.message())
                .toolContext(Map.of("customerId", customer.id()))
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        auth.getName()))
                .call()
                .content();
        return new AssistantResponse(reply);
    }
}
