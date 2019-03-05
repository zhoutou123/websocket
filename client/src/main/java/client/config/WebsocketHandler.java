package client.config;

import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Component;

/**
 * @date 2018年5月23日 下午5:46:48
 *
 * @author zhoutuo
 */
@Component
public class WebsocketHandler implements StompFrameHandler {

	@Autowired
	private WssSessionManager wssSessionManager;

	/**
	 * @see org.springframework.messaging.simp.stomp.StompFrameHandler#handleFrame(org.springframework.messaging.simp.stomp.StompHeaders,
	 *      java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		Map<String, String> data = (Map<String, String>) payload;
		System.out.println("客户端接受到服务端的消息：" + data.get("msg"));
		wssSessionManager.getStompSession().getSession().send("/app/get/msg/from/client", "我是客户端");
	}

	/** 
	 * @see org.springframework.messaging.simp.stomp.StompFrameHandler#getPayloadType(org.springframework.messaging.simp.stomp.StompHeaders)
	 */
	@Override
	public Type getPayloadType(StompHeaders headers) {
		return Map.class;
	}

}
