package com.sas.demo.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sas.demo.domain.Message;
import com.sas.demo.domain.TweetInfo;
import com.sas.demo.domain.User;
import com.sas.demo.util.Connection;
import com.sas.demo.util.GenerateExcelFile;

import twitter4j.DirectMessage;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;

@RestController
@RequestMapping("/")
public class TwitterController {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TwitterController.class);

	@RequestMapping("/getTimeline")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public List<Message> getTimelineTweets() {
		LOG.info("Getting timeline tweets");
		Twitter twitter = Connection.getTwiterInstance();
		List<Message> msgList = new ArrayList<>();
		try {
			List<Status> statuses = twitter.getHomeTimeline();
			for (Status status : statuses) {
				Message msg = new Message();
				msg.setUserName(status.getUser().getName());
				msg.setStatus(status.getText());
				msg.setFavoriteCount(status.getFavoriteCount());
				msg.setRetweetCount(status.getRetweetCount());
				msgList.add(msg);
			}
		} catch (TwitterException ex) {
			LOG.error(ex.getMessage());
		}
		return msgList;
	}

	@RequestMapping("/postTweet")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Status postTweet(@RequestBody Message msg) {
		LOG.info("Posting tweet on timeline");
		Twitter twitter = Connection.getTwiterInstance();
		Status status = null;
		try {
			status = twitter.updateStatus(msg.getTweet());
		} catch (TwitterException ex) {
			LOG.error(ex.getMessage());
		}
		return status;
	}

	@RequestMapping("/searchTweets")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Message> searchTweets(@RequestBody Message msg) {
		LOG.info("Searching tweets with input query");
		Twitter twitter = Connection.getTwiterInstance();
		List<Message> msgList = new ArrayList<>();
		try {
			GenerateExcelFile.initialize("Twitter Search");
			GenerateExcelFile.insertRow("ScreenName~UserName~Status~RetweetCount~FavoriteCount~CreatedAt");
			Query query = new Query(msg.getQuery());
			query.setCount(100);
			QueryResult result = twitter.search(query);
			for (Status status : result.getTweets()) {
				Message returnMsg = new Message();
				returnMsg.setTweetId(status.getId());
				returnMsg.setCreatedAt(status.getCreatedAt());
				returnMsg.setUserScreenName(status.getUser().getScreenName());
				returnMsg.setUserName(status.getUser().getName());
				returnMsg.setStatus(status.getText());
				returnMsg.setRetweetCount(status.getRetweetCount());
				returnMsg.setFavoriteCount(status.getFavoriteCount());				
				String output = status.getUser().getScreenName() + "~" + status.getUser().getName() + "~"
						+ status.getText() + "~" + status.getRetweetCount() + "~" + status.getFavoriteCount() + "~" + status.getCreatedAt().toString();
				GenerateExcelFile.insertRow(output);
				msgList.add(returnMsg);
			}
			GenerateExcelFile.writeFile("Twitter_Search_"+ new Date().getTime() +".xlsx");
		} catch (TwitterException ex) {
			LOG.error(ex.getMessage());
		}
		return msgList;
	}

	@RequestMapping("/getMessages")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Message> getDirectMessages() {
		LOG.info("Getting direct messages from inbox");
		Twitter twitter = Connection.getTwiterInstance();
		List<DirectMessage> directMsgList = null;
		List<Message> messageList = new ArrayList<>();
		try {
			directMsgList = twitter.getDirectMessages();
			for (DirectMessage directMsg : directMsgList) {
				Message message = new Message();
				message.setUserScreenName(directMsg.getSenderScreenName());
				message.setMessage(directMsg.getText());
				messageList.add(message);
			}
		} catch (TwitterException ex) {
			LOG.error(ex.getMessage());
		}
		return messageList;
	}

	@RequestMapping("/sendMessage")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String sendDirectMessages(@RequestBody Message msg) {
		LOG.info("Sending direct message to a recipient");
		Twitter twitter = Connection.getTwiterInstance();
		DirectMessage message = null;
		try {
			message = twitter.sendDirectMessage(msg.getRecipientId(), msg.getMessage());
		} catch (TwitterException ex) {
			LOG.error(ex.getMessage());
			return ex.getMessage();
		}
		return "Sent message '" + message.getText() + "' to @" + message.getRecipientScreenName();
	}

	@RequestMapping("/streaming")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void streaming(@RequestBody Message msg) {
		LOG.info("Streaming tweets based on filter query");
		TwitterStream twitterStream = Connection.getTwiterStreamInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                System.out.println(status.getUser().getScreenName() + "," + status.getText() + "," + status.getRetweetCount() + "," + status.getFavoriteCount());
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
            	LOG.error(ex.getMessage());
            }
        };
        twitterStream.addListener(listener);
        twitterStream.filter(msg.getQuery());
    }
	
	@RequestMapping("/getTweetInfo/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TweetInfo getTweetInfo(@PathVariable(value="id") Long tweetId) {
		LOG.info("Get tweet information based on tweet id");
		Twitter twitter = Connection.getTwiterInstance();
		TweetInfo tweetInfo = new TweetInfo();
		try {
			GenerateExcelFile.initialize("Tweet Statistics");
			GenerateExcelFile.insertRow("retweet_count~favourite_count~status~created_at~user_id_reply~screenname_reply~user_reply~favourite_count_reply~retweet_count_reply~user_id_retweet~screenname_retweet");
			Status tweetStatus = twitter.showStatus(tweetId);
			tweetInfo.setRetweetCount(tweetStatus.getRetweetCount());
			tweetInfo.setFavoriteCount(tweetStatus.getFavoriteCount());
			tweetInfo.setStatus(tweetStatus.getText());
			tweetInfo.setCreatedAt(tweetStatus.getCreatedAt());
			ResponseList<Status> mentionsTimeline = twitter.getMentionsTimeline();
			ResponseList<Status> retweets = twitter.getRetweets(tweetId);
			
			int replyCount = 0;
			for (Status status : mentionsTimeline) {
				Long replyToStatusId = status.getInReplyToStatusId();
				if(replyToStatusId.equals(tweetId))
					replyCount++;
			}
			int rowCount = replyCount > retweets.size() ? replyCount : retweets.size();
			int i=0;	
			do {
				GenerateExcelFile.insertRow(tweetStatus.getRetweetCount() + "~" + tweetStatus.getFavoriteCount() + "~"+ tweetStatus.getText()+"~"+tweetStatus.getCreatedAt().toString());
				i++;
			} while (i < rowCount);
			int rowNum = 1;
			for (Status status : mentionsTimeline) {
				Long replyToStatusId = status.getInReplyToStatusId();
				if(replyToStatusId.equals(tweetId)) {
					User usr = new User();
					usr.setId(status.getUser().getId());
					GenerateExcelFile.insertData(rowNum, 4, String.valueOf(status.getUser().getId()));
					
					usr.setScreenName(status.getUser().getScreenName());
					GenerateExcelFile.insertData(rowNum, 5, status.getUser().getScreenName());
					
					usr.setReply(status.getText());
					GenerateExcelFile.insertData(rowNum, 5, status.getText());
					
					usr.setFavoriteCount(status.getFavoriteCount());
					GenerateExcelFile.insertData(rowNum, 7, String.valueOf(status.getFavoriteCount()));
					
					usr.setRetweetCount(status.getRetweetCount());
					GenerateExcelFile.insertData(rowNum, 8, String.valueOf(status.getRetweetCount()));
					
					tweetInfo.getUserReply().add(usr);
					rowNum++;
				}
			}
			rowNum = 1;
			for (Status status : retweets) {
				User usr = new User();
				usr.setId(status.getUser().getId());
				GenerateExcelFile.insertData(rowNum, 9, String.valueOf(status.getUser().getId()));
				
				usr.setScreenName(status.getUser().getScreenName());
				GenerateExcelFile.insertData(rowNum, 10, status.getUser().getScreenName());
				
				tweetInfo.getUserRetweet().add(usr);
				rowNum++;
			}
			GenerateExcelFile.writeFile("Tweet_Statistics_"+new Date().getTime()+".xlsx");
		} catch (TwitterException ex) {
			LOG.error(ex.getMessage());
		}
		return tweetInfo;
	}
}
