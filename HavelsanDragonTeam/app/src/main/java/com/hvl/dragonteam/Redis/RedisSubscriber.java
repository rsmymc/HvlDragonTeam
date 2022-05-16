package com.hvl.dragonteam.Redis;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;

public class RedisSubscriber {

	private RedisPubSubConnection<String, String> asyncSub = null;
	private RedisAsyncConnection<String, String> asyncCommand = null;
	private String redisAddress = null;
	private String channel = null;
	private RedisClient client = null;
	private RedisPubSubListener<String, String> redisListener = null;
	private ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();

    public RedisSubscriber(String redisAddress, String prefix, String channel, RedisPubSubListener<String, String> redisListener){
    	this.redisAddress = redisAddress;
    	this.channel = prefix + channel;
    	this.redisListener = redisListener;
    	init();
    }
    
    private void init(){
    	try {
    		this.client = RedisClient.create(this.redisAddress);
    		asyncSub = this.client.connectPubSub();
    		asyncCommand = this.client.connectAsync();
    		subscribe();
		} catch (Exception e) {
			System.out.println("andac connection exception");
			e.printStackTrace();
		}
    }

    private void subscribe() {
		this.asyncSub.addListener(this.redisListener);
    	this.asyncSub.subscribe(this.channel);
    	getFirstRecordAsync();
    }

	private void getFirstRecordAsync() {
		RedisFuture<String> get = this.asyncCommand.get(this.channel);

		get.addListener(new Runnable() {

			@Override
			public void run() {
				try {

					redisListener.message(channel, get.get());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, executor);

	}
    public void unsubscribe() {
		this.asyncSub.unsubscribe(this.channel);
		this.asyncSub.removeListener(this.redisListener);
    }
}
