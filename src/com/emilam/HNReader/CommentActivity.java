package com.emilam.HNReader;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;


/**
 * CommentList displays comments for the current post or current comment
 * @author emilam
 *
 */
public class CommentActivity extends ListActivity {

		
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		
		//pull are linkable object out of the intent to get our url
		Intent intent = getIntent();
		HNLinkable linkable = intent.getParcelableExtra("linkable");
		
		
		setContentView(R.layout.comment_list);
		
		ListView list = (ListView)findViewById(android.R.id.list);
		CommentList adapter = new CommentList(this, linkable);
		list.setAdapter(adapter);
		list.setOnItemClickListener(adapter);
		
		adapter.loadComments();
	}

	





}
