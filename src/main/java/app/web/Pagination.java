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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import infra.core.style.ToStringBuilder;
import infra.persistence.Page;

/**
 * 分页 Model
 *
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:36
 */
public final class Pagination<T> implements ListableHttpResult<T> {

  @SuppressWarnings("rawtypes")
  private static final Pagination empty = new Pagination<>(0,
          0, 0, 0, Collections.emptyList());

  /** amount of page */
  private final int pages;

  /** all row in database */
  private final int total;

  /** every page size */
  private final int size;

  /** current page */
  private final int current;

  private final List<T> data;

  public Pagination(int pages, int total, int size, int current, List<T> data) {
    this.pages = pages;
    this.total = total;
    this.size = size;
    this.current = current;
    this.data = data;
  }

  public int getPages() {
    return pages;
  }

  public int getTotal() {
    return total;
  }

  public int getSize() {
    return size;
  }

  public int getCurrent() {
    return current;
  }

  @Override
  public List<T> getData() {
    return data;
  }

  @Override
  public boolean equals(Object param) {
    if (this == param)
      return true;
    if (!(param instanceof Pagination<?> that))
      return false;
    return pages == that.pages && total == that.total
            && size == that.size && current == that.current
            && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pages, total, size, current, data);
  }

  @Override
  public String toString() {
    return ToStringBuilder.forInstance(this)
            .append("current", current)
            .append("pages", pages)
            .append("total", total)
            .append("size", size)
            .append("data", data)
            .toString();
  }

  // Static Factory Methods

  @SuppressWarnings("unchecked")
  public static <T> Pagination<T> empty() {
    return empty;
  }

  public static <T> Pagination<T> from(Page<T> page) {
    return new Pagination<>(page.getTotalPages(), page.getTotalRows().intValue(),
            page.getLimit(), page.getPageNumber(), page.getRows());
  }

}
