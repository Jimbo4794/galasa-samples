package com.example.myapp.manager;

import dev.galasa.ManagerException;

public class MyAppException extends ManagerException {
    private static final long serialVersionUID = 1L;

    public MyAppException() {
    }

    public MyAppException(String message) {
        super(message);
    }

    public MyAppException(Throwable cause) {
        super(cause);
    }

    public MyAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyAppException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
