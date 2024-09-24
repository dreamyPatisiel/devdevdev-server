package com.dreamypatisiel.devdevdev.eventlistener;

import com.dreamypatisiel.devdevdev.aop.WriteOperationContext;
import org.hibernate.event.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Deprecated
@Component
@Profile("test")
public class WriteQueryEventListener implements PreInsertEventListener, PreUpdateEventListener, PreDeleteEventListener {

    private static final Logger log = LoggerFactory.getLogger(WriteQueryEventListener.class);

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        log.info("Insert operation detected on entity: {}", event.getEntity().getClass().getName());
        WriteOperationContext.setWriteOperationDetected(true);
        return false;
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        log.info("Update operation detected on entity: {}", event.getEntity().getClass().getName());
        WriteOperationContext.setWriteOperationDetected(true);

        return false;
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        log.info("Delete operation detected on entity: {}", event.getEntity().getClass().getName());
        WriteOperationContext.setWriteOperationDetected(true);
        return false;
    }
}
