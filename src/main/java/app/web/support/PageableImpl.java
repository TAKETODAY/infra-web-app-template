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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import app.web.ErrorMessageException;
import app.web.Pageable;
import infra.core.style.ToStringBuilder;
import infra.lang.Nullable;
import infra.util.StringUtils;
import infra.web.RequestContext;

/**
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:40
 */
final class PageableImpl implements Pageable, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Nullable
  private Integer size;

  @Nullable
  private Integer current;

  private final RequestContext request;

  private final String pageRequestParameterName;

  private final String pageSizeRequestParameterName;

  private final int maxPageSize;

  private final int defaultPageSize;

  public PageableImpl(RequestContext request, String pageRequestParameterName,
          String pageSizeRequestParameterName, int defaultPageSize, int maxPageSize) {
    this.request = request;
    this.pageRequestParameterName = pageRequestParameterName;
    this.pageSizeRequestParameterName = pageSizeRequestParameterName;
    this.defaultPageSize = defaultPageSize;
    this.maxPageSize = maxPageSize;
  }

  @Override
  public int pageNumber() {
    if (current == null) {
      String parameter = request.getParameter(pageRequestParameterName);
      if (StringUtils.isEmpty(parameter)) {
        current = 1;
      }
      else if ((current = parseInt(parameter)) <= 0) {
        throw ErrorMessageException.failed("分页页数必须大于0");
      }
    }
    return current;
  }

  @Override
  public int pageSize() {
    if (size == null) {
      int size;
      String parameter = request.getParameter(pageSizeRequestParameterName);
      if (StringUtils.isEmpty(parameter)) {
        size = defaultPageSize;
      }
      else {
        size = parseInt(parameter);
        if (size <= 0) {
          throw ErrorMessageException.failed("每页大小必须大于0");
        }

        if (size > maxPageSize) {
          throw ErrorMessageException.failed("分页大小超出限制");
        }
      }
      return this.size = size;
    }
    return size;
  }

  private Integer parseInt(String parameter) {
    try {
      return Integer.valueOf(parameter);
    }
    catch (NumberFormatException e) {
      throw ErrorMessageException.failed("分页参数错误");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o instanceof PageableImpl that) {
      return Objects.equals(size, that.size)
              && Objects.equals(current, that.current);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, current);
  }

  @Override
  public String toString() {
    return ToStringBuilder.forInstance(this)
            .append("size", size)
            .append("current", current)
            .toString();
  }

}
