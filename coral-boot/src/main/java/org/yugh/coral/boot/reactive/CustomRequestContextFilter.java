/*
 * Copyright (c) 2019-2029, yugenhai108@gmail.com.
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yugh.coral.boot.reactive;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.yugh.coral.core.common.constant.StringPool;
import org.yugh.coral.core.pojo.bo.RequestHeaderBO;
import org.yugh.coral.core.utils.SnowFlakeGenerateUtils;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.Map;

/**
 * @author yugenhai
 */
@Slf4j
@Order(Integer.MIN_VALUE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class CustomRequestContextFilter implements WebFilter {

    @Override
    @Nullable
    public Mono<Void> filter(@Nullable ServerWebExchange exchange, @Nullable WebFilterChain chain) {
        Assert.notNull(exchange, () -> "ServerWebExchange '" + exchange + "' must not be null");
        Assert.notNull(chain, () -> "WebFilterChain '" + chain + "' must not be null");
        ServerHttpRequest request = exchange.getRequest();
        String msgId = String.valueOf(SnowFlakeGenerateUtils.snowFlakeGenerate());
        exchange.getRequest().mutate().headers(httpHeaders -> httpHeaders.add(StringPool.REQUEST_ID_KEY, msgId));
        Map<String, Object> contextMap = Maps.newConcurrentMap();
        contextMap.put(StringPool.CONTEXT_MAP, msgId);
        log.info("Reactive Request Started  : {}", msgId);
        return chain.filter(exchange).subscriberContext(ctx ->
                CustomRequestContextHolder.setServerHttpRequestReactor(ctx, request)).subscriberContext(ctx ->
                CustomRequestContextHolder.setRequestHeaderReactor(ctx, RequestHeaderBO.builder().baseList(Arrays.asList(msgId, request.getId())).baseMap(contextMap).build()));

    }
}
