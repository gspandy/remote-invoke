package com.haotian.remote;

public class RemoteInvokeException extends RuntimeException {
    public RemoteInvokeException(String message) {
        super(message);
    }

    public RemoteInvokeException(String message, Throwable e) {
        super(message, e);
    }
}
