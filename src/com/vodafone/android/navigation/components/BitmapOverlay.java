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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;

public class BitmapOverlay implements AnimatedOverlay {
	
    private Bitmap origBitmap;
    private Bitmap bitmap;
    private Paint paint;
    private int oldAngle = 0;
    private int[] coords;

	public BitmapOverlay(Bitmap bitmap) {
	    this.setBitmap(bitmap);
        this.paint = new Paint();
	}
	
	public Bitmap getBitmap() {
	    return this.origBitmap;
	}
	
	public void setBitmap(Bitmap bitmap) {
	    this.origBitmap = bitmap;
	    this.rotateBitmap(this.oldAngle);
	}
	
	public void draw(Canvas canvas, MapCameraInterface mapCamera, MapRenderer mapRenderer, View mapOverlay) {
	    if(canvas == null) {
	        return;
	    }
	    
        if(canvas != null && mapRenderer != null && this.bitmap != null) {
            this.coords = this.getScreenCoords(mapCamera, mapRenderer, this.coords);
            canvas.drawBitmap(
                    this.bitmap, 
                    this.coords[0] - (this.bitmap.getWidth() >> 1), 
                    this.coords[1] - (this.bitmap.getHeight() >> 1), 
                    this.paint);
        }
	}

    protected int[] getScreenCoords(MapCameraInterface mapCamera, MapRenderer mapRenderer, int[] coords) {
        if(coords == null) {
            coords = new int[2];
        }
        
        coords[0] = mapRenderer.getActiveScreenPointX();
        coords[1] = mapRenderer.getActiveScreenPointY();
        
        return coords;
    }

	public void tick() {
	}

    public void updateRotation(int angle) {
        if(this.oldAngle != angle || this.bitmap == null) {
            this.rotateBitmap(angle);
            this.oldAngle = angle;
        }
    }

    private void rotateBitmap(int angle) {
        Matrix m = new Matrix();
        int width = this.origBitmap.getWidth();
        int height = this.origBitmap.getHeight();
        m.postRotate(angle, (width >> 1), (height >> 1));
        this.bitmap = Bitmap.createBitmap(this.origBitmap, 0, 0, width, height, m, true);
    }
}
