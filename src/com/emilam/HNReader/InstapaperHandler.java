package com.emilam.HNReader;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Used to save an article to instapaper
 * @author emilam
 *
 */
public class InstapaperHandler {

	/*
	 * Activity's context used for dialogs
	 */
	private Context mContext;
	
	/**
	 * Dialog for displaying that we are saving to instapaper
	 */
	private ProgressDialog mDialog;
	
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String toastMessage = msg.getData().getString("message");
			mDialog.hide();
			Toast toast = Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 50);
			toast.show();
		}
	};
	
	public InstapaperHandler(Context context) {
		mContext = context;
		mDialog = new ProgressDialog(mContext);
		mDialog.setMessage(mContext.getText(R.string.instapaper_saving));
	}
	
	/**
	 * Saves the article by calling hitting the instapaper rest api
	 * @param article
	 */
	public void save(Article article) {
		mDialog.show();
		InstapaperSaver saver = new InstapaperSaver(article, mHandler);
		Thread thread = new Thread(saver);
		thread.start();
	}

	/**
	 * InstapaperWeb does the post to the server 
	 * 
	 * @author emilam
	 *
	 */
	private class InstapaperSaver implements Runnable {
		
		private static final int RESPONSE_OK = 201;
		private static final int RESPONSE_BAD_REQUEST = 400;
		private static final int RESPONSE_INVALID_ACCOUNT = 403;
		private static final int RESPONSE_ERROR = 500;
		
		/**
		 * The base url for all instapaper api requests
		 */
		private static final String BASE_URL = "http://www.instapaper.com/api/";
		
		/**
		 * The article to be saved
		 */
		private Article mArticle;
		
		/**
		 * The handler used to send a message back to the gui thread
		 */
		private Handler mHandler;
		
		
		
		public InstapaperSaver(Article article, Handler handler) {
			mArticle = article;
			mHandler = handler;
		}
		
		@Override
		public void run() {
			String toastMessage = "";
			try {
				toastMessage = saveArticle();
			} catch (Exception e) {
				Log.e("InstapaperSaver", e.toString());
				toastMessage = "Bad network connection.";
			}
			Message msg = mHandler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("message", toastMessage);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
		
		/**
		 * Using the http client, we post to the instapaper server
		 * @throws Exception
		 */
		private String saveArticle() throws Exception {
			HttpClient httpClient = new DefaultHttpClient();

			StringBuilder uriBuilder = new StringBuilder(BASE_URL);
			uriBuilder.append("add");

			HttpPost request = new HttpPost(uriBuilder.toString());
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			List<NameValuePair> params = new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("username", prefs.getString(UserPrefs.INSTAPAPER_USERNAME, "")));
			params.add(new BasicNameValuePair("password", prefs.getString(UserPrefs.INSTAPAPER_PASSWORD, "")));
			params.add(new BasicNameValuePair("url", mArticle.getURL()));
			
			request.setEntity(new UrlEncodedFormEntity(params));
			
			HttpResponse response = httpClient.execute(request);

			int status = response.getStatusLine().getStatusCode();
			String toastMessage;
			switch(status) {
				case RESPONSE_OK:
					toastMessage = "Article saved on Instapaper.";
					break;
				case RESPONSE_ERROR:
					toastMessage = "Error working with Instapaper. Please try later.";
					break;
				case RESPONSE_BAD_REQUEST:
					toastMessage = "Invalid request sent to Instapaper";
					break;
				case RESPONSE_INVALID_ACCOUNT:
					toastMessage = "Bad username or password";
					break;
				default:
					toastMessage = "Unknown response from Instapaper.";
					break;
			}
			
			return toastMessage;
			
		}

	}
}
