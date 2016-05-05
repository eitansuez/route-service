package org.cloudfoundry.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RateLimiter {
    private final static  Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    private ConcurrentHashMap<String, AtomicInteger> map = new ConcurrentHashMap<String, AtomicInteger>(1000);
    
    @Scheduled(fixedRate = 15000)
    public void reportCurrentTime() {
        map.clear();
  }

	public boolean rateLimitRequest(RequestEntity<?> incoming)  {
			String forwardUrl = incoming.getHeaders().get(Controller.FORWARDED_URL).get(0);
			URI uri;
			try {
				uri = new URI(forwardUrl);
			} catch (URISyntaxException e) {
				logger.error("error parsing url", e);
				return false;
			}
			
			String host = uri.getHost();
		    AtomicInteger value = map.get(host);
		    int requestsPerSecond = 1;
		    if (value == null){
		    	value = new AtomicInteger(1);
		    	map.put(host, value);
		    }
		    else{
		    	requestsPerSecond = value.incrementAndGet();
		    }
		    
		    if(requestsPerSecond > 3)
		    	return true;
		    else
		    	return false;
	}
}
