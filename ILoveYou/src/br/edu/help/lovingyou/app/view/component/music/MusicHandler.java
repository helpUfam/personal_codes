/**
 * 
 */
package br.edu.help.lovingyou.app.view.component.music;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * @author hildoneduardo
 * 
 */
public class MusicHandler {
	private MediaPlayer mediaPlayer;
	private final Context context;
	private int iVolume;

	private final static int INT_VOLUME_MAX = 100;
	private final static int INT_VOLUME_MIN = 0;
	private final static float FLOAT_VOLUME_MAX = 1;
	private final static float FLOAT_VOLUME_MIN = 0;
	
	private boolean inEffect = false;

	public MusicHandler(final Context context) {
		this.context = context;
	}

	public void load(final String path, final boolean looping) {
		mediaPlayer = MediaPlayer.create(context, Uri.fromFile(new File(path)));
		mediaPlayer.setLooping(looping);
	}
	
	public void load(final int rawId, final boolean looping) {
		mediaPlayer = MediaPlayer.create(context, rawId);
		mediaPlayer.setLooping(looping);
	}

	public boolean play(final int fadeDuration) {
		// Set current volume, depending on fade or not
		if (fadeDuration > 0)
			iVolume = INT_VOLUME_MIN;
		else
			iVolume = INT_VOLUME_MAX;

		if (!inEffect) {
			updateVolume(0);
	
			// Play music
			if (!mediaPlayer.isPlaying())
				mediaPlayer.start();
	
			// Start increasing volume in increments
			if (fadeDuration > 0) {
				inEffect = true;
				final Timer timer = new Timer(true);
				final TimerTask timerTask = new TimerTask() {
					@Override
					public void run() {
						updateVolume(1);
						if (iVolume == INT_VOLUME_MAX) {
							inEffect = false;
							timer.cancel();
							timer.purge();
						}
					}
				};
	
				// calculate delay, cannot be zero, set to 1 if zero
				int delay = fadeDuration / INT_VOLUME_MAX;
				if (delay == 0)
					delay = 1;
	
				timer.schedule(timerTask, delay, delay);
			}
			return true;
		} 
		return false;
	}

	public boolean pause(final int fadeDuration) {
		// Set current volume, depending on fade or not
		if (fadeDuration > 0)
			iVolume = INT_VOLUME_MAX;
		else
			iVolume = INT_VOLUME_MIN;

		
		if (!inEffect) {
			updateVolume(0);
			// Start increasing volume in increments
			if (fadeDuration > 0) {
				inEffect = true;
				final Timer timer = new Timer(true);
				final TimerTask timerTask = new TimerTask() {
					@Override
					public void run() {
						updateVolume(-1);
						if (iVolume == INT_VOLUME_MIN) {
							inEffect = false;
							// Pause music
							if (mediaPlayer.isPlaying())
								mediaPlayer.pause();
							timer.cancel();
							timer.purge();
						}
					}
				};
	
				// calculate delay, cannot be zero, set to 1 if zero
				int delay = fadeDuration / INT_VOLUME_MAX;
				if (delay == 0)
					delay = 1;
	
				timer.schedule(timerTask, delay, delay);
			}
			return true;
		}
		return false;
	}

	private void updateVolume(final int change) {
		// increment or decrement depending on type of fade
		iVolume = iVolume + change;

		// ensure iVolume within boundaries
		if (iVolume < INT_VOLUME_MIN)
			iVolume = INT_VOLUME_MIN;
		else if (iVolume > INT_VOLUME_MAX)
			iVolume = INT_VOLUME_MAX;

		// convert to float value
		float fVolume = 1 - ((float) Math.log(INT_VOLUME_MAX - iVolume) / (float) Math
				.log(INT_VOLUME_MAX));

		// ensure fVolume within boundaries
		if (fVolume < FLOAT_VOLUME_MIN)
			fVolume = FLOAT_VOLUME_MIN;
		else if (fVolume > FLOAT_VOLUME_MAX)
			fVolume = FLOAT_VOLUME_MAX;

		mediaPlayer.setVolume(fVolume, fVolume);
	}
	
	/**
	 * 
	 */
	public void release() {
		mediaPlayer.release();
	}
	
	private static MusicHandler instance;
	
	/**
	 * Instantiate single musicHandler instance
	 * 
	 * @param applicationContext
	 * @return
	 */
	public static MusicHandler newInstance(Context applicationContext) {
		if (instance == null) {
			instance = new MusicHandler(applicationContext);
		}
		return instance;
	}
}
