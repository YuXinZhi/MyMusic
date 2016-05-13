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
import android.text.TextUtils;
import android.util.Log;
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

	private String mSortOrder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_picker_activity);
		mAdapter.setActivity(this);
		setListAdapter(mAdapter);
		setTitle(R.string.working_songs);

	}

	//
	// private Cursor getTrackCursor(TrackListAdapter.TrackQueryHandler
	// queryhandler, String filter, boolean async) {
	//
	// if (queryhandler == null) {
	// throw new IllegalArgumentException();
	// }
	//
	// Cursor ret = null;
	// mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
	// StringBuilder where = new StringBuilder();
	// where.append(MediaStore.Audio.Media.TITLE + " != ''");
	//
	// if (mGenre != null) {
	// Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external",
	// Integer.valueOf(mGenre));
	// if (!TextUtils.isEmpty(filter)) {
	// uri = uri.buildUpon().appendQueryParameter("filter",
	// Uri.encode(filter)).build();
	// }
	// mSortOrder = MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER;
	// ret = queryhandler.doQuery(uri, mCursorCols, where.toString(), null,
	// mSortOrder, async);
	// } else if (mPlaylist != null) {
	// if (mPlaylist.equals("nowplaying")) {
	// if (MusicUtils.sService != null) {
	// ret = new NowPlayingCursor(MusicUtils.sService, mCursorCols);
	// if (ret.getCount() == 0) {
	// finish();
	// }
	// } else {
	// // Nothing is playing.
	// }
	// } else if (mPlaylist.equals("podcasts")) {
	// where.append(" AND " + MediaStore.Audio.Media.IS_PODCAST + "=1");
	// Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	// if (!TextUtils.isEmpty(filter)) {
	// uri = uri.buildUpon().appendQueryParameter("filter",
	// Uri.encode(filter)).build();
	// }
	// ret = queryhandler.doQuery(uri, mCursorCols, where.toString(), null,
	// MediaStore.Audio.Media.DEFAULT_SORT_ORDER, async);
	// } else if (mPlaylist.equals("recentlyadded")) {
	// // do a query for all songs added in the last X weeks
	// Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	// if (!TextUtils.isEmpty(filter)) {
	// uri = uri.buildUpon().appendQueryParameter("filter",
	// Uri.encode(filter)).build();
	// }
	// int X = MusicUtils.getIntPref(this, "numweeks", 2) * (3600 * 24 * 7);
	// where.append(" AND " + MediaStore.MediaColumns.DATE_ADDED + ">");
	// where.append(System.currentTimeMillis() / 1000 - X);
	// ret = queryhandler.doQuery(uri, mCursorCols, where.toString(), null,
	// MediaStore.Audio.Media.DEFAULT_SORT_ORDER, async);
	// } else {
	// Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
	// Long.valueOf(mPlaylist));
	// if (!TextUtils.isEmpty(filter)) {
	// uri = uri.buildUpon().appendQueryParameter("filter",
	// Uri.encode(filter)).build();
	// }
	// mSortOrder = MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER;
	// ret = queryhandler.doQuery(uri, mPlaylistMemberCols, where.toString(),
	// null, mSortOrder, async);
	// }
	// } else {
	// if (mAlbumId != null) {
	// where.append(" AND " + MediaStore.Audio.Media.ALBUM_ID + "=" + mAlbumId);
	// mSortOrder = MediaStore.Audio.Media.TRACK + ", " + mSortOrder;
	// }
	// if (mArtistId != null) {
	// where.append(" AND " + MediaStore.Audio.Media.ARTIST_ID + "=" +
	// mArtistId);
	// }
	// where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
	// Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	// if (!TextUtils.isEmpty(filter)) {
	// uri = uri.buildUpon().appendQueryParameter("filter",
	// Uri.encode(filter)).build();
	// }
	// ret = queryhandler.doQuery(uri, mCursorCols, where.toString(), null,
	// mSortOrder, async);
	// }
	//
	// // This special case is for the "nowplaying" cursor, which cannot be
	// // handled
	// // asynchronously using AsyncQueryHandler, so we do some extra
	// // initialization here.
	// if (ret != null && async) {
	// init(ret, false);
	// setTitle();
	// }
	// return ret;
	// }
	//

	// 查询歌曲信息的ListView适配器
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

		// A helper class to help make handling asynchronous ContentResolver
		// queries easier
		// 处理异步ContentResolver查询的帮助类
		class TrackQueryHandler extends AsyncQueryHandler {

			// 查询的参数
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
					/**
					 * public void startQuery(int token, Object cookie, Uri uri,
					 * String[] projection, String selection, String[]
					 * selectionArgs, String orderBy)
					 * 
					 * token: A token passed into onQueryComplete to identify
					 * the query.
					 * 
					 * cookie: An object that gets passed into onQueryComplete
					 * 
					 * This method begins an asynchronous query. When the query
					 * is done onQueryComplete is called.
					 */
					// 执行异步查询，查询结果args传给 onQueryComplete()
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
				// 查询完成
				Log.i("@@@", "query complete: " + cursor.getCount() + " " + mActivity);
				// cookie==null说明是startQuery执行的的不是异步查询，cookie是startQuery执行后传入的参数
				mActivity.init(cursor, cookie != null);

				/**
				 * token==0 执行的是异步查询
				 * 
				 * cookie the cookie object passed in from startQuery.
				 * 
				 * 
				 */
				if (token == 0 && cookie != null && cursor != null && !cursor.isClosed() && cursor.getCount() >= 100) {
					QueryArgs args = (QueryArgs) cookie;
					startQuery(1, null, args.uri, args.projection, args.selection, args.selectionArgs, args.orderBy);
				}

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
