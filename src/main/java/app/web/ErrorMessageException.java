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

package app.web;

import java.util.function.Supplier;

import infra.core.NoStackTraceRuntimeException;
import infra.http.HttpStatus;
import infra.http.HttpStatusCode;
import infra.lang.Assert;
import infra.lang.Nullable;
import infra.web.HttpStatusProvider;

/**
 * 用于展示 message 字段到前端的异常
 *
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/7/19 22:01
 */
public class ErrorMessageException extends NoStackTraceRuntimeException implements HttpStatusProvider {

  private final HttpStatus status;

  public ErrorMessageException(@Nullable String msg) {
    this(msg, null, HttpStatus.BAD_REQUEST);
  }

  public ErrorMessageException(@Nullable String msg, @Nullable Throwable cause, HttpStatus status) {
    super(msg, cause);
    Assert.notNull(status, "http status is required");
    this.status = status;
  }

  @Override
  public HttpStatusCode getStatusCode() {
    return status;
  }

  public static ErrorMessageException failed(String message) {
    return new ErrorMessageException(message);
  }

  public static ErrorMessageException failed(String message, HttpStatus status) {
    return new ErrorMessageException(message, null, status);
  }

  public static void notNull(@Nullable Object obj, String message) {
    if (obj == null) {
      throw ErrorMessageException.failed(message, HttpStatus.NOT_FOUND);
    }
  }

  public static void notNull(@Nullable Object obj, Supplier<String> supplier) {
    if (obj == null) {
      throw new ErrorMessageException(supplier.get());
    }
  }

}
