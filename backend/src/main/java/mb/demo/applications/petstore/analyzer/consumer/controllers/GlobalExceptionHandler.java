package mb.demo.applications.petstore.analyzer.consumer.controllers;

import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.consumer.service.InvalidEventException;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.ErrorResponse;
import org.apache.camel.CamelExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final BigDecimal REJECTED_EVENT_CODE = BigDecimal.valueOf(2000);
    private static final BigDecimal UNEXPECTED_ERROR_CODE = BigDecimal.valueOf(5000);

    /**
     * The producer template wraps whatever is thrown inside the route, so a rejection has to be
     * unwrapped before it can be mapped onto a response.
     */
    @ExceptionHandler(CamelExecutionException.class)
    public ResponseEntity<ErrorResponse> handleCamelExecutionException(final CamelExecutionException exception) {
        if (exception.getCause() instanceof InvalidEventException invalidEventException) {
            return handleInvalidEventException(invalidEventException);
        }
        log.error("processing the event failed unexpectedly", exception);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_CODE, "An error has occurred");
    }

    @ExceptionHandler(InvalidEventException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEventException(final InvalidEventException exception) {
        log.info("rejecting event: {}", exception.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, REJECTED_EVENT_CODE, exception.getMessage());
    }

    /**
     * An event violating the contract's constraints is rejected before it reaches the route.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        final String message = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> "event property '" + fieldError.getField() + "' " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.info("rejecting event: {}", message);
        return errorResponse(HttpStatus.BAD_REQUEST, REJECTED_EVENT_CODE, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception) {
        log.info("rejecting unreadable event: {}", exception.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, REJECTED_EVENT_CODE, "event could not be read as json");
    }

    private static ResponseEntity<ErrorResponse> errorResponse(final HttpStatus status, final BigDecimal code, final String message) {
        return ResponseEntity.status(status).body(new ErrorResponse()
                .code(code)
                .message(message)
                .receivedAt(OffsetDateTime.now(ZoneOffset.UTC)));
    }
}
