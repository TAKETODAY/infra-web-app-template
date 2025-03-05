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

package app.web.interceptor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import infra.lang.Constant;
import infra.web.annotation.Interceptor;

/**
 * Web 请求限流拦截器
 * <p>
 * 默认值：一秒钟请求一次
 * <p>
 * 在设置的时间段内可以请求的次数, 超出部分将 {@link #errorMessage()} 返回给客户端,
 * 默认错误消息：{@link RequestLimitInterceptor#defaultErrorMessage}
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @see RequestLimitInterceptor#setDefaultErrorMessage(String)
 * @since 1.0 2025/3/4 21:44
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(RequestLimitInterceptor.class)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RequestLimit {

  /**
   * 允许访问的次数，默认值 1 次
   */
  int count() default 1;

  /**
   * 时间段内能访问 {@link #count()} 次
   */
  long timeout() default 1;

  /**
   * {@link #timeout} 值的时间单位, 默认：秒
   */
  TimeUnit unit() default TimeUnit.SECONDS;

  /**
   * 限流触发时的错误消息
   */
  String errorMessage() default Constant.DEFAULT_NONE;
}