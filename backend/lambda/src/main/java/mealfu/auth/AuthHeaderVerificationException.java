package mealfu.auth;

public class AuthHeaderVerificationException extends Exception {
    public AuthHeaderVerificationException() {
    }

    public AuthHeaderVerificationException(String message) {
        super(message);
    }

    public AuthHeaderVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthHeaderVerificationException(Throwable cause) {
        super(cause);
    }

    public AuthHeaderVerificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
