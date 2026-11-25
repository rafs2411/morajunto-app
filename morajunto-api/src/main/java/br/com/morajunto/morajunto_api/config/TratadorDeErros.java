package br.com.morajunto.morajunto_api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", erros);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> status(ResponseStatusException ex) {
        String mensagem = ex.getReason() != null ? ex.getReason() : "Erro na requisição";
        return resposta(ex.getStatusCode(), mensagem, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "O corpo da requisição está inválido ou mal formatado", Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "O parâmetro '" + ex.getName() + "' está em formato inválido", Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroFaltando(MissingServletRequestParameterException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Informe o parâmetro '" + ex.getParameterName() + "'", Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido para este endereço", Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> enderecoNaoEncontrado(NoResourceFoundException ex) {
        return resposta(HttpStatus.NOT_FOUND, "Endereço não encontrado", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> conflitoNoBanco(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "A operação conflita com dados já cadastrados", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", Map.of());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatusCode status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), mensagem, erros));
    }
}