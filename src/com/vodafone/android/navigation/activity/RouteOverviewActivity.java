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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.components.BitmapOverviewOverlay;
import com.vodafone.android.navigation.util.ResourceUtil;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.wayfinder.core.map.vectormap.MapDetailedConfigInterface;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.shared.BoundingBox;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.RouteSettings;
import com.wayfinder.core.shared.route.Waypoint;
import com.wayfinder.core.shared.util.UnitsFormatter;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;

public class RouteOverviewActivity extends AbstractRouteActivity {

    public static final String KEY_AUTOMATIC_ROUTING = "key_automatic_routing";

	private WayfinderMapView mapView;
    private TextView textTime;
    private TextView textDist;
    private Handler handler = new Handler();
    private boolean automaticRouting;
    private Position startPos;
    private BitmapOverviewOverlay overlay;
    private LinearLayout layoutTimeDist;
    private ImageView muteImage;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.route_overview_activity);
        
        Intent intent = this.getIntent();
        this.automaticRouting = intent.getBooleanExtra(KEY_AUTOMATIC_ROUTING, true);
        
        this.mapView = (WayfinderMapView) this.findViewById(R.id.map);
        this.textTime = (TextView) this.findViewById(R.id.text_time);
        this.textDist = (TextView) this.findViewById(R.id.text_distance);
        
        this.layoutTimeDist = (LinearLayout) this.findViewById(R.id.include_route_time_dist);
        if(getApp().isSIMCardAbsent()){
        	this.displayNoSIMWarning();
        } else {
            this.displaySafetyWarning();
        }
        muteImage = (ImageView) findViewById(R.id.route_mute_icon);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus) {
            this.mapView.getControlsLayer().setRulerY(this.layoutTimeDist.getHeight());
        }
        else {
            this.mapView.getControlsLayer().setRulerY(0);
        }
        
        super.onWindowFocusChanged(hasFocus);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.options_menu_route_overview, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        VectorMapInterface map = this.mapView.getMapLayer().getMap();

        MenuItem cmdRoute = menu.findItem(R.id.cmd_route);
        MenuItem cmdNightMode = menu.findItem(R.id.cmd_night_mode);
        MenuItem cmdDayMode = menu.findItem(R.id.cmd_day_mode);

        MenuItem cmdPlay = menu.findItem(R.id.cmd_play_route);
        cmdPlay.setVisible(NavigatorApplication.DEBUG_ENABLED);
        
        if(this.automaticRouting) {
            cmdRoute.setEnabled(true);
            cmdRoute.setVisible(true);
        }
        else {
            cmdRoute.setEnabled(false);
            cmdRoute.setVisible(false);
        }
        
        if(map.isNightMode()) {
            cmdNightMode.setVisible(false);
            cmdNightMode.setEnabled(false);
            cmdDayMode.setVisible(true);
            cmdDayMode.setEnabled(true);
        }
        else {
            cmdNightMode.setVisible(true);
            cmdNightMode.setEnabled(true);
            cmdDayMode.setVisible(false);
            cmdDayMode.setEnabled(false);
        }        
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.cmd_route: {
                this.startRouteActivity();
                return true;
            }
            case R.id.cmd_stop_route: {
                this.stopRoute();
                return true;
            }
            case R.id.cmd_night_mode: {
                setNightMode(this, true);
                return true;
            }
            case R.id.cmd_day_mode: {
                setNightMode(this, false);
                return true;
            }
            case R.id.cmd_play_route: {
                this.getApp().getCore().getRouteInterface().simulate();
                return true;
            }
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected WayfinderMapView getMapView() {
        return this.mapView;
    }

    protected void setupMap(boolean activate) {
        if(activate) {
            LocationInformation loc = this.getApp().getOwnLocationInformation();
            this.shouldDisplayWaitingForPositionDialog(loc);

            VectorMapInterface map = this.mapView.getMapLayer().getMap();
            MapDetailedConfigInterface mapConfig = map.getMapDetailedConfigInterface();

            this.mapView.enablePanning(false);
            this.startPos = null;

            Bitmap bitmap = null;
            int transportMode = this.getTransportMode();
            if(transportMode == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.navigation_pedestrian);
                bitmap = ResourceUtil.scale(bitmap, 30, 30);
            }
            else if(transportMode == RouteSettings.TRANSPORT_MODE_CAR) {
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.navigation_car);
                bitmap = ResourceUtil.scale(bitmap, 30, 30);
            }

            Route route = this.getRoute();
            String routeID = route.getRouteID();
            if(!routeID.equals(mapConfig.getRouteID())) {
                Log.i("RouteOverviewActivity", "Adding route");
                mapConfig.setRoute(route);
            }
            
            map.set3DMode(false);
            map.setFollowGpsPosition(false);
            map.setRotation(ANGLE_NORTH);
            
            int x = this.getX();
            int y = this.getY();
            map.setActiveScreenPoint(x, y);

            BoundingBox routeBox = route.getBoundingBox();
            int minLatitude = routeBox.getMinLatitude();
            int minLongitude = routeBox.getMinLongitude();
            int maxLatitude = routeBox.getMaxLatitude();
            int maxLongitude = routeBox.getMaxLongitude();
            
            int deltaLat = (int) ((float) Math.abs(minLatitude - maxLatitude) * 0.1f);
            int deltaLon = (int) ((float) Math.abs(minLongitude - maxLongitude) * 0.1f);
            
            minLatitude -= deltaLat;
            maxLatitude += deltaLat;
            
            minLongitude -= deltaLon;
            maxLongitude += deltaLon;
            
            map.setWorldBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
            
            int lat = minLatitude + ((maxLatitude - minLatitude) >> 1);
            int lon = minLongitude + ((maxLongitude - minLongitude) >> 1);
            Position position = new Position(lat, lon);
            this.mapView.getMapLayer().enableRubberBandEffect(position);

            this.overlay = new BitmapOverviewOverlay(bitmap);
            this.mapView.setMyPositionOverlay(this.overlay);
        
            Position pos = route.getActualOrigin();
            this.overlay.updateRotation(0);
            this.overlay.updatePosition(pos);
        }
        else {
            this.mapView.getMapLayer().disableRubberBandEffect();
            this.mapView.enablePanning(true);
        }
    }

    private int getX() {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int width = defaultDisplay.getWidth();
        int x = (width >> 1);
        return x;
    }

    private int getY() {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int height = defaultDisplay.getHeight();
        int y = (height >> 1);
        return y;
    }

    private void startRouteActivity() {
        this.startActivity(new Intent(this, RouteActivity.class));
        this.finish();
    }

    protected void internalNavigationInfoUpdated(final NavigationInfo info) {
        LocationInformation ownLoc = this.getApp().getOwnLocationInformation();
        this.shouldDisplayWaitingForPositionDialog(ownLoc);
        
        VectorMapInterface map = this.mapView.getMapLayer().getMap();

        Position pos = null;
        if(info != null) {
            pos = info.getSnappedPosition();
        }
        
        if(pos == null) {
            Log.i("RouteOverviewActivity", "pos from route is null");
            pos = ownLoc.getMC2Position(); 
        }

        if(this.automaticRouting) {
            if(this.startPos == null) {
                this.startPos = pos;
            }
            else {
                if(this.startPos.distanceTo(pos) > 25) {
                    this.startRouteActivity();
                }
            }
        }

        if(this.overlay != null && info != null) {
            int course = info.getSnappedCourseDeg();
            this.overlay.updateRotation(course);
            this.overlay.updatePosition(pos);
        }
        
        if(info != null) {
            map.getMapDetailedConfigInterface().setNavigationInfo(info);
        }
        map.setGpsPosition(pos.getMc2Latitude(), pos.getMc2Longitude(), 0);
        
        this.handler.post(new Runnable() {
            public void run() {
                String timeStr;
                String distStr;
                if(info != null) {
                    int time = (!info.isFollowing() ? 0: info.getTimeSecondsTotalRemaining()); //seconds
                    int dist = (!info.isFollowing() ? 0: info.getDistanceMetersTotalRemaining()); //meters
                    UnitsFormatter formatter = getApp().getUnitsFormatter();
                    timeStr = formatter.formatTimeWithUnitStrings(time, false);
                    FormattingResult result = formatter.formatDistance(dist);
                    distStr = result.getRoundedValue() + " " + result.getUnitAbbr();
                }
                else {
                    timeStr = "-";
                    distStr = "-";
                }
                
                textTime.setText(timeStr); 
                textTime.invalidate();
                
                textDist.setText(distStr);
                textDist.invalidate();
            }
        });
    }

	@Override
	protected void hideMuteImage() {
		muteImage.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void showMuteImage() {
		muteImage.setVisibility(View.VISIBLE);
	}

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractRouteActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Route route = this.getRoute();
		if(route != null){
			Waypoint point = route.getFirstTurnWpt();
			int time = point.getTimeSecondsToEnd(); //seconds
	        int dist = point.getDistanceMetersToEnd(); //meters
	        UnitsFormatter formatter = getApp().getUnitsFormatter();
	        String timeStr = formatter.formatTimeWithUnitStrings(time, false);
	        FormattingResult result = formatter.formatDistance(dist);
	        String distStr = result.getRoundedValue() + " " + result.getUnitAbbr();
	        textTime.setText(timeStr); 
	        textTime.invalidate();
	        
	        textDist.setText(distStr);
	        textDist.invalidate();
		}
	}
	
	
}	
