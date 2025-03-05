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

package app.web.http.console;

import java.util.Objects;

import app.model.User;
import app.model.UserStatus;
import app.web.ErrorMessageException;
import app.web.Pageable;
import app.web.Pagination;
import app.web.interceptor.RequiresLogin;
import infra.persistence.EntityManager;
import infra.persistence.EntityRef;
import infra.persistence.Id;
import infra.web.annotation.DELETE;
import infra.web.annotation.GET;
import infra.web.annotation.POST;
import infra.web.annotation.PUT;
import infra.web.annotation.PathVariable;
import infra.web.annotation.RequestBody;
import infra.web.annotation.RequestMapping;
import infra.web.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 后台管理接口
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 22:08
 */
@RequiresLogin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/console/users")
class UserConsoleHttpHandler {

  private final EntityManager entityManager;

  @GET
  public Pagination<User> listUsers(Pageable pageable) {
    return Pagination.from(entityManager.page(User.class, pageable));
  }

  /**
   * 后台创建用户
   */
  @POST
  public void create(@RequestBody User user) {
    entityManager.persist(user);
  }

  @PUT(path = "/{id}", params = "status")
  public void updateStatus(@PathVariable long id, UserStatus status) {
    entityManager.updateById(new UserStatusUpdate(id, status));
  }

  @DELETE("/{id}")
  public void delete(@PathVariable long id) {
    entityManager.delete(User.class, id);
  }

  @PUT("/{id}")
  public void update(@PathVariable long id, @Valid @RequestBody UserSettingsForm form) {
    User oldUser = entityManager.findById(User.class, id);
    ErrorMessageException.notNull(oldUser, "用户不存在");

    User user = new User();
    boolean change = false;
    if (!Objects.equals(form.name, oldUser.getName())) {
      user.setName(form.name);
      change = true;
    }

    if (!Objects.equals(form.introduce, oldUser.getIntroduce())) {
      user.setIntroduce(form.introduce);
      change = true;
    }

    if (change) {
      user.setId(oldUser.getId());
      entityManager.updateById(user);
    }
    else {
      throw ErrorMessageException.failed("资料未更改");
    }
  }

  @Setter
  public static class UserSettingsForm {

    @NotEmpty(message = "请输入姓名或昵称")
    private String name;

    private String introduce = "暂无介绍";
  }

  @EntityRef(User.class)
  static class UserStatusUpdate {

    @Id
    public final Long id;

    public final UserStatus status;

    UserStatusUpdate(Long id, UserStatus status) {
      this.id = id;
      this.status = status;
    }
  }

}

