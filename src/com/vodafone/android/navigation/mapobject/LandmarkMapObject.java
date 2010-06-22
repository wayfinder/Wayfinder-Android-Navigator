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
package com.vodafone.android.navigation.mapobject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.components.Landmark;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;

public class LandmarkMapObject extends AndroidMapObject {

    private static WFImage placeMarkerImage;
    private static Bitmap defaultBitmap;

    private Landmark landmark;

    public LandmarkMapObject(Context context, VectorMapInterface map, Landmark landmark, int contextMenuType) {
        super(context, map, landmark.getImageName(), landmark.getName(), landmark.getPosition(), 24000, contextMenuType); //Max zoom-out is 24000
        this.landmark = landmark;
    
        createPlaceMarkerImage(context);

        this.setBitmap(null, defaultBitmap);
    }
    
    public static int getImageMaxWidth(Context context) {
        createPlaceMarkerImage(context);
        return (placeMarkerImage != null ? placeMarkerImage.getWidth() - 4 : 30);
    }
    
    public static int getImageMaxHeight(Context context) {
        createPlaceMarkerImage(context);
        return (placeMarkerImage != null ? placeMarkerImage.getHeight() - 13 : 30);
    }

    private static void createPlaceMarkerImage(Context context) {
        if(defaultBitmap == null) {
            Resources resources = context.getResources();
            defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.cat_all);
        }
    }

    @Override
    public String getId() {
        return this.landmark.getId();
    }

    public Landmark getLandmark() {
        return this.landmark;
    }

    @Override
    protected void drawNormalPOI(WFGraphics g, WFImage img, int left, int top, int width, int height) {
        boolean hasValidImage = (img != null && !((Bitmap) img.getNativeImage()).isRecycled());

        this.drawPlaceMarker(g, left, top, width + 3, height + 4 + this.getArrowHeight());
        if(hasValidImage) {
            g.drawImage(img, left + 2, top + 2, WFGraphics.ANCHOR_LEFT | WFGraphics.ANCHOR_TOP);
        }
    }

    @Override
	public String getDesc() {
		return landmark.getDescription();
	}

	@Override
	public long getPlaceDBId() {
		return landmark.getPlaceDBId();
	}
}
