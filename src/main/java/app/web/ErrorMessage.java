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

import infra.core.style.ToStringBuilder;
import infra.lang.Nullable;

/**
 * 错误消息 model
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:36
 */
public final class ErrorMessage implements HttpResult {

  @Nullable
  private final String message;

  ErrorMessage(@Nullable String message) {
    this.message = message;
  }

  public static ErrorMessage failed(@Nullable String message) {
    return new ErrorMessage(message);
  }

  @Nullable
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return ToStringBuilder.forInstance(this)
            .append("message", message)
            .toString();
  }

}
