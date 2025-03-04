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

package app.config;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import app.ConditionalOnDevelop;
import app.ConditionalOnProduction;
import app.ConditionalOnTesting;
import app.web.interceptor.RequestLimitInterceptor;
import app.web.support.PageableMethodArgumentResolver;
import infra.cache.annotation.EnableCaching;
import infra.cache.support.CaffeineCacheManager;
import infra.context.annotation.Configuration;
import infra.jdbc.RepositoryManager;
import infra.persistence.DefaultEntityManager;
import infra.persistence.EntityManager;
import infra.persistence.PropertyUpdateStrategy;
import infra.persistence.platform.MySQLPlatform;
import infra.session.SessionIdResolver;
import infra.session.config.SessionProperties;
import infra.stereotype.Component;

/**
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 17:48
 */
@EnableCaching
@Configuration(proxyBeanMethods = false)
public class AppConfig {

  @Component
  public static CaffeineCacheManager caffeineCacheManager() {
    return new CaffeineCacheManager(Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(100));
  }

  @Component
  public static RepositoryManager repositoryManager(DataSource dataSource) {
    RepositoryManager manager = new RepositoryManager(dataSource);
    DefaultEntityManager entityManager = new DefaultEntityManager(manager);
    entityManager.setDefaultUpdateStrategy(PropertyUpdateStrategy.noneNull());
    entityManager.setPlatform(new MySQLPlatform());
    manager.setEntityManager(entityManager);
    return manager;
  }

  @Component
  public static EntityManager entityManager(RepositoryManager repositoryManager) {
    return repositoryManager.getEntityManager();
  }

  @Component
  public static PageableMethodArgumentResolver pageableMethodArgumentResolver() {
    return new PageableMethodArgumentResolver();
  }

  @Component
  public static RequestLimitInterceptor requestLimitInterceptor() {
    return new RequestLimitInterceptor();
  }

  @Component
  @ConditionalOnProduction
  public static SessionIdResolver sessionIdResolver(SessionProperties properties) {
    return SessionIdResolver.forComposite(
            SessionIdResolver.forCookie(properties.cookie),
            SessionIdResolver.forHeader(SessionIdResolver.HEADER_X_AUTH_TOKEN));
  }

  @Component
  @ConditionalOnDevelop
  @ConditionalOnTesting
  public static SessionIdResolver devSessionIdResolver(SessionProperties properties) {
    return SessionIdResolver.forComposite(
            SessionIdResolver.forCookie(properties.cookie),
            SessionIdResolver.forHeader(SessionIdResolver.HEADER_X_AUTH_TOKEN),
            SessionIdResolver.forParameter("auth"));
  }

}
