package br.com.zup.edu.nossalojavirtual.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class HandlerException {

    Logger logger = LoggerFactory.getLogger(HandlerException.class);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValid(MethodArgumentNotValidException ex){
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        CustomErrorMessage customErrorMessage = new CustomErrorMessage();

        fieldErrors.forEach(customErrorMessage::adicionar);

        logger.warn(customErrorMessage.getMensagens().toString());

        return ResponseEntity.badRequest().body(customErrorMessage);
    }
}
