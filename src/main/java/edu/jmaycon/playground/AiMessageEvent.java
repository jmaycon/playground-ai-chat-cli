package edu.jmaycon.playground;

import org.springframework.context.ApplicationEvent;

public class AiMessageEvent extends ApplicationEvent {
    private final String text;
    private final boolean done;

    public AiMessageEvent(Object src, String text, boolean done) {
        super(src);
        this.text = text;
        this.done = done;
    }

    public String text() {
        return text;
    }

    public boolean done() {
        return done;
    }
}
