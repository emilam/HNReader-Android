package com.emilam.HNReader;

import java.util.HashMap;
import java.util.Map;

import com.emilam.HNReader.R;
import com.flurry.android.FlurryAgent;


import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

/**
 * HNReader
 * HNReader is the primary activity for the app
 * From here we create our tabs and access the other activities in the app
 * @author emilam
 *
 */
public class HNReader extends TabActivity implements OnTabChangeListener {

	/* Tab Tag IDs */
	private static final String TAB_CURRENT = "tab_current";
	private static final String TAB_NEW = "tab_new";
	private static final String TAB_TOP = "tab_top";
	private static final String TAB_SAVED = "tab_saved";

	/* Options Menu Item IDs */
	private static final int MENU_OPTION_REFRESH = 1;
	private static final int MENU_OPTION_PREFS = 2;
	
	/* Used to id returning intents */
	private static final int REQUEST_USER_PREFS = 1;
	private static final int REQUEST_DISCUSS = 2;
	
	/**
	 * A map of all article tabs Map<TagID, ArticleList> This makes it easy for
	 * updating based off of TagID when tabs change or just finding the active
	 * tab
	 */
	private Map<String, ArticleList> mArticlesMap = new HashMap<String, ArticleList>();

	/**
	 * Used for saving articles to instapaper.
	 * The handler will make the web request
	 */
	private InstapaperHandler mInstapaper;
	
	//private ShakeSensor shaker = new ShakeSensor();

	/**
	 * Fired when the app starts
	 * Start out analytics at the same time
	 */
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, "1L2ML8VJZAVHE1ULJP6L");

	}

	/**
	 * Fired when the app is stopped
	 * We want to let our analytics catch this event
	 */
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TabHost tabHost = getTabHost();
		TabSpec tabSpec;
		Resources resources = getResources();

		tabSpec = tabHost.newTabSpec(TAB_CURRENT);
		tabSpec.setIndicator(resources.getString(R.string.tab_current_label),
				resources.getDrawable(R.drawable.light_button));
		tabSpec.setContent(R.id.current_list);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec(TAB_TOP);
		tabSpec.setIndicator(resources.getString(R.string.tab_top_label),
				resources.getDrawable(R.drawable.ribbon_button));
		tabSpec.setContent(R.id.top_list);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec(TAB_NEW);
		tabSpec.setIndicator(resources.getString(R.string.tab_new_label),
				resources.getDrawable(R.drawable.calendar_button));
		tabSpec.setContent(R.id.new_list);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec(TAB_SAVED);
		tabSpec.setIndicator(resources.getString(R.string.tab_saved_label),
				resources.getDrawable(R.drawable.saved_button));
		tabSpec.setContent(R.id.saved_list);
		tabHost.addTab(tabSpec);

		// Add the current article list
		ListView list = (ListView) findViewById(R.id.current_list);
		ArticleList aList = new ArticleListWeb(this, "");
		aList.loadArticles(false);
		list.setAdapter(aList);
		list.setOnItemClickListener(aList);
		list.setOnCreateContextMenuListener(aList);
		mArticlesMap.put(TAB_CURRENT, aList);

		// Add the top article list
		list = (ListView) findViewById(R.id.top_list);
		aList = new ArticleListWeb(this, "best");
		list.setAdapter(aList);
		list.setOnItemClickListener(aList);
		list.setOnCreateContextMenuListener(aList);
		mArticlesMap.put(TAB_TOP, aList);

		// Add the new article list
		list = (ListView) findViewById(R.id.new_list);
		aList = new ArticleListWeb(this, "newest");
		list.setAdapter(aList);
		list.setOnItemClickListener(aList);
		list.setOnCreateContextMenuListener(aList);
		mArticlesMap.put(TAB_NEW, aList);

		list = (ListView) findViewById(R.id.saved_list);
		aList = new ArticleListSaved(this);
		list.setAdapter(aList);
		list.setOnItemClickListener(aList);
		list.setOnCreateContextMenuListener(aList);
		mArticlesMap.put(TAB_SAVED, aList);

		tabHost.setOnTabChangedListener(this);
		// Set the current tab the current news
		tabHost.setCurrentTabByTag(TAB_CURRENT);

		//TODO: This is still in the works
		//shaker.registerListener();
		
		mInstapaper = new InstapaperHandler(this);
	}

	@Override
	public void onTabChanged(String tabID) {
		if (mArticlesMap.containsKey(tabID) == false)
			return;

		ArticleList view = mArticlesMap.get(tabID);
		view.loadArticles(false);

	}

	/**
	 * Returns the current tab selected
	 * 
	 * @return
	 */
	public ArticleList getCurrentTab() {
		String tabID = this.getTabHost().getCurrentTabTag();
		if (mArticlesMap.containsKey(tabID) == false)
			return null;

		ArticleList view = mArticlesMap.get(tabID);
		return view;
	}

	/**
	 * Refreshes the list on the current tab
	 */
	public void refreshCurrentTab() {

		ArticleList view = getCurrentTab();
		if (view != null)
			view.loadArticles(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_OPTION_REFRESH, Menu.NONE,
				this.getResources().getText(R.string.menu_refresh)).setIcon(
				android.R.drawable.ic_menu_rotate);

		menu.add(Menu.NONE, MENU_OPTION_PREFS, Menu.NONE,
				this.getResources().getText(R.string.menu_prefs)).setIcon(
				android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_OPTION_REFRESH:
			refreshCurrentTab();
			return true;
		case MENU_OPTION_PREFS:
			startActivityForResult(new Intent(this, UserPrefs.class), REQUEST_USER_PREFS);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Saves the article to the ArticleListSaved or deletes it from the list
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ArticleList saveList;
		ArticleList currentList = getCurrentTab();
		Article selectedArticle = currentList.getSelectedArticle();
		switch (item.getItemId()) {
		case ArticleList.MENU_CONTEXT_SAVE:
			saveList = mArticlesMap.get(TAB_SAVED);
			((ArticleListSaved) saveList).saveArticle(selectedArticle);
			return true;
		case ArticleList.MENU_CONTEXT_DELETE:
			((ArticleListSaved) currentList).delete((SavedArticle) selectedArticle);
			return true;
		case ArticleList.MENU_CONTEXT_INSTAPAPER:
			mInstapaper.save(selectedArticle);
			return true;
		case ArticleList.MENU_CONTEXT_DISCUSS:
			Intent intent = new Intent(this, CommentActivity.class);
			intent.putExtra("linkable", selectedArticle);
			startActivityForResult(intent, REQUEST_DISCUSS);
			
			return true;
		}
		return false;
	}

	/**
	 * When the phone has been "shaken" this event will be fired We want the
	 * current tabs list to be updated
	 */
	public void onShakeEvent() {
		refreshCurrentTab();
	}
	
	@SuppressWarnings("unused")
	private class ShakeSensor implements SensorEventListener {

		@SuppressWarnings("unused")
		public void registerListener() {

			SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
			if (manager.getSensorList(SensorManager.SENSOR_ACCELEROMETER)
					.size() <= 0)
				return;

			boolean accelSupported = manager.registerListener(this, manager
					.getSensorList(SensorManager.SENSOR_ACCELEROMETER).get(0),
					SensorManager.SENSOR_DELAY_UI);

			if (accelSupported == false) {
				Log.w("ShakeSensor", "Accelerometer not supported");
				// no accelerometer on this device

				manager.unregisterListener(this, manager.getSensorList(
						SensorManager.SENSOR_ACCELEROMETER).get(0));
			}
		}

		/**
		 * How much and how fast does the phone have to move to be considered a
		 * "shake"
		 */
		private static final int SHAKE_SPEED_THRESHOLD = 500;

		/**
		 * How often to check if its a shake
		 */
		private static final int SHAKE_TIME_MIN = 200;

		/**
		 * Time the sensor was last checked
		 */
		private long timeLastUpdated;

		private float x, xLast, y, yLast, z, zLast;

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() != SensorManager.SENSOR_ACCELEROMETER)
				return;

			long timeCurrent = System.currentTimeMillis();

			if ((timeCurrent - timeLastUpdated) > SHAKE_TIME_MIN) {
				long timeDiff = timeCurrent - timeLastUpdated;
				timeLastUpdated = timeCurrent;

				x = event.values[SensorManager.DATA_X];
				y = event.values[SensorManager.DATA_Y];

				float xSpeed = Math.abs(x - xLast) / timeDiff * 10000;
				float ySpeed = Math.abs(y - yLast) / timeDiff * 10000;
				float zSpeed = Math.abs(z - zLast) / timeDiff * 10000;

				float speed = (xSpeed + ySpeed + zSpeed) / 3;

				if (speed > SHAKE_SPEED_THRESHOLD)
					onShakeEvent();

				xLast = x;
				yLast = y;
				zLast = z;
			}

		}

	}

}