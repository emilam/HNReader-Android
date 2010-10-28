package com.emilam.HNReader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;
import android.widget.TwoLineListItem;
import android.widget.AdapterView.OnItemClickListener;

public class CommentList implements ListAdapter, OnItemClickListener {

	/**
	 * The comments to display in the list
	 * This will be updated by the runner
	 */
	private List<Comment> mComments = new ArrayList<Comment>();
	
	/**
	 * Represents the pacel array key in the bundle
	 */
	private static final String PARCEL_NAME = "comments";
	
	/**
	 * This contains the item which has the url
	 */
	private HNLinkable mLinkable;
	
	/**
	 * Dialog used for showing loading
	 */
	protected ProgressDialog mDialog;
	
	/*
	 * List of Observers looking for changes to our CommentList
	 */
	private List<DataSetObserver> mObservers = new ArrayList<DataSetObserver>();

	
	/**
	 * Used for the context. It is actually this activity
	 */
	private Context mContext;
	

	/*
	 * Layout inflater
	 */
	private LayoutInflater mInflater;
	
	
	/**
	 * Handler for when the WebRunner returns comments
	 */
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mDialog.hide();
			
			List<Comment> comments = msg.getData().getParcelableArrayList(PARCEL_NAME);
						
			// We only want to update the list if we actually got some articles back 
			if(comments != null && comments.size() > 0) {
				mComments.clear();
				mComments.addAll(comments);
				fireUpdateNotification();
			} else {
				Toast toast = Toast.makeText(mContext,
											R.string.no_comments, 
											Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};
	
	
	public CommentList(Context context, HNLinkable linkable) {
		mContext = context;
		
		//this is the article/comment we are currently viewing
		mLinkable = linkable;
		
		mInflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mDialog = new ProgressDialog(mContext);
		mDialog.setMessage(mContext.getText(R.string.list_loading));
	}

	/**
	 * Handles requesting the comments and setting up UI
	 */
	public void loadComments() {

		mDialog.show();
		try {
			getComments();
		} catch (Exception e) {
			Log.e("GetComments", e.toString());
			mDialog.hide();
		}
		
	}
	
	/**
	 * Called when we want to load/reload comments
	 * Fires off a thread to handle loading the articles so we dont load on the UI thread
	 */
	private void getComments() throws Exception {
		WebRunner runner = new WebRunner(mHandler, mLinkable.getDiscussionURL());
		Thread thread = new Thread(runner);
		thread.start();
    }
	

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}

	@Override
	public int getCount() {
		return mComments.size();
	}

	@Override
	public Object getItem(int position) {
		return mComments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	/*
	 * Returns a news article view element (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Comment comment = mComments.get(position);
		TwoLineListItem view;

		if (convertView != null && convertView instanceof TwoLineListItem) {
			view = (TwoLineListItem) convertView;
		} else {
			view = (TwoLineListItem) mInflater.inflate(
					R.layout.two_line_comment, parent, false);
		}

		view.getText1().setText(comment.getAuthor().concat("" + comment.getReplyCount()));
		view.getText2().setText(comment.getText());
		
		
		return view;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return !(mComments.size() > 0);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mObservers.add(observer);

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mObservers.remove(observer);

	}

	/**
	 * Notified all observers the list has been updated
	 */
	public void fireUpdateNotification() {
		mDialog.hide();
		for (DataSetObserver observer : mObservers) {
			observer.onChanged();
		}
	}

	/*
	 * When an article is clicked by the user this method is called
	 * We want to open this up in our browser. We are not forcing
	 * the Google Browser to launch just whatever is registered for this
	 *
	 */
	protected void onCommentClicked(Comment comment) {
		Intent intent = new Intent(mContext, CommentActivity.class);
		intent.putExtra("linkable", comment);
		mContext.startActivity(intent);
		
	}
	
	/**
	 * Handles when a list is clicked on. 
	 * Passes the article clicked on to the onArticleClicked method
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("CommentList", "Clicked Position: " + position);
		Comment comment = mComments.get(position);
		onCommentClicked(comment);
	}

	   
	/**
	 * WebRunner will grab the hn comments on a different thread 
	 * 
	 * @author emilam
	 *
	 */
	private class WebRunner implements Runnable {
		/**
		 * The handler that will receive the articles after we fetch them
		 */
		Handler mHandler;
		
		
		/**
		 * The url to the current article or comment
		 */
		private String mURL;
		
		/**
		 * Used to create a WebRunner to get the comments for the list
		 * @param handler
		 * @param url
		 */
		public WebRunner(Handler handler, String url) {
			mHandler = handler;
			mURL = url;
		}
		
		@Override
		public void run() {
			try {
				getComments();
			} catch (Exception e) {
				Log.e("Comment.WebRunner", e.toString());
			}
			
		}
		
		/**
		 * Using the http client, this will get the html from the site
		 * and parse out all the current level comments
		 * @throws Exception
		 */
		private void getComments() throws Exception {
			HttpClient httpClient = new DefaultHttpClient();

			
			HttpGet request = new HttpGet(mURL);
			HttpResponse response = httpClient.execute(request);

			int status = response.getStatusLine().getStatusCode();

			if (status != HttpStatus.SC_OK) {

				// Log whatever the server returns in the response body.
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();

				response.getEntity().writeTo(ostream);
				mHandler.sendEmptyMessage(0);
				return;
			}

			InputStream content = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(content));
			StringBuilder result = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}

			// Clean up and close connection.
			content.close();

			String html = result.toString();
			//This is our regex to match top level comments
			//This is done by checking width of 0 on the s.gif
			Pattern p = Pattern.compile("<img src=\".*?s\\.gif\" height=1 width=(\\d+)>.*?<a href=\"user\\?id=.*?\">(.*?)<\\/a>.*?<a href=\"(item\\?id=\\d+)\">link<\\/a>.*?<font color=#.*?>(.*?)<\\/font>");
			Matcher m = p.matcher(html);
			List<Comment> comments = new ArrayList<Comment>();
			Comment each = null;
			
			while(m.find()) {
				//we can distinguish top level comments by their width
				boolean isTop = m.group(1).equals("10") || m.group(1).equals("0");
				String author = m.group(2);
				String url = m.group(3);
				String text = m.group(4);
				if (isTop) {
					each = new Comment(author, url, text);
					comments.add(each);
				} else {
					each.addReply();
				}
			}

			Message msg = mHandler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList(PARCEL_NAME, 
					(ArrayList<? extends Parcelable>) comments);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

	}
}
