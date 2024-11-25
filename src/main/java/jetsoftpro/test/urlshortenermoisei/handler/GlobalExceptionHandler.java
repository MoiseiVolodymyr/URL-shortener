package jetsoftpro.test.urlshortenermoisei.handler;

import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlExceptionResponse;
import jetsoftpro.test.urlshortenermoisei.exception.ShortUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ShortUrlException.class)
    public ResponseEntity<ShortUrlExceptionResponse> handleUserNotFoundException(ShortUrlException ex) {
        ShortUrlExceptionResponse errorDetails = new ShortUrlExceptionResponse(ex.getMessage(), Instant.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}
