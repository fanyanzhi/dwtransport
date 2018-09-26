package ksyun.dwtransport.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ksyun.dwtransport.service.TransportService;
import ksyun.dwtransport.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TransportController {

	@Autowired
	private TransportService transportService;

	/**
	 * 从172.16.16.65中将两张表的数据读出来，冗余存在172.16.16.24数据库的单表中
	 */
	/*@GetMapping("/transport/start")
	public Object transport() {
		Map<String, String> map = new HashMap<String, String>();
		// 放线程池异步执行
		ThreadUtil.execute(() -> {
			try {
				//transportService.transport();
				transportService.transportBatch();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});
		map.put("success", "200");
		return map;
	}*/
	
	/**
	 * 从172.16.16.24的单表中取出1亿条数据到172.16.16.13的分表中
	 * @return
	 */
	/*@GetMapping("/transport/oneyi")
	public Object transportOneYi() {
		Map<String, String> map = new HashMap<String, String>();
		// 放线程池异步执行
		ThreadUtil.execute(() -> {
			try {
				transportService.transportOneYiBatch();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});
		map.put("success", "200");
		return map;
	}*/
	
	/**
	 * 从172.16.16.24的单表中取出全部数据(10亿)到172.16.16.65的分表中
	 * @return
	 */
	/*@GetMapping("/transport/alltopar")
	public Object transportAllToPar() {
		Map<String, String> map = new HashMap<String, String>();
		// 放线程池异步执行
		ThreadUtil.execute(() -> {
			try {
				transportService.transportAllToParBatch();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});
		map.put("success", "200");
		return map;
	}*/
	
	/**
	 * 从172.16.16.13的1亿数据的分表中取出分页缓存数据到public_img_channelandlabelpage
	 * @return
	 */
	/*@GetMapping("/transport/oneyitocache")
	public Object transportOneYiToCacheTable() {
		Map<String, String> map = new HashMap<String, String>();
		// 放线程池异步执行
		ThreadUtil.execute(() -> {
			try {
				transportService.transportOneYiToCacheTable();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});
		map.put("success", "200");
		return map;
	}*/
	
	/**
	 * 从172.16.16.65的10亿数据的分表中取出分页缓存数据到public_img_channelandlabelpage
	 * @return
	 */
	@GetMapping("/transport/alltocache")
	public Object transportTenYiToCacheTable() {
		Map<String, String> map = new HashMap<String, String>();
		// 放线程池异步执行
		ThreadUtil.execute(() -> {
			try {
				transportService.transportTenYiToCacheTable();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		});
		map.put("success", "200");
		return map;
	}
}
