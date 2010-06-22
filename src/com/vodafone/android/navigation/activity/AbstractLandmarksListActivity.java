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
import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.LandmarkListAdapter;
import com.vodafone.android.navigation.adapter.LandmarkListAdapter.OnSearchMatchClickListener;
import com.vodafone.android.navigation.components.Landmark;
import com.vodafone.android.navigation.listeners.ViewSizeChangedListener;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.LandmarkMapObject;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.vodafone.android.navigation.util.ResourceUtil;
import com.vodafone.android.navigation.util.ImageDownloader.ImageDownloadListener;
import com.vodafone.android.navigation.view.MapLayerView;
import com.vodafone.android.navigation.view.PinsLayer;
import com.vodafone.android.navigation.view.ResizeHandleImageButton;
import com.vodafone.android.navigation.view.SearchResultListView;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.wayfinder.core.map.MapObjectImage;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.shared.Position;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;

public abstract class AbstractLandmarksListActivity 
    extends AbstractMapActivity 
    implements  ViewSizeChangedListener, 
                OnItemSelectedListener, 
                OnSearchMatchClickListener, 
                OnKeyListener, 
                ImageDownloadListener { 

    public static final String KEY_SEARCH_AROUND_ME = "key_search_around_me";
    
	protected SearchResultListView landmarksListView;
	protected LandmarkListAdapter searchResultsAdapter;
	protected ResizeHandleImageButton resizeButton;
	protected WayfinderMapView mapView;
	protected ArrayList<Landmark> landmarks = new ArrayList<Landmark>();
	protected HashMap<Landmark, AndroidMapObject> mappedMapObjects = new HashMap<Landmark, AndroidMapObject>();

	protected boolean centerOnMyPosition;
	protected int selectedIndex = ListView.INVALID_POSITION;
    protected HashMap<String, MapObjectImage> cachedMapObjectImages = new HashMap<String, MapObjectImage>();
    protected Handler handler = new Handler();
    protected String errorMessage;
    protected TextView title;
    protected ImageView layoutContent;
    private MapObjectImage defaultMapObjectImage;

	private int resultListTop;

	/** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_results_activity);
		
		Intent intent = this.getIntent();
		this.centerOnMyPosition = intent.getBooleanExtra(KEY_SEARCH_AROUND_ME, false);
	
	    this.mapView = (WayfinderMapView) this.findViewById(R.id.map);
		
	    this.layoutContent = (ImageView) this.findViewById(R.id.img_search_result_list_top);
		
        this.resizeButton = (ResizeHandleImageButton) findViewById(R.id.handle);
		this.resizeButton.setSizeChangedListener(this);
		
		this.landmarksListView = (SearchResultListView) findViewById(R.id.listview_search_results);

        this.searchResultsAdapter = new LandmarkListAdapter(this, this.landmarks, this);
		this.landmarksListView.setAdapter(searchResultsAdapter);
		this.landmarksListView.setOnItemSelectedListener(this);
		this.landmarksListView.setOnKeyListener(this);
		this.landmarksListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    mapView.lockMapUpdates(false);
                }
                else if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mapView.lockMapUpdates(true);
                }
            }
            
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });

        this.title = (TextView) findViewById(R.id.activity_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VectorMapInterface map = this.getApp().getCore().getVectorMapInterface();
        if(map != null) {
            map.getMapDetailedConfigInterface().setServerPOIsVisible(false);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        VectorMapInterface map = this.mapView.getMapLayer().getMap();
        if(map != null) {
            map.getMapDetailedConfigInterface().setServerPOIsVisible(true);
            map.removeAllMapObjects();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            VectorMapInterface map = this.mapView.getMapLayer().getMap();
            if(map != null) {
                map.removeAllMapObjects();
            }
            this.finish();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected WayfinderMapView getMap() {
        return this.mapView;
    }
    
    protected void addMapObject(VectorMapInterface map, Landmark landmark, LandmarkMapObject mapObject) {
        String imageName = landmark.getImageName();
        Bitmap bitmap = ImageDownloader.get().queueDownload(this, imageName, mapObject, this);
        if(bitmap != null) {
            MapObjectImage mapObjectImage = this.getMapObjectImage();
            map.addMapObject(mapObject, mapObjectImage);
            mapObject.setBitmap(imageName, bitmap);
        }
    }

	public void onImageDownloaded(final Bitmap scaledBitmap, final Bitmap origBitmap, final String imageName, final AndroidMapObject[] mapObjects) {
	    this.handler.post(new Runnable() {
            public void run() {
                MapObjectImage mapObjectImage = getMapObjectImage();
                VectorMapInterface map = mapView.getMapLayer().getMap();
                for(AndroidMapObject androidMapObject: mapObjects) {
                    if(androidMapObject instanceof LandmarkMapObject) {
                        LandmarkMapObject mapObject = (LandmarkMapObject) androidMapObject;
                        mapObject.setBitmap(imageName, scaledBitmap);
                        map.addMapObject(mapObject, mapObjectImage);
                        int position = searchResultsAdapter.getPosition(mapObject.getLandmark());
                        View view = landmarksListView.getChildAt(position);
                        if(view != null) {
                            ImageView imageView = (ImageView) view.findViewById(R.id.category_icon);
                            if(imageView != null){
                            	imageView.setImageBitmap(origBitmap);
                            }
                        }
                    }
                }
            }
	    });
	}

    private MapObjectImage getMapObjectImage() {
        if(this.defaultMapObjectImage == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.cat_all);
            int imageMaxWidth = LandmarkMapObject.getImageMaxWidth(this);
            int imageMaxHeight = LandmarkMapObject.getImageMaxHeight(this);
            Bitmap scaled = ResourceUtil.scale(bitmap, imageMaxWidth, imageMaxHeight);
            WFImage image = this.getApp().getAndroidFactory().createWFImage(scaled);
            this.defaultMapObjectImage = new MapObjectImage(image, WFGraphics.ANCHOR_HCENTER | WFGraphics.ANCHOR_BOTTOM);
        }
        return this.defaultMapObjectImage;
    }
	
	public void onViewSizeChanged(int sizeStep) {
		this.updateActiveScreenPoint();
		this.mapView.postInvalidate();
	}

	@Override
    public void onSelect(AndroidMapObject mapObject, boolean centerMapObject, boolean displayContextMenu) {
        //callback when an AndroidMapObject is selected in map. 
	    this.updateActiveScreenPoint();
	    if(centerMapObject) {
	        this.centerMapObject(mapObject, displayContextMenu);
	    }
        if(!displayContextMenu) {
            if(mapObject instanceof LandmarkMapObject) {
                //Will focus the view in searchresult list
                LandmarkMapObject srMapObject = (LandmarkMapObject) mapObject; 
                if(this.searchResultsAdapter != null) {
                    this.selectedIndex = this.searchResultsAdapter.getPosition(srMapObject.getLandmark());
                    this.setSelectionInList(this.selectedIndex, true);
                }
            }
        }
        else {
            //second click, display context menu
            this.displayContextMenu(mapObject);
        }
    }

    private void setSelectionInList(int index, boolean centerViewInList) {
        int listHeight = this.landmarksListView.getHeight();
        int childCount = this.landmarksListView.getChildCount();
        int childHeight = 0;
        if(childCount > 0) {
            childHeight = (listHeight / childCount);
        }
        int y = ((listHeight - childHeight) >> 1);
        if(centerViewInList) {
            this.landmarksListView.setSelectionFromTop(index, y);
        }
        else {
            this.landmarksListView.setHilighted(index);
            this.landmarksListView.invalidate();
        }
    }
	
	protected abstract int getType();
	
    public void centerMap() {
        MapLayerView mapLayer = this.mapView.getMapLayer();
        if(this.centerOnMyPosition) {
            this.centerMyPosition(mapLayer);
        }
        else {
            this.centerMass(mapLayer);
        }
    }
    
    /**
     * centers map around my position, and zooms map so that all mapobjects of interest fits
     * @param index all mapobject between 0 - index are of interest
     * @param mapLayer
     */
    private void centerMyPosition(MapLayerView mapLayer) {
        Position position = this.getApp().getOwnLocationInformation().getMC2Position();
        long deltaLat = 0;
        long deltaLon = 0;
        
        int size = this.landmarks.size();
        for(int i = 0; i < 5 && i < size; i ++) {
            Landmark landmark = landmarks.get(i);
            AndroidMapObject currMapObject = this.mappedMapObjects.get(landmark);
            if(currMapObject != null){
	            int dLat = Math.abs(currMapObject.getLatitude() - position.getMc2Latitude());
	            int dLon = Math.abs(currMapObject.getLongitude() - position.getMc2Longitude());
	
	            if(dLat > deltaLat) {
	                deltaLat = dLat;
	            }
	            if(dLon > deltaLon) {
	                deltaLon = dLon;
	            }
            }
        }
        
        if(deltaLon > 0 && deltaLat > 0) {
            int aCornerLat1 = (int) (position.getMc2Latitude() + deltaLat);
            int aCornerLon1 = (int) (position.getMc2Longitude() - deltaLon);
            int aCornerLat2 = (int) (position.getMc2Latitude() - deltaLat);
            int aCornerLon2 = (int) (position.getMc2Longitude() + deltaLon);

            int resultListHeight = this.layoutContent.getTop();
            int centerVisibleY = (resultListHeight) >> 1;
            double ratioLatPerPixel = (double) deltaLat / (double) centerVisibleY;
            aCornerLat2 -= (resultListHeight * ratioLatPerPixel);

            this.setMapWorldBox(mapLayer, aCornerLat1, aCornerLon1, aCornerLat2, aCornerLon2, ratioLatPerPixel);
        }
    }

    /**
     * zooms map so that all mapobjets of interest fits
     * @param index mapobjects are grouped by 5 when map is zoomed, so index is used 
     * for calculating what group of results to display in map. Group #0 = item0-item4
     * @param mapLayer
     */
    private void centerMass(MapLayerView mapLayer) {
        int size = this.landmarks.size();
        if(size > 1) {
            int aCornerLat1 = Integer.MIN_VALUE;
            int aCornerLon1 = Integer.MIN_VALUE;
            int aCornerLat2 = Integer.MIN_VALUE;
            int aCornerLon2 = Integer.MIN_VALUE;

            for(int i = 0; i < 5 && i < size; i ++) {
                Landmark landmark = landmarks.get(i);
                AndroidMapObject currMapObject = this.mappedMapObjects.get(landmark);
                if(currMapObject != null) {
	                int lat = currMapObject.getLatitude();
	                int lon = currMapObject.getLongitude();
	                if(aCornerLat1 == Integer.MIN_VALUE || lat > aCornerLat1) {
	                    aCornerLat1 = lat;
	                }
	                if(aCornerLat2 == Integer.MIN_VALUE || lat < aCornerLat2) {
	                    aCornerLat2 = lat;
	                }
	                if(aCornerLon1 == Integer.MIN_VALUE || lon < aCornerLon1) {
	                    aCornerLon1 = lon;
	                }
	                if(aCornerLon2 == Integer.MIN_VALUE || lon > aCornerLon2) {
	                    aCornerLon2 = lon;
	                }
                }
            }
    
            int mapHeight = mapLayer.getHeight();
            
            int deltaLat = Math.abs(aCornerLat1 - aCornerLat2);
            double ratioLatPerPixel = (double) deltaLat  / (double) (resultListTop);
            aCornerLat2 = (int) (aCornerLat1 - (mapHeight * ratioLatPerPixel));
    
            if(deltaLat > 0) {
                this.setMapWorldBox(mapLayer, aCornerLat1, aCornerLon1, aCornerLat2, aCornerLon2, ratioLatPerPixel);
            }
            else {
                VectorMapInterface map = mapLayer.getMap();
                map.setScale(5.0f);
                map.setCenter(aCornerLat1, aCornerLon1);
            }
        }
        else if(size == 1) {
            VectorMapInterface map = mapLayer.getMap();
            map.setScale(5.0f);

            Landmark landmark = landmarks.get(0);
            AndroidMapObject mapObject = this.mappedMapObjects.get(landmark);
            if(mapObject != null) {
                this.getMap().getPinsLayer().setSelectedMapObject(mapObject, true, false);
            }
        }
    }

    private void setMapWorldBox(MapLayerView mapLayer, int aCornerLat1, int aCornerLon1, int aCornerLat2, int aCornerLon2, double ratioLatPerPixel) {
        double padding = ratioLatPerPixel * 50;
        aCornerLat1 = (int) (aCornerLat1 + padding);
        aCornerLat2 = (int) (aCornerLat2 - padding);
        aCornerLon1 = (int) (aCornerLon1 - padding);
        aCornerLon2 = (int) (aCornerLon2 + padding);

        VectorMapInterface map = mapLayer.getMap();
        map.setWorldBox(aCornerLat1, aCornerLon1, aCornerLat2, aCornerLon2);
    }
    
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        //callback when a view in searchresult list is selected (Usually by moving around in the list with the arrow-keys).
        //Will center the AndroidMapObject in map.
        Landmark landmark = this.landmarks.get(position);
        this.onClick(view, landmark, true);
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void onClick(View view, Landmark landmark) {
        //callback when a view in searchresult list is clicked.
        //Will center the AndroidMapObject in map. If already selected, will open details-view
        this.onClick(view, landmark, false);
    }
    
    private void onClick(View view, Landmark landmark, boolean skipDetailedInfo) {
        AndroidMapObject mapObject = this.mappedMapObjects.get(landmark);
        PinsLayer pinslayer = this.mapView.getPinsLayer();
        pinslayer.setSelectedMapObject(mapObject, true, skipDetailedInfo);
    }

    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if(view.equals(this.landmarksListView)) {
            if(keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                int position = this.landmarksListView.getSelectedItemPosition();
                if(position >= 0) {
                    this.onClick(this.landmarksListView.getSelectedView(), this.landmarks.get(position));
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void updateActiveScreenPoint() {
        WayfinderMapView mapView = getMap();
        if(mapView != null) {
            MapLayerView mapLayer = mapView.getMapLayer();
            int menuHeight = this.resizeButton.getMenuHeight();
            resultListTop = this.resizeButton.getRootView().getHeight() - menuHeight;
            int zoomButtonsHeight = mapView.getControlsLayer().getZoomControls().getHeight();
            int centerVisibleY = (resultListTop >> 1) + LandmarkMapObject.getImageMaxHeight(this) + zoomButtonsHeight;
            int centerVisibleX = (mapLayer.getWidth() >> 1);
            VectorMapInterface map = mapLayer.getMap();
            Log.i("AbstractLandmarkListActivity", "Setting active screen[" + centerVisibleX + ", " + centerVisibleY + "]");
            map.setActiveScreenPoint(centerVisibleX, centerVisibleY);
        }
    }
    
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus){
	        MapLayerView mapLayer = this.mapView.getMapLayer();
	        mapLayer.setVisibleMapArea(this.layoutContent);
			updateActiveScreenPoint();
		}
		super.onWindowFocusChanged(hasFocus);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.options_menu_landmarks, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem cmdNavigate = menu.findItem(R.id.cmd_navigate);
        MenuItem cmdDetails = menu.findItem(R.id.cmd_details);
        MenuItem cmdAdd = menu.findItem(R.id.cmd_add);
        MenuItem cmdEdit = menu.findItem(R.id.cmd_edit);
        
        cmdNavigate.setVisible(false);
        cmdDetails.setVisible(false);
        cmdAdd.setVisible(false);
        cmdEdit.setVisible(false);

        if(this.selectedIndex >= 0 && this.selectedIndex < landmarks.size()) {
            Landmark landmark = this.landmarks.get(this.selectedIndex);
            if(landmark != null) {
                AndroidMapObject mapObject = this.mappedMapObjects.get(landmark);
                if(mapObject != null) {
                    this.setMapObject(mapObject);
                    
                    int type = mapObject.getContextMenuType();
                    if(type == ContextActivity.TYPE_PLACES) {
                        cmdNavigate.setVisible(true);
                        cmdDetails.setVisible(false);
                        cmdEdit.setVisible(true);
                    }
                    else if(type == ContextActivity.TYPE_DESTINATIONS) {
                        cmdNavigate.setVisible(true);
                        cmdDetails.setVisible(false);
                        cmdAdd.setVisible(true);
                        cmdEdit.setVisible(false);
                    }
                    else {
                        cmdNavigate.setVisible(true);
                        cmdDetails.setVisible(true);
                        cmdAdd.setVisible(true);
                    }
                    return true;
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.cmd_navigate: {
                this.onActivityResult(REQUEST_CODE_CONTEXT_MENU, ContextActivity.RESULT_NAVIGATE_TO, null);
                return true;
            }
            case R.id.cmd_details: {
                this.onActivityResult(REQUEST_CODE_CONTEXT_MENU, ContextActivity.RESULT_DISPLAY_DETAILS, null);
                return true;
            }
            case R.id.cmd_add: {
                this.onActivityResult(REQUEST_CODE_CONTEXT_MENU, ContextActivity.RESULT_SAVE_PLACE, null);
                return true;
            }
            case R.id.cmd_edit: {
                this.onActivityResult(REQUEST_CODE_CONTEXT_MENU, ContextActivity.RESULT_EDIT_PLACE, null);
                return true;
            }
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
}
