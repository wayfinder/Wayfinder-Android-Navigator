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
package com.vodafone.android.navigation.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.activity.ChooseMapObjectActivity;
import com.vodafone.android.navigation.components.AnimatedOverlay;
import com.vodafone.android.navigation.listeners.MapInterationDetectedListener;
import com.vodafone.android.navigation.listeners.MapObjectSelectedListener;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.UserMapObject;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.map.vectormap.VectorMapInterface;

public class WayfinderMapView extends FrameLayout{

	private MapLayerView iMapLayer;
	private AnimatedOverlayLayer iAnimLayer;
	private PinsLayer iPinsLayer;
	private ControlsLayerView iControlsLayer;
	private NavigatorApplication application;
    private boolean mapClicked;
    private int mapClickedX;
    private int mapClickedY;
    private Paint paint;
    private MapInterationDetectedListener mapInterationListener;
	 
	public WayfinderMapView(Context context) {
		super(context);
		this.init(context);
	}

	public WayfinderMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init(context);
	}

	public WayfinderMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init(context);
	}
	
	public MapLayerView getMapLayer() {
	    return this.iMapLayer;
	}
	
	@Override
	protected void onAttachedToWindow() {
	    super.onAttachedToWindow();
//        this.iAnimLayer.startAnimation();
	}
	
	@Override
	protected void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
//	    this.iAnimLayer.stopAnimation();
	}
	
	public PinsLayer getPinsLayer() {
	    return this.iPinsLayer;
	}
	
	public ControlsLayerView getControlsLayer() {
	    return this.iControlsLayer;
	}

	private void init(Context context) {		
	    View.inflate(context, R.layout.map, this);
	    
        Resources res = this.getResources();
	    this.paint = new Paint();
        this.paint.setColor(res.getColor(R.color.color_black));
	    this.paint.setTextSize(15);
	    this.paint.setAntiAlias(true);
	    
	    this.application = (NavigatorApplication) context.getApplicationContext();
	    
	    this.setBackgroundResource(R.color.color_map_bkg);
	    
		iMapLayer = new MapLayerView(context, this);
		iAnimLayer = new AnimatedOverlayLayer(context, this);
		iPinsLayer = new PinsLayer(context, this, application);
		iControlsLayer = new ControlsLayerView(context, this);
		
		this.addView(iMapLayer);
		this.addView(iControlsLayer, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		iControlsLayer.setLayoutParams(new FrameLayout.LayoutParams(getWidth(), getHeight()));
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setZoomInControlEnabled(boolean enabled) {
		iControlsLayer.getZoomControls().setIsZoomInEnabled(enabled);
	}

	public void setZoomOutControlEnabled(boolean enabled) {
		iControlsLayer.getZoomControls().setIsZoomOutEnabled(enabled);
	}

    public void startZoomIn() {
        iMapLayer.startZoomIn();
    }

    public void stopZoomIn() {
        iMapLayer.stopZoomIn();
    }

    public void startZoomOut() {
        iMapLayer.startZoomOut();
    }

    public void stopZoomOut() {
        iMapLayer.stopZoomOut();
    }
    
    public void showControls() {
        iControlsLayer.showControls();
    }
    
    public void hideControls(boolean hideControls) {
        iControlsLayer.hideControls(hideControls);
    }

	public void updateMap(){
		iMapLayer.updateMap();
	}

	public void drawOverlays(Canvas canvas, MapRenderer mapRenderer, MapCameraInterface mapCamera, View mapOverlay) {
        //if multiple MapObjects have been pressed, display a list with these
	    AndroidMapObject[] pressedMapObjects = this.application.getPressedMapObjects();
	    if(pressedMapObjects.length > 1) {
	    	this.application.movePressedMapObjectToActivity();
            Context context = this.getContext();
            context.startActivity(new Intent(context, ChooseMapObjectActivity.class));
	    }
	    else if(pressedMapObjects.length == 1) {
	        boolean centerMapObject = true;
	        if(pressedMapObjects[0] instanceof UserMapObject) {
	            centerMapObject = false;
	        }
	        this.iPinsLayer.setSelectedMapObject(pressedMapObjects[0], centerMapObject, false);
	    }
	    else if(pressedMapObjects.length == 0 && this.mapClicked) {
	        this.iPinsLayer.addPositionPin(this.mapClickedX, this.mapClickedY);
	    }
	    this.application.clearPressedMapObjects();
        this.mapClicked = false;
	    
        iPinsLayer.drawOverlay(canvas, mapCamera, mapRenderer);
		iAnimLayer.drawOverlay(canvas, mapCamera, mapRenderer, mapOverlay);
		iControlsLayer.drawOverlay(canvas, mapCamera, mapRenderer);
	}

    public void enablePanning(boolean enabled) {
	    this.iMapLayer.enablePanning(enabled);
	}

    public void setMyPositionOverlay(AnimatedOverlay overlay) {
        this.iAnimLayer.setMyPositionOverlay(overlay);
    }
	
    public void setMapObjectSelectedListener(MapObjectSelectedListener listener) {
    	iPinsLayer.setMapObjectSelectListener(listener);
    }
    
    public void centerMapTo(int latitude, int longitude, boolean animatedMove) {
        iMapLayer.centerMapTo(latitude, longitude, animatedMove);
    }
    
    public void centerMapTo(int latitude, int longitude, boolean animatedMove, Runnable finalTask) {
        iMapLayer.centerMapTo(latitude, longitude, animatedMove, finalTask);
    }

    public void setMapClicked(int x, int y) {
        this.mapClicked = true;
        this.mapClickedX = x;
        this.mapClickedY = y;
    }

	/**
	 * @param mapInterationListener the mapInterationListener to set
	 */
	public void setMapInterationListener(
			MapInterationDetectedListener mapInterationListener) {
		this.mapInterationListener = mapInterationListener;
	}
	
	public void mapInterationDetected(){
		if(this.mapInterationListener != null){
			mapInterationListener.mapInterationDetected();
		}
	}
	
	public boolean isNightModeOn(){
		VectorMapInterface map = application.getMapInterface();
		return map.isNightMode();
	}

    public void updateCopyrightTextPosition(int y) {
        this.iMapLayer.updateCopyrightTextPosition(y);
    }

    public void hideCopyrightText() {
        this.iMapLayer.hideCopyrightText();
    }
    
    public void showMuteImage(){
    	if(application.getMapInterface().getMapDetailedConfigInterface().getRouteID() != null){
    		iControlsLayer.showMuteImage();
    	}
    }
    
    public void hideMuteImage(){
    	this.iControlsLayer.hideMuteImage();
    }

    public void lockMapUpdates(boolean lock) {
        this.iMapLayer.lockMapUpdates(lock);
    }
}
