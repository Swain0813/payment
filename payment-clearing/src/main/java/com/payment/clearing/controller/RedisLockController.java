package com.payment.clearing.controller;
import com.payment.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("redislock")
public class RedisLockController {


	@Resource
	private RedisService redisService;

	private int num = 0;

	private static final String key = ">>>>lock<<<<";
	private static final int expertime  = 10*1000;

	@GetMapping("lock")
	public void lock(){
		for(int i=0; i<20; i++){
			new RedisLockThread().start();
		}
		for(int i=0; i<20; i++){
			new RedisLockThread2().start();
		}
		for(int i=0; i<10; i++){
			new RedisLockThread3().start();
		}
		for(int i=0; i<10; i++){
			new RedisLockThread4().start();
		}
		for(int i=0; i<10; i++){
			new RedisLockThread2().start();
		}
		for(int i=0; i<20; i++){
			new RedisLockThread().start();
		}
		for(int i=0; i<20; i++){
			new RedisLockThread3().start();
		}
	}

	class RedisLockThread extends Thread {

		@Override
		public void run() {
			if(redisService.lock(key,expertime)) {
				log.info(num+" get lock success : +2" + key+(num +=2));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("exp", e);
				} finally {
				    while(!redisService.releaseLock(key)) {
				    	log.info("release lock failed: " + key);
				    }
				    log.info("release lock success : " + key);
				}
			}else {
				log.info("get lock failed : " + key);

			}

		}
	}


	class RedisLockThread2 extends Thread {

		@Override
		public void run() {
			if(redisService.lock(key,expertime)) {
				log.info(num+" get lock success : -1" + key+(num -=1));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("exp", e);
				} finally {
				    while(!redisService.releaseLock(key)) {
				    	log.info("release lock failed: " + key);
				    }
				    log.info("release lock success : " + key);
				}
			}else {
				log.info("get lock failed : " + key);

			}

		}
	}


	class RedisLockThread3 extends Thread {

		@Override
		public void run() {
			if(redisService.lock(key,expertime)) {
				log.info(num+" get lock success : +3" + key+(num +=3));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("exp", e);
				} finally {
				    while(!redisService.releaseLock(key)) {
				    	log.info("release lock failed: " + key);
				    }
				    log.info("release lock success : " + key);
				}
			}else {
				log.info("get lock failed : " + key);

			}

		}
	}


	class RedisLockThread4 extends Thread {

		@Override
		public void run() {
			if(redisService.lock(key,expertime)) {
				log.info(num+" get lock success : +1" + key+(num +=1));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("exp", e);
				} finally {
				    while(!redisService.releaseLock(key)) {
				    	log.info("release lock failed: " + key);
				    }
				    log.info("release lock success : " + key);
				}
			}else {
				log.info("get lock failed : " + key);

			}

		}
	}


}
