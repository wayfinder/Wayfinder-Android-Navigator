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

import com.vodafone.android.navigation.components.RecentDestination;
import com.vodafone.android.navigation.components.SearchHistoryItem;
import com.wayfinder.core.shared.Position;

public class DatabaseAdapter {
	public static final String DATABASE_NAME = "places.db";
	public static final int DATABASE_VERSION = 4;
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_NAME = 1;
	public static final int INDEX_DESCRIPTION = 2;
	public static final int INDEX_LATITUDE = 3;
	public static final int INDEX_LONGITUDE= 4;
	public static final int INDEX_SRVSTRING = 5;
	public static final int INDEX_TIMESTAMP = 6;
	public static final int INDEX_IMAGE_NAME = 7;
	
	public static final int INDEX_WHAT_TEXT = 1;
	public static final int INDEX_WHERE_TEXT = 2;
	public static final int INDEX_TOPREGIONID = 3;
	public static final int INDEX_CATEGORYID = 4;
	public static final int INDEX_SEARCH_TIME = 5;
	
	private static final String SAVED_PLACES_TABLE = "saved_places";
	private static final String PREVIOUS_DESTINATIONS_TABLE = "previous_destionations";
	private static final String PREVIOUS_SEARCHES_TABLE = "previous_searches";

	private static final String KEY_ID = "_id";
	private static final String KEY_NAME = "name";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_LONGITUDE = "longitude";
	private static final String KEY_SRVSTRING = "srvstring";
	private static final String KEY_TIMESTAMP = "timestamp";
	private static final String KEY_IMAGE_NAME = "imagename";
	private static final String KEY_WHAT_TEXT = "whattext";
	private static final String KEY_WHERE_TEXT = "wheretext";
	private static final String KEY_TOPREGIONID = "topregionid";
	private static final String KEY_CATEGORYID = "categoryid";
	
	
//	private static final String SAVED_PLACES_TABLE_CREATE = "create table " + SAVED_PLACES_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " text not null, " + KEY_DESCRIPTION + " text not null, " + KEY_LATITUDE + " int not null, " + KEY_LONGITUDE + " int not null, " + KEY_SRVSTRING + " text not null, " + KEY_TIMESTAMP + " long not null, " + KEY_IMAGE_NAME + " text not null);";
	private static final String PREVIOUS_DESTINATIONS_TABLE_CREATE = "create table " + PREVIOUS_DESTINATIONS_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " text not null, " + KEY_DESCRIPTION + " text not null, " + KEY_LATITUDE + " int not null, " + KEY_LONGITUDE + " int not null, " + KEY_SRVSTRING + " text not null, " + KEY_TIMESTAMP + " long not null, " + KEY_IMAGE_NAME + " text not null);";
	private static final String PREVIOUS_SEARCHES_TABLE_CREATE = "create table " + PREVIOUS_SEARCHES_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_WHAT_TEXT + " text not null, " + KEY_WHERE_TEXT + " text not null, " + KEY_CATEGORYID + " int not null, " + KEY_TOPREGIONID + " int not null, " + KEY_TIMESTAMP + " long not null);";
	
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;

	public DatabaseAdapter(Context context) {
		this.dbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private void open() throws SQLException {
		this.db = dbHelper.getWritableDatabase();
	}

	private void close() {
		db.close();
	}

	private long addPlace(RecentDestination destination, String databaseTable) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_NAME, destination.getName());
		contentValues.put(KEY_DESCRIPTION, destination.getDescription());
		contentValues.put(KEY_LATITUDE, destination.getPosition().getMc2Latitude());
		contentValues.put(KEY_LONGITUDE, destination.getPosition().getMc2Longitude());
		contentValues.put(KEY_SRVSTRING, "");
		contentValues.put(KEY_TIMESTAMP, destination.getTimestamp());
		contentValues.put(KEY_IMAGE_NAME, destination.getIconName());
		
		return db.insert(databaseTable, null, contentValues);
		 
	}
	
//	public void addSavedPlace(AndroidFavorite fav){
//		this.open();
//		long id = addPlace(fav, SAVED_PLACES_TABLE);
//		fav.setId(id);
//		this.close();
//	}
	
	public void addPreviousDestination(RecentDestination fav){
		this.open();
		long id = addPlace(fav, PREVIOUS_DESTINATIONS_TABLE);
		fav.setId(id);
		this.close();
	}
	
	public void addPreviousSearch(SearchHistoryItem item){
		this.open();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_WHAT_TEXT, item.getWhatText());
		contentValues.put(KEY_WHERE_TEXT, item.getWhereText());
		contentValues.put(KEY_TOPREGIONID, item.getTopRegionId());
		contentValues.put(KEY_CATEGORYID, item.getCategoryId());
		contentValues.put(KEY_TIMESTAMP, item.getTimestamp());
		
		long id = db.insert(PREVIOUS_SEARCHES_TABLE, null, contentValues);
		item.setId(id);
		this.close();
	}
	
//	public void updateSavedPlace(AndroidFavorite fav){
//		this.open();
//		ContentValues contentValues = new ContentValues();
//		contentValues.put(KEY_NAME, fav.getName());
//		contentValues.put(KEY_DESCRIPTION, fav.getDescription());
//		db.update(SAVED_PLACES_TABLE, contentValues, KEY_ID + "=\"" + fav.getId() + "\"", null);
//		this.close();
//	}
	
	public void updatePreviusDestinations(RecentDestination fav){
		this.open();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_NAME, fav.getName());
		contentValues.put(KEY_DESCRIPTION, fav.getDescription());
		db.update(PREVIOUS_DESTINATIONS_TABLE, contentValues, KEY_ID + "=\"" + fav.getId() + "\"", null);
		this.close();
	}
	
//	public void deleteSavedPlace(AndroidFavorite fav){
//		deletePlace(fav, SAVED_PLACES_TABLE);
//	}
	
	public void deletePreviousDestination(RecentDestination fav){
		deletePlace(fav, PREVIOUS_DESTINATIONS_TABLE);
	}
	
	public void deletePreviousSearch(SearchHistoryItem item){
		this.open();
		db.delete(PREVIOUS_SEARCHES_TABLE, KEY_ID + "='" + item.getId() + "'", null);
		this.close();
	}
	
	public void deleteAllPreviousDestinations() {
		this.open();
		db.delete(PREVIOUS_DESTINATIONS_TABLE, null, null);
		this.close();
	}
	
	private void deletePlace(RecentDestination fav, String databaseTable){
		this.open();
		db.delete(databaseTable, KEY_ID + "='" + fav.getId() + "'", null);
		this.close();
	}

	public ArrayList<RecentDestination> readSavedPlaces() {
		ArrayList<RecentDestination> places;
		places = new ArrayList<RecentDestination>();
		try{
			this.open();
			places = readPlaces(SAVED_PLACES_TABLE);
			this.close();
		}catch(SQLException e){}
		return places;
	}
	
	public ArrayList<RecentDestination> readPreviousDestionations() {
		ArrayList<RecentDestination> places;
		this.open();
		places = readPlaces(PREVIOUS_DESTINATIONS_TABLE);
		this.close();
		return places;
	}
	
	public ArrayList<SearchHistoryItem> readPreviousSearches() {
		ArrayList<SearchHistoryItem> searches = new ArrayList<SearchHistoryItem>();
		this.open();
		String[] resultColumns = new String[] {KEY_ID, KEY_WHAT_TEXT, KEY_WHERE_TEXT, KEY_TOPREGIONID, KEY_CATEGORYID, KEY_TIMESTAMP};
		String orderBy = KEY_TIMESTAMP + " DESC";
		Cursor cursor = db.query(PREVIOUS_SEARCHES_TABLE, resultColumns, null, null, null , null, orderBy);
		try{
			if(cursor.moveToFirst()){
				do{
					long id = cursor.getLong(INDEX_ID);
					String whatString = cursor.getString(INDEX_WHAT_TEXT);
					String whereString = cursor.getString(INDEX_WHERE_TEXT);
					int categoryId = cursor.getInt(INDEX_CATEGORYID);
					int topRegionId = cursor.getInt(INDEX_TOPREGIONID);
					long timestamp = cursor.getLong(INDEX_SEARCH_TIME);
					
					SearchHistoryItem item = new SearchHistoryItem(id, whatString, whereString, topRegionId, categoryId, timestamp);
					searches.add(item);
				} while(cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		this.close();
		return searches;
	}
	
	private ArrayList<RecentDestination> readPlaces(String databaseTable) {
		ArrayList<RecentDestination> places = new ArrayList<RecentDestination>();
		
		String[] resultColumns = new String[] { KEY_ID, KEY_NAME, KEY_DESCRIPTION, KEY_LATITUDE, KEY_LONGITUDE, KEY_SRVSTRING , KEY_TIMESTAMP, KEY_IMAGE_NAME};
		String orderBy = KEY_TIMESTAMP + " DESC";
		Cursor cursor = db.query(databaseTable, resultColumns, null, null, null , null, orderBy);
		try{
			if(cursor.moveToFirst()){
				do{
					long id = cursor.getLong(INDEX_ID);
					String name = cursor.getString(INDEX_NAME);
					String description = cursor.getString(INDEX_DESCRIPTION);
					int latitude = cursor.getInt(INDEX_LATITUDE);
					int longitude = cursor.getInt(INDEX_LONGITUDE);
					long timestamp = cursor.getLong(INDEX_TIMESTAMP);
					Position pos = new Position(latitude, longitude);
					String imageName = cursor.getString(INDEX_IMAGE_NAME);
					
					RecentDestination place = new RecentDestination(id, name, description, pos, timestamp, imageName, null);  //TODO add poiInfo
					places.add(place);
				} while(cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return places;
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
			db.execSQL(PREVIOUS_DESTINATIONS_TABLE_CREATE);
			db.execSQL(PREVIOUS_SEARCHES_TABLE_CREATE);
		}

		// Called when there is a database version mismatch meaning that
		// the version of the database on disk needs to be upgraded to
		// the current version.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + PREVIOUS_DESTINATIONS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SAVED_PLACES_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + PREVIOUS_SEARCHES_TABLE);
			onCreate(db);
		}
	}

	public void deleteAllPreviousSearches() {
		this.open();
		db.delete(PREVIOUS_SEARCHES_TABLE, null, null);
		this.close();
	}
	
	public void deleteSavedPlacesTable(){
		try{
			this.open();
			db.delete(SAVED_PLACES_TABLE, null, null);
			this.close();
		}catch(SQLException e){}
	}


}
