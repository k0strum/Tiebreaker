package com.Tiebreaker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.Tiebreaker.exception.auth.LoginException;
import com.Tiebreaker.dto.auth.ErrorResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * @Valid 어노테이션을 사용한 유효성 검사 실패 시 발생하는 예외를 처리합니다.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    errors.put("message", errorMessage);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * 중복 데이터와 같이 상태가 올바르지 않은 요청에 대한 예외를 처리합니다. (409 Conflict)
   * 예: 이미 존재하는 이메일로 회원가입 시도
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("message", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.CONFLICT); // 409 Conflict 상태 코드 사용
  }

  /**
   * 잘못된 인자값으로 요청 시 발생하는 예외를 처리합니다. (400 Bad Request)
   * 예: 존재하지 않는 회원 조회, 잘못된 비밀번호 입력
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("message", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * 로그인 관련 예외를 세분화하여 처리합니다.
   */
  @ExceptionHandler(LoginException.class)
  public ResponseEntity<ErrorResponse> handleLoginException(LoginException ex) {
    ErrorResponse errorResponse = ErrorResponse.builder()
      .message(ex.getMessage())
      .error(ex.getErrorType().name())
      .status(HttpStatus.BAD_REQUEST.value())
      .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
  
  // 여기에 다른 종류의 예외 처리 핸들러도 추가할 수 있습니다.
}