package org.apache.cordova.media;

import org.apache.cordova.CordovaInterface;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

import android.util.Log;
import android.R;
import android.content.Context;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.net.Uri;

import android.app.NotificationChannel;
import org.apache.cordova.LOG;

public class MusicControlsNotification {
	private Activity cordovaActivity;
	private NotificationManager notificationManager;
	private Notification.Builder notificationBuilder;
	private int notificationID;
	protected MusicControlsInfos infos;
	private Bitmap bitmapCover;
	private String CHANNEL_ID;

	public boolean prev_down = false;
	public boolean next_down = false;
	public boolean pause_down = false;
	public boolean play_down = false;

	// Public Constructor
	public MusicControlsNotification(Activity cordovaActivity, int id){
		this.CHANNEL_ID = UUID.randomUUID().toString();
		this.notificationID = id;
		this.cordovaActivity = cordovaActivity;
		Context context = cordovaActivity;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// use channelid for Oreo and higher
		if (Build.VERSION.SDK_INT >= 26) {
			// The user-visible name of the channel.
			CharSequence name = "Audio Controls";
			// The user-visible description of the channel.
			String description = "Control Playing Audio";

			int importance = NotificationManager.IMPORTANCE_LOW;

			NotificationChannel mChannel = new NotificationChannel(this.CHANNEL_ID, name,importance);

			// Configure the notification channel.
			mChannel.setDescription(description);

			// Don't show badges for this channel
			mChannel.setShowBadge(false);

			this.notificationManager.createNotificationChannel(mChannel);
    	}
	}

	// Show or update notification
	public void updateNotification(MusicControlsInfos newInfos){
		// Check if the cover has changed	
		if (!newInfos.cover.isEmpty() && (this.infos == null || !newInfos.cover.equals(this.infos.cover))){
			this.getBitmapCover(newInfos.cover);
		}
		this.infos = newInfos;
		this.createBuilder();
		Notification noti = this.notificationBuilder.build();
		this.notificationManager.notify(this.notificationID, noti);
		this.onNotificationUpdated(noti);
	}

	public void updateCover(String newCover) {
		if (!this.infos.cover.equals(newCover)) {
			this.getBitmapCover(newCover);
			this.infos.cover = newCover;
			this.createBuilder();
			Notification noti = this.notificationBuilder.build();
			this.notificationManager.notify(this.notificationID, noti);
			this.onNotificationUpdated(noti);
		}
	}

	// Toggle the play/pause button
	public boolean updateIsPlaying(boolean isPlaying) {
		if (this.infos == null) {
			return false;
		}

		try {
			this.infos.isPlaying=isPlaying;
			this.createBuilder();
			Notification noti = this.notificationBuilder.build();
			this.notificationManager.notify(this.notificationID, noti);
			this.onNotificationUpdated(noti);
		} catch (Exception e) {
            LOG.e("MusicControls ERROR: ", "updateIsPlaying ", e);

			try {
				Thread.sleep(100);
			} catch (Exception ex) {}

			this.updateIsPlaying(this.infos.isPlaying);
        }

		return true;
	}

	// Refresh
	public boolean refresh() {
		if (this.infos == null) {
			return false;
		}

		try {
			this.createBuilder();
			Notification noti = this.notificationBuilder.build();
			this.notificationManager.notify(this.notificationID, noti);
			this.onNotificationUpdated(noti);
		} catch (Exception e) {
            LOG.e("MusicControls ERROR: ", "refresh ", e);
        }

		return true;
	}

	// Toggle the dismissable status
	public void updateDismissable(boolean dismissable){
		if (this.infos == null) {
			return;
		}
		
		this.infos.dismissable=dismissable;
		this.createBuilder();
		Notification noti = this.notificationBuilder.build();
		this.notificationManager.notify(this.notificationID, noti);
		this.onNotificationUpdated(noti);
	}

	// Get image from url
	private void getBitmapCover(String coverURL){
		try{
			if(coverURL.matches("^(https?|ftp)://.*$"))
				// Remote image
				this.bitmapCover = getBitmapFromURL(coverURL);
			else{
				// Local image
				this.bitmapCover = getBitmapFromLocal(coverURL);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// get Local image
	private Bitmap getBitmapFromLocal(String localURL){
		try {
			Uri uri = Uri.parse(localURL);
			File file = new File(uri.getPath());
			FileInputStream fileStream = new FileInputStream(file);
			BufferedInputStream buf = new BufferedInputStream(fileStream);
			Bitmap myBitmap = BitmapFactory.decodeStream(buf);
			buf.close();
			return myBitmap;
		} catch (Exception ex) {
			try {
				InputStream fileStream = cordovaActivity.getAssets().open("www/" + localURL);
				BufferedInputStream buf = new BufferedInputStream(fileStream);
				Bitmap myBitmap = BitmapFactory.decodeStream(buf);
				buf.close();
				return myBitmap;
			} catch (Exception ex2) {
				ex.printStackTrace();
				ex2.printStackTrace();
				return null;
			}
		}
	}

	// get Remote image
	private Bitmap getBitmapFromURL(String strURL) {
		try {
			URL url = new URL(strURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void createBuilder(){
		Context context = cordovaActivity;
		Notification.Builder builder = new Notification.Builder(context);

		// use channelid for Oreo and higher
		if (Build.VERSION.SDK_INT >= 26) {
			builder.setChannelId(this.CHANNEL_ID);
		}

		//Configure builder
		builder.setContentTitle(infos.track);
		if (!infos.artist.isEmpty()){
			builder.setContentText(infos.artist);
		}
		builder.setWhen(0);

		// set if the notification can be destroyed by swiping
		if (infos.dismissable){
			builder.setOngoing(false);
			Intent dismissIntent = new Intent("audio-music-controls-destroy");
			PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 1, dismissIntent, 0);
			builder.setDeleteIntent(dismissPendingIntent);
		} else {
			builder.setOngoing(true);
		}
		if (!infos.ticker.isEmpty()){
			builder.setTicker(infos.ticker);
		}
		
		builder.setPriority(Notification.PRIORITY_MAX);

		//If 5.0 >= set the controls to be visible on lockscreen
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
			builder.setVisibility(Notification.VISIBILITY_PUBLIC);
		}

		//Set SmallIcon
		boolean usePlayingIcon = infos.notificationIcon.isEmpty();
		if(!usePlayingIcon){
			int resId = this.getResourceId(infos.notificationIcon, 0);
			usePlayingIcon = resId == 0;
			if(!usePlayingIcon) {
				builder.setSmallIcon(resId);
			}
		}

		if(usePlayingIcon){
			if (infos.isPlaying){
				builder.setSmallIcon(this.getResourceId(infos.playIcon, android.R.drawable.ic_media_play));
			} else {
				builder.setSmallIcon(this.getResourceId(infos.pauseIcon, android.R.drawable.ic_media_pause));
			}
		}

		//Set LargeIcon
		if (!infos.cover.isEmpty() && this.bitmapCover != null){
			builder.setLargeIcon(this.bitmapCover);
		}

		//Open app if tapped
		Intent resultIntent = new Intent(context, cordovaActivity.getClass());
		resultIntent.setAction(Intent.ACTION_MAIN);
		resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
		builder.setContentIntent(resultPendingIntent);

		//Controls
		int nbControls=0;

		if (infos.hasPrev){
			/* Previous  */
			nbControls++;
			String prevIcon = this.prev_down ? "prev_down" : "prev";

			Intent previousIntent = new Intent("audio-music-controls-previous");
			PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context, 1, previousIntent, 0);
			builder.addAction(this.getResourceId(prevIcon, android.R.drawable.ic_media_previous), "", previousPendingIntent);
		}
		if (infos.isPlaying){
			/* Pause  */
			nbControls++;
			String pauseIcon = this.pause_down ? "pause_down" : "pause";

			Intent pauseIntent = new Intent("audio-music-controls-pause");
			PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 1, pauseIntent, 0);
			builder.addAction(this.getResourceId(pauseIcon, android.R.drawable.ic_media_pause), "", pausePendingIntent);
		} else {
			/* Play  */
			nbControls++;
			String playIcon = this.play_down ? "play_down" : "play";

			Intent playIntent = new Intent("audio-music-controls-play");
			PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 1, playIntent, 0);
			builder.addAction(this.getResourceId(playIcon, android.R.drawable.ic_media_play), "", playPendingIntent);
		}

		if (infos.hasNext){
			/* Next */
			nbControls++;
			String nextIcon = this.next_down ? "next_down" : "next";

			Intent nextIntent = new Intent("audio-music-controls-next");
			PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, 0);
			builder.addAction(this.getResourceId(nextIcon, android.R.drawable.ic_media_next), "", nextPendingIntent);
		}
		if (infos.hasClose){
			/* Close */
			nbControls++;
			Intent destroyIntent = new Intent("audio-music-controls-destroy");
			PendingIntent destroyPendingIntent = PendingIntent.getBroadcast(context, 1, destroyIntent, 0);
			builder.addAction(this.getResourceId(infos.closeIcon, android.R.drawable.ic_menu_close_clear_cancel), "", destroyPendingIntent);
		}

		//If 5.0 >= use MediaStyle
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
			int[] args = new int[nbControls];
			for (int i = 0; i < nbControls; ++i) {
				args[i] = i;
			}
			builder.setStyle(new Notification.MediaStyle().setShowActionsInCompactView(args));
		}
		this.notificationBuilder = builder;
	}

	private int getResourceId(String name, int fallback){
		try{
			if(name.isEmpty()){
				return fallback;
			}

			int resId = this.cordovaActivity.getResources().getIdentifier(name, "drawable", this.cordovaActivity.getPackageName());
			return resId == 0 ? fallback : resId;
		}
		catch(Exception ex){
			return fallback;
		}
	}

	public void destroy(){
		this.notificationManager.cancel(this.notificationID);
		this.onNotificationDestroyed();
	}

	protected void onNotificationUpdated(Notification notification) {}
	protected void onNotificationDestroyed() {}
}
