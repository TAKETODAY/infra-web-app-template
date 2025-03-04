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

package app.web.handler;

import java.util.Optional;

import app.model.User;
import infra.lang.Nullable;
import infra.session.SessionManager;
import infra.session.SessionManagerOperations;
import infra.session.WebSession;
import infra.stereotype.Component;
import infra.web.RequestContext;
import infra.web.RequestContextHolder;

/**
 * 关于 获取用户会话 的处理器
 * <p>
 * 可以获取当前登录用户
 * </p>
 * 该类一般在控制层使用
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 21:55
 */
@Component
public class UserSessionResolver extends SessionManagerOperations {

  public UserSessionResolver(SessionManager sessionManager) {
    super(sessionManager);
  }

  /**
   * 获取当前登录的用户
   */
  @Nullable
  public User getLoginUser() {
    return getLoginUser(RequestContextHolder.getRequired());
  }

  /**
   * 获取当前登录的用户
   */
  @Nullable
  public User getLoginUser(RequestContext request) {
    WebSession session = getSession(request, false);
    if (session != null) {
      return User.find(session);
    }
    return null;
  }

  /**
   * 获取当前登录的用户
   */
  public Optional<User> loginUser() {
    return Optional.ofNullable(getLoginUser());
  }

  /**
   * 获取当前登录的用户
   */
  public Optional<User> loginUser(RequestContext request) {
    return Optional.ofNullable(getLoginUser(request));
  }

}

