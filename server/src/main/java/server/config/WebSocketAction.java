package server.config;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * @description 海航websocket响应
 * @date 2018年5月23日 下午4:11:03
 *
 * @author zhoutuo
 */
@Controller
public class WebSocketAction {

	/**
	 * 接受客户端的响应
	 * @author zhoutuo
	 * @date 2018年5月28日 下午5:41:45
	 */
	@MessageMapping("/get/msg/from/client")
	public void getcustomerinfo(String msg) {
		System.out.println("接受到客户端的消息：" + msg);
	}

	/**
	 *心跳包
	 * @author zhoutuo
	 * @date 2018年5月28日 下午5:42:26
	 */
	@MessageMapping("/register")
	public void register(String msg) {
		System.out.println("接受到客户端的心跳包：" + msg);
	}

}
