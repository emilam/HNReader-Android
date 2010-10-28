package com.emilam.HNReader;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents an Article from YCombinator
 * These are immutable!
 * 
 * @author emilam
 *
 */
public class Article implements Parcelable, HNLinkable {
	private String mURL;
	private String mTitle;
	private String mAuthor;
	private String mDomain;
	private String mDiscussionID;

	/**
	 * Creates an Article
	 * 
	 * @param title
	 * @param domain
	 * @param url
	 * @param author
	 * @param discussionID
	 */
	public Article(String title, String domain, String url, String author, String discussionID) {
		mURL = url;
		mTitle = title;
		mAuthor = author;
		mDomain = domain;
		if (mDomain == null)
			mDomain = new String();
		mDiscussionID = discussionID;
	}

	/**
	 * Creates an article from a Marshaled Parcel
	 * @param in
	 */
	private Article(Parcel in) {
		mTitle = in.readString();
		mDomain = in.readString();
		mURL = in.readString();
		mAuthor = in.readString();
		mDiscussionID = in.readString();
	}

	
	/**
	 * Some articles on HN are hosted on hn and some are not.
	 * If they are hosted on HN the article and the discussion are the same
	 * We can tell remote articles if the discussion does not equal the article url
	 * @return if its a hn article boolean
	 */
	public boolean isLocalArticle() {
		return getURL().equalsIgnoreCase(getDiscussionURL());
	}
	
	/**
	 * Returns the url for the article
	 * the domain news.ycombinator.com may be appended if its considered a local article
	 * @return url string
	 */
	public String getURL() {
		if (mURL.startsWith("http://"))
			return mURL;
		
		return "http://news.ycombinator.com/".concat(mURL);
	}

	/**
	 * Returns the title for the article
	 * 
	 * @return title string
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Returns the author for the article
	 * 
	 * @return author string
	 */
	public String getAuthor() {
		return mAuthor;
	}

	/**
	 * Returns the domain the article says its from
	 * 
	 * @return domain string
	 */
	public String getDomain() {
		return mDomain;
	}

	
	/**
	 * Returns a string URL for where the articles in discussed on HN
	 * @return url string
	 */
	public String getDiscussionURL() {
		return "http://news.ycombinator.com/item?id=".concat(getDiscussionID());
	}
	
	/**
	 * Returns the discussion ID on hacker news
	 * This should be accessed through the discussion URL
	 * @return discussion id string
	 */
	public String getDiscussionID() {
		return mDiscussionID;
	}
	
	
	/**
	 * Used to describe what type of data is being parceled
	 * @return bitmask flag
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	/*
	 * Writes to a parcel object all the fields in the Article
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(getTitle());
		out.writeString(getDomain());
		out.writeString(getURL());
		out.writeString(getAuthor());
		out.writeString(getDiscussionID());
	}



	
	/**
	 * CREATOR is used by Bundle for Parceling objects around threads
	 */
	public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
		public Article createFromParcel(Parcel in) {
			return new Article(in);
		}

		public Article[] newArray(int size) {
			return new Article[size];
		}
	};

}
