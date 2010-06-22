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
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;

public class ControlsLayerView extends LinearLayout {

    private WayfinderMapView iOwnerMap;
    private ZoomControls zoomControls;
    private Animation hideAnimation;
    private LinearLayout rulerControls;
    private ImageView muteImage;
    private boolean hideControls;
    private Context context;

    private Runnable showRulerRunnable = new Runnable() {
        public void run() {
            rulerControls.setVisibility(View.VISIBLE);
        }
    };

    private Runnable hideRulerRunnable = new Runnable() {
        public void run() {
            rulerControls.setVisibility(View.INVISIBLE);
        }
    };

    public ControlsLayerView(Context context, WayfinderMapView aOwnerMap) {
        super(context);
        this.init(context, aOwnerMap);
    }

    public ControlsLayerView(Context context, AttributeSet attrs,
            WayfinderMapView aOwnerMap) {
        super(context, attrs);
        this.init(context, aOwnerMap);
    }

    private void init(Context context, WayfinderMapView aOwnerMap) {
        View.inflate(context, R.layout.controls_layer, this);
        iOwnerMap = aOwnerMap;
        this.context = context;

        this.hideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        this.hideAnimation.setDuration(500);
        this.hideAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                rulerControls.setVisibility(View.INVISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        rulerControls = (LinearLayout) findViewById(R.id.layout_ruler);
        rulerControls.setVisibility(View.INVISIBLE);

        zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
        zoomControls.setOnZoomInListeners(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Handler handler = getHandler();

                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN) {
                    iOwnerMap.mapInterationDetected();
                    iOwnerMap.startZoomIn();

                    handler.removeCallbacks(hideRulerRunnable);
                    handler.post(showRulerRunnable);
                }
                else if(action == MotionEvent.ACTION_UP) {
                    iOwnerMap.stopZoomIn();
                    handler.postDelayed(hideRulerRunnable, 3000);
                }
                return false;
            }
        });

        zoomControls.setOnZoomOutListeners(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Handler handler = getHandler();

                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN) {
                    iOwnerMap.mapInterationDetected();
                    iOwnerMap.startZoomOut();

                    handler.removeCallbacks(hideRulerRunnable);
                    handler.post(showRulerRunnable);
                }
                else if(action == MotionEvent.ACTION_UP) {
                    iOwnerMap.stopZoomOut();
                    handler.postDelayed(hideRulerRunnable, 3000);
                }
                return false;
            }
        });
        
        muteImage = (ImageView) findViewById(R.id.mute_icon);
        muteImage.setVisibility(View.INVISIBLE);
    }

    public ZoomControls getZoomControls() {
        return zoomControls;
    }

    public void showControls() {
        if (!this.hideControls) {
            if (!zoomControls.isShown()) {
                zoomControls.show();
            }
        }
    }

    public void hideControls(boolean hideControls) {
        if (hideControls) {
            this.zoomControls.hide();
        }
        this.hideControls = hideControls;
    }

    public void drawOverlay(Canvas canvas, MapCameraInterface mapCamera, MapRenderer mapRenderer) {
        int width = canvas.getWidth();
        int mapY = (canvas.getHeight() >> 1);

        long[] pos = mapCamera.getWorldCoordinate(0, mapY);
        Position posLeft = new Position((int) pos[0], (int) pos[1]);
        
        pos = mapCamera.getWorldCoordinate(width, mapY);
        Position posRight = new Position((int) pos[0], (int) pos[1]);

        int distance = posLeft.distanceTo(posRight);
        NavigatorApplication app = (NavigatorApplication) this.getContext().getApplicationContext();
        FormattingResult result = app.getUnitsFormatter().formatDistance(distance);
        String text = result.getRoundedValue() + " " + result.getUnitAbbr();
        TextView textScale = (TextView) this.findViewById(R.id.text_scale);
        if(iOwnerMap.isNightModeOn()){
        	textScale.setTextAppearance(context, R.style.label_text_white_big);
        } else {
        	textScale.setTextAppearance(context, R.style.label_text_black_big);
        }
        textScale.setText(text);
    }

    public void setRulerY(int y) {
        this.rulerControls.setPadding(0, y, 0, 0);
    }
    
    public void showMuteImage(){
    	muteImage.setVisibility(View.VISIBLE);
    }
    
    public void hideMuteImage(){
    	muteImage.setVisibility(View.INVISIBLE);
    }
}
