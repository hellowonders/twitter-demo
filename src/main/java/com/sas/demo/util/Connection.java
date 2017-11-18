package com.sas.demo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@Component
@PropertySource("classpath:application.properties")
public class Connection {

	@Value("${consumer.key}")
	private String consumerKey;
	
	@Value("${consumer.secret}")
	private String consumerSecret;
	
	@Value("${access.token}")
	private String accessToken;
	
	@Value("${access.token.secret}")
	private String accessTokenSecret;
	
	private static Twitter twitterInstance;

	public static Twitter getTwiterInstance() {
		if (twitterInstance != null) {
			return twitterInstance;
		}
		try {
			Configuration conf = getTwitterConfiguration();
		TwitterFactory tf = new TwitterFactory(conf);
		twitterInstance = tf.getInstance();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return twitterInstance;

	}

	private static Configuration getTwitterConfiguration() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("Og9KvuK9jKK2C1MfvX1en1Yoz")
				.setOAuthConsumerSecret("q2itiVnR9PJZjogRPlYYOiIqxUgeBjpvW4uHuMeeTKqgHDu6SS")
				.setOAuthAccessToken("927117367166898176-ufkCDJuu1whG0Efml9USKix5ko5uweZ")
				.setOAuthAccessTokenSecret("A9bGUNzoIYBlz1gO18GjespPp1JbpSoTqYnBhSNFgCI2p");
		Configuration conf = cb.build();
		return conf;
	}

	public static TwitterStream getTwiterStreamInstance() {
		TwitterStreamFactory factory = new TwitterStreamFactory(getTwitterConfiguration());
		return factory.getInstance();
	}
}
