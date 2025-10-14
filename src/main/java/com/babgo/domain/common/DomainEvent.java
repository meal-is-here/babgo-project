package com.babgo.domain.common;

public interface DomainEvent {

    String eventKey();
    String eventName();
    Long userId();

}
