package ksyun.dwtransport.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/test")
@RestController
public class TestController {

	@GetMapping("/show")
	public Object show() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("shiyan", "sss");
		return map;
	}
}
