package com.dreamypatisiel.devdevdev.eventlistener;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class HibernateEventListenerConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final WriteQueryEventListener writeQueryEventListener;

    @PostConstruct
    public void registerListeners() {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        assert registry != null;
        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(writeQueryEventListener);
        registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(writeQueryEventListener);
        registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener(writeQueryEventListener);
    }
}
