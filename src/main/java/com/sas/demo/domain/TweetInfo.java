package com.sas.demo.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TweetInfo {
	private int retweetCount;
	private int favoriteCount;
	private String status;
	private List<User> userReply;
	private List<User> userRetweet;
	private Date createdAt;
	
	public int getRetweetCount() {
		return retweetCount;
	}
	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}
	public int getFavoriteCount() {
		return favoriteCount;
	}
	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<User> getUserReply() {
		if (userReply == null)
			return userReply = new ArrayList<>();
		return userReply;
	}
	public List<User> getUserRetweet() {
		if (userRetweet == null)
			return userRetweet = new ArrayList<>();
		return userRetweet;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
