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

import java.util.ArrayList;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.LandmarkListAdapter;
import com.vodafone.android.navigation.components.Landmark;
import com.vodafone.android.navigation.components.RecentDestination;
import com.vodafone.android.navigation.dialog.AlertDialog;
import com.vodafone.android.navigation.listeners.SearchResultsListener;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.LandmarkMapObject;
import com.vodafone.android.navigation.view.PinsLayer;
import com.vodafone.android.navigation.view.ResizeHandleImageButton;
import com.vodafone.android.navigation.view.SearchDialogView;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.SearchReply;
import com.wayfinder.core.search.onelist.OneListSearchReply;
import com.wayfinder.core.search.onelist.OneListSearchReply.MatchList;
import com.wayfinder.core.search.onelist.OneListSearchReply.SearchMatch;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;

public class SearchResultsActivity extends AbstractLandmarksListActivity 
implements SearchResultsListener {
	
    public static final int RESULT_NO_RESULTS = RESULT_FIRST_USER;
    public static final int RESULT_ERROR = RESULT_NO_RESULTS + 1;

    private static final int DIALOG_NORMAL_SEARCH = DIALOG_NEXT_AVAILABLE_ID;
    private static final int DIALOG_ERROR = DIALOG_NORMAL_SEARCH + 1;
    
    private static final int MAX_NUMBER_OF_DISPLAYED_SPONSORED_RESULTS = 3;
    private static final boolean BRING_SPONSORED_RESULTS_TO_FRONT = false;
    private static final String SPONSORED_RESULTS_PROVIDER_ID = "eniro";
    private static final int SPONSORED_RESULTS_PROXIMITY_LIMIT = 5000;
	
	private SearchQuery preparedSearchQuery;
	private boolean searchCancelled;
    private boolean shouldCenterItem = true;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title.setText(R.string.qtn_andr_search_result_txt);
		NavigatorApplication app = getApp();
		app.setSearchResultsListener(this);
		preparedSearchQuery = app.getPreparedSearchQuery();
		this.searchCancelled = false;
		if (app.isSearhQueryPending()) {
            app.clearSearch();
		    RequestID requestId = app.getCore().getSearchInterface().getOneListSearch().search(preparedSearchQuery, app);
		    app.setSearchRequestId(requestId);
			showDialog(DIALOG_NORMAL_SEARCH);
			app.setSearchQueryPending(false);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    
	    if(hasFocus) {
	        if (this.getApp().isSearhQueryPending()) {
	           this.resizeButton.resizeView(ResizeHandleImageButton.MENU_HEIGHT_STEP_MIN);
	        }
	        else{
	        	this.resizeButton.resizeView(ResizeHandleImageButton.MENU_HEIGHT_STEP_MED);
	        }    

            this.handler.post(new Runnable() {
                public void run() {
                    if(selectedIndex >= 0 && landmarks != null && landmarks.size() > selectedIndex) {
                        Landmark landmark = landmarks.get(selectedIndex);
                        AndroidMapObject mapObject = mappedMapObjects.get(landmark);
                        PinsLayer pinslayer = mapView.getPinsLayer();
                        pinslayer.setSelectedMapObject(mapObject, shouldCenterItem, true);
                    }
                }
            });
	    }
	}
	
	@Override
	protected void onRestart() {
		this.getApp().setSearchResultsListener(this);

		super.onRestart();
	}
	
	@Override
	protected void onResume() {
	    NavigatorApplication app = this.getApp();
	    if (app.getSearchReply() != null) {
            updateUIArrays(app.getSearchReply());
        }
	    if(landmarksListView != null){
        	landmarksListView.resetOldSavedIndexes();
        }
        this.searchResultsAdapter.notifyDataSetChanged();
        super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();

		this.getApp().setSearchResultsListener(null);
	}
	
	public void searchDone(RequestID reqID, SearchReply reply) {
	    if(this.searchCancelled) {
	        return;
	    }
	    
	    dismissDialog(DIALOG_NORMAL_SEARCH);
		if(updateUIArrays(reply)){
			searchResultsAdapter.notifyDataSetChanged();
			resizeButton.resizeView(ResizeHandleImageButton.MENU_HEIGHT_STEP_MED);
            dismissDialog(DIALOG_NORMAL_SEARCH);

            this.centerMap();
		} else if(this.landmarks.size() == 0) {
			setResult(RESULT_NO_RESULTS);
        	finish();
		}
	}

	public void searchUpdated(RequestID reqID, SearchReply reply) {
        if(this.searchCancelled) {
            return;
        }
        
		if(updateUIArrays(reply)){
			searchResultsAdapter.notifyDataSetChanged();
			resizeButton.resizeView(ResizeHandleImageButton.MENU_HEIGHT_STEP_MED);
			dismissDialog(DIALOG_NORMAL_SEARCH);
		}
		this.centerMap();
	}

	public void error(RequestID requestID, CoreError error) {
	    super.error(requestID, error);
	    try {
	        this.dismissDialog(DIALOG_NORMAL_SEARCH);
	    } catch(IllegalArgumentException e) {}
	    
	    this.setResult(RESULT_ERROR);
	    finish();
        this.errorMessage = error.getInternalMsg();
        Log.e("SearchResultsActivity", "error() " + this.errorMessage);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
    		case DIALOG_NORMAL_SEARCH: {
    			SearchDialogView searchDialog = new SearchDialogView(this, preparedSearchQuery);
    			AlertDialog dialog = new AlertDialog(this);
    			dialog.setView(searchDialog);
    			dialog.setTitle(R.string.qtn_andr_search_for_txt);
    			dialog.setIcon(R.drawable.search_dialog_icon);
    			dialog.setCancelable(true);
    			dialog.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        searchCancelled = true;
                        finish();
                    }
				});
    			return dialog;
    		}
            case DIALOG_ERROR: {
                View routeView = View.inflate(this, R.layout.error_dialog, null);
                TextView textMessage = (TextView) routeView.findViewById(R.id.text_message);
                textMessage.setText(this.errorMessage);
                AlertDialog dialog = new AlertDialog(this);
                dialog.setView(routeView);
                dialog.setTitle(R.string.qtn_andr_note_txt);
                dialog.setIcon(R.drawable.error);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        getApp().setRouteListener(null);
                    }
                });
                return dialog;
            }
    		default: {
    			return super.onCreateDialog(id);
    		}
	    }
	}
	
	protected boolean updateUIArrays(SearchReply reply) {
	    Log.i("SearchResultsActivity", "updateUIArrays()");
        VectorMapInterface map = this.mapView.getMapLayer().getMap();
        
        if(reply.getReplyType() == SearchReply.TYPE_ONELIST) {
            OneListSearchReply oneReply = (OneListSearchReply) reply;
            MatchList matchList = oneReply.getMatchList();
            if(matchList.getNbrOfMatches() == 0) {
            	return false;
            }
        
            this.landmarks.clear();
            map.removeAllMapObjects();
            NavigatorApplication app = this.getApp();
            
            for (int j = 0; j < matchList.getNbrOfMatches(); j++) {
        		SearchMatch match = matchList.getMatch(j);
        		Landmark landmark = new Landmark(match);
        		if(match.getPosition().isValid()) {
        			this.landmarks.add(landmark);
        			
                    LandmarkMapObject mapObject = new LandmarkMapObject(this, map, landmark, getType());
                    this.mappedMapObjects.put(landmark, mapObject);
        
                    this.addMapObject(map, landmark, mapObject);
        		}
            }
            
            ArrayList<Landmark> sponsoredLandmarks = new ArrayList<Landmark>();
            for(int i = sponsoredLandmarks.size() - 1; i >=0; i--){
            	Landmark l = sponsoredLandmarks.get(i);
            	landmarks.remove(l);
            	landmarks.add(0, l);
            }
        }
        return true;
	}

	private boolean isCloseEnough(Landmark landmark) {
		LocationInformation loc = getApp().getOwnLocationInformation();
		Position ownPos = loc.getMC2Position();
		return ownPos.distanceTo(landmark.getPosition()) <= SPONSORED_RESULTS_PROXIMITY_LIMIT;
	}

	@Override
	protected int getType() {
		return ContextActivity.TYPE_SEARCH;
	}

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractActivity#shouldBeCloseOnError()
	 */
	@Override
	public boolean shouldBeCloseOnError() {
		return true;
	}
	
	
}
