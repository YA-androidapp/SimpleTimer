package jp.gr.java_conf.ya.simpletimer; // Copyright (c) 2013-2015 YA <ya.androidapp@gmail.com> All rights reserved.

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
	private boolean[] rem = { true, true, true, true, true, true, true };
	private Button button_start, button_pose, button_reset, button_tts;
	private int[] pre = { 0, 0, 0 };
	private MediaPlayer mediaPlayer = null;
	private MyCountDownTimer myCountDownTimer;
	private NumberPicker numberPicker_h, numberPicker_m, numberPicker_s;
	private SharedPreferences sharedPreferences;
	private String alarm_uri = null;
	private String default_time = null;
	private TextToSpeech tts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_start = (Button) findViewById(R.id.button_start);
		button_pose = (Button) findViewById(R.id.button_pose);
		button_reset = (Button) findViewById(R.id.button_reset);
		button_tts = (Button) findViewById(R.id.button_tts);
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
			default_time = sharedPreferences.getString("default_time", "0:03:00");
			numberPicker_h.setValue(Integer.parseInt(( default_time.split(":") )[0]));
			numberPicker_m.setValue(Integer.parseInt(( default_time.split(":") )[1]));
			numberPicker_s.setValue(Integer.parseInt(( default_time.split(":") )[2]));
		} catch (Exception e) {
			numberPicker_h.setValue(0);
			numberPicker_m.setValue(5);
			numberPicker_s.setValue(0);
		}

		button_start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				long millisInFuture = ( numberPicker_h.getValue() * 3600 + numberPicker_m.getValue() * 60 + numberPicker_s.getValue() ) * 1000;
				myCountDownTimer = new MyCountDownTimer(millisInFuture);
				myCountDownTimer.start();

				numberPickersSetEnabled(false);
				button_tts.setEnabled(true);

				for (int i = 0; i < rem.length; i++) {
					rem[i] = true;
				}

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
				numberPickersSetEnabled(true);
				button_tts.setEnabled(false);
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
				numberPickersSetEnabled(true);
				button_tts.setEnabled(false);

				numberPicker_h.setValue(pre[0]);
				numberPicker_m.setValue(pre[1]);
				numberPicker_s.setValue(pre[2]);
			}
		});

		button_tts.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int start = pre[0] * 3600 + pre[1] * 60 + pre[2];

				int now = numberPicker_h.getValue() * 3600 + numberPicker_m.getValue() * 60 + numberPicker_s.getValue();

				int time = start - now;
				if (time < 0)
					return;

				int m = time / 60;
				int s = time % 60;
				int h = m / 60;
				m = m % 60;

				String result = "";
				if (h > 0)
					result += Integer.toString(h) + "時間";
				if (m > 0)
					result += Integer.toString(m) + "分";
				if (s > 0)
					result += Integer.toString(s) + "秒";

				speech(result + "、経過しました");
			}
		});
	}

	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.settings);
		menu.add(0, 1, 0, R.string.copyright);
		return true;
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

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.JAPAN);
			if (result >= TextToSpeech.LANG_AVAILABLE) {
				Toast.makeText(getApplicationContext(), "TTS Success", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "TTS Failure", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), "TTS Failure", Toast.LENGTH_SHORT).show();
		}
	}

	private void speech(String str) {
		tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
	}

	private void numberPickersSetEnabled(boolean enabled) {
		numberPicker_h.setEnabled(enabled);
		numberPicker_m.setEnabled(enabled);
		numberPicker_s.setEnabled(enabled);
	}

	public class MyCountDownTimer extends CountDownTimer {

		public MyCountDownTimer(long millisInFuture) {
			super(millisInFuture, 500);

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
			if (( alarm_uri != null ) && ( alarm_uri.equals("") == false )) {
				mediaPlayer.start();
			}

			Toast.makeText(getApplicationContext(), "終了", Toast.LENGTH_SHORT).show();

			if (myCountDownTimer != null) {
				myCountDownTimer.cancel();
			}
			numberPickersSetEnabled(true);
		}

		@Override
		public void onTick(final long millisUntilFinished) {
			numberPicker_h.setValue((int) ( millisUntilFinished / 1000 / 3600 ));
			numberPicker_m.setValue((int) ( millisUntilFinished / 1000 / 60 ));
			numberPicker_s.setValue((int) ( millisUntilFinished / 1000 % 60 ));

			int untilFinished = (int) ( millisUntilFinished / 1000 );
			switch (untilFinished) {
			case 1:
				if (rem[0]) {
					speech("1");
					rem[0] = false;
				}
				break;
			case 2:
				if (rem[1]) {
					speech("2");
					rem[1] = false;
				}
				break;
			case 3:
				if (rem[2]) {
					speech("3");
					rem[2] = false;
				}
				break;
			case 10:
				if (rem[3]) {
					speech("残り10秒");
					Toast.makeText(getApplicationContext(), "残り10秒", Toast.LENGTH_SHORT).show();
					rem[3] = false;
				}
				break;
			case 60:
				if (rem[4]) {
					speech("残り1分");
					Toast.makeText(getApplicationContext(), "残り1分", Toast.LENGTH_SHORT).show();
					rem[4] = false;
				}
				break;
			case 600:
				if (rem[5]) {
					speech("残り10分");
					Toast.makeText(getApplicationContext(), "残り10分", Toast.LENGTH_SHORT).show();
					rem[5] = false;
				}
				break;
			case 3600:
				if (rem[6]) {
					speech("残り1時間");
					Toast.makeText(getApplicationContext(), "残り1時間", Toast.LENGTH_SHORT).show();
					rem[6] = false;
				}
				break;
			}
		}
	}

}
