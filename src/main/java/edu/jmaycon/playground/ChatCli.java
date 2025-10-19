package edu.jmaycon.playground;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@ConditionalOnProperty(name = "cli.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
class ChatCli implements CommandLineRunner {

    private final ChatClient chat;
    private final ApplicationEventPublisher events;
    private final BusinessCardTools businessCardTools;

    @Override
    public void run(String... args) throws Exception {
        try (var in = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("CLI Chat. Type 'exit' to quit.");
            while (true) {
                System.out.print("\n> ");
                var prompt = in.readLine();
                if (prompt == null || "exit".equalsIgnoreCase(prompt.trim())) break;

                Flux<String> stream = chat.prompt().user(prompt).stream().content();

                stream.doOnNext(t -> events.publishEvent(new AiMessageEvent(this, t, false)))
                        .doOnComplete(() -> events.publishEvent(new AiMessageEvent(this, "", true)))
                        .blockLast();
            }
        }
    }
}
