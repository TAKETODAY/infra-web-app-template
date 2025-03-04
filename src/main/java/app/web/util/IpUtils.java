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

package app.web.util;

import java.util.List;

import infra.http.HttpHeaders;
import infra.lang.Nullable;
import infra.util.StringUtils;
import infra.web.RequestContext;

/**
 * @author <a href="https://github.com/TAKETODAY">海子 Yang</a>
 * @since 1.0 2025/3/4 21:46
 */
public abstract class IpUtils {

  private static final List<String> IP_HEADERS = List.of(
          "X-Forwarded-For",     // X-Forwarded-For：Squid 服务代理
          "X-Real-IP",           // X-Real-IP：nginx服务代理
          "Proxy-Client-IP",     // Proxy-Client-IP：apache 服务代理
          "WL-Proxy-Client-IP",  // WL-Proxy-Client-IP：weblogic 服务代理
          "HTTP_CLIENT_IP"      // HTTP_CLIENT_IP：有些代理服务器
  );

  public static String remoteAddress(RequestContext request) {
    String ipAddresses = getIpAddresses(request.requestHeaders());

    //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
    if (StringUtils.isNotEmpty(ipAddresses)) {
      String ip = ipAddresses.split(",")[0];
      if (isIP(ip)) {
        return ip;
      }
    }

    //还是不能获取到，最后再通过 request.getRemoteAddress();获取
    return request.getRemoteAddress();
  }

  @Nullable
  private static String getIpAddresses(HttpHeaders requestHeaders) {
    for (String ipHeader : IP_HEADERS) {
      String ipAddresses = requestHeaders.getFirst(ipHeader);
      if (isIP(ipAddresses)) {
        return ipAddresses;
      }
    }
    return null;
  }

  private static boolean isIP(String ipAddresses) {
    return StringUtils.hasText(ipAddresses);
  }

}
