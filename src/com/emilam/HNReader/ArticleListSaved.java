package com.emilam.HNReader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

public class ArticleListSaved extends ArticleList {

	private Database db;
	
	
	
	private static final String DB_TABLE = "article";
	
	public ArticleListSaved(Context context) {
		super(context);
		db = new Database();
	}

	@Override
	protected void getArticles() throws Exception {
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor c = reader.rawQuery("select * from " + DB_TABLE, null);
		mArticles.clear();
		while(c.moveToNext()) {
			int id = c.getInt(0);
			String title = c.getString(1);
			String domain = c.getString(2);
			String url = c.getString(3);
			String author = c.getString(4);
			String discussionID = c.getString(5);
			SavedArticle article = new SavedArticle(title, domain, url, author, discussionID, id);
			mArticles.add(0, article);
		}
		c.close();
		reader.close();
		fireUpdateNotification();
	}

	/**
	 * Saves the article to the DB and then creates a saved article object
	 * @param article
	 */
	public void saveArticle(Article article) {
		SQLiteDatabase writer = db.getWritableDatabase();
		
		ContentValues cvalues = new ContentValues();
		cvalues.put("title", article.getTitle());
		cvalues.put("domain", article.getDomain());
		cvalues.put("url", article.getURL());
		cvalues.put("author", article.getAuthor());
		cvalues.put("discussion_id", article.getDiscussionID());
		
		int id = (int)writer.insert(DB_TABLE, null, cvalues);
		if (id == -1) {
			Log.w("ArticleListSaved", "Unable to save article. Already in DB");
			return;
		}
		
		SavedArticle saved =  new SavedArticle(article, id);
		mArticles.add(0, saved);
		writer.close();
		fireUpdateNotification();
	}
	
	

    /*
     * Shows the save button
     * (non-Javadoc)
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(Menu.NONE, MENU_CONTEXT_DELETE, Menu.NONE, mContext.getText(R.string.context_delete));
        
    }
    
    
	
	public void delete(SavedArticle article) {
		SQLiteDatabase writer = db.getWritableDatabase();
		writer.delete(DB_TABLE, "id = ?", new String[] { "" + article.getID() } );
		writer.close();
		mArticles.remove(article);
		fireUpdateNotification();
	}
	
	private class Database extends SQLiteOpenHelper {

		private static final String DB_NAME = "hnreader.db";
		private static final int DB_VERSION = 1;
		public Database() {
			super(mContext, DB_NAME, null, DB_VERSION);
		}

		
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE \"article\" (\"id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"title\" VARCHAR NOT NULL , \"domain\" VARCHAR NOT NULL , \"url\" VARCHAR NOT NULL , \"author\" VARCHAR NOT NULL , \"discussion_id\" VARCHAR NOT NULL )";
			db.execSQL(sql);
			
			sql = "CREATE UNIQUE INDEX \"urlkey\" ON \"article\" (\"url\" ASC)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int v1, int v2) {
			
		}
		
	}
}
