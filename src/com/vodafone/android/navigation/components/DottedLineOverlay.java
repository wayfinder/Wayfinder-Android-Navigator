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

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.view.View;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.shared.Position;

public class DottedLineOverlay implements AnimatedOverlay {
	

	private float destLong = 664700000;
	private float destLat = 157000000;
	
    private NavigatorApplication application;
	private WayfinderMapView iOwnerMap;

	private float[] intervals1 = { 10, 10 };
	private int dottedLineFrameNumber;
	private DashPathEffect[] effects = { new DashPathEffect(intervals1, 20f),
			new DashPathEffect(intervals1, 17.5f),
			new DashPathEffect(intervals1, 15f),
			new DashPathEffect(intervals1, 12.5f),
			new DashPathEffect(intervals1, 10f),
			new DashPathEffect(intervals1, 7.5f),
			new DashPathEffect(intervals1, 5f), 
    };
	
	public DottedLineOverlay(WayfinderMapView ownerMap) {
		iOwnerMap = ownerMap;
		this.application = (NavigatorApplication) this.iOwnerMap.getContext().getApplicationContext();
	}
	
	public void draw(Canvas canvas, MapCameraInterface mapCamera, MapRenderer mapRenderer, View mapOverlay) {
		Position position = this.application.getOwnLocationInformation().getMC2Position();
        System.out.println(position.getMc2Latitude() + "," + position.getMc2Longitude());
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(0xFFAAAAAA);
		p.setStrokeWidth(4);
		p.setStrokeCap(Cap.ROUND);
		p.setPathEffect(effects[dottedLineFrameNumber]);
		int[] startCoords = mapCamera.getScreenCoordinate(
		        position.getMc2Latitude(), 
		        position.getMc2Longitude());
		
		int[] destCoords = mapCamera.getScreenCoordinate((int)destLat, (int)destLong);
		if(isOnScreen(startCoords[0], startCoords[1]) && isOnScreen(destCoords[0], destCoords[1])){
			canvas.drawLine(startCoords[0], startCoords[1], destCoords[0], destCoords[1], p);
		} else {

			int y1 = (int) ((float) (destCoords[1] - destCoords[0])
					* (float) (0 - startCoords[0])
					/ (float) (startCoords[1] - startCoords[0]) + destCoords[0]);
			int y2 = (int) ((float) (destCoords[1] - destCoords[0])
					* (float) (iOwnerMap.getWidth() - startCoords[0])
					/ (float) (startCoords[1] - startCoords[0]) + destCoords[0]);
			int x1 = (int) ((float) (startCoords[1] - startCoords[0])
					* (float) (0 - destCoords[0])
					/ (float) (destCoords[1] - destCoords[0]) + startCoords[0]);
			int x2 = (int) ((float) (startCoords[1] - startCoords[0])
					* (float) (iOwnerMap.getHeight() - destCoords[0])
					/ (float) (destCoords[1] - destCoords[0]) + startCoords[0]);

			int[][] lineCoords = new int[4][2];
			int cursor = 0;
			if (isOnScreen(0, y1)) {
				lineCoords[cursor][0] = 0;
				lineCoords[cursor][1] = y1;
				cursor++;
			}

			if (isOnScreen(iOwnerMap.getWidth(), y2)) {
				lineCoords[cursor][0] = iOwnerMap.getWidth();
				lineCoords[cursor][1] = y2;
				cursor++;
			}

			if (isOnScreen(x1, 0)) {
				lineCoords[cursor][0] = x1;
				lineCoords[cursor][1] = 0;
				cursor++;
			}

			if (isOnScreen(x2, iOwnerMap.getHeight())) {
				lineCoords[cursor][0] = x2;
				lineCoords[cursor][1] = iOwnerMap.getHeight();
				cursor++;
			}

			if (cursor >= 2) {
				canvas.drawLine(lineCoords[0][0], lineCoords[0][1],
						lineCoords[1][0], lineCoords[1][1], p);
				System.out.println(lineCoords[0][0] + ","
						+ lineCoords[0][1] + "," + lineCoords[1][0] + ","
						+ lineCoords[1][1]);
			}
		}
	}

	private boolean isOnScreen(int x, int y) {
		return x >= 0 && y >=0 && x <= iOwnerMap.getWidth() && y <= iOwnerMap.getHeight();
	}

	public void tick() {
		dottedLineFrameNumber++;
		dottedLineFrameNumber %= 7;
	}
}
