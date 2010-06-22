/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/
package com.vodafone.android.navigation.persistence;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class PredictiveWritingDatabaseAdapter {
	public static final String DATABASE_NAME = "predictive_writing.db";
	public static final int DATABASE_VERSION = 1;
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_WHERE_TEXT = 1;
	
	private static final String PREDICTIVE_WRITING_TABLE = "predictive_writing";

	private static final String KEY_ID = "_id";
	private static final String KEY_WHERE_TEXT = "wheretext";
	
	private static final String PREDICTIVE_WRITING_TABLE_CREATE = "create table " + PREDICTIVE_WRITING_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_WHERE_TEXT + " text not null);";
	
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;

	public PredictiveWritingDatabaseAdapter(Context context) {
		this.dbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private void open() throws SQLException {
		this.db = dbHelper.getWritableDatabase();
	}

	private void close() {
		db.close();
	}

	public boolean addPredictString(String searchWhere) {
		this.open();
		
		String[] resultColumns = new String[] {KEY_ID, KEY_WHERE_TEXT};
		String orderBy = KEY_WHERE_TEXT;
		String selection = KEY_WHERE_TEXT +" = \""+searchWhere+"\"";
		
		Cursor cursor = db.query(PREDICTIVE_WRITING_TABLE, resultColumns, selection, null, null , null, orderBy);
		if(cursor.getCount() != 0){
			this.close();
			return false;
		}
		else{
			ContentValues contentValues = new ContentValues();
			contentValues.put(KEY_WHERE_TEXT, searchWhere);
			db.insert(PREDICTIVE_WRITING_TABLE, null, contentValues);
			this.close();
			return true;
		}
	}
	
	public void deleteAllPredictStrings() {
		this.open();
		db.delete(PREDICTIVE_WRITING_TABLE, null, null);
		this.close();
	}


	public ArrayList<String> readPredictStrings() {
		ArrayList<String> previusStrings = new ArrayList<String>();
		this.open();
		String[] resultColumns = new String[] {KEY_ID, KEY_WHERE_TEXT};
		String orderBy = KEY_WHERE_TEXT;
		Cursor cursor = db.query(PREDICTIVE_WRITING_TABLE, resultColumns, null, null, null , null, orderBy);
		try{
			if(cursor.moveToFirst()){
				do{
					String whereString = cursor.getString(INDEX_WHERE_TEXT);
					previusStrings.add(whereString);
				} while(cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		this.close();
		return previusStrings;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		// Called when no database exists in
		// disk and the helper class needs
		// to create a new one.
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(PREDICTIVE_WRITING_TABLE_CREATE);
		}

		// Called when there is a database version mismatch meaning that
		// the version of the database on disk needs to be upgraded to
		// the current version.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + PREDICTIVE_WRITING_TABLE);
			onCreate(db);
		}
	}

	public void deleteAllPreviousStrings() {
		this.open();
		db.delete(PREDICTIVE_WRITING_TABLE, null, null);
		this.close();
	}


}
