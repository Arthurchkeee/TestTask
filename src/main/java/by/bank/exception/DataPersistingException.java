package by.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataPersistingException extends RuntimeException {
    public DataPersistingException(String message) {
        super(message);
    }

    public DataPersistingException(String message, Throwable cause) {
        super(message, cause);
    }
}
