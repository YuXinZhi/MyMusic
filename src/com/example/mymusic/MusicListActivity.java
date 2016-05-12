package com.example.mymusic;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MusicListActivity extends ListActivity {

	SimpleCursorAdapter mAdapter;
	ListView mListView;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.avtivity_music_list);
		mListView = getListView();
		
		//Cursor c=new Cursor();
		mAdapter=new SimpleCursorAdapter(getApplicationContext(), R.layout.track_list_item, null, new String[]{}, new int[]{});
		
		mListView.setAdapter(mAdapter);

	}
	//
	// mAdapter=new
	// TrackListAdapter(getApplication(),this,R.layout.track_list_item,null,new
	// String[]{},new
	// int[]{},"nowplaying".equals(mPlaylist),mPlaylist!=null&&!(mPlaylist.equals("podcasts")||mPlaylist.equals("recentlyadded")));
	//
	// setListAdapter(mAdapter);

}
