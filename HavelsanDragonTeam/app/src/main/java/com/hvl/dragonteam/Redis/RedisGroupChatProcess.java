package com.hvl.dragonteam.Redis;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hvl.dragonteam.Interface.MyFunction;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;


public class RedisGroupChatProcess{

	private RedisPubSubConnection<String, String> asyncPubSub = null;
	private String redisAddress = null;
	private RedisClient client = null;
	private int maxChatListSize = 16;
	private RedisAsyncConnection<String, String> asyncConnection = null;
	private RedisConnection<String, String> syncConnection = null;
	private ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();
	
	private HashMap<String, List<RedisPubSubListener>> listenerMap = new HashMap<>();
	
	private RedisPubSubListener<String, String> createlistener(String channel, MyFunction<Collection<?>, Boolean> callback){
		RedisPubSubListener<String, String> listener = new RedisPubSubListener<String, String>() {
			@Override
			public void message(String channel, String message) {
				System.out.println("message1");
				fillChatMessages(channel, callback);
			}
			@Override
			public void message(String pattern, String channel, String message) {
				System.out.println("message2");
			}
			@Override
			public void subscribed(String channel, long count) {
				System.out.println("subscribed1");
			}
			@Override
			public void psubscribed(String pattern, long count) {
				System.out.println("psubscribed1");
			}
			@Override
			public void unsubscribed(String channel, long count) {
				System.out.println("unsubscribed1");
			}
			@Override
			public void punsubscribed(String pattern, long count) {
				System.out.println("punsubscribed");
			}
		};
		if(listenerMap.containsKey(channel)) {
			listenerMap.get(channel).add(listener);
		}else {
			listenerMap.put(channel, new ArrayList<>(Arrays.asList(listener)));
		}
		
		return listener;
	}
	
	public RedisGroupChatProcess(String redisAddress) {
		this.redisAddress = redisAddress;
	}

	public void init() {
		try {
			this.client = RedisClient.create(this.redisAddress);
			asyncPubSub = this.client.connectPubSub();
			asyncConnection = this.client.connectAsync();
			syncConnection = this.client.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void publish(String channel, String message) {
		try {
			this.asyncConnection.rpush(channel + "_list", message);
			this.asyncConnection.publish(channel, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void subscribe(String channel, MyFunction<Collection<?>, Boolean> callback) {
		RedisPubSubListener<String, String> redisListener = createlistener(channel, callback);
		this.asyncPubSub.addListener(redisListener);
		this.asyncPubSub.subscribe(channel);
		fillChatMessages(channel, callback);
	}

	private void fillChatMessages(String channel, MyFunction<Collection<?>, Boolean> callback) {
		RedisFuture<Long> llen = this.asyncConnection.llen(channel + "_list");

		llen.addListener(new Runnable() {

			@Override
			public void run() {
				try {
					long length = llen.get();
					if(length == 0) {
						return;
					}

					long start = 0;
					long end = 0;
					System.out.println("I am at accept of llen");
					end = length -1;
					start = length - maxChatListSize > -1 ? length - maxChatListSize : 0;
					MyListCoordinator.getInstance().setStartEnd(channel, start, end);

					List<String> chatList  = syncConnection.lrange(channel + "_list", start, end);
					callback.apply(chatList);


				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, executor);
	}

	public void getOldChatMessages(String channel, MyFunction<Collection<?>, Boolean> callback) {
		RedisFuture<Long> llen = this.asyncConnection.llen(channel + "_list");

		llen.addListener(new Runnable() {

			@Override
			public void run() {
				try {
					long myCurrentStart = MyListCoordinator.getInstance().getStart(channel);
					long currentMaxChatListSize =MyListCoordinator.getInstance().getCurrentMaxChatListSize(channel);

					if(myCurrentStart == 0) {
						return;
					}else {
						currentMaxChatListSize = currentMaxChatListSize >= maxChatListSize ? currentMaxChatListSize*2: maxChatListSize;
						long start = 0;
						long end = 0;

						System.out.println("I am at accept of llen");
						end = myCurrentStart -1;
						start = myCurrentStart - currentMaxChatListSize > -1 ? myCurrentStart - currentMaxChatListSize : 0;

						MyListCoordinator.getInstance().setStart(channel, start);
						List<String> chatList  = syncConnection.lrange(channel + "_list", start, end);
						boolean isContinue = callback.apply(chatList);
						if(isContinue) {
							MyListCoordinator.getInstance().setCurrentMaxChatListSize(channel, currentMaxChatListSize);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, executor);
	}
	
	
	public void unsubscribe(String channel) {
		this.asyncPubSub.unsubscribe(channel);
	}
	
	public void unsubscribeAll() {
		for (Iterator<Entry<String, List<RedisPubSubListener>>> iterator = listenerMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, List<RedisPubSubListener>> e =  iterator.next();
			List<RedisPubSubListener> list = e.getValue();
			for (Iterator<RedisPubSubListener> iterator2 = list.iterator(); iterator2.hasNext();) {
				RedisPubSubListener listener = iterator2.next();
				this.asyncPubSub.removeListener(listener);
			}
			this.asyncPubSub.unsubscribe(e.getKey());
		}
		listenerMap.clear();
		
	}
}

class MyListCoordinator{
	private HashMap<String, MyList> map = new HashMap<>();
	private MyListCoordinator() {};
	private static MyListCoordinator instance = null;
	public synchronized static MyListCoordinator getInstance() {
		if(instance == null) {
			instance = new MyListCoordinator();
		}
		return instance;
	}

	long getStart(String channel) {
		if(map.containsKey(channel)) {
			return map.get(channel).start;
		}
		return 0;
	}

	long getEnd(String channel) {
		if(map.containsKey(channel)) {
			return map.get(channel).end;
		}
		return 0;
	}

	long getCurrentMaxChatListSize(String channel) {
		if(map.containsKey(channel)) {
			return map.get(channel).currentMaxChatListSize;
		}
		return 0;
	}

	void setStart(String channel, long start) {
		if(map.containsKey(channel)) {
			map.get(channel).start = start;
		}else {
			map.put(channel, new MyList(start, start));
		}

	}

	void setEnd(String channel, long end) {
		if(map.containsKey(channel)) {
			map.get(channel).end = end;
		}else {
			map.put(channel, new MyList(end, end));
		}

	}

	void setCurrentMaxChatListSize(String channel, long currentMaxChatListSize) {
		if(map.containsKey(channel)) {
			map.get(channel).currentMaxChatListSize = currentMaxChatListSize;
		}else {
			System.out.println("Last Range ERR can not ve insterted to map");
		}

	}

	void setStartEnd(String channel, long start, long end) {
		if(map.containsKey(channel)) {
			map.get(channel).start = start;
			map.get(channel).end = end;
		}else {
			map.put(channel, new MyList(start, end));
		}

	}
	class MyList{
		long start;
		long end;
		long currentMaxChatListSize;

		MyList(long start, long end) {
			this.start = start;
			this.end = end;
			this.currentMaxChatListSize = 1;
		}
	}
}


	
