package hr.java.web.prosport.exception;

public class PayPalException extends RuntimeException {

    public PayPalException(String message) {
        super(message);
    }

    public PayPalException(String message, Throwable cause) {
        super(message, cause);
    }
}