package com.emilam.HNReader;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TwoLineListItem;
import android.widget.AdapterView.OnItemClickListener;

public abstract class ArticleList implements ListAdapter, OnItemClickListener, OnCreateContextMenuListener {

	/**
	 * Context menu items
	 */
	public static final int MENU_CONTEXT_INSTAPAPER = 4;
	public static final int MENU_CONTEXT_DISCUSS = 3;
	public static final int MENU_CONTEXT_DELETE = 2;
	public static final int MENU_CONTEXT_SAVE = 1;
	
	
	/**
	 * list of articles in the ArticleList
	 */
	protected List<Article> mArticles = new ArrayList<Article>();

	/*
	 * Context for HNReader
	 */
	protected Context mContext;

	/*
	 * List of Observers looking for changes to our ArticleList
	 */
	private List<DataSetObserver> mObservers = new ArrayList<DataSetObserver>();

	
	/*
	 * Selected article index for the context menu
	 */
	protected int selectedArticleIndex;
	
	
	/**
	 * Flag to see if the list has been loaded before
	 * We dont use size because this wont work on all types of lists
	 */
	protected boolean listHasLoaded;
	
	/*
	 * Layout inflater
	 */
	private LayoutInflater mInflater;

	
	/**
	 * Dialog used for showing loading
	 */
	protected ProgressDialog mDialog;
	
	
	/**
	 * Performs loading the articles
	 * @throws Exception
	 */
	protected abstract void getArticles() throws Exception;
	

	 
	/**
	 * Initializes our list adapter
	 * @param context
	 */
	public ArticleList(Context context) {
		mContext = context;
		listHasLoaded = false;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDialog = new ProgressDialog(mContext);
		mDialog.setMessage(mContext.getText(R.string.list_loading));
	}

	public Article getSelectedArticle() {
		return mArticles.get(selectedArticleIndex);
	}
	
	/**
	 * Displays the loading dialog
	 */
	protected void showLoadingDialog() {
		mDialog.show();
	}
	
	/**
	 * Hides the loading dialog
	 */
	protected void hideLoadingDialog() {
		mDialog.hide();
	}
	
	/**
	 * Loads the articles by calling getArticles
	 * It does a simple check to make sure we dont keep reloading
	 * unless the reload has been set. this is called multiple times through
	 * the activity when the view becomes active. We want the articles to 
	 * load the first time, but not anymore unless reload is true
	 * @param reload
	 */
	public void loadArticles(boolean reload) {
		if (listHasLoaded == true && reload == false)
			return;
		
		
		showLoadingDialog();
		try {
			getArticles();
		} catch (Exception e) {
			Log.e("GetArticles", e.toString());
			hideLoadingDialog();
		}
		
		listHasLoaded = true;
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
		return mArticles.size();
	}

	@Override
	public Object getItem(int position) {
		return mArticles.get(position);
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
		Article article = mArticles.get(position);
		TwoLineListItem view;

		if (convertView != null && convertView instanceof TwoLineListItem) {
			view = (TwoLineListItem) convertView;
		} else {
			view = (TwoLineListItem) mInflater.inflate(
					R.layout.two_line, parent, false);
		}

		view.getText1().setText(article.getTitle());
		view.getText2().setText(article.getDomain());

		return view;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return !(mArticles.size() > 0);
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
		hideLoadingDialog();
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
	protected void onArticleClicked(Article article) {
		Intent intent = new Intent();
		
		intent.setAction("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.BROWSABLE");
		Uri uri = Uri.parse(article.getURL());
		
		intent.setData(uri);
		mContext.startActivity(intent);
		
	}
	
	/**
	 * Handles when a list is clicked on. 
	 * Passes the article clicked on to the onArticleClicked method
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("ArticleList", "Clicked Position: " + position);
		Article article = mArticles.get(position);
		onArticleClicked(article);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        TwoLineListItem listItem = (TwoLineListItem) info.targetView;
        String selectedItem = listItem.getText1().getText().toString();
        selectedArticleIndex = info.position;
        
        String title = new String(selectedItem);
        if (title.length() > 20)
        	title = title.substring(0, 20);
        menu.setHeaderTitle(title); // truncate the title so we are on 1 line
        menu.add(Menu.NONE, MENU_CONTEXT_DISCUSS, Menu.NONE, mContext.getText(R.string.context_discuss));
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean showInstapaper = !(prefs.getString(UserPrefs.INSTAPAPER_USERNAME, "").equals(""));
        if (showInstapaper)
        	menu.add(Menu.NONE, MENU_CONTEXT_INSTAPAPER, Menu.NONE, mContext.getText(R.string.context_instapaper));
	}

	
	
	
}
