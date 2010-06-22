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

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.components.AnimatedOverlay;
import com.vodafone.android.navigation.components.OwnPositionOverlay;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;

public class AnimatedOverlayLayer extends LinearLayout {

	private WayfinderMapView map;

	private TimerTask animationTimerTask;
	private Timer timer;
	
	private OwnPositionOverlay positionOverlay;
    private AnimatedOverlay bitmapOverlay;

	public AnimatedOverlayLayer(Context context, WayfinderMapView aOwnerMap) {
	    super(context);
		this.init(context, aOwnerMap);
	}

	private void init(Context context, WayfinderMapView map) {
        View.inflate(context, R.layout.animated_layer, this);
		this.map = map;
		this.positionOverlay = new OwnPositionOverlay(this.map);
	}

	protected void drawOverlay(Canvas canvas, MapCameraInterface mapCamera, MapRenderer mapRenderer, View mapOverlay) {
	    if(this.bitmapOverlay != null) {
	        this.bitmapOverlay.draw(canvas, mapCamera, mapRenderer, mapOverlay);
	    }
	    else {
	        this.positionOverlay.draw(canvas, mapCamera, mapRenderer, mapOverlay);
	    }
	}

	public void startAnimation() {
		if(this.animationTimerTask == null) {
	        Log.i("AnimatedOverlayLayer", "startAnimation()");
	        final Handler handler = new Handler();
    		this.animationTimerTask = new TimerTask() {
    			public void run() {
    				positionOverlay.tick();
    				handler.post(new Runnable(){
    					public void run() {
    						map.updateMap();
    					}
    				});
    			}
    		};
    
    		if(this.timer == null) {
    		    this.timer = new Timer("AnimatedOverlayLayer-Timer");
    		}
    		this.timer.schedule(this.animationTimerTask, 1000, 1000);
		}
	}
	
	public void stopAnimation() {
	    if(this.animationTimerTask != null) {
	        Log.i("AnimatedOverlayLayer", "stopAnimation()");
	        this.animationTimerTask.cancel();
	        this.animationTimerTask = null;
	    }
	}

    public void setMyPositionOverlay(AnimatedOverlay myPositionOverlay) {
        this.bitmapOverlay = myPositionOverlay;
    }
}
