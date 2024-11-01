package com.avella.store.merchant.infrastructure.event;

import com.avella.store.merchant.infrastructure.repository.JpaEventRepository;
import com.avella.store.merchant.infrastructure.repository.model.EventDb;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.function.Consumer;

@Component
public class DirectDispatchEventListener {

    private final Consumer<EventDb> dispatchAction;
    private final JpaEventRepository jpaRepository;

    public DirectDispatchEventListener(Consumer<EventDb> dispatchAction, JpaEventRepository jpaRepository) {
        this.dispatchAction = dispatchAction;
        this.jpaRepository = jpaRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dispatchEvent(EventDb event) {
        dispatchAction.accept(event);

        jpaRepository.deleteById(event.getId());
    }
}
