package synk.meeteam.global.common.exception;

import static synk.meeteam.global.common.exception.GlobalExceptionType.INVALID_INPUT_VALUE;
import static synk.meeteam.global.common.exception.GlobalExceptionType.SERVER_ERROR;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String LOG_FORMAT = "%s";

    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<ExceptionResponse> handleBaseException(BaseCustomException e) {
        ExceptionType exceptionType = e.getExceptionType();
        log.error(String.format(LOG_FORMAT, e.getMessage()), e);
        return ResponseEntity.status(exceptionType.httpStatus())
                .body(ExceptionResponse.of(exceptionType.name(), exceptionType.message()));
    }

    @ExceptionHandler(ConstraintViolationException.class)  // @Notnull 오류
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException e) {
        ExceptionType exceptionType = INVALID_INPUT_VALUE;
        log.error(String.format(LOG_FORMAT, e.getMessage()), e);
        return ResponseEntity.status(exceptionType.httpStatus())
                .body(ExceptionResponse.of(exceptionType.name(), exceptionType.message()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)  // null value 오류
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ExceptionType exceptionType = INVALID_INPUT_VALUE;
        log.error(String.format(LOG_FORMAT, e.getMessage()), e);
        return ResponseEntity.status(exceptionType.httpStatus())
                .body(ExceptionResponse.of(exceptionType.name(), exceptionType.message()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)  // null value 오류
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ExceptionType exceptionType = INVALID_INPUT_VALUE;
        log.error(String.format(LOG_FORMAT, e.getMessage()), e);
        return ResponseEntity.status(exceptionType.httpStatus())
                .body(ExceptionResponse.of(exceptionType.name(), exceptionType.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ExceptionType exceptionType = INVALID_INPUT_VALUE;
        log.error(String.format(LOG_FORMAT, e.getMessage()), e);
        return ResponseEntity.status(exceptionType.httpStatus())
                .body(ExceptionResponse.of(exceptionType.name(), exceptionType.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        log.error(String.format(LOG_FORMAT, e.getMessage()), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.of(SERVER_ERROR.name(), SERVER_ERROR.message()));
    }

}

