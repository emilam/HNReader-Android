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
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;

public class ArticleListWeb extends ArticleList {

	
	
	/*
	 * Base Web URL used by the WebRunner
	 */
	public final String BASE_URL = "http://news.ycombinator.com/"; // 174.132.225.106
	
	/**
	 * The appended part of the url for each article section
	 */
	private String mURL;
	
	/**
	 * if the runner is running this will be true
	 */
	private Thread thread = null;
	
	
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			List<Article> articles = msg.getData().getParcelableArrayList("articles");
			
			// We only want to update the list if we actually got some articles back 
			if(articles != null && articles.size() > 0) {
				mArticles.clear();
				mArticles.addAll(articles);
				fireUpdateNotification();
			} else {
				Toast toast = Toast.makeText(mContext, R.string.no_articles, 
						Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};
	
	
	/**
	 * Construct which takes the url to append to the ycombinator URL
	 * @param context
	 * @param url
	 */
	public ArticleListWeb(Context context, String url) {
		super(context);
		mURL = url;
		
		
	}
	
	/**
	 * Called when we want to load/reload articles
	 * Fires off a thread to handle loading the articles so we dont 
	 * load on the UI thread
	 */
	@Override
	protected synchronized void getArticles() throws Exception {
		if(thread == null || thread.isAlive() == false) {
			WebRunner runner = new WebRunner(mHandler);
			thread = new Thread(runner);
			thread.start();
		}
    }
	
    /*
     * Shows the save button
     * (non-Javadoc)
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, view, menuInfo);
    	menu.add(Menu.NONE, MENU_CONTEXT_SAVE, Menu.NONE, 
    			mContext.getText(R.string.context_save));
    }
    

    
	/**
	 * WebRunner will grab the hn articles on a different thread 
	 * 
	 * @author emilam
	 *
	 */
	private class WebRunner implements Runnable {
		/**
		 * The handler that will receive the articles after we fetch them
		 */
		Handler mHandler;
		
		public WebRunner(Handler handler) {
			mHandler = handler;
		}
		
		@Override
		public void run() {
			try {
				getArticles();
			} catch (Exception e) {
				Log.e("ArticleListWeb", e.toString());
			}
			
		}
		
		/**
		 * Using the http client, this will get the html from the site
		 * @throws Exception
		 */
		private void getArticles() throws Exception {
			HttpClient httpClient = new DefaultHttpClient();

			StringBuilder uriBuilder = new StringBuilder(BASE_URL);
			uriBuilder.append(mURL);

			HttpGet request = new HttpGet(uriBuilder.toString());
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			StringBuilder result = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}

			// Clean up and close connection.
			content.close();

			String html = result.toString();
			//This is our regex to match each article
			//This website's html is not xml compatible so regex is next best thing
			Pattern p = Pattern.compile("<td class=\"title\"><a href=\"(.*?)\".*?>(.*?)<\\/a>(<span class=\"comhead\">(.*?)<\\/span>)?.*?<\\/td><\\/tr><tr><td colspan=2><\\/td><td class=\"subtext\">.*? by <a href=\"user\\?.*?\">(.*?)<\\/a>.*?<a href=\"item\\?id=(.*?)\">");
			Matcher m = p.matcher(html);
			List<Article> articles = new ArrayList<Article>();
			while(m.find()) {
				String url = m.group(1);
				String title = m.group(2);
				String domain = m.group(4);
				String author = m.group(5);
				String discussionID = m.group(6);
				Article eachArticle = new Article(title, domain, url, author, discussionID);
				articles.add(eachArticle); 
			}

			Message msg = mHandler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("articles", (ArrayList<? extends Parcelable>) articles);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

	}





}
