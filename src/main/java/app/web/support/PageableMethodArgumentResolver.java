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

import app.AppConstant;
import app.web.Pageable;
import infra.lang.Assert;
import infra.lang.Nullable;
import infra.web.RequestContext;
import infra.web.bind.resolver.ParameterResolvingStrategy;
import infra.web.handler.method.ResolvableMethodParameter;

/**
 * 用于处理 {@link Pageable} 参数的解析器
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 21:55
 */
public class PageableMethodArgumentResolver implements ParameterResolvingStrategy {

  private int maxPageSize = 20;

  private int defaultPageSize = 10;

  private String pageRequestParameterName = AppConstant.PARAMETER_CURRENT;

  private String pageSizeRequestParameterName = AppConstant.PARAMETER_SIZE;

  public void setPageRequestParameterName(@Nullable String pageRequestParameterName) {
    this.pageRequestParameterName = pageRequestParameterName == null ? AppConstant.PARAMETER_CURRENT : pageRequestParameterName;
  }

  public void setPageSizeRequestParameterName(@Nullable String pageSizeRequestParameterName) {
    this.pageSizeRequestParameterName = pageSizeRequestParameterName == null ? AppConstant.PARAMETER_SIZE : pageSizeRequestParameterName;
  }

  public void setMaxPageSize(int maxPageSize) {
    Assert.isTrue(maxPageSize > 0, "maxPageSize must be greater than 0");
    this.maxPageSize = maxPageSize;
  }

  public void setDefaultPageSize(int defaultPageSize) {
    Assert.isTrue(defaultPageSize > 0, "defaultPageSize must be greater than 0");
    this.defaultPageSize = defaultPageSize;
  }

  @Override
  public boolean supportsParameter(ResolvableMethodParameter parameter) {
    return parameter.isAssignableTo(Pageable.class);
  }

  @Override
  public Object resolveArgument(RequestContext context, ResolvableMethodParameter parameter) {
    return new PageableImpl(context, pageRequestParameterName, pageSizeRequestParameterName, defaultPageSize, maxPageSize);
  }

}
