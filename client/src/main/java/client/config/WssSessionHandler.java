package client.config;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class WssSessionHandler extends StompSessionHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(WssSessionHandler.class);

	private final CountDownLatch latch;

	/**
	 * the tcp session.
	 */
	private StompSession session = null;

	/**
	 * CountDownLatch类是一个同步倒数计数器,构造时传入int参数,该参数就是计数器的初始值， 每调用一次countDown()方法，计数器减1,计数器大于0 时，await()方法会阻塞后面程序执行，
	 * 直到计数器为0，await(long timeout, TimeUnit unit)，是等待一定时间，然后执行，不管计数器是否到0了。
	 *
	 * @param latch
	 */
	public WssSessionHandler(final CountDownLatch latch) {
		this.latch = latch;
	}

	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

		logger.info("After wss connected .");

		try {

			this.setSession(session);

			//// 调用Register
			session.send("/app/register", "心跳包");

			System.out.println("Send register message .");

			// 客户端查询会员信息通知
			session.subscribe("/topic/sendmsgtoclient", SpringUtil.getBean(WebsocketHandler.class));

			System.out.println("Subscribed all topics .");
		} finally {
			latch.countDown();
		}

	}

	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
			Throwable exception) {
		exception.printStackTrace();

		System.out.println("WS handle exception " + exception.getMessage());
	}

	@Override
	public void handleTransportError(StompSession session, Throwable exception) {
		System.out.println("Error: " + exception);
		System.out.println(exception.toString());
		System.out.println("Ws Transport Error : " + exception.getMessage());
		super.handleTransportError(session, exception);

		if (exception instanceof ConnectionLostException) {
			// if connection lost, call this
		}

	}

	@Override
	public Type getPayloadType(StompHeaders headers) {
		return String.class;
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		System.out.println("Received: {}");
	}

	public StompSession getSession() {
		return session;
	}

	public void setSession(StompSession session) {
		this.session = session;
	}
}
