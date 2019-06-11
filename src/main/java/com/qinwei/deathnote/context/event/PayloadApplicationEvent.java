package com.qinwei.deathnote.context.event;

/**
 * @author qinwei
 * @date 2019-06-11
 */
public class PayloadApplicationEvent<T> extends ApplicationEvent {

    private final T payload;

    public PayloadApplicationEvent(Object source, T payload) {
        super(source);
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        this.payload = payload;
    }


    public T getPayload() {
        return this.payload;
    }
}
