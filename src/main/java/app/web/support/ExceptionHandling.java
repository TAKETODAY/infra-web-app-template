/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.web.support;

import java.sql.SQLException;

import app.web.ErrorMessage;
import app.web.ErrorMessageException;
import infra.beans.TypeMismatchException;
import infra.dao.DataAccessResourceFailureException;
import infra.http.HttpHeaders;
import infra.http.HttpStatus;
import infra.http.HttpStatusCode;
import infra.http.ResponseEntity;
import infra.http.converter.HttpMessageNotReadableException;
import infra.lang.Nullable;
import infra.validation.ObjectError;
import infra.web.InternalServerException;
import infra.web.NotFoundHandler;
import infra.web.RequestContext;
import infra.web.ResponseStatusException;
import infra.web.annotation.ExceptionHandler;
import infra.web.annotation.ResponseStatus;
import infra.web.annotation.RestControllerAdvice;
import infra.web.bind.MethodArgumentNotValidException;
import infra.web.bind.MissingRequestParameterException;
import infra.web.bind.NotMultipartRequestException;
import infra.web.bind.resolver.ParameterConversionException;
import infra.web.handler.ResponseEntityExceptionHandler;
import infra.web.handler.SimpleNotFoundHandler;

/**
 * Web 异常处理
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:43
 */
@RestControllerAdvice
class ExceptionHandling extends ResponseEntityExceptionHandler implements NotFoundHandler {

  private static final ErrorMessage illegalArgument = ErrorMessage.failed("参数错误");

  private static final ErrorMessage internalServerError = ErrorMessage.failed("服务器内部异常");

  @Nullable
  @Override
  public Object handleNotFound(RequestContext request) {
    request.setStatus(HttpStatus.NOT_FOUND);
    SimpleNotFoundHandler.logNotFound(request);
    return ErrorMessage.failed("资源找不到");
  }

  @ExceptionHandler(ErrorMessageException.class)
  public ResponseEntity<ErrorMessage> errorMessage(ErrorMessageException errorMessage) {
    HttpStatusCode httpStatus = errorMessage.getStatusCode();
    return ResponseEntity.status(httpStatus)
            .body(ErrorMessage.failed(errorMessage.getMessage()));
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ErrorMessage illegalArgument() {
    return illegalArgument;
  }

  @ExceptionHandler(InternalServerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorMessage internal(InternalServerException internal) {
    logger.error("服务器内部错误", internal);
    return ErrorMessage.failed(internal.getReason());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> statusException(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
            .headers(e.getHeaders())
            .body(ErrorMessage.failed(e.getReason()));
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorMessage error(Throwable exception) {
    logger.error("An Exception occurred", exception);
    if (exception instanceof SQLException) {
      return internalServerError;
    }
    return ErrorMessage.failed("服务器内部错误,稍后重试");
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(NullPointerException.class)
  public ErrorMessage nullPointer(NullPointerException exception) {
    logger.error("Null Pointer occurred", exception);
    return internalServerError;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ParameterConversionException.class)
  public ErrorMessage conversion() {
    return ErrorMessage.failed("参数转换失败");
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(NotMultipartRequestException.class)
  public ErrorMessage notMultipart() {
    return ErrorMessage.failed("请求错误");
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(DataAccessResourceFailureException.class)
  public ErrorMessage dataAccessException(DataAccessResourceFailureException accessException) {
    logger.error("数据库连接出错", accessException);
    return ErrorMessage.failed("数据库连接出错");
  }

  @Nullable
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
          HttpHeaders headers, HttpStatusCode status, RequestContext request) {
    if (ex.hasErrors()) {
      ObjectError objectError = ex.getGlobalError();
      if (objectError == null) {
        objectError = ex.getFieldError();
      }
      if (objectError != null) {
        String defaultMessage = objectError.getDefaultMessage();
        return handleExceptionInternal(ex, ErrorMessage.failed(defaultMessage), headers, status, request);
      }
    }
    return handleExceptionInternal(ex, illegalArgument, headers, status, request);
  }

  @Nullable
  @Override
  protected ResponseEntity<Object> handleMissingRequestParameter(MissingRequestParameterException ex,
          HttpHeaders headers, HttpStatusCode status, RequestContext request) {
    return handleExceptionInternal(ex, ErrorMessage.failed("缺少参数'" + ex.getParameterName() + "'"), headers, status, request);
  }

  @Nullable
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
          HttpHeaders headers, HttpStatusCode status, RequestContext request) {
    return handleExceptionInternal(ex, ErrorMessage.failed("参数读取错误，请检查格式"), headers, status, request);
  }

  @Nullable
  @Override
  protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
          HttpHeaders headers, HttpStatusCode status, RequestContext request) {
    return handleExceptionInternal(ex, ErrorMessage.failed("参数错误，请检查"), headers, status, request);
  }

}
