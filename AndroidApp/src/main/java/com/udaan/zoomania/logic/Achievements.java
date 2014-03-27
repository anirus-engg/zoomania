package com.udaan.zoomania.logic;

import android.content.Context;
import android.util.Log;

import com.udaan.zoomania.ui.R;

public class Achievements {
	private DBAdapter dbAdapter;
	private String data[][];
	
	public Achievements(Context context) {
		dbAdapter = new DBAdapter(context);
		String sql = "SELECT * FROM " + DBAdapter.ACHIEVEMENTS_TABLE + " ORDER BY " + DBAdapter.ACHIEVEMENTS_ID;
		data = dbAdapter.getRows(dbAdapter.getReadableDatabase(), sql);
	}

	public int getCount() {
		return data.length;
	}
	
	public String getAchievement (int row, int column) {
		return data[row][column];
	}

	public boolean isAchieved(int row) {
		return data[row][4].equals("0") ? false : true;
	}

	public String getMedal(int row) {
		return data[row][3];
	}

	public CharSequence getAchievementDesc(int row) {
		return data[row][2];
	}
	
	public CharSequence getAchievementName(int row) {
		return data[row][1];
	}

	public void close() {
		dbAdapter.close();
		data = null;
	}

	public void setAchievement(String category, int continuousScoreAchieved, Context context) {
		int id = 0;
		Log.d(getClass().toString(), "continuousScoreAchieved:" + continuousScoreAchieved);
		
		if (category.equals(context.getString(R.string.mammals))) {
			if (continuousScoreAchieved >= 35) id = 4;
			else if (continuousScoreAchieved >= 25) id = 3;
			else if (continuousScoreAchieved >= 15) id = 2;
			else if (continuousScoreAchieved >= 5) id = 1;
		}
		else if (category.equals(context.getString(R.string.birds))) {
			if (continuousScoreAchieved >= 35) id = 8;
			else if (continuousScoreAchieved >= 25) id = 7;
			else if (continuousScoreAchieved >= 15) id = 6;
			else if (continuousScoreAchieved >= 5) id = 5;
		}
		
		String sql = "UPDATE " + DBAdapter.ACHIEVEMENTS_TABLE + " SET " + DBAdapter.ACHIEVEMENTS_ACHIEVED + "=1 WHERE " + 
				DBAdapter.ACHIEVEMENTS_ID + "=" + id;
		if (id != 0) dbAdapter.execSQL(dbAdapter.getWritableDatabase(), sql);
	}
}
