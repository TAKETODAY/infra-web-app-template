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

import app.model.User;
import infra.lang.Nullable;

/**
 * 用户会话
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 21:31
 */
public class UserSession {

  @Nullable
  private User loginUser;

  public boolean isLoggedIn() {
    return loginUser != null;
  }

  /**
   * 获取登录用户ID，如果未登录将抛出 {@link UnauthorizedException}
   *
   * @throws UnauthorizedException 如果未登录将抛出
   */
  public long loginUserId() throws UnauthorizedException {
    return loginUser().getId();
  }

  /**
   * 获取登录用户信息，如果未登录将抛出 {@link UnauthorizedException}
   *
   * @throws UnauthorizedException 如果未登录将抛出
   */
  public User loginUser() throws UnauthorizedException {
    if (loginUser == null) {
      throw new UnauthorizedException();
    }
    return loginUser;
  }

  @Nullable
  public User getLoginUser() {
    return loginUser;
  }

  public void setLoginUser(@Nullable User loginUser) {
    this.loginUser = loginUser;
  }

}
