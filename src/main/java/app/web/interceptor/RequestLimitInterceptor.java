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

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import app.web.ErrorMessage;
import app.web.ErrorMessageException;
import app.web.util.IpUtils;
import infra.http.HttpStatus;
import infra.http.MediaType;
import infra.http.ResponseEntity;
import infra.lang.Assert;
import infra.lang.Constant;
import infra.lang.Nullable;
import infra.util.ConcurrentReferenceHashMap;
import infra.util.MapCache;
import infra.web.HandlerInterceptor;
import infra.web.InterceptorChain;
import infra.web.RequestContext;
import infra.web.handler.method.HandlerMethod;

/**
 * Web 限流拦截器实现
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 21:44
 */
public class RequestLimitInterceptor implements HandlerInterceptor {

  static final MapCache<HandlerMethod, RequestLimit, Object> requestLimitConfigCache = new MapCache<>(
          new ConcurrentReferenceHashMap<>(128), RequestLimitInterceptor::findRequestLimit);

  private int maxCacheSize = 1024;

  private String defaultErrorMessage = "操作频繁";

  private final Clock clock = Clock.systemUTC();

  private final ExpiredChecker expiredChecker = new ExpiredChecker();

  private final ConcurrentHashMap<RequestKey, RequestLimitEntry> requestLimitCache = new ConcurrentHashMap<>();

  public void setDefaultErrorMessage(String defaultErrorMessage) {
    Assert.notNull(defaultErrorMessage, "默认的错误消息不能为空");
    this.defaultErrorMessage = defaultErrorMessage;
  }

  public void setMaxCacheSize(int maxCacheSize) {
    Assert.isTrue(maxCacheSize > 0, "最大缓存数不能小于0");
    this.maxCacheSize = maxCacheSize;
  }

  @Nullable
  @Override
  public Object intercept(RequestContext request, InterceptorChain chain) throws Throwable {
    HandlerMethod handlerMethod = HandlerMethod.unwrap(chain.getHandler());
    if (handlerMethod != null) {
      RequestLimit requestLimit = requestLimitConfigCache.get(handlerMethod);
      if (requestLimit != null && hasTooManyRequests(request, handlerMethod, requestLimit)) {
        return writeTooManyRequests(requestLimit, handlerMethod);
      }
      //不需要限流
    }

    // next in the chain
    return chain.proceed(request);
  }

  private ResponseEntity<ErrorMessage> writeTooManyRequests(RequestLimit requestLimit, HandlerMethod handler) {
    String errorMessage = requestLimit.errorMessage();
    if (Constant.DEFAULT_NONE.equals(errorMessage)) {
      errorMessage = defaultErrorMessage;
    }

    if (!handler.isResponseBody()) {
      throw ErrorMessageException.failed(errorMessage, HttpStatus.TOO_MANY_REQUESTS);
    }

    // X-RateLimit-Limit: The maximum number of requests you're permitted to make per hour.
    // X-RateLimit-Remaining: The number of requests remaining in the current rate limit window.
    // X-RateLimit-Reset: the time at which the current rate limit window resets in UTC epoch seconds

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorMessage.failed(errorMessage));
  }

  private void checkMaxCacheLimit() {
    if (requestLimitCache.size() >= maxCacheSize) {
      expiredChecker.removeExpired(clock.instant());
    }
  }

  /**
   * 接口的访问频次限制
   */
  private boolean hasTooManyRequests(RequestContext request, HandlerMethod handler, RequestLimit requestLimit) {
    Instant now = clock.instant();
    expiredChecker.checkIfNecessary(now);

    Method method = handler.getMethod();
    String ip = IpUtils.remoteAddress(request);
    RequestKey key = new RequestKey(ip, method);

    return requestLimitCache.computeIfAbsent(key, requestKey -> new RequestLimitEntry(requestLimit))
            .isExceeded(now);
  }

  /**
   * Check for expired entry and remove them.
   */
  private void removeExpiredEntries() {
    expiredChecker.removeExpired(clock.instant());
  }

  @Nullable
  private static RequestLimit findRequestLimit(HandlerMethod handlerMethod) {
    if (handlerMethod.hasMethodAnnotation(RequestLimit.class)) {
      return handlerMethod.getMethodAnnotation(RequestLimit.class);
    }
    return handlerMethod.getBeanType().getAnnotation(RequestLimit.class);
  }

  record RequestKey(String ip, Method action) {

  }

  class RequestLimitEntry {
    public final int maxCount;

    public volatile int requestCount;

    public Instant lastAccessTime = Instant.now(clock);

    public final Duration timeout;

    RequestLimitEntry(RequestLimit requestLimit) {
      long timeout = requestLimit.timeout();
      TimeUnit timeUnit = requestLimit.unit();
      this.maxCount = requestLimit.count();
      this.timeout = Duration.of(timeout, timeUnit.toChronoUnit());
    }

    public boolean isNew() {
      return requestCount == 0;
    }

    // test Exceeded maximum number of requests
    public synchronized boolean isExceeded(Instant now) {
      // 判断是不是已经在规定时间之外了
      if (checkExpired(now)) {
        // 在规定时间之外直接返回不限流
        requestCount = 0;
        lastAccessTime = Instant.now(clock);
        return false;
      }
      int requestCount = this.requestCount;
      this.requestCount++;
      // 在超时时间范围内，请求次数大于最大次数就需要限制
      return requestCount >= maxCount;
    }

    public boolean isExpired() {
      return isExpired(clock.instant());
    }

    private boolean isExpired(Instant now) {
      return checkExpired(now);
    }

    private boolean checkExpired(Instant currentTime) {
      return currentTime.minus(timeout).isAfter(lastAccessTime);
    }

  }

  private final class ExpiredChecker {

    /** Max time between expiration checks. */
    private static final int CHECK_PERIOD = 10;

    private final ReentrantLock lock = new ReentrantLock();

    private Instant checkTime = clock.instant().plus(CHECK_PERIOD, ChronoUnit.SECONDS);

    public void checkIfNecessary() {
      checkIfNecessary(clock.instant());
    }

    public void checkIfNecessary(Instant now) {
      if (checkTime.isBefore(now)) {
        removeExpired(now);
      }
    }

    public void removeExpired(Instant now) {
      if (!requestLimitCache.isEmpty()) {
        if (lock.tryLock()) {
          try {
            Iterator<RequestLimitEntry> iterator = requestLimitCache.values().iterator();
            while (iterator.hasNext()) {
              RequestLimitEntry limitEntry = iterator.next();
              if (limitEntry.isExpired(now) && limitEntry.isNew()) {
                iterator.remove();
              }
            }
          }
          finally {
            this.checkTime = now.plus(CHECK_PERIOD, ChronoUnit.MILLIS);
            lock.unlock();
          }
        }
      }
    }
  }

}
