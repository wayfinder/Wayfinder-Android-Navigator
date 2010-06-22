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
package com.vodafone.android.navigation.components;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.view.View;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.activity.AbstractActivity;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;

public class OwnPositionOverlay implements AnimatedOverlay {
	
	private WayfinderMapView iOwnerMap;
    private Bitmap ownPositionPinEnabled1;
    private Bitmap ownPositionPinEnabled2;
	private Bitmap ownPositionPinDisabled;
	private Bitmap arrow;
    private Bitmap rotatedArrow;
	private boolean on = true;
    private NavigatorApplication application;
    private Paint radiusPaint;
    private Paint borderPaint;
    private int oldAngle;
    private int overlayHeight;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int textSize;

	public OwnPositionOverlay(WayfinderMapView ownerMap) {
		iOwnerMap = ownerMap;
		this.application = (NavigatorApplication) this.iOwnerMap.getContext().getApplicationContext();
		
        this.radiusPaint = new Paint();
        this.radiusPaint.setAntiAlias(true);
        this.borderPaint = new Paint();
        this.borderPaint.setStrokeWidth(1);
        this.borderPaint.setAntiAlias(true);
        this.borderPaint.setStyle(Style.STROKE);
        this.textSize = this.application.getResources().getDimensionPixelSize(R.dimen.distance_to_myposition_text_size);
        this.borderPaint.setTextSize(this.textSize);
		
		Resources resources = iOwnerMap.getContext().getResources();
        ownPositionPinEnabled1 = BitmapFactory.decodeResource(resources, R.drawable.position_1);
        ownPositionPinEnabled2 = BitmapFactory.decodeResource(resources, R.drawable.position_2);
		ownPositionPinDisabled = BitmapFactory.decodeResource(resources, R.drawable.position_disabled);
		arrow = BitmapFactory.decodeResource(resources, R.drawable.arrow);
	}
	
	public void draw(Canvas canvas, MapCameraInterface mapCamera, MapRenderer mapRenderer, View mapOverlay) {
	    if(canvas == null) {
	        return;
	    }
	    
		LocationInformation ownLocation = this.application.getOwnLocationInformation();
		LocationProvider provider = this.application.getLocationProvider();
		
		int[] ownPositionCoords = mapCamera.getScreenCoordinate((int) ownLocation.getMC2Position().getMc2Latitude(), (int) ownLocation.getMC2Position().getMc2Longitude());
		if (provider != null) {
            int origX = ownPositionCoords[0];
            int origY = ownPositionCoords[1];
            int x = origX;
            int y = origY;
            
            boolean isOnScreen = true;
            int arrowSize = this.arrow.getWidth() >> 1;
            
            int coords[] = {0,0};
            if(mapOverlay != null) {
                mapOverlay.getLocationOnScreen(coords);
            }
            int top = coords[1];
            
            iOwnerMap.getLocationOnScreen(coords);
            top -= coords[1];
            
            int canvasHeight = canvas.getHeight();
			this.overlayHeight = (mapOverlay != null ? canvasHeight - top : 0);
            this.minX = arrowSize;
            this.maxX = canvas.getWidth() - arrowSize;
            this.minY = arrowSize;
            this.maxY = canvasHeight - this.overlayHeight - arrowSize;
            
            if(x < this.minX){
                x = this.minX;
                isOnScreen = false;
            }
            
            if(x > this.maxX){
                x = this.maxX;
                isOnScreen = false;
            }
            
            if(y < this.minY){
                y = this.minY;
                isOnScreen = false;
            }

            if(y > this.maxY){
                y = this.maxY;
                isOnScreen = false;
            }
            
            draw(canvas, mapCamera, ownLocation, provider, origX, origY, x, y, isOnScreen);
		}
	}

    private void draw(Canvas canvas, MapCameraInterface mapCamera, LocationInformation ownLocation, LocationProvider provider, int origX, int origY, int x, int y, boolean isOnScreen) {
    	int radius = ownLocation.getAccuracy();
        if(radius <= AbstractActivity.ACCURACY_MAX) {
        	if(provider.getState() == LocationProvider.PROVIDER_STATE_AVAILABLE) {
        	    if(this.on) {
        	        drawMarker(canvas, ownPositionPinEnabled1, origX, origY);
        	    }
        	    else {
                    drawMarker(canvas, ownPositionPinEnabled2, origX, origY);
        	    }
        	} else {
        		drawMarker(canvas, ownPositionPinDisabled, origX, origY);
        	}
        } 
        else {
        	float screenRadius = (float) radius / (float) this.application.getMapInterface().getScale();
        	Resources resources = application.getResources();
        	float minRadious = resources.getDimensionPixelSize(R.dimen.location_circle_radius); 

        	if (screenRadius < minRadious) {
        		screenRadius = minRadious;
			} 

        	int radiusColor = 0x4C0099DA;
            int borderColor = 0xAA0099DA;
            if(provider.getState() != LocationProvider.PROVIDER_STATE_AVAILABLE) {
                radiusColor = 0x4C999999;
                borderColor = 0xAA999999;
            }
            this.radiusPaint.setColor(radiusColor);
            this.borderPaint.setColor(borderColor);
            
            canvas.drawCircle(origX, origY, screenRadius, this.radiusPaint);
        	canvas.drawCircle(origX, origY, screenRadius, this.borderPaint);
        }
        
        if(!isOnScreen) {
            int arrowSize = this.arrow.getWidth();
            int textX = x;
            int textY = y;
            Align align = Paint.Align.LEFT;
            int angle = 0;
            if(origX >= this.minX && origX <= this.maxX) {
                if(origY <= this.minY) {
                    //outside the top of screen
                    angle = 0;
                    align = Paint.Align.CENTER;
                    textY += arrowSize;
                }
                else if(origY >= this.maxY) {
                    //outside the bottom of screen
                    angle = 180;
                    align = Paint.Align.CENTER;
                    textY -= arrowSize;
                }
                else {
                    //onscreen, shouldn't happen here
                }
            }
            else if(origY >= this.minY && origY <= this.maxY) {
                if(origX <= this.minX) {
                    //outside the left of screen
                    angle = 270;
                    align = Paint.Align.LEFT;
                    textX += arrowSize;
                }
                else if(origX >= this.maxX) {
                    //outside the right of screen
                    angle = 90;
                    align = Paint.Align.RIGHT;
                    textX -= arrowSize;
                }
                else {
                    //onscreen, shouldn't happen here
                }
            }
            else {
                //both x and y is outside of screen�s boundaries
                if(origX >= this.maxX && origY <= this.minY) {
                    //top, right
                    angle = 45;
                    align = Paint.Align.RIGHT;
                    textX -= arrowSize;
                    textY += arrowSize;
                }
                else if(origX >= this.maxX && origY >= this.maxY) {
                    //bottom, right
                    angle = 135;
                    align = Paint.Align.RIGHT;
                    textX -= arrowSize;
                    textY -= arrowSize;
                }
                else if(origX <= this.minX && origY <= this.minY) {
                    //top, left
                    angle = 315;
                    align = Paint.Align.LEFT;
                    textX += arrowSize;
                    textY += arrowSize;
                }
                else if(origX <= this.minX && origY >= this.maxY) {
                    //bottom, left
                    angle = 225;
                    align = Paint.Align.LEFT;
                    textX += arrowSize;
                    textY -= arrowSize;
                }
                else {
                    //onscreen, shouldn't happen here
                }
            }
            
            long[] worldCoords = mapCamera.getWorldCoordinate(x, y);
            Position position = new Position((int) worldCoords[0], (int) worldCoords[1]);
            int d = position.distanceTo(ownLocation.getMC2Position());
            FormattingResult result = this.application.getUnitsFormatter().formatDistance(d);
            String distance = result.getRoundedValue() + " " + result.getUnitAbbr();
            
            this.updateRotation(angle);
            canvas.drawBitmap(this.rotatedArrow, x - (this.rotatedArrow.getWidth() >> 1), y - (this.rotatedArrow.getHeight() >> 1), this.borderPaint);
            
            this.borderPaint.setTextAlign(align);

            this.borderPaint.setColor(0xFFFFFFFF);
            for(int i = 0; i < 9; i ++) {
                if(i != 4) {
                    canvas.drawText(distance, textX - (i %3) + 1, textY - ((int) this.borderPaint.ascent() >> 1) - (i / 3) + 1, this.borderPaint);
                }
            }
            this.borderPaint.setColor(0xFF000000);
            canvas.drawText(distance, textX, textY - ((int) this.borderPaint.ascent() >> 1), this.borderPaint);
        }
    }

	public void tick() {
		this.on = !this.on;
	}
	
	private void drawMarker(Canvas c, Bitmap b, int x, int y){
		c.drawBitmap(b,	x - (b.getWidth() >> 1), y - (b.getHeight() >> 1), null);
	}

    private void updateRotation(int angle) {
        if(this.oldAngle != angle || this.rotatedArrow == null) {
            this.rotateBitmap(angle);
            this.oldAngle = angle;
        }
    }

    private void rotateBitmap(int angle) {
        Matrix m = new Matrix();
        int width = this.arrow.getWidth();
        int height = this.arrow.getHeight();
        m.postRotate(angle, (width >> 1), (height >> 1));
        this.rotatedArrow = Bitmap.createBitmap(this.arrow, 0, 0, width, height, m, true);
    }
}
