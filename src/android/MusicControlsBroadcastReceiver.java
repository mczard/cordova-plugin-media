package org.apache.cordova.media;

import org.apache.cordova.LOG;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicControlsBroadcastReceiver extends BroadcastReceiver {
	private CallbackContext cb;
	private MusicControls musicControls;

    private static int NEXT_MEDIA = 101;
    private static int PREV_MEDIA = 102;

	public MusicControlsBroadcastReceiver(MusicControls musicControls){
		this.musicControls = musicControls;
	}

	public void setCallback(CallbackContext cb){
		this.cb = cb;
	}

	public void stopListening(){
		if (this.cb != null){
			this.cb.success("{\"message\": \"music-controls-stop-listening\" }");
			this.cb = null;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// if (this.cb != null){
			String message = intent.getAction();

			if(message.equals(Intent.ACTION_HEADSET_PLUG)){
				// Headphone plug/unplug
				int state = intent.getIntExtra("state", -1);
				switch (state) {
					case 0:
						// this.cb.success("{\"message\": \"music-controls-headset-unplugged\"}");
						this.cb = null;
						this.musicControls.unregisterMediaButtonEvent();
						break;
					case 1:
						// this.cb.success("{\"message\": \"music-controls-headset-plugged\"}");
						this.cb = null;
						this.musicControls.registerMediaButtonEvent();
						break;
					default:
						break;
				}
			} else if (message.equals("music-controls-media-button")){
				// Media button
				KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (event.getAction() == KeyEvent.ACTION_DOWN) {

					int keyCode = event.getKeyCode();
					switch (keyCode) {
						case KeyEvent.KEYCODE_MEDIA_NEXT:
							// this.cb.success("{\"message\": \"music-controls-media-button-next\"}");
							this.sendStatusChange(NEXT_MEDIA, null, null);
							break;
						case KeyEvent.KEYCODE_MEDIA_PAUSE:
							// this.cb.success("{\"message\": \"music-controls-media-button-pause\"}");
							this.musicControls.handler.pausePlayingAudio(null);
							break;
						case KeyEvent.KEYCODE_MEDIA_PLAY:
							// this.cb.success("{\"message\": \"music-controls-media-button-play\"}");
							this.musicControls.handler.startPlayingAudio(null, null);
							break;
						case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
							// this.cb.success("{\"message\": \"music-controls-media-button-play-pause\"}");
							break;
						case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
							// this.cb.success("{\"message\": \"music-controls-media-button-previous\"}");
							this.sendStatusChange(PREV_MEDIA, null, null);
							break;
						case KeyEvent.KEYCODE_MEDIA_STOP:
							// this.cb.success("{\"message\": \"music-controls-media-button-stop\"}");
							break;
						case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
							// this.cb.success("{\"message\": \"music-controls-media-button-fast-forward\"}");
							break;
						case KeyEvent.KEYCODE_MEDIA_REWIND:
							// this.cb.success("{\"message\": \"music-controls-media-button-rewind\"}");
							break;
						case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
							// this.cb.success("{\"message\": \"music-controls-media-button-skip-backward\"}");
							break;
						case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
							// this.cb.success("{\"message\": \"music-controls-media-button-skip-forward\"}");
							break;
						case KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD:
							// this.cb.success("{\"message\": \"music-controls-media-button-step-backward\"}");
							break;
						case KeyEvent.KEYCODE_MEDIA_STEP_FORWARD:
							// this.cb.success("{\"message\": \"music-controls-media-button-step-forward\"}");
							break;
						case KeyEvent.KEYCODE_META_LEFT:
							// this.cb.success("{\"message\": \"music-controls-media-button-meta-left\"}");
							break;
						case KeyEvent.KEYCODE_META_RIGHT:
							// this.cb.success("{\"message\": \"music-controls-media-button-meta-right\"}");
							break;
						case KeyEvent.KEYCODE_MUSIC:
							// this.cb.success("{\"message\": \"music-controls-media-button-music\"}");
							break;
						case KeyEvent.KEYCODE_VOLUME_UP:
							// this.cb.success("{\"message\": \"music-controls-media-button-volume-up\"}");
							break;
						case KeyEvent.KEYCODE_VOLUME_DOWN:
							// this.cb.success("{\"message\": \"music-controls-media-button-volume-down\"}");
							break;
						case KeyEvent.KEYCODE_VOLUME_MUTE:
							// this.cb.success("{\"message\": \"music-controls-media-button-volume-mute\"}");
							break;
						case KeyEvent.KEYCODE_HEADSETHOOK:
							// this.cb.success("{\"message\": \"music-controls-media-button-headset-hook\"}");
							break;
						default:
							// this.cb.success("{\"message\": \"" + message + "\"}");
							break;
					}
					this.cb = null;
				}
			} else if (message.equals("music-controls-destroy")){
				// Close Button
				// this.cb.success("{\"message\": \"music-controls-destroy\"}");
				this.cb = null;
				this.musicControls.destroyPlayerNotification();
			} else {
				LOG.d("MusicControls onReceive: ", message);
				switch (message) {
					case "music-controls-next":
						// this.cb.success("{\"message\": \"music-controls-media-button-next\"}");
						this.sendStatusChange(NEXT_MEDIA, null, null);
						break;
					case "music-controls-pause":
						// this.cb.success("{\"message\": \"music-controls-media-button-pause\"}");
						this.musicControls.handler.pausePlayingAudio(null);
						this.musicControls.updateIsPlaying(false);
						break;
					case "music-controls-play":
						// this.cb.success("{\"message\": \"music-controls-media-button-play\"}");
						this.musicControls.handler.startPlayingAudio(null, null);
						this.musicControls.updateIsPlaying(true);
						break;
					case "music-controls-previous":
						// this.cb.success("{\"message\": \"music-controls-media-button-previous\"}");
						this.sendStatusChange(PREV_MEDIA, null, null);
						break;
				}
				// this.cb.success("{\"message\": \"" + message + "\"}");
				this.cb = null;
			}


		// }
	}

	private void sendStatusChange(int messageType, Integer additionalCode, Float value) {

        if (additionalCode != null && value != null) {
            throw new IllegalArgumentException("Only one of additionalCode or value can be specified, not both");
        }

        JSONObject statusDetails = new JSONObject();
        try {
            statusDetails.put("id", this.musicControls.handler.playerId);
            statusDetails.put("msgType", messageType);
            if (additionalCode != null) {
                JSONObject code = new JSONObject();
                code.put("code", additionalCode.intValue());
                statusDetails.put("value", code);
            }
            else if (value != null) {
                statusDetails.put("value", value.floatValue());
            }
        } catch (JSONException e) {
            LOG.e("MusicControls ERROR: ", "Failed to create status details", e);
        }

        this.musicControls.handler.sendEventMessage("status", statusDetails);
    }
}
