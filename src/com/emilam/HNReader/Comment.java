package com.emilam.HNReader;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
/**
 * Represents a comment from the HN discussion
 * 
 * @author emilam
 * 
 */
public class Comment implements Parcelable, HNLinkable {

	private String mAuthor;
	private String mURL;
	private String mText;
	private int mReplies;
	
	/**
	 * Creates the comment
	 * 
	 * @param author
	 * @param url
	 * @param text
	 */
	public Comment(String author, String url, String text) {
		this(author, url, text, 0);
	}
	
	public Comment(String author, String url, String text, int replies) {
		mAuthor = new String(author);
		mURL = new String(url);
		
		//clean up the html
		text = text.replaceAll("<p>", "<br/>");
		text = text.replaceAll("<\\/?a>", "");
		CharSequence unescaped = Html.fromHtml(text);
		mText = new String(unescaped.toString());
		
		mReplies = replies;
	}

	/**
	 * Used to create a comment from a Parcel.
	 * This is used by the Bundle in CommentList.WebRunner
	 * 
	 * @param in
	 */
	public Comment(Parcel in) {
		//The order needs to match writeToPacel
		mAuthor = in.readString();
		mURL = in.readString();
		mText = in.readString();
		mReplies = in.readInt();
	}
	
	/**
	 * Returns the author of the comment
	 * 
	 * @return
	 */
	public String getAuthor() {
		return mAuthor;
	}

	/**
	 * Returns the full url to the comment
	 * 
	 * @return
	 */
	public String getDiscussionURL() {
		return "http://news.ycombinator.com/".concat(mURL);
	}

	/**
	 * Returns the actual comment text
	 * 
	 * @return
	 */
	public String getText() {
		return mText;
	}

	/**
	 * Returns the number of replies this comment has received
	 * @return
	 */
	public int getReplyCount() {
		return mReplies;
	}
	
	/**
	 * Increments the number of replies of this comment
	 */
	public void addReply() {
		mReplies += 1;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mAuthor);
		out.writeString(mURL);
		out.writeString(mText);
		out.writeInt(mReplies);
	}

	/**
	 * CREATOR is used by Bundle for Parceling objects around threads
	 */
	public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
		public Comment createFromParcel(Parcel in) {
			return new Comment(in);
		}

		public Comment[] newArray(int size) {
			return new Comment[size];
		}
	};
}
