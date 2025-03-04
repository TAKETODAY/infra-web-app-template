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

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import infra.persistence.Id;
import infra.persistence.NewEntityIndicator;

/**
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:50
 */
public abstract class BasicModel implements Serializable, NewEntityIndicator {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  protected Long id;

  protected Instant createAt;

  protected Instant updateAt;

  public void setId(Long id) {
    this.id = id;
  }

  public void setCreateAt(Instant createAt) {
    this.createAt = createAt;
  }

  public void setUpdateAt(Instant updateAt) {
    this.updateAt = updateAt;
  }

  public Long getId() {
    return id;
  }

  public Instant getCreateAt() {
    return createAt;
  }

  public Instant getUpdateAt() {
    return updateAt;
  }

  @JsonIgnore
  @Override
  public boolean isNew() {
    return id == null;
  }

}
