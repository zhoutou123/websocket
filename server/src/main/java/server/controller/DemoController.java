package server.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 
 * @date 2019年3月5日 下午4:28:10 
 *
 * @author zhoutuo  
 */
@RestController
public class DemoController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	/**
	* 服务端向客户端推送消息
	*
	* @author zhoutuo
	* @date 2019年3月5日 下午4:28:54 
	*/
	@GetMapping("/send/msg/to/client")
	public String sendMsgToClient() {
		Map<String, String> data = new HashMap<>();
		data.put("msg", "我是服务端");
		messagingTemplate.convertAndSend("/topic/sendmsgtoclient", data);
		return "发送消息完毕";

	}

}
