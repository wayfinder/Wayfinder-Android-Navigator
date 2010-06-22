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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.Toast;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.listeners.MapObjectSelectedListener;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.UserMapObject;
import com.vodafone.android.navigation.util.ResourceUtil;
import com.wayfinder.core.geocoding.GeocodeListener;
import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.map.MapObjectImage;
import com.wayfinder.core.map.MapObjectListener;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.geocoding.AddressInfo;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;


public class PinsLayer implements MapObjectListener {
    private static final String TAG = "PinsLayer";
    
    private WayfinderMapView mapView;
    private volatile AndroidMapObject selectedMapObject;
    private Context context;
    private MapObjectSelectedListener selectListener;
    private MapCameraInterface mapCamera;
    private NavigatorApplication application;
    private MapObjectImage pinMapObjectImage;
    private RequestID requestId;
    private boolean addingPositionPinEnabled = false;
    private NavigatorApplication app;

	public PinsLayer(Context context, WayfinderMapView mapView, NavigatorApplication app) {
		this.app = app;
	    this.context = context;
		this.mapView = mapView;
		this.application = (NavigatorApplication) this.context.getApplicationContext();

        Bitmap bitmap = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.pin);
        WFImage wfImage = this.application.getAndroidFactory().createWFImage(bitmap);
        this.pinMapObjectImage = new MapObjectImage(wfImage, WFGraphics.ANCHOR_HCENTER | WFGraphics.ANCHOR_BOTTOM);
	}

    protected void drawOverlay(Canvas canvas, MapCameraInterface mapCamera, MapRenderer mapRenderer) {
        this.mapCamera = mapCamera;
    }

    public boolean mapObjectPressed(MapObject mapObject) {
        Log.i(TAG, "mapObjectPressed: " + mapObject);

        if(mapObject instanceof AndroidMapObject) {
            if(mapObject.equals(this.selectedMapObject)) {
                this.application.clearPressedMapObjects();
                this.application.addPressedMapObject((AndroidMapObject) mapObject);
                return true;
            }
            else {
                this.application.addPressedMapObject((AndroidMapObject) mapObject);
            }
        }
        
        return false;
    }

    public boolean mapObjectSelected(MapObject mapObject) {
        if(mapObject.equals(this.selectedMapObject)) {
//            Log.i(TAG, "mapObjectSelected: " + mapObject);
            return true;
        }
		return false;
    }

    public void mapObjectUnSelected(MapObject mapObject) {
        Log.i(TAG, "mapObjectUnSelected: " + mapObject);
    }

    public boolean poiObjectPressed(String poiName, Position position) {
        Log.i(TAG, "poiObjectPressed: " + poiName);
        return false;
    }

    public void poiSelected(String poiName, Position position)  {
        Log.i(TAG, "poiSelected: " + poiName);
    }

    public void poiUnSelected(String poiName, Position position) {
        Log.i(TAG, "poiUnSelected: " + poiName);
    }
    
    public void setMapObjectSelectListener(MapObjectSelectedListener listener) {
        this.selectListener = listener;
    }

    public void setSelectedMapObject(AndroidMapObject mapObject, boolean centerMapObject, boolean skipContextMenu) {
        if(mapObject == null 
                || !mapObject.equals(this.selectedMapObject) 
                || (centerMapObject && !this.isCentered(mapObject))) {
            if(this.selectedMapObject != null) {
                this.selectedMapObject.setAsSelected(false);
            }
            this.selectedMapObject = mapObject;
            if(this.selectedMapObject != null) {
                this.selectedMapObject.setAsSelected(true);
            }
            this.mapView.getMapLayer().getMap().setSelectedMapObject(this.selectedMapObject);

            if(this.selectListener != null) {
                this.selectListener.onSelect(this.selectedMapObject, centerMapObject, false);
            }
        }
        else if(!skipContextMenu) {
            //second press on this mapObject, display context-menu
            if(this.selectListener != null) {
                this.selectListener.onSelect(this.selectedMapObject, centerMapObject, true);
            }
        }
    }

    public void setPositionPinEnabled(boolean enabled) {
        this.addingPositionPinEnabled = enabled;
    }
    
    public void addPositionPin(int mapClickedX, int mapClickedY) {
        if(this.addingPositionPinEnabled) {
            long[] coords = this.mapCamera.getWorldCoordinate(mapClickedX, mapClickedY);
            Position position = new Position((int) coords[0], (int) coords[1]);
            
            VectorMapInterface map = this.mapView.getMapLayer().getMap();
            UserMapObject oldPin = app.getUserPin();
            if(oldPin != null) {
                //remove old user-pins
                map.removeMapObject(oldPin);
            }
            
            final UserMapObject pin = new UserMapObject(this.context, map, position);
            app.setUserPin(pin);
            map.addMapObject(pin, this.pinMapObjectImage);
            if(this.selectListener != null) {
                this.setSelectedMapObject(pin, false, true);
            }
            
            this.requestId = this.application.getCore().getGeocodeInterface().reverseGeocode(position, new GeocodeListener() {
                public void reverseGeocodeDone(RequestID requestID, AddressInfo addressInfo) {
                    if(requestID.equals(requestId)) {
                        String address = ResourceUtil.getAddressAsString(addressInfo, true);
                        Toast.makeText(application, address, Toast.LENGTH_LONG).show();
                        pin.updateTitle(address.replace('\n', ' '));
                    }
                }
    
                public void error(RequestID requestID, CoreError error) {
                    Log.e("LocalSearchActivity.GeocodeListener", "error() " + error.getInternalMsg());
                }
            });
        }
    }

    private boolean isCentered(MapObject mapObject) {
        if(this.mapCamera != null){
            Position position = this.mapView.getMapLayer().getMap().getActivePosition();
            int[] coords = this.mapCamera.getScreenCoordinate(position.getMc2Latitude(), position.getMc2Longitude());
            int activeX = coords[0];
            int activeY = coords[1];
            
            coords = this.mapCamera.getScreenCoordinate(mapObject.getLatitude(), mapObject.getLongitude());
            int objectX = coords[0];
            int objectY = coords[1];
    
            int dx = objectX - activeX;
            int dy = objectY - activeY;
            double distance = Math.sqrt((dx * dx) + (dy * dy));
            return (distance <= 20);
        }
        return false;
    }
}
