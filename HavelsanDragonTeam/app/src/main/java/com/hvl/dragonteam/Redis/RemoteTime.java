package com.hvl.dragonteam.Redis;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

//not working need to handle with threads
//do not use it
public class RemoteTime {

	private static long getRemoteNTPTime() {

		long currentTime = -1;
	    NTPUDPClient client = new NTPUDPClient();
	    // We want to timeout if a response takes longer than 5 seconds
	    client.setDefaultTimeout(5000);
	    //NTP server list
	    List<String> hosts = Arrays.asList(new String("ntp02.oal.ul.pt"), new String("ntp04.oal.ul.pt"), new String("ntp.xs4all.nl"));
	    for (String host : hosts) {

	        try {
	            InetAddress hostAddr = InetAddress.getByName(host);
	            TimeInfo info = client.getTime(hostAddr);
	            currentTime = info.getMessage().getTransmitTimeStamp().getTime();
				System.out.print("Current Time for timediff =" + currentTime);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            return currentTime;
	        }
	        finally{
	        	client.close();
	        }
	    }
	    return currentTime;
	}
}
