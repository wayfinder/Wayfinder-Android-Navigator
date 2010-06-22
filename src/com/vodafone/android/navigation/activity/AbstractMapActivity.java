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

import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.vodafone.android.navigation.listeners.MapObjectSelectedListener;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.shared.Position;

public abstract class AbstractMapActivity extends AbstractActivity implements MapObjectSelectedListener {

    private static final Timer timer = new Timer("AbstractMapActivity-Timer");

    private TimerTask updateTimerTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        WayfinderMapView map = this.getMap();
        if(map != null) {
            map.setMapObjectSelectedListener(this);
            map.hideControls(false);
            int backlight = ApplicationSettings.get().getBacklight();
            if(backlight == ApplicationSettings.BACKLIGHT_ALWAYS_ON) {
            	map.setKeepScreenOn(true);
            }
        }

        adjustMuteIcon();
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        WayfinderMapView map = this.getMap();
        map.setMapObjectSelectedListener(null);
        int backlight = ApplicationSettings.get().getBacklight();
        if(backlight != ApplicationSettings.BACKLIGHT_ALWAYS_ON) {
            map.setKeepScreenOn(false);
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            //hide the copyright-string when routing
            WayfinderMapView mapView = this.getMap();
            mapView.hideCopyrightText();
        }
    }
    
    protected abstract WayfinderMapView getMap();
    
    public void onSelect(AndroidMapObject mapObject, boolean centerMapObject, boolean displayContextMenu) {
        if(centerMapObject) {
            this.centerMapObject(mapObject, displayContextMenu);
        }
        if(displayContextMenu) {
            this.displayContextMenu(mapObject);
        }
    }

    protected void setupMap() {
        WayfinderMapView mapView = this.getMap();
        if(mapView != null) {
            mapView.enablePanning(true);
            mapView.setMyPositionOverlay(null);

            VectorMapInterface map = mapView.getMapLayer().getMap();
            if(map != null) {
                int width = mapView.getWidth();
                int height = mapView.getHeight();
                int x = (width >> 1);
                int y = (height >> 1);
                map.setActiveScreenPoint(x, y);
        
                map.setRotation(AbstractRouteActivity.ANGLE_NORTH);
                map.set3DMode(false);
        
                LocationInformation ownLocationInformation = this.getApp().getOwnLocationInformation();
                if(ownLocationInformation != null) {
                    Position pos = ownLocationInformation.getMC2Position();
                    map.setGpsPosition(pos.getMc2Latitude(), pos.getMc2Longitude(), AbstractRouteActivity.ANGLE_NORTH);
                }
            }
            else {
                Log.e("AbstractMapActivity", "setupMap() map is null");
            }
        }
        else {
            Log.e("AbstractMapActivity", "setupMap() mapView is null");
        }
    }

    protected void centerMapObject(final AndroidMapObject mapObject, boolean updateWithoutWait) {
        if(!updateWithoutWait) {
            if(this.updateTimerTask != null) {
                this.updateTimerTask.cancel();
            }
            this.updateTimerTask = new TimerTask() {
                public void run() {
                    centerMapObject(mapObject);
                    updateTimerTask = null;
                }
            };
            timer.schedule(this.updateTimerTask, 700);
        }
        else {
            this.centerMapObject(mapObject);
        }
    }

    private void centerMapObject(AndroidMapObject mapObject) {
        this.getMap().getMapLayer().centerMapTo(mapObject.getLatitude(), mapObject.getLongitude(), false);
    }

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
			this.getMap().showMuteImage();
		} else {
			this.getMap().hideMuteImage();
		}
	}
	
	public void handleSoundMuteStateChanged(){
		adjustMuteIcon();
	}

}
