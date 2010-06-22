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
package com.vodafone.android.navigation.activity;

import java.util.Comparator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.LandmarkListAdapter;
import com.vodafone.android.navigation.components.Landmark;
import com.vodafone.android.navigation.components.RecentDestination;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.LandmarkMapObject;
import com.vodafone.android.navigation.view.PinsLayer;
import com.vodafone.android.navigation.view.ResizeHandleImageButton;
import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.favorite.ListDataListener;
import com.wayfinder.core.favorite.ListModel;
import com.wayfinder.core.map.vectormap.VectorMapInterface;

public class PlacesActivity extends AbstractLandmarksListActivity implements ListDataListener{
	
	private static final int MENU_ITEM_RECENT_DESTINATIONS_ID = 0;
	private static final int MENU_ITEM_MANAGE_PLACES_ID = 1;
	private static final int MENU_ITEM_CLEAR_LIST = 2;
	private static final int MENU_ITEM_MANAGE_DESTINATIONS_ID = 3;
	private static final int MENU_ITEM_SYNC_PLACES = 4;
	
	public static final int TYPE_SAVED_PLACES = 0;
	public static final int TYPE_DESTIONATIONS = 1;

	
    public static final String KEY_TYPE = "type_key";
    public static final String KEY_PLACE_ID = "key_place_id";
	
	private MenuItem cmdManagePlaces;
	private MenuItem cmdRecentDestination;
	private MenuItem cmdSync;
	private MenuItem cmdClear;
	
	private int type;
	private boolean emptyList;
	private boolean shouldCenterItem = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = this.getIntent();
		selectedIndex = 0;
		long placeId = intent.getLongExtra(KEY_PLACE_ID, -1);
		if(placeId >= 0) {
            NavigatorApplication app = this.getApp();
            app.initiate(null);
            Favorite place = app.getSavedPlace((int)placeId);
            if(place != null) {
                this.navigate(place.getName(), place.getDescription(), place.getPosition(), "", place.getIconName());
            }
		}
		
		this.type = intent.getIntExtra(KEY_TYPE, TYPE_SAVED_PLACES);
		if(type == TYPE_SAVED_PLACES){
			title.setText(R.string.qtn_andr_my_places_txt);
		} else {
			title.setText(R.string.qtn_andr_recent_dest_txt);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(type == TYPE_SAVED_PLACES){
			cmdRecentDestination = menu.add(Menu.NONE, MENU_ITEM_RECENT_DESTINATIONS_ID, Menu.NONE, R.string.qtn_andr_recent_dest_tk);
            cmdRecentDestination.setIcon(R.drawable.cmd_recent_destinations);
            
            cmdSync = menu.add(Menu.NONE, MENU_ITEM_SYNC_PLACES, Menu.NONE, R.string.qtn_andr_synchronise_tk);
            cmdSync.setIcon(R.drawable.cmd_sync); //TODO maybe wrong icon
			
            cmdManagePlaces = menu.add(Menu.NONE, MENU_ITEM_MANAGE_PLACES_ID, Menu.NONE, R.string.qtn_andr_manage_tk);
            cmdManagePlaces.setIcon(R.drawable.cmd_manage);
            cmdManagePlaces.setEnabled(getApp().getSavedPlaces().getSize() != 0);
		} else {
			cmdClear = menu.add(Menu.NONE, MENU_ITEM_CLEAR_LIST, Menu.NONE, R.string.qtn_andr_clear_tk);
			cmdClear.setIcon(R.drawable.cmd_remove);
			cmdClear.setEnabled(getApp().getPreviousDestinations().length != 0);
			
			cmdManagePlaces = menu.add(Menu.NONE, MENU_ITEM_MANAGE_DESTINATIONS_ID, Menu.NONE, R.string.qtn_andr_manage_tk);
            cmdManagePlaces.setIcon(R.drawable.cmd_manage);
            cmdManagePlaces.setEnabled(getApp().getPreviousDestinations().length != 0);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    if(type == TYPE_SAVED_PLACES) {
            cmdManagePlaces.setVisible(true);
	    }
	    else {
            cmdManagePlaces.setVisible(false);
	    }
	    return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		final Intent intent;
		
		switch ( item.getItemId() ) {
		case MENU_ITEM_RECENT_DESTINATIONS_ID:
			if(getApp().getPreviousDestinations().length == 0 ){
				Toast.makeText(this, R.string.qtn_andr_no_recent_dest_txt, Toast.LENGTH_LONG)
				.show();
				return true;
			}
			intent = new Intent(this, PlacesActivity.class);
			intent.putExtra(PlacesActivity.KEY_TYPE, PlacesActivity.TYPE_DESTIONATIONS);
			startActivity(intent);
			return true;
			
			
		case MENU_ITEM_MANAGE_PLACES_ID:
			shouldCenterItem = false;
			intent = new Intent(this, ManagePlacesActivity.class);
			intent.putExtra(PlacesActivity.KEY_TYPE, PlacesActivity.TYPE_SAVED_PLACES);
			startActivity(intent);
			return true;
		
		case MENU_ITEM_MANAGE_DESTINATIONS_ID:
			shouldCenterItem = false;
			intent = new Intent(this, ManagePlacesActivity.class);
			intent.putExtra(PlacesActivity.KEY_TYPE, PlacesActivity.TYPE_DESTIONATIONS);
			startActivity(intent);
			return true;
			
		
		case MENU_ITEM_CLEAR_LIST:
			getApp().removeAllPreviousDestionations();
			VectorMapInterface map = this.mapView.getMapLayer().getMap();
	        if(map != null) {
	             map.removeAllMapObjects();
	        }
			updateUIArrays();
			searchResultsAdapter.notifyDataSetChanged();
			
		case MENU_ITEM_SYNC_PLACES:
			getApp().getCore().getFavoriteInterface().synchronizeFavorites(getApp());
			Toast.makeText(this, R.string.qtn_andr_saved_places_r_syncd_txt, Toast.LENGTH_LONG).show();
		}
		return super.onOptionsItemSelected(item);
	}

	protected void updateUIArrays() {
		landmarks.clear();
		VectorMapInterface map = this.mapView.getMapLayer().getMap();
		if(type == TYPE_SAVED_PLACES){
			ListModel listModel = getApp().getSavedPlaces();
			if(cmdManagePlaces != null){
				cmdManagePlaces.setEnabled(listModel.getSize() != 0);
			}
			if (listModel.getSize() == 0) {
				emptyList = true;
				landmarks.add(new Landmark(new RecentDestination(LandmarkListAdapter.DISABLED_STR + this.getString(R.string.qtn_andr_no_saved_places_txt), "", null, 0, "", null)));
				return;
			}
			emptyList = false;
			for (int i = 0; i < listModel.getSize(); i++) {
				Favorite fav = (Favorite) listModel.getElementAt(i);
				Landmark landmark = new Landmark(fav);
				if (landmark.getPosition().isValid()
						&& !this.landmarks.contains(landmark)) {
					this.landmarks.add(landmark);
					LandmarkMapObject mapObject = new LandmarkMapObject(this, map, landmark, getType());
					this.mappedMapObjects.put(landmark, mapObject);
					
					this.addMapObject(map, landmark, mapObject);
				}
			}
		} else {
			RecentDestination[] places = getApp().getPreviousDestinations();
			if(cmdClear != null){
				cmdClear.setEnabled(places.length != 0);
			}
			if (places.length == 0) {
				emptyList = true;
				landmarks.add(new Landmark(new RecentDestination(LandmarkListAdapter.DISABLED_STR + this.getString(R.string.qtn_andr_no_recent_dest_txt), "", null, 0, "", null)));
				return;
			}
			emptyList = false;
			for (RecentDestination place : places) {
				Landmark landmark = new Landmark((RecentDestination)place);
				if (landmark.getPosition().isValid()
						&& !this.landmarks.contains(landmark)) {
					this.landmarks.add(landmark);
					LandmarkMapObject mapObject = new LandmarkMapObject(this, map, landmark, getType());
					this.mappedMapObjects.put(landmark, mapObject);
					
					this.addMapObject(map, landmark, mapObject);
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractLandmarksListActivity#onResume()
	 */
	@Override
	protected void onResume() {
		updateUIArrays();
        if(type == TYPE_SAVED_PLACES){
        	getApp().getSavedPlaces().addListDataListener(this);
//			searchResultsAdapter.sort(new Comparator<Landmark>(){
//				public int compare(Landmark l1, Landmark l2) {
//					return l1.getName().toLowerCase().compareTo(l2.getName().toLowerCase());
//				}
//			});
		} else {
			searchResultsAdapter.sort(new Comparator<Landmark>(){
				public int compare(Landmark l1, Landmark l2) {
					return (int)(l2.getTimestamp() - l1.getTimestamp());
				}
			});
		}
        if(landmarksListView != null){
        	landmarksListView.resetOldSavedIndexes();
        }
		searchResultsAdapter.notifyDataSetChanged();
        super.onResume();
	}

	@Override
	protected int getType() {
		if(type == TYPE_SAVED_PLACES){
			return ContextActivity.TYPE_PLACES;
		}
		return ContextActivity.TYPE_DESTINATIONS;
	}

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractLandmarksListActivity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus) {
		    this.resizeButton.resizeView(ResizeHandleImageButton.MENU_HEIGHT_STEP_MED);
	        if(!emptyList && shouldCenterItem){
	            this.handler.post(new Runnable() {
	                public void run() {
	                    Landmark landmark = landmarks.get(selectedIndex);
	                    AndroidMapObject mapObject = mappedMapObjects.get(landmark);
	                    PinsLayer pinslayer = mapView.getPinsLayer();
	                    pinslayer.setSelectedMapObject(mapObject, shouldCenterItem, true);
	                }
	            });
	        }
		}
	}

	public void contentsChanged(int indexStart, int indexEnd) {
		if(type == TYPE_SAVED_PLACES){
			VectorMapInterface map = this.mapView.getMapLayer().getMap();
			 if(map != null) {
	             map.removeAllMapObjects();
	        }
			updateUIArrays();
			searchResultsAdapter.notifyDataSetChanged();
		}
	}

	public void intervalAdded(int indexStart, int indexEnd) {
		if(type == TYPE_SAVED_PLACES){
			VectorMapInterface map = this.mapView.getMapLayer().getMap();
			 if(map != null) {
	             map.removeAllMapObjects();
	        }
			updateUIArrays();
			searchResultsAdapter.notifyDataSetChanged();
		}
	}

	public void intervalRemoved(int indexStart, int indexEnd) {
		if(type == TYPE_SAVED_PLACES){
			VectorMapInterface map = this.mapView.getMapLayer().getMap();
			 if(map != null) {
	             map.removeAllMapObjects();
	        }
			updateUIArrays();
			searchResultsAdapter.notifyDataSetChanged();
		}
	}

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractLandmarksListActivity#onPause()
	 */
	@Override
	protected void onPause() {
		if(type == TYPE_SAVED_PLACES){
			getApp().getSavedPlaces().removeListDataListener(this);
		}
		super.onPause();
	}
	
}
