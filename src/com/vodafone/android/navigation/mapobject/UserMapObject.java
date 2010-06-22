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
import android.util.Log;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.activity.ContextActivity;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.shared.Position;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;

public class UserMapObject extends AndroidMapObject {

    private static Bitmap defaultBitmap;
    private OnTitleUpdateListener onTitleUpdateListener;

    public UserMapObject(Context context, VectorMapInterface map, Position position) {
        super(context, map, ImageDownloader.IMAGE_NAME_EMPTY, "", position, 24000, ContextActivity.TYPE_MAP); //Max zoom-out is 24000
        Resources resources = context.getResources();
        if(defaultBitmap == null) {
            defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.pin);
        }
        this.setBitmap(null, defaultBitmap);
    }
    
    @Override
    public String getId() {
//        String serverString = this.getApplication().getMapInterface().getMapDetailedConfigInterface().getServerString();
    	String serverString = "C:"+getLatitude()+":"+getLongitude() + ":0:";
        Log.i("UserMapObject", "getId() serverString: " + serverString);
        return serverString;
    }
    
    @Override
    public String getDesc() {
        return "";
    }
    
    @Override
    protected void drawNormalPOI(WFGraphics g, WFImage img, int left, int top, int width, int height) {
        if(img != null && !((Bitmap) img.getNativeImage()).isRecycled()) {
            g.drawImage(img, left + (width >> 1), top + (height >> 1), WFGraphics.ANCHOR_HCENTER | WFGraphics.ANCHOR_VCENTER);
        }
        else {
            defaultBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.pin);
            img = this.setBitmap(null, defaultBitmap);
            g.drawImage(img, left + (width >> 1), top + (height >> 1), WFGraphics.ANCHOR_HCENTER | WFGraphics.ANCHOR_VCENTER);
        }
    }
    
    @Override
    protected boolean drawInflatedWhenSelected() {
        return false;
    }
    
    @Override
    public void updateTitle(String title) {
        super.updateTitle(title);
        if(this.onTitleUpdateListener != null) {
            this.onTitleUpdateListener.onUpdateTitle(title);
        }
    }
    
    public void setOnTitleUpdateListener(OnTitleUpdateListener onTitleUpdateListener) {
        this.onTitleUpdateListener = onTitleUpdateListener;
    }

    public interface OnTitleUpdateListener {
        void onUpdateTitle(String title);
    }
}
