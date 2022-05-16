package com.hvl.dragonteam.Redis;

import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.hvl.dragonteam.Utilities.URLs;

public class RedisPublisher {

	private RedisAsyncConnection<String, String> asynCommand = null;
	private RedisClient client = null;
	private static RedisPublisher instance = null;

	public static synchronized RedisPublisher getInstance(){
		if(instance == null){
			instance = new RedisPublisher();
		}
		return  instance;
	}
    
    private RedisPublisher(){
        try {
            this.client = RedisClient.create(URLs.redisAddress);
            this.asynCommand = this.client.connectAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void publish(String prefix, String channel, String message) {
    	this.asynCommand.set(prefix + channel  , message);
    	this.asynCommand.publish(prefix + channel, message);
    }
}
