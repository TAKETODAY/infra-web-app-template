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

package app.web.http;

import org.hibernate.validator.constraints.Length;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import app.model.User;
import app.model.UserStatus;
import app.util.HashUtils;
import app.web.ErrorMessageException;
import app.web.RequestLimit;
import app.web.RequiresLogin;
import infra.beans.support.BeanProperties;
import infra.http.HttpStatus;
import infra.persistence.EntityManager;
import infra.session.SessionManager;
import infra.session.SessionManagerOperations;
import infra.session.WebSession;
import infra.web.RequestContext;
import infra.web.annotation.DELETE;
import infra.web.annotation.GET;
import infra.web.annotation.POST;
import infra.web.annotation.PUT;
import infra.web.annotation.RequestBody;
import infra.web.annotation.RequestMapping;
import infra.web.annotation.ResponseStatus;
import infra.web.annotation.RestController;
import infra.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import static infra.persistence.QueryCondition.isEqualsTo;

/**
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 22:09
 */
@RestController
@RequestMapping("/api/auth")
class AuthHttpHandler extends SessionManagerOperations {

  private final EntityManager entityManager;

  public AuthHttpHandler(SessionManager sessionManager, EntityManager entityManager) {
    super(sessionManager);
    this.entityManager = entityManager;
  }

  @GET
  public User selfInfo(User loginUser) {
    return loginUser;
  }

  @DELETE
  public void logout(WebSession session) {
    session.invalidate();
  }

  static class UserFrom {

    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "请您输入正确格式的邮箱")
    public String username;

    @NotEmpty(message = "密码不能为空")
    public String password;
  }

  /**
   * 登录 API
   * <pre> {@code
   * {
   *   "id": 1,
   *   "status": "NORMAL",
   *   "name": "TAKETODAY",
   *   "avatar": "/upload/2019/4/9/6cd520c4-509a-4d35-9028-2485a87bd5c6.png",
   *   "introduce": "代码是我心中的一首诗"
   * }
   * } </pre>
   */
  @POST
  @RequestLimit(unit = TimeUnit.MINUTES, count = 5, errorMessage = "一分钟只能尝试5次登陆,请稍后重试")
  public User login(@Valid @RequestBody UserFrom user, RequestContext request) {
    User loginUser = entityManager.findUnique(User.class, isEqualsTo("username", user.username));
    if (loginUser == null) {
      throw ErrorMessageException.failed(user.username + " 账号不存在!");
    }

    String passwd = HashUtils.getEncodedPassword(user.password);
    if (!Objects.equals(loginUser.getPassword(), passwd)) {
      throw ErrorMessageException.failed("密码错误!");
    }

    // check user state
    UserStatus status = loginUser.getStatus();
    switch (status) {
      case NORMAL -> { }
      case LOCKED, RECYCLE, INACTIVE -> throw ErrorMessageException.failed(status.getDescription());
      default -> throw ErrorMessageException.failed("系统错误");
    }

    WebSession session = getSession(request);
    loginUser.bindTo(session);
    return loginUser;
  }

  //---------------------------------------------------------------------
  // 修改当前登录用户的信息
  //---------------------------------------------------------------------

  public static class InfoForm {

    @NotBlank(message = "请输入姓名或昵称")
    public String name;

    @Length(max = 256, message = "介绍最多256个字符")
    public String introduce;

  }

  /**
   * 当前登录用户信息 API
   *
   * @param loginUser 登录用户
   * @param form 表单
   */
  @PUT
  @RequestLimit(count = 2, unit = TimeUnit.MINUTES, errorMessage = "一分钟只能最多修改2次用户信息")
  public User userInfo(User loginUser, @RequestBody @Valid InfoForm form) {
    // 要判断不一致才更新
    if (Objects.equals(form.name, loginUser.getName())
            && Objects.equals(form.introduce, loginUser.getIntroduce())) {
      throw ErrorMessageException.failed("未更改任何信息");
    }

    Long id = loginUser.getId();
    // TODO 验证用户有效性
    // 设置新值
    User user = new User();
    user.setId(id);
    user.setName(form.name);
    user.setIntroduce(form.introduce);

    entityManager.updateById(user);

    // update to session
    BeanProperties.copy(user, loginUser);
    return loginUser;
  }

  public static class ChangePasswordForm {

    @NotBlank(message = "旧密码不能为空")
    public String oldPassword;

    @Length(min = 6, max = 48, message = "新密码至少输入6个字符，最多48个字符")
    public String newPassword;

    public String confirmNewPassword;
  }

  /**
   * 修改用户密码 API
   *
   * @param loginUser 登录用户
   * @param form 表单
   */
  @PUT(params = "password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequestLimit(unit = TimeUnit.MINUTES, errorMessage = "一分钟只能最多修改2次密码")
  public void changePassword(User loginUser, @RequestBody @Valid ChangePasswordForm form) {
    // 校验密码是否有效
    if (!Objects.equals(form.confirmNewPassword, form.newPassword)) {
      throw ErrorMessageException.failed("两次输入的新密码不一致");
    }

    // 校验数据是否存在该用户
    User byId = entityManager.findById(User.class, loginUser.getId());

    ErrorMessageException.notNull(byId, "要修改密码的用户不存在");

    // 校验旧密码
    String oldPassword = HashUtils.getEncodedPassword(form.oldPassword);
    if (!Objects.equals(oldPassword, byId.getPassword())) {
      throw ErrorMessageException.failed("原密码错误");
    }

    // 重新生成
    String newPassword = HashUtils.getEncodedPassword(form.newPassword);

    // 更新数据库
    User user = new User();
    user.setId(loginUser.getId());
    user.setPassword(newPassword);

    entityManager.updateById(user);
  }

  /**
   * 更改头像 API
   */
  @PUT(params = "avatar")
  @RequestLimit(unit = TimeUnit.MINUTES, errorMessage = "一分钟只能最多修改1次头像")
  public User changeAvatar(@RequiresLogin User loginUser, MultipartFile avatar) {
    String originalFilename = avatar.getOriginalFilename();

    String uri = saveAvatarFile(avatar);
    User user = new User();
    user.setId(loginUser.getId());
    user.setAvatar(uri);

    entityManager.updateById(user);
    loginUser.setAvatar(uri);
    return loginUser;
  }

  String saveAvatarFile(MultipartFile avatar) {
    // FIXME 尚未实现
    throw new UnsupportedOperationException();
  }

}
