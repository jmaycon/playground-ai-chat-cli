package edu.jmaycon.playground;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class ConsolePrinter {
    @EventListener
    void onAi(AiMessageEvent e) {
        System.out.print(e.text());
        if (e.done()) System.out.println();
    }
}
