package com.example.mymusic;

import android.app.ListActivity;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * 
 * ListActivity:
 * 
 * An activity that displays a list of items by binding to a data source such as
 * an array or Cursor, and exposes event handlers when the user selects an item.
 * 
 * ListActivity hosts a ListView object that can be bound to different data
 * sources, typically either an array or a Cursor holding query results.
 * Binding, screen layout, and row layout are discussed in the following
 * sections.
 * 
 * @author Ben
 *
 */

public class TrackBrowserActivity extends ListActivity {
	private ListView mTrackList;
	private TrackListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_picker_activity);

	}

	static class TrackListAdapter extends SimpleCursorAdapter {
		boolean mIsNowPlaying;//
		boolean mDisableNowPlayingIndicator;

		static class ViewHolder {
			TextView song;
			TextView artist;
			TextView duration;
			ImageView play_indicator;
			CharArrayBuffer buffer1;
			char[] buffer2;
		}

		public TrackListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to,
				boolean isnowplaying, boolean disablenowplayingindicator) {
			super(context, layout, cursor, from, to);

		}

	}
}
