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
package com.vodafone.android.navigation.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.LiveFolders;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.favorite.ListModel;

public class ContentProviderPlaces extends ContentProvider {

	public static final String AUTHORITY = "com.authority.my";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/places");

	public static final String _ID = "_id";

    private static final int TYPE_PLACES = 1;
    private static final int TYPE_SINGLE_PLACE = 2;

	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, "places", TYPE_PLACES);
        URI_MATCHER.addURI(AUTHORITY, "places/#", TYPE_SINGLE_PLACE);
	}

	// Set of columns needed by a LiveFolder
	// This is the live folder contract
	private static final String[] CURSOR_COLUMNS = new String[] { 
		BaseColumns._ID, 
		LiveFolders.NAME, 
		LiveFolders.DESCRIPTION, 
		LiveFolders.INTENT, 
		LiveFolders.ICON_PACKAGE, 
		LiveFolders.ICON_RESOURCE };


    // The error cursor to use
    private static MatrixCursor sErrorCursor = new MatrixCursor(new String[0]);

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Figure out the uri and return error if not matching
		int type = URI_MATCHER.match(uri);
        if (type == UriMatcher.NO_MATCH) {
            return sErrorCursor;
        }

        String id = null;
        if(type == TYPE_SINGLE_PLACE) {
            id = uri.getPathSegments().get(1);
        }
            
		MatrixCursor mc = loadNewData(id);
		return mc;
	}
	
	private MatrixCursor loadNewData(String id) {
		MatrixCursor c = new MatrixCursor(CURSOR_COLUMNS);

        Context context = this.getContext();
        NavigatorApplication app = (NavigatorApplication) context.getApplicationContext();
        
        ListModel places = app.getSavedPlaces();
        
        if(places != null) {
    		for (int i = 0; i < places.getSize(); i++) {
    			Favorite place = (Favorite)places.getElementAt(i);
                String currId = "" + i;
                if(id == null || currId.equals(id)) {
        			Uri uri = ContentUris.withAppendedId(CONTENT_URI, i);
        			
                    Object[] values = new Object[] {
        				currId,                     //id
        				place.getName(),            //name, 
        				place.getDescription(),     //desc
        				uri,                        //intent
        				context.getPackageName(),   //package
        				R.drawable.cmd_my_places    //icon
        			};
        			c.addRow(values);
                }
    		}
        }
		return c;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
            case TYPE_PLACES:
                return "vnd.android.cursor.dir/vnd.wayfinder.places";
            case TYPE_SINGLE_PLACE:
                return "vnd.android.cursor.item/vnd.wayfinder.places";
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
