package jp.gr.java_conf.ya.simpletimer; // Copyright (c) 2013-2015 YA <ya.androidapp@gmail.co

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

public class SimpleTimerActivity extends Activity implements TextToSpeech.OnInitListener {
	public class MyCountDownTimer extends CountDownTimer {

		public MyCountDownTimer(long millisInFuture) {
			super(millisInFuture, 1000);

			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			alarm_uri = sharedPreferences.getString("alarm_uri", "");
			if (( alarm_uri != null ) && ( alarm_uri.equals("") == false )) {
				mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(alarm_uri));
				mediaPlayer.setLooping(true);
				mediaPlayer.seekTo(0);
			}
		}

		@Override
		public void onFinish() {
			numberPicker_h.setValue(0);
			numberPicker_m.setValue(0);
			numberPicker_s.setValue(0);

			if (( alarm_uri != null ) && ( alarm_uri.equals("") == false )) {
				mediaPlayer.start();
			}

			Toast.makeText(getApplicationContext(), getString(R.string.finish), Toast.LENGTH_SHORT).show();

			if (myCountDownTimer != null) {
				myCountDownTimer.cancel();
			}
			numberPickersButtonsSetEnabled(true);
		}

		@Override
		public void onTick(final long millisUntilFinished) {
			numberPicker_h.setValue((int) ( millisUntilFinished / 1000 / 3600 ));
			numberPicker_m.setValue((int) ( millisUntilFinished / 1000 / 60 ));
			numberPicker_s.setValue((int) ( millisUntilFinished / 1000 % 60 ));

			final int untilFinished = (int) ( millisUntilFinished / 1000 );
			if (speech_second.indexOf("," + Integer.toString(untilFinished) + ",") > -1)
				speech(( untilFinished < 11 ) ? ( Integer.toString(untilFinished) ) : ( getString(R.string.left) + time2str(untilFinished) ));
		}
	}

	private Button button_start, button_pose, button_reset, button_tts_elapsed, button_tts_left;
	private int[] pre = { 0, 0, 0 };
	private MediaPlayer mediaPlayer;
	private MyCountDownTimer myCountDownTimer;
	private NumberPicker numberPicker_h, numberPicker_m, numberPicker_s;
	private SharedPreferences sharedPreferences;
	private String alarm_uri, default_time, speech_second;
	private TextToSpeech tts;

	private void numberPickersSetEnabled(boolean enabled) {
		numberPicker_h.setEnabled(enabled);
		numberPicker_m.setEnabled(enabled);
		numberPicker_s.setEnabled(enabled);
	}

	private void numberPickersButtonsSetEnabled(boolean enabled) {
		numberPickersSetEnabled(enabled);
		button_tts_elapsed.setEnabled(!enabled);
		button_tts_left.setEnabled(!enabled);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_start = (Button) findViewById(R.id.button_start);
		button_pose = (Button) findViewById(R.id.button_pose);
		button_reset = (Button) findViewById(R.id.button_reset);
		button_tts_elapsed = (Button) findViewById(R.id.button_tts_elapsed);
		button_tts_left = (Button) findViewById(R.id.button_tts_left);
		numberPicker_h = (NumberPicker) findViewById(R.id.numberPicker_h);
		numberPicker_m = (NumberPicker) findViewById(R.id.numberPicker_m);
		numberPicker_s = (NumberPicker) findViewById(R.id.numberPicker_s);
		numberPicker_h.setMaxValue(99);
		numberPicker_h.setMinValue(0);
		numberPicker_m.setMaxValue(59);
		numberPicker_m.setMinValue(0);
		numberPicker_s.setMaxValue(59);
		numberPicker_s.setMinValue(0);

		tts = new TextToSpeech(this, this);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		try {
			default_time = sharedPreferences.getString("default_time", "0:01:00");
			numberPicker_h.setValue(Integer.parseInt(( default_time.split(":") )[0]));
			numberPicker_m.setValue(Integer.parseInt(( default_time.split(":") )[1]));
			numberPicker_s.setValue(Integer.parseInt(( default_time.split(":") )[2]));
		} catch (Exception e) {
			numberPicker_h.setValue(0);
			numberPicker_m.setValue(5);
			numberPicker_s.setValue(0);
		}
		try {
			speech_second = "," + sharedPreferences.getString("speech_second", "1,2,3") + ",";
		} catch (Exception e) {
			speech_second = ",1,2,3,";
		}

		button_start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final long millisInFuture = ( numberPicker_h.getValue() * 3600 + numberPicker_m.getValue() * 60 + numberPicker_s.getValue() ) * 1000;
				myCountDownTimer = new MyCountDownTimer(millisInFuture);
				myCountDownTimer.start();

				numberPickersButtonsSetEnabled(false);

				pre[0] = numberPicker_h.getValue();
				pre[1] = numberPicker_m.getValue();
				pre[2] = numberPicker_s.getValue();
			}
		});

		button_pose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (myCountDownTimer != null) {
					myCountDownTimer.cancel();
				}
				numberPickersButtonsSetEnabled(true);
			}
		});

		button_reset.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mediaPlayer != null) {
					try {
						if (mediaPlayer.isPlaying()) {
							mediaPlayer.stop();
						}
					} catch (IllegalStateException e) {
					} catch (Exception e) {
					}
				}

				if (myCountDownTimer != null) {
					myCountDownTimer.cancel();
				}
				numberPickersButtonsSetEnabled(true);

				numberPicker_h.setValue(pre[0]);
				numberPicker_m.setValue(pre[1]);
				numberPicker_s.setValue(pre[2]);
			}
		});

		button_tts_elapsed.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final int start = pre[0] * 3600 + pre[1] * 60 + pre[2];
				final int now = numberPicker_h.getValue() * 3600 + numberPicker_m.getValue() * 60 + numberPicker_s.getValue();
				final int time = start - now;
				if (time < 0)
					return;
				speech(time2str(time) + "経過");
			}
		});

		button_tts_left.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final int now = numberPicker_h.getValue() * 3600 + numberPicker_m.getValue() * 60 + numberPicker_s.getValue();
				if (now < 0)
					return;
				speech(getString(R.string.left) + time2str(now));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.settings);
		menu.add(0, 1, 0, R.string.copyright);
		return true;
	}

	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			final int result = tts.setLanguage(Locale.JAPAN);
			if (result >= TextToSpeech.LANG_AVAILABLE) {
				Toast.makeText(getApplicationContext(), "TTS Success", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "TTS Failure", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), "TTS Failure", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent();
			intent.setClassName("jp.gr.java_conf.ya.simpletimer", "jp.gr.java_conf.ya.simpletimer.PrefActivity");
			startActivity(intent);
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		try {
			speech_second = "," + sharedPreferences.getString("speech_second", "1,2,3") + ",";
		} catch (Exception e) {
			speech_second = ",1,2,3,";
		}
	}

	private void speech(final String str) {
		tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
	}

	private String time2str(final int second) {
		final int s = second % 60;
		int m = second / 60;
		final int h = m / 60;
		m = m % 60;

		String result = "";
		if (h > 0)
			result += Integer.toString(h) + "時間";
		if (m > 0)
			result += Integer.toString(m) + "分";
		if (s > 0)
			result += Integer.toString(s) + "秒";

		return result;
	}

}
