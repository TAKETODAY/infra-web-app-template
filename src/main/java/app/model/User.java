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

package app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import app.web.UnauthorizedException;
import infra.core.AttributeAccessor;
import infra.lang.Nullable;
import infra.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:49
 */
@Data
@Table("t_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BasicModel {

  public static final String KEY = "user-session";

  private String name;

  private String username;

  @JsonIgnore
  private String password;

  private String avatar;

  private String introduce;

  private UserStatus status;

  /**
   * Bind this instance to AttributeAccessor
   *
   * @param accessor session or request
   */
  public void bindTo(AttributeAccessor accessor) {
    accessor.setAttribute(KEY, this);
  }

  // Static

  /**
   * 查找登录会话
   */
  @Nullable
  public static User find(AttributeAccessor accessor) {
    Object attribute = accessor.getAttribute(KEY);
    if (attribute instanceof User user) {
      return user;
    }
    return null;
  }

  public static User obtain(AttributeAccessor accessor) {
    User blogger = find(accessor);
    if (blogger == null) {
      throw new UnauthorizedException();
    }
    return blogger;
  }

  /**
   * 取消绑定
   */
  public static void unbind(AttributeAccessor accessor) {
    accessor.removeAttribute(KEY);
  }

  /**
   * 判断登录用户是否存在
   */
  public static boolean isPresent(@Nullable AttributeAccessor accessor) {
    return accessor != null && find(accessor) != null;
  }

}
