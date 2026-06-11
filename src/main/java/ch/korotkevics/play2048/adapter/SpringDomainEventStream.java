package ch.korotkevics.play2048.adapter;

import ch.korotkevics.play2048.domain.service.DomainEventStream;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public final class SpringDomainEventStream implements DomainEventStream {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventStream(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(GameEvent event) {
        publisher.publishEvent(event);
    }
}
