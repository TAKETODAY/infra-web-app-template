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

import app.model.User;
import app.web.UnauthorizedException;
import infra.session.SessionHandlerInterceptor;
import infra.session.SessionManager;
import infra.web.RequestContext;
import infra.web.resource.ResourceHttpRequestHandler;

/**
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 21:43
 */
public class LoginInterceptor extends SessionHandlerInterceptor {

  // Authorization

  public LoginInterceptor(SessionManager sessionManager) {
    super(sessionManager);
  }

  @Override
  public boolean beforeProcess(RequestContext request, Object handler) throws Throwable {
    if (User.isPresent(getSession(request, false))) {
      return true;
    }

    if (handler instanceof ResourceHttpRequestHandler) {
      request.setStatus(404);
      request.getWriter().write("Not Found");
      return false;
    }
    throw new UnauthorizedException();
  }

}
