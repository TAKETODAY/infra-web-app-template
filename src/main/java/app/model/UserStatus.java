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

import infra.lang.Enumerable;
import infra.lang.Nullable;

/**
 * 用户状态
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:51
 */
public enum UserStatus implements Enumerable<Integer> {

  NORMAL(0, "正常"),

  INACTIVE(1, "账号尚未激活"),

  LOCKED(2, "账号被锁"),

  RECYCLE(3, "账号被冻结");

  private final int value;

  private final String desc;

  UserStatus(int value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  @Override
  public Integer getValue() {
    return value;
  }

  @Override
  public String getDescription() {
    return desc;
  }

  @Nullable
  public static UserStatus valueOf(int code) {
    return Enumerable.of(UserStatus.class, code);
  }

}
