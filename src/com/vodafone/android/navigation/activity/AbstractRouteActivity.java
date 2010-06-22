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

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.wayfinder.core.map.vectormap.MapDetailedConfigInterface;
import com.wayfinder.core.map.vectormap.PoiCategory;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.NavigationInfoListener;
import com.wayfinder.core.shared.route.Route;

public abstract class AbstractRouteActivity extends AbstractActivity implements NavigationInfoListener {

    public static final int MIN_DELTA_ANGLE = 20;
    public static final int ANGLE_NORTH = 0;
    
    private int angle = ANGLE_NORTH;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        Log.i(this.toString(), "onResume()");

        NavigatorApplication app = this.getApp();
        app.setFollowRoute(true);

        WayfinderMapView mapView = this.getMapView();
        mapView.hideControls(true);

        int backlight = ApplicationSettings.get().getBacklight();
        if(backlight == ApplicationSettings.BACKLIGHT_ALWAYS_ON || backlight == ApplicationSettings.BACKLIGHT_ON_ROUTE) {
            mapView.setKeepScreenOn(true);
        }
        
        VectorMapInterface map = mapView.getMapLayer().getMap();
        
        Route route = app.getRoute();
        if(route == null) {
            //There's no route, and we're not waiting for one either. Let's exit from here
            this.finish();
            Log.e("AbstractRouteActivity", "No route available, exiting from " + this);
            return;
        }
        else {
        	map.removeAllMapObjects();
            this.setupMap(true);
        }

        app.getCore().getRouteInterface().addNavigationInfoListener(this);

        MapDetailedConfigInterface mapConfig = map.getMapDetailedConfigInterface();
        PoiCategory[] poiCats = mapConfig.getPoiCategories();
        ApplicationSettings.get().setupPoiCategories(poiCats);
        mapConfig.setPoiCategories(poiCats);
        adjustMuteIcon();
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        Log.i(this.toString(), "onPause()");
        
        NavigatorApplication app = this.getApp();
        app.getCore().getRouteInterface().removeNavigationInfoListener(this);
        app.setFollowRoute(false);

        int backlight = ApplicationSettings.get().getBacklight();
        if(backlight != ApplicationSettings.BACKLIGHT_ALWAYS_ON && backlight != ApplicationSettings.BACKLIGHT_ON_ROUTE) {
            this.getMapView().setKeepScreenOn(false);
        }
        
        this.setupMap(false);
        
        WayfinderMapView mapView = this.getMapView();
        VectorMapInterface map = mapView.getMapLayer().getMap();
        MapDetailedConfigInterface mapConfig = map.getMapDetailedConfigInterface();
        PoiCategory[] poiCats = mapConfig.getPoiCategories();
        if(poiCats != null) {
            for(PoiCategory poiCat: poiCats) {
                poiCat.setEnable(true);
            }
            mapConfig.setPoiCategories(poiCats);
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            //hide the copyright-string when routing
            WayfinderMapView mapView = this.getMapView();
            mapView.hideCopyrightText();
        }
    }

    protected Route getRoute() {
        return this.getApp().getRoute();
    }
    
    protected int getTransportMode() {
        return this.getApp().getRouteTransportMode();
    }
    
    protected void stopRoute() {
        super.stopRoute();

        this.setupMap(false);
        this.finish();
    }

    protected void addSensorListener(SensorEventListener listener) {
        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if(sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    protected void removeSensorListener(SensorEventListener listener) {
        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(listener);
        this.angle = ANGLE_NORTH;
    }
    
    protected void setAngle(int angle) {
        this.angle = angle;
    }
    
    protected int getAngle() {
        return this.angle;
    }

    public void locationUpdate(LocationInformation locationInformation, LocationProvider locationProvider) {
        super.locationUpdate(locationInformation, locationProvider);
        this.shouldDisplayWaitingForPositionDialog(locationInformation);
    }
    
    public void routeDone(RequestID requestID, Route route) {
        try {
            this.dismissDialog(DIALOG_ROUTING);
        } catch (IllegalArgumentException e) {
        }

        VectorMapInterface map = this.getMapView().getMapLayer().getMap();
        map.getMapDetailedConfigInterface().setRoute(route);
        this.getApp().setRoute(route);
        Log.i("AbstractRouteActivity", "routeDone() new route available and set in map");
    }
    
    @Override
    protected void onCancelWaitingForPositionDialog() {
        super.onCancelWaitingForPositionDialog();
        stopRoute();
    }
    
    public void navigationInfoUpdated(NavigationInfo info) {
        this.internalNavigationInfoUpdated(info);
    }

    protected abstract void setupMap(boolean activate);
    protected abstract WayfinderMapView getMapView();
    protected abstract void internalNavigationInfoUpdated(NavigationInfo info);
    protected abstract void showMuteImage();
    protected abstract void hideMuteImage();
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			adjustMuteIcon();
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void adjustMuteIcon(){
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if(getApp().getRoute() != null && (volume == 0 || getApp().isSoundMuted())){
			showMuteImage();
		} else {
			hideMuteImage();
		}
	}
	
	public void handleSoundMuteStateChanged(){
		adjustMuteIcon();
	}
	
}
