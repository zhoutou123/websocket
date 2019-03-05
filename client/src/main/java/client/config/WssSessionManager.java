
package client.config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * caojianlin 2018/3/19
 *
 * 负责建立Web Socket连接， 发送hello心跳报文。
 */
@Component
public class WssSessionManager extends StompSessionHandlerAdapter implements ApplicationRunner, InitializingBean {

	/**
	 * WSS服务器URL： Synccloud服务器地址。
	 */
	@Value("${wss.server.url}")
	private String wssServerUrl;

	private final static Logger logger = LoggerFactory.getLogger(WssSessionManager.class);

	public static WssSessionManager get() {
		return instance;
	}

	/**
	 * 静态单实例。
	 */
	private static WssSessionManager instance;

	/**
	 * stomp Session. the wss session
	 */
	private WssSessionHandler stompSession;

	private void connectSyncCloud() {
		try {

			/// 断开TCP连接； 如果之前是连接的。
			if (stompSession != null) {

				if (stompSession.getSession() != null && stompSession.getSession().isConnected()) {

					System.out.println("The stomp session is still connected, disconnect first. ");
					stompSession.getSession().disconnect();
				}
			}

			WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
			// webSocketContainer.setDefaultMaxTextMessageBufferSize(300 * 1024 * 1024);
			// webSocketContainer.setDefaultMaxBinaryMessageBufferSize(200 * 1024 * 1024);

			WebSocketClient webSocketClient = new StandardWebSocketClient(webSocketContainer);// new
																								// SockJsClient(transports);

			WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

			// 消息使用JSON传送
			stompClient.setMessageConverter(new MappingJackson2MessageConverter());
			stompClient.setTaskScheduler(new ConcurrentTaskScheduler());

			// 设置100M
			// stompClient.setInboundMessageSizeLimit(100 * 1024 * 1024);

			String url = wssServerUrl;

			logger.info("Trying connect to synccloud server using: " + url);

			CountDownLatch latch = new CountDownLatch(1);
			stompSession = new WssSessionHandler(latch);

			StompSessionHandler sessionHandler = stompSession;
			stompClient.connect(url, sessionHandler);

			if (latch.await(20, TimeUnit.SECONDS)) {
			}

		} catch (InterruptedException e) {
			logger.info("Ws connect Interrupted Exception : " + e.getMessage());
		} catch (Exception e) {
			logger.info("Ws connect Exception : " + e.getMessage());
		}

	}

	/**
	 * ws 连接通过stomp心跳来确保连接正常。
	 *
	 * 本定时任务，通过发送一个消息来确认业务工作正常。
	 */
	@Scheduled(fixedDelay = 15000, initialDelay = 5000)
	public void wssConnectionWatcher() {
		boolean needConnect = false;

		if (stompSession == null) {
			needConnect = true;
		} else {
			if (stompSession.getSession() == null || !stompSession.getSession().isConnected()) {
				needConnect = true;
			}
		}

		if (!needConnect) {
			// 尝试发送hello

			try {
				stompSession.getSession().send("/app/register", "心跳包");
			} catch (Exception e) {
				needConnect = true;
				logger.error("Error send register", e);
			}
		}

		if (needConnect) {
			connectSyncCloud();
		}
	}

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		connectSyncCloud();
	}

	/**
	 * Invoked by a BeanFactory after it has set all bean properties supplied (and satisfied BeanFactoryAware and
	 * ApplicationContextAware).
	 * <p>
	 * This method allows the bean instance to perform initialization only possible when all bean properties have been
	 * set and to throw an exception in the event of misconfiguration.
	 *
	 * @throws Exception in the event of misconfiguration (such as failure to set an essential property) or if
	 *             initialization fails.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	public WssSessionHandler getStompSession() {
		return stompSession;
	}
}
