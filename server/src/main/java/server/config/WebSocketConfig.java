
package server.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/** 
 * @date 2019年3月5日 下午4:24:49 
 *
 * @author zhoutuo  
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 添加连接节点
		registry.addEndpoint("/register", "/registerData").addInterceptors(syncClientHandshakeInterceptor())
				.setHandshakeHandler(syncClientHandshakeHandler())
				// 允许跨域
				.setAllowedOrigins("*");
	}

	/**
	 * WebSocket 握手拦截器 可做一些用户认证拦截处理
	 */
	private HandshakeInterceptor syncClientHandshakeInterceptor() {
		return new HandshakeInterceptor() {

			/**
			 * websocket握手连接
			 * 
			 * @return 返回是否同意握手
			 */
			@Override
			public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
					WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
				ServletServerHttpRequest req = (ServletServerHttpRequest) request;
				// 通过url的query参数获取认证参数
				String token = req.getServletRequest().getParameter("username");
				// 根据token认证用户，不通过返回拒绝握手
				Principal user = authenticate(token);
				if (user == null) {
					return true;
				}
				// 保存认证用户
				attributes.put("user", user);
				return true;
			}

			@Override
			public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
					WebSocketHandler wsHandler, Exception exception) {

			}
		};
	}

	// WebSocket 握手处理器
	private DefaultHandshakeHandler syncClientHandshakeHandler() {
		return new DefaultHandshakeHandler() {

			@Override
			protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
					Map<String, Object> attributes) {
				// 设置认证通过的用户到当前会话中
				return (Principal) attributes.get("user");
			}
		};
	}

	/**
	 * 根据token认证授权
	 * 
	 * @param token
	 */
	private Principal authenticate(String token) {
		// TODO 解析token并获取认证用户信息
		return null;
	}
}
