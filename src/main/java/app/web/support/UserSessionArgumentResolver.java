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

import java.util.Optional;

import app.model.User;
import app.web.UnauthorizedException;
import app.web.UserSession;
import app.web.interceptor.RequiresLogin;
import infra.lang.Nullable;
import infra.session.SessionManager;
import infra.session.SessionManagerOperations;
import infra.session.WebSession;
import infra.stereotype.Component;
import infra.web.RequestContext;
import infra.web.bind.resolver.ParameterResolvingStrategy;
import infra.web.handler.method.ResolvableMethodParameter;

/**
 * 登录信息参数解析
 * <p>支持以下参数类型：
 * <ul>
 *   <li>{@code User}没有登录会抛异常</li>
 *   <li>{@code UserSession}不会抛异常，使用{@link UserSession#isLoggedIn()}判断是否登录</li>
 *   <li>{@code Optional<User>}不会抛异常</li>
 *   <li>{@code @Nullable User}不会抛异常</li>
 *   <li>{@code @RequiresLogin User}没有登录会抛异常</li>
 *   <li>{@code @RequiresLogin UserSession}没有登录会抛异常</li>
 * </ul>
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 2019-07-25 00:56
 */
@Component
final class UserSessionArgumentResolver extends SessionManagerOperations implements ParameterResolvingStrategy {

  public UserSessionArgumentResolver(SessionManager sessionManager) {
    super(sessionManager);
  }

  @Override
  public boolean supportsParameter(ResolvableMethodParameter parameter) {
    if (parameter.is(Optional.class)) {
      var nested = parameter.nested();
      return nested.is(User.class);
    }

    return parameter.is(User.class)
            || parameter.is(UserSession.class);
  }

  @Nullable
  @Override
  public Object resolveArgument(RequestContext context, ResolvableMethodParameter parameter) {
    WebSession session = getSession(context, false);
    if (session != null) {
      if (parameter.is(Optional.class)) {
        // Optional<User>
        return Optional.ofNullable(User.find(session));
      }

      if (parameter.is(User.class)) {
        User user = User.find(session);
        if (user != null) {
          return user;
        }
        else if (parameter.isNotRequired()) {
          return null;
        }
        throw new UnauthorizedException();
      }
      else {
        // 使用了 UserSession，在没有登录情况下会抛出异常 UnauthorizedException
        UserSession info = new UserSession();
        User loginUser = User.find(session);
        if (loginUser != null) {
          info.setLoginUser(loginUser);
        }
        else if (parameter.hasParameterAnnotation(RequiresLogin.class)) {
          throw new UnauthorizedException();
        }
        return info;
      }
    }

    // session is null

    if (parameter.is(Optional.class)) {
      return Optional.empty();
    }

    if (parameter.is(User.class)) {
      if (parameter.isNotRequired()) {
        return null;
      }
      throw new UnauthorizedException();
    }

    if (parameter.hasParameterAnnotation(RequiresLogin.class)) {
      throw new UnauthorizedException();
    }

    return new UserSession();
  }

}
