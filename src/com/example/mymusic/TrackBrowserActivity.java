package com.example.mymusic;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
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
	private Cursor mTrackCursor;

	private String mPlaylist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_picker_activity);

		mAdapter = new TrackListAdapter(getApplication(), // need to use
				// application
				// context to
				// avoid leaks
				this, R.layout.track_list_item, null, // cursor
				new String[] {}, new int[] {}, "nowplaying".equals(mPlaylist),
				mPlaylist != null && !(mPlaylist.equals("podcasts") || mPlaylist.equals("recentlyadded")));
		setListAdapter(mAdapter);
		setTitle(R.string.working_songs);
		getTrackCursor(mAdapter.getQueryHandler(), null, true);

		// if (mAdapter == null) {
		// mAdapter = new TrackListAdapter(getApplication(), // need to use
		// // application
		// // context to
		// // avoid leaks
		// this, R.layout.track_list_item, null, // cursor
		// new String[] {}, new int[] {}, "nowplaying".equals(mPlaylist),
		// mPlaylist != null && !(mPlaylist.equals("podcasts") ||
		// mPlaylist.equals("recentlyadded")));
		// setListAdapter(mAdapter);
		// setTitle(R.string.working_songs);
		// getTrackCursor(mAdapter.getQueryHandler(), null, true);
		// } else {
		// mTrackCursor = mAdapter.getCursor();
		// // If mTrackCursor is null, this can be because it doesn't have
		// // a cursor yet (because the initial query that sets its cursor
		// // is still in progress), or because the query failed.
		// // In order to not flash the error dialog at the user for the
		// // first case, simply retry the query when the cursor is null.
		// // Worst case, we end up doing the same query twice.
		// if (mTrackCursor != null) {
		// init(mTrackCursor, false);
		// } else {
		// setTitle(R.string.working_songs);
		// getTrackCursor(mAdapter.getQueryHandler(), null, true);
		// }
		// }

	}

	static class TrackListAdapter extends SimpleCursorAdapter {
		boolean mIsNowPlaying;
		boolean mDisableNowPlayingIndicator;

		int mTitleIdx;
		int mArtistIdx;
		int mDurationIdx;
		int mAudioIdIdx;

		private final StringBuilder mBuilder = new StringBuilder();
		private final String mUnknownArtist;
		private final String mUnknownAlbum;

		private TrackBrowserActivity mActivity = null;
		private TrackQueryHandler mQueryHandler;

		private String mConstraint = null;// 约束
		private boolean mConstraintIsValid = false;

		static class ViewHolder {
			TextView song;
			TextView artist;
			TextView duration;
			ImageView play_indicator;
			CharArrayBuffer buffer1;
			char[] buffer2;
		}

		class TrackQueryHandler extends AsyncQueryHandler {

			class QueryArgs {
				public Uri uri;
				public String[] projection;
				public String selection;
				public String[] selectionArgs;
				public String orderBy;
			}

			TrackQueryHandler(ContentResolver cr) {
				super(cr);
			}

			/**
			 * 
			 * @param uri
			 * @param projection
			 *            查询的条目
			 * @param selection
			 *            查询条件
			 * @param selectionArgs
			 *            查询条件中占位符参数
			 * @param orderBy
			 * @param async
			 *            是否是异步查询
			 */

			public Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs,
					String orderBy, boolean async) {
				if (async) {
					Uri limituri = uri.buildUpon().appendQueryParameter("limit", "100").build();
					QueryArgs args = new QueryArgs();
					args.uri = uri;
					args.projection = projection;
					args.selection = selection;
					args.selectionArgs = selectionArgs;
					args.orderBy = orderBy;
					// 交给handler处理查询,异步查询
					startQuery(0, args, limituri, projection, selection, selectionArgs, orderBy);
					return null;
				}
				return MusicUtils.query(mActivity, uri, projection, selection, selectionArgs, orderBy);
			}

			/**
			 * token: the token to identify the query, passed in from
			 * startQuery.
			 * 
			 * cookie: the cookie object passed in from startQuery.
			 * 
			 * cursor :The cursor holding the results from the query.
			 */

			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				mActivity.init(cursor, cookie != null);// 当cookie!=null说明limit是100，不止一条记录

			}

		}

		@SuppressWarnings("deprecation")
		public TrackListAdapter(Context context, TrackBrowserActivity currentactivity, int layout, Cursor cursor,
				String[] from, int[] to, boolean isnowplaying, boolean disablenowplayingindicator) {
			super(context, layout, cursor, from, to);
			mActivity = currentactivity;
			getColums(cursor);
			mIsNowPlaying = isnowplaying;
			mDisableNowPlayingIndicator = disablenowplayingindicator;
			mUnknownArtist = context.getString(R.string.unknown_artist_name);
			mUnknownAlbum = context.getString(R.string.unknown_album_name);

			mQueryHandler = new TrackQueryHandler(context.getContentResolver());
		}

		public void setActivity(TrackBrowserActivity newactivity) {
			mActivity = newactivity;
		}

		public TrackQueryHandler getQueryHandler() {
			return mQueryHandler;
		}

		// 设置查询结果赋值给
		private void getColums(Cursor cursor) {
			if (cursor != null) {
				mTitleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
				mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
				mDurationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
				try {
					mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
				} catch (IllegalArgumentException ex) {
					mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
				}
			}
		}

		/**
		 * Inflates view(s) from the specified XML file.
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);
			ImageView iv = (ImageView) view.findViewById(R.id.icon);
			iv.setVisibility(View.GONE);

			ViewHolder vh = new ViewHolder();
			vh.song = (TextView) view.findViewById(R.id.song);
			vh.artist = (TextView) view.findViewById(R.id.artist);
			vh.duration = (TextView) view.findViewById(R.id.duration);
			vh.play_indicator = (ImageView) view.findViewById(R.id.play_indicator);
			vh.buffer1 = new CharArrayBuffer(100);
			vh.buffer2 = new char[200];
			view.setTag(vh);

			return view;
		}

		/**
		 * Binds all of the field names passed into the "to" parameter of the
		 * constructor with their corresponding cursor columns as specified in
		 * the "from" parameter.
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder vh = (ViewHolder) view.getTag();
			// 把mTitleIdx列的值赋给vh.buffer1
			cursor.copyStringToBuffer(mTitleIdx, vh.buffer1);
			vh.song.setText(vh.buffer1.data, 0, vh.buffer1.sizeCopied);

			// 歌曲持续时间转换成秒
			int secs = cursor.getInt(mDurationIdx) / 1000;
			if (secs == 0) {
				vh.duration.setText("");

			} else {
				vh.duration.setTag(MusicUtils.makeTimeString(context, secs));
			}

			final StringBuilder builder = mBuilder;
			builder.delete(0, builder.length());

			String name = cursor.getString(mArtistIdx);
			if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
				builder.append(mUnknownAlbum);
			} else {
				builder.append(name);
			}

			int len = builder.length();
			if (vh.buffer2.length < len) {
				vh.buffer2 = new char[len];
			}

			builder.getChars(0, len, vh.buffer2, 0);
			vh.artist.setText(vh.buffer2, 0, len);

			ImageView iv = vh.play_indicator;
			long id = -1;

			if (MusicUtils.sService != null) {
				try {
					if (mIsNowPlaying) {
						id = MusicUtils.sService.getQueuePosition();
					} else {
						id = MusicUtils.sService.getAudioId();
					}
				} catch (RemoteException e) {
				}
			}

			if ((mIsNowPlaying && cursor.getPosition() == id)
					|| (!mIsNowPlaying && !mDisableNowPlayingIndicator && cursor.getLong(mAudioIdIdx) == id)) {
				iv.setImageResource(R.drawable.indicator_ic_mp_playing_list);
				iv.setVisibility(View.VISIBLE);
			} else {
				iv.setVisibility(View.GONE);
			}
		}

		@Override
		public void changeCursor(Cursor cursor) {
			if (mActivity.isFinishing() && cursor != null) {
				cursor.close();
				cursor = null;
			}
			if (cursor != mActivity.mTrackCursor) {
				mActivity.mTrackCursor = cursor;
				super.changeCursor(cursor);
				getColums(cursor);
			}
		}

		/**
		 * After this method returns the resulting cursor is passed to
		 * changeCursor(Cursor) and the previous cursor is closed.
		 */
		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			String s = constraint.toString();
			if (mConstraintIsValid && ((s == null) || (s != null && s.equals(mConstraint)))) {
				return getCursor();
			}
			Cursor cursor = mActivity.getTrackCursor(mQueryHandler, s, false);
			mConstraint = s;
			mConstraintIsValid = true;
			return cursor;
		}

	}

	private Cursor getTrackCursor(TrackListAdapter.TrackQueryHandler queryhandler, String filter, boolean async) {
		return null;
	}

	public void init(Cursor newCursor, boolean isLimited) {

	}
}
