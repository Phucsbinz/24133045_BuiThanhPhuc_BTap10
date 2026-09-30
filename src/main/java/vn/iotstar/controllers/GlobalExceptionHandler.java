package vn.iotstar.controllers;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(Exception.class) public ProblemDetail handleSecurityException(Exception e){
  int status=500; String detail="Lỗi xử lý nội bộ.";
  if(e instanceof AccountStatusException){status=403;detail="Tài khoản bị khóa hoặc không hoạt động.";}
  else if(e instanceof AuthenticationException){status=401;detail=e instanceof BadCredentialsException?e.getMessage():"Thông tin đăng nhập hoặc JWT không hợp lệ.";}
  else if(e instanceof AccessDeniedException){status=403;detail="Không có quyền truy cập tài nguyên.";}
  else if(e instanceof MethodArgumentNotValidException || e instanceof HttpMessageNotReadableException){status=400;detail="Dữ liệu yêu cầu không hợp lệ.";}
  else if(e instanceof DataIntegrityViolationException){status=409;detail="Email đã được đăng ký.";}
  else if(e instanceof ResponseStatusException response){status=response.getStatusCode().value();detail=response.getReason();}
  if(status==500) org.slf4j.LoggerFactory.getLogger(getClass()).error("Request failed",e);
  var problem=ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status),detail);problem.setProperty("description",detail);return problem;
 }
}

