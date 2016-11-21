package com.udaan.zoomania.ui;

import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.udaan.zoomania.logic.Achievements;
import com.udaan.zoomania.logic.Board;
import com.udaan.zoomania.logic.CountDownTimerPausable;

public class MainActivity extends Activity {
	public static final boolean PAID = false;
	public static final int MAX_CHOICE = 4;
	private static final String MY_AD_UNIT_ID = "ca-app-pub-8996795250788622/4263178693";
    private static final String BLANK_TIMER = "      ";
    private static String sTimer = "1:00.0";
	private static int gameTime = 60000;
	
	private ImageView flag0, flag1, flag2, flag3;
	private TextView option0, option1, option2, option3;
	private TextView timer, scoreField, scoreText;
	private ImageView settings, help;// barcode;
	private LinearLayout adLL;
	private AdView adView;
	private AdRequest adRequest;
	private Board board;
	private String[] flag;
	private String[] option;
	private int score;
	private int continuousScore;
	private int continuousScoreAchieved;
	private SharedPreferences prefs;
	private CountDownTimerPausable countDown;
	private boolean settingsClicked;
	private boolean showResumeAlert = false;
    private boolean showTimer = true;
	private String longCategory = null;
	Configuration config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		scoreField = (TextView)findViewById(R.id.score);
		timer = (TextView)findViewById(R.id.timer);
		
		flag0 = (ImageView)findViewById(R.id.flag0);
		flag1 = (ImageView)findViewById(R.id.flag1);
		flag2 = (ImageView)findViewById(R.id.flag2);
		flag3 = (ImageView)findViewById(R.id.flag3);
		
		option0 = (TextView)findViewById(R.id.option0);
		option1 = (TextView)findViewById(R.id.option1);
		option2 = (TextView)findViewById(R.id.option2);
		option3 = (TextView)findViewById(R.id.option3);
		
		scoreText = (TextView)findViewById(R.id.score_text);
		settings = (ImageView)findViewById(R.id.settings);
		help = (ImageView)findViewById(R.id.help);
		//barcode = (ImageView)findViewById(R.id.barcode);
		
		if (!PAID) {
			//MobileAds.initialize(this, MY_AD_UNIT_ID);
			adView = new AdView(this);
			adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(MY_AD_UNIT_ID);
			adLL = (LinearLayout) findViewById(R.id.ad_ll);
		    adLL.addView(adView);
		    adRequest = new AdRequest.Builder()
                    .addTestDevice("A4163BE3E608B682241FF4D4EA7BD69D")
                    .build();
		    //adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
		    adView.loadAd(adRequest);
		}
	    
	    settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				settingsClicked = true;
				showResumeAlert = false;
				Intent intent = new Intent(v.getContext(), SettingsActivity.class);
				startActivity(intent);
			}
		});
	    
		help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), FavoritesActivity.class);
				startActivity(intent);
			}
		});
	    
		alertNewGame();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	protected void onStart() {
		super.onStart();
		Log.d(getClass().toString(), "onStart");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	String category = prefs.getString("category", "birds");
        showTimer = prefs.getBoolean("enable_timer", true);
		switch (category) {
			case "mammals":
				sTimer = "1:00.0";
				gameTime = 60000;
				longCategory = getString(R.string.mammals);
				break;
			case "birds":
				sTimer = "1:00.0";
				gameTime = 60000;
				longCategory = getString(R.string.birds);
				break;
			case "insects":
				sTimer = "1:00.0";
				gameTime = 60000;
				longCategory = getString(R.string.insects);
				break;
		}
    	
    	scoreText.setText(R.string.score_text);
    			
    	Board.category = category;
    	
    	if (showResumeAlert && countDown.isPaused()) alertResume();
    	if (settingsClicked) {
    		alertNewGame();
    		settingsClicked = false;
    	}
	}
	
	protected void onResume() {
		super.onResume();
		Log.d(getClass().toString(), "onResume");
	}
	
	protected void onStop() {
		super.onStop();
		Log.d(getClass().toString(), "onStop");
	}
	
	protected void onDestroy() {
		super.onDestroy();
		if (countDown != null) {
			countDown.cancel();
			countDown = null;
		}
	}
	
	protected void onPause() {
		super.onPause();
		Log.d(getClass().toString(), "onPause");
		if (countDown != null && !countDown.isPaused()) countDown.pause();
	}
	
	public void newGame() {
		if (board != null) board.close();
		board = new Board(this);
		
		countDown = new CountDownTimerPausable(gameTime, 100) {

			@Override
			public void onTick(long millisUntilFinished) {
				timer.setText(formatTime(millisUntilFinished));
			}

			@Override
			public void onFinish() {
				timer.setText("0:00.0");
				alertGameOver();
			}
    		
    	};
    	
		if(showTimer) {
            countDown.start();
        }
        else {
            timer.setText(BLANK_TIMER);
        }
		loadBoard();
	}
	
	public void alertNewGame() {
		Log.d(getClass().toString(), "alertNewGame");
		timer.setText(sTimer);
		score = 0;
		continuousScore = 0;
		continuousScoreAchieved = 0;
		AlertDialog.Builder alertConfirm = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		alertConfirm.setTitle(R.string.new_game);
		alertConfirm.setView(inflater.inflate(R.layout.alert_newgame, null));
		alertConfirm.setCancelable(false);
		alertConfirm.setNeutralButton(getResources().getString(R.string.start), new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showResumeAlert = true;
				newGame();
			}
		});
		alertConfirm.show();
	}
	
	public void alertGameOver() {
		Log.d(getClass().toString(), "alertGameOver");
		showResumeAlert = false;
		board.close();
		
		Achievements achievement = new Achievements(this);
		achievement.setAchievement(longCategory, continuousScoreAchieved > continuousScore ? continuousScoreAchieved : continuousScore, this);
		achievement.close();
		
		AlertDialog.Builder alertConfirm = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		alertConfirm.setTitle(R.string.times_up);
		alertConfirm.setView(inflater.inflate(R.layout.alert_gameover, null));
		alertConfirm.setCancelable(false);
		alertConfirm.setNeutralButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertNewGame();
			}
		});
		alertConfirm.show();
	}

    public void alertGameComplete() {
        Log.d(getClass().toString(), "alertGameComplete");
        showResumeAlert = false;
        board.close();

        /*Achievements achievement = new Achievements(this);
        achievement.setAchievement(longCategory, continuousScoreAchieved > continuousScore ? continuousScoreAchieved : continuousScore, this);
        achievement.close();*/

        AlertDialog.Builder alertConfirm = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        alertConfirm.setTitle(R.string.game_complete);
        alertConfirm.setView(inflater.inflate(R.layout.alert_gamecomplete, null));
        alertConfirm.setCancelable(false);
        alertConfirm.setNeutralButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertNewGame();
            }
        });
        alertConfirm.show();
    }
	
	public void alertResume() {
		showResumeAlert = false;
		AlertDialog.Builder alertConfirm = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		alertConfirm.setTitle(R.string.resume);
		alertConfirm.setView(inflater.inflate(R.layout.alert_resume, null));
		alertConfirm.setCancelable(false);
		alertConfirm.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showResumeAlert = true;
				if(showTimer) {
                    countDown.start();
                }
                else {
                    timer.setText(BLANK_TIMER);
                }
			}
		});
		alertConfirm.show();
		Log.d(getClass().toString(), "alertResume");
	}
	
	private void loadBoard() {
		flag = board.getFlags();
		option = board.getOptions();
		
		scoreField.setText("" + score);
		
		flag0.setImageResource(getResources().getIdentifier(flag[0], "drawable", this.getPackageName()));
		flag1.setImageResource(getResources().getIdentifier(flag[1], "drawable", this.getPackageName()));
		flag2.setImageResource(getResources().getIdentifier(flag[2], "drawable", this.getPackageName()));
		flag3.setImageResource(getResources().getIdentifier(flag[3], "drawable", this.getPackageName()));
		
		//Randomize the options
		Integer[] randInt = new Integer[MAX_CHOICE];
		for (int i = 0; i < MAX_CHOICE; i++) randInt[i] = i;
		
		flag0.setId(randInt[0]);
		flag0.setTag(board.getName(flag[0]));
		flag0.setOnDragListener(new MyDragListener());
		flag1.setId(randInt[1]);
		flag1.setTag(board.getName(flag[1]));
		flag1.setOnDragListener(new MyDragListener());
		flag2.setId(randInt[2]);
		flag2.setTag(board.getName(flag[2]));
		flag2.setOnDragListener(new MyDragListener());
		flag3.setId(randInt[3]);
		flag3.setTag(board.getName(flag[3]));
		flag3.setOnDragListener(new MyDragListener());
		
		Collections.shuffle(Arrays.asList(randInt));

		option0.setText(option[randInt[0]]);
		option0.setTag(option[randInt[0]]);
		option0.setOnTouchListener(new MyTouchListener());
		option1.setText(option[randInt[1]]);
		option1.setTag(option[randInt[1]]);
		option1.setOnTouchListener(new MyTouchListener()); 
		option2.setText(option[randInt[2]]);
		option2.setTag(option[randInt[2]]);
		option2.setOnTouchListener(new MyTouchListener()); 
		option3.setText(option[randInt[3]]);
		option3.setTag(option[randInt[3]]);
		option3.setOnTouchListener(new MyTouchListener()); 
		
	}
	
	private void isMatched(int index) {
		score++;
		continuousScore++;

		if(board.removeFlag(index)) {
            loadBoard();
        }
        else {
            alertGameComplete();
        }
	}

    private CharSequence formatTime(long millisUntilFinished) {
		int timer100mili = (int) (millisUntilFinished / 100);
		int timerSec = timer100mili / 10;
		int timerMin = timerSec / 60;
		
		return timerMin + ":" + String.format("%02d", timerSec % 60) + "." + (timer100mili % 10);
	}
	
	/*
	private void showAlert(String msg) {
		AlertDialog.Builder alertConfirm = new AlertDialog.Builder(this);
		//alertConfirm.setTitle(R.string.times_up);
		alertConfirm.setMessage(msg);
		alertConfirm.setCancelable(false);
		alertConfirm.setNeutralButton("OK", new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		alertConfirm.show();
	}
	*/
	
	private class MyTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				ClipData data = ClipData.newPlainText((String)v.getTag(), (String)v.getTag());
			    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
			    v.startDrag(data, shadowBuilder, v, 0);
			    v.setVisibility(View.DRAWING_CACHE_QUALITY_AUTO);
			    return true;
			} 
			else {
				return false;
			}
		}
	}
	
	private class MyDragListener implements OnDragListener {
		@Override
		public boolean onDrag(View v, DragEvent event) {
		    switch (event.getAction()) {
		    case DragEvent.ACTION_DROP:
		    	//Dropped, reassign View to ViewGroup
		    	ClipData.Item item = event.getClipData().getItemAt(0);
		    	Log.d(getClass().toString(), item.getText() + " " + (String) v.getTag() + " " + v.getId());
		    	if (item.getText().equals(v.getTag())) {
					isMatched(v.getId());
		    	}
		    	else {
		    		MediaPlayer player = MediaPlayer.create(v.getContext(), R.raw.wrong);
					player.start();
					player = null;
					continuousScoreAchieved = continuousScoreAchieved > continuousScore ? continuousScoreAchieved : continuousScore;
					continuousScore = 0;
		    	}
		    	break;
		    case DragEvent.ACTION_DRAG_STARTED:
		    case DragEvent.ACTION_DRAG_ENTERED:
		    case DragEvent.ACTION_DRAG_EXITED:        
		    case DragEvent.ACTION_DRAG_ENDED:
		    default:
		      break;
		    }
		    return true;
		}
	}
}
