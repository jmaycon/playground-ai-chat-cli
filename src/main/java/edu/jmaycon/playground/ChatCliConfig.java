package edu.jmaycon.playground;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatCliConfig {

    @Bean
    Advisor rotatingMemoryAdvisor() {
        return MessageChatMemoryAdvisor.builder(
                        MessageWindowChatMemory.builder().maxMessages(20).build())
                .build();
    }

    @Bean
    ChatClient chatClient(
            ChatClient.Builder chatBuilder, BusinessCardTools businessCardTools, Advisor rotatingMemoryAdvisor) {
        return chatBuilder
                .defaultSystem(
                        """
            You help users find service providers (cleaning, construction, plumbing,
            electrical, painting, HVAC, landscaping, moving, pest control, handyman, IT).
            Always prefer tools when searching, filtering by service and price.
            Keep answers concise; include provider name, service, city, and estimated price.
            """)
                .defaultTools(businessCardTools)
                .defaultAdvisors(rotatingMemoryAdvisor)
                .build();
    }
}
