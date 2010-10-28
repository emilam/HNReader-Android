package com.emilam.HNReader;


public class SavedArticle extends Article {
	/**
	 * Database ID
	 */
	private int mID;
	

	public SavedArticle(Article article, int id) {
		super(article.getTitle(), article.getDomain(), article.getURL(), article.getAuthor(), article.getDiscussionID());
		mID = id;
	}
	public SavedArticle(String title, String domain, String url, String author, String discussionID, int id) {
		super(title, domain, url, author, discussionID);
		mID = id;
	}
	
	public int getID() {
		return mID;
	}
}
