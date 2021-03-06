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

import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.shared.Position;

public class BitmapOverviewOverlay extends BitmapOverlay {
	
	private Position position;

    public BitmapOverviewOverlay(Bitmap bitmap) {
	    super(bitmap);
	}

	public void updatePosition(Position position) {
	    this.position = position;
	}
	
	@Override
	protected int[] getScreenCoords(MapCameraInterface mapCamera, MapRenderer mapRenderer, int[] coords) {
        if(coords == null) {
            coords = new int[2];
        }
        
        if(this.position != null) {
            int[] screenCoords = mapCamera.getScreenCoordinate(this.position.getMc2Latitude(), this.position.getMc2Longitude());
            coords[0] = screenCoords[0];
            coords[1] = screenCoords[1];
        }
        else {
            coords = super.getScreenCoords(mapCamera, mapRenderer, coords);
        }
        
        return coords;
	}
}
