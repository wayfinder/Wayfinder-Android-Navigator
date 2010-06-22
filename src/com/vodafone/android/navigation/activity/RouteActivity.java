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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.components.BitmapOverlay;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.util.ResourceUtil;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.wayfinder.core.map.vectormap.MapDetailedConfigInterface;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.RouteSettings;
import com.wayfinder.core.shared.route.Turn;
import com.wayfinder.core.shared.route.Waypoint;
import com.wayfinder.core.shared.util.UnitsFormatter;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;

public class RouteActivity extends AbstractRouteActivity implements SensorEventListener {

    private static final boolean SPEED_SIGN_ENABLED = (NavigatorApplication.DEBUG_ENABLED && false);
    private static final boolean USERS_SPEED_ENABLED = (NavigatorApplication.DEBUG_ENABLED && false);
    private static final int ONTRACK_COUNTER = 5;
    
    private WayfinderMapView mapView;
    private TextView textTotalTime;
    private TextView textTotalDist;
    private TextView textTurnDist;
    private TextView textNextStreet;
    private TextView textCurrStreet;
    private ImageView imgGuide;
    private TextView textSpeed;
    private ImageView muteImage;
    private FrameLayout layoutSpeed;

    private Handler handler = new Handler();

    private BitmapOverlay overlay;
    private Bitmap pictogram;
    private Waypoint oldWpt;
    private boolean isOfftrackLoaded;
    private boolean isFinishLoaded;
    private NavigationInfo info;
    private boolean isSpeedCamLoaded;
    private int onTrackCounter;
	private TextView textPictogram;
	private TextView textPictogramShadow;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.route_activity);
        
        this.mapView = (WayfinderMapView)findViewById(R.id.map);
        this.textTotalTime = (TextView) this.findViewById(R.id.text_time);
        this.textTotalDist = (TextView) this.findViewById(R.id.text_distance);
        this.textTurnDist = (TextView) this.findViewById(R.id.text_dist_to_next_turn);
        this.textNextStreet = (TextView) this.findViewById(R.id.text_next_street_name);
        this.textCurrStreet = (TextView) this.findViewById(R.id.text_current_street);
        this.imgGuide = (ImageView) this.findViewById(R.id.img_guide);
        this.layoutSpeed = (FrameLayout) this.findViewById(R.id.layout_speed);
        this.textSpeed = (TextView) this.findViewById(R.id.text_speed);
        this.textPictogram = (TextView) this.findViewById(R.id.text_pictogram);
        this.textPictogramShadow = (TextView) this.findViewById(R.id.text_pictogram_shadow);
        this.muteImage = (ImageView) findViewById(R.id.route_mute_icon);
        
        
        this.onTrackCounter = ONTRACK_COUNTER;
        if(!SPEED_SIGN_ENABLED) {
            this.layoutSpeed.setVisibility(View.GONE);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.options_menu_route, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        VectorMapInterface map = this.mapView.getMapLayer().getMap();

        MenuItem cmd3d = menu.findItem(R.id.cmd_3d_mode);
        MenuItem cmd2d = menu.findItem(R.id.cmd_2d_mode);
        MenuItem cmdNightMode = menu.findItem(R.id.cmd_night_mode);
        MenuItem cmdDayMode = menu.findItem(R.id.cmd_day_mode);

        if(this.getTransportMode() == RouteSettings.TRANSPORT_MODE_CAR) {
            if(map.isIn3DMode()) {
                cmd3d.setVisible(false);
                cmd3d.setEnabled(false);
                cmd2d.setVisible(true);
                cmd2d.setEnabled(true);
            }
            else {
                cmd3d.setVisible(true);
                cmd3d.setEnabled(true);
                cmd2d.setVisible(false);
                cmd2d.setEnabled(false);
            }
        }
        else {
            cmd3d.setVisible(false);
            cmd3d.setEnabled(false);
            cmd2d.setVisible(false);
            cmd2d.setEnabled(false);
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
        VectorMapInterface map = this.mapView.getMapLayer().getMap();

        int id = item.getItemId();
        int transportMode = this.getTransportMode();
        ApplicationSettings settings = ApplicationSettings.get();
        switch(id) {
            case R.id.cmd_overview: {
                this.startRouteOverviewActivity();
                return true;
            }
            case R.id.cmd_stop_route: {
                this.stopRoute();
                return true;
            }
            case R.id.cmd_2d_mode: {
                this.overlay.setBitmap(ResourceUtil.scale(this.overlay.getBitmap(), 40, 40));

                map.set3DMode(false);
                settings.set3DModePrefferred(false);
                settings.commit();
                int x = this.getX();
                int y = this.getY(transportMode);
                map.setActiveScreenPoint(x, y);
                return true;
            }
            case R.id.cmd_3d_mode: {
                if(transportMode == RouteSettings.TRANSPORT_MODE_CAR) {
                    this.overlay.setBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.navigation_car));
                }
                else if(transportMode == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
                    this.overlay.setBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.navigation_pedestrian));
                }
                
                map.set3DMode(true);
                settings.set3DModePrefferred(true);
                settings.commit();
                int x = this.getX();
                int y = this.getY(RouteSettings.TRANSPORT_MODE_CAR);
                if(!map.setActiveScreenPoint(x, y)) {
                    Log.e("RouteActivity", "setupMap() invalid coords[" + x + ", " + y + "]");
                }
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
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //TODO: This breaks the straight backward flow in the application.
            //If user calculates a route from search-results he is transported to 
            //routeoverview and then route. Pressing back, the user should end up 
            //in searchresult, but the code is hardcoded to transport user to 
            //searchactivity. We can�t remove this code since if user click on 
            //the widget when there�s a route, is transported to routeactivity 
            //and then presses back, he ends up in the homescreen, and there is 
            //no easy way for hte user to get back to the searchactivity.
            //What needs to be implemented is the following flow: User presses 
            //widget (when there�s a route), ends up in splash->searchactivity->routeactivity. 
            //This way when the user presses back, the searchactivity is in the stack. 
            //Until this is done, the current behaviour stays. // 091016
            Intent intent = new Intent(this, SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected WayfinderMapView getMapView() {
        return this.mapView;
    }
    
    private void startRouteOverviewActivity() {
        Intent intent = new Intent(this, RouteOverviewActivity.class);
        intent.putExtra(RouteOverviewActivity.KEY_AUTOMATIC_ROUTING, false);
        this.startActivity(intent);
    }

    protected void setupMap(boolean activate) {
        VectorMapInterface map = this.mapView.getMapLayer().getMap();

        if(activate) {
            LocationInformation loc = this.getApp().getOwnLocationInformation();
            this.shouldDisplayWaitingForPositionDialog(loc);
            
            MapDetailedConfigInterface mapConfig = map.getMapDetailedConfigInterface();

            this.mapView.enablePanning(false);
            
            Bitmap bitmap = null;
            boolean use3dMode = true;
            int transportMode = this.getTransportMode();
            if(transportMode == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
                use3dMode = false;
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.navigation_pedestrian);
                bitmap = ResourceUtil.scale(bitmap, 40, 40);
            }
            else if(transportMode == RouteSettings.TRANSPORT_MODE_CAR) {
                use3dMode = ApplicationSettings.get().get3DModePrefferred();
                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.navigation_car);
                if(!use3dMode) {
                    bitmap = ResourceUtil.scale(bitmap, 40, 40);
                }
                else {
                    bitmap = ResourceUtil.scale(bitmap, 60, 60);
                }
            }
            
            Route route = this.getRoute();
            String routeID = route.getRouteID();
            if(!routeID.equals(mapConfig.getRouteID())) {
                Log.i("RouteActivity", "Adding route");
                mapConfig.setRoute(route);
            }
            map.set3DMode(use3dMode);
            map.setFollowGpsPosition(true);

            this.overlay = new BitmapOverlay(bitmap);
            this.mapView.setMyPositionOverlay(this.overlay);

            int x = this.getX();
            int y = this.getY(transportMode);
            map.setActiveScreenPoint(x, y);
            
            Position pos = loc.getMC2Position();
            map.setCenter(pos.getMc2Latitude(), pos.getMc2Longitude());

            this.addSensorListener(this);
        }
        else {
            map.set3DMode(false);
            map.setFollowGpsPosition(false);
            this.mapView.enablePanning(true);
            this.removeSensorListener(this);
        }
    }

    protected void internalNavigationInfoUpdated(NavigationInfo info) {
        LocationInformation locationInfo = this.getApp().getOwnLocationInformation();
        this.shouldDisplayWaitingForPositionDialog(locationInfo);

        VectorMapInterface map = this.mapView.getMapLayer().getMap();

        this.info = info;

        //if we had an offtrack and then get ontrack we wait 5 updates until we use the navigation-info.
        //This is done in case the new route tells us to turn around immediately we won�t be stuck in the snapped position.
        this.onTrackCounter ++;
        if(this.onTrackCounter <= ONTRACK_COUNTER) {
            Log.i("RouteActivity", "onTrackCounter <= " + ONTRACK_COUNTER);
            return;
        }
        
        if(this.info == null || this.info.isOfftrack()) {
            this.onTrackCounter = 0;
            
            Log.i("RouteActivity", "is null or OFFTRACK");
            return;
        }

        this.handler.post(new Runnable() {
            public void run() {
                updateOverlayInfo(RouteActivity.this.info);
            }
        });

        if(info.getSpeed() > 4) {
            this.getApp().setUseSensorForRoute(false);
        }
        
        
//        int course = info.getSnappedCourseDeg();
//        Position pos = info.getSnappedPosition();
        int course = info.getFakedCourseDeg();
        Position pos = info.getFakedPosition();
        
        int transportMode = this.getTransportMode();
        if(transportMode == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
            //read course from the built in compass. If no such exists, set the map to be north oriented; angle = 0
            course = this.getAngle();
            if(info != null) {
                pos = info.getEvaluatedPosition();
                Log.i("RouteActivity", "Evaluated Pos[" + pos.getMc2Latitude() + ", " + pos.getMc2Longitude() + "]");
            }
        }
        else if(this.getApp().getUseSensorForRoute()) {
            course = this.getAngle();
        }

        if(pos == null) {
            Log.i("RouteActivity", "pos from route is null");
            course = locationInfo.getCourse();
            pos = locationInfo.getMC2Position(); 
        }

        Log.i("RouteActivity", "Pos[" + pos.getMc2Latitude() + ", " + pos.getMc2Longitude() + "]");

        int x = getX();
        int y = getY(transportMode);
        map.setActiveScreenPoint(x, y);
        map.setFollowGpsPosition(true);
        if(info != null) {
            map.getMapDetailedConfigInterface().setNavigationInfo(info);
        }
        map.setGpsPosition(pos.getMc2Latitude(), pos.getMc2Longitude(), course);
        if(this.overlay != null) {
            this.overlay.updateRotation(0);
        }
    }

    @Override
    public void locationUpdate(LocationInformation locationInformation, LocationProvider locationProvider) {
        super.locationUpdate(locationInformation, locationProvider);
        
        Route route = this.getRoute();
        if(route == null || this.info == null || this.info.isOfftrack() || this.onTrackCounter <= ONTRACK_COUNTER) {
            Log.i("RouteActivity", "locationUpdate() using gps-position");
            int course = locationInformation.getCourse();
            Position pos = locationInformation.getMC2Position();

            VectorMapInterface map = this.mapView.getMapLayer().getMap();
            map.setGpsPosition(pos.getMc2Latitude(), pos.getMc2Longitude(), course);
            
            this.updateOverlayInfo(this.info);
        }
    }
    
    private int getY(int transportMode) {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int height = defaultDisplay.getHeight();
        int y = (height * 3) >> 2;
        if(transportMode == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
            y = (height << 1) / 3;
            Log.i("RouteActivity", "getY() Pedestrian: " + y);
        }
        else {
            Log.i("RouteActivity", "getY() Car: " + y);
        }
        
        y -= (this.overlay.getBitmap().getHeight() >> 1);
        
        return y;
    }

    private int getX() {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int width = defaultDisplay.getWidth();
        int x = (width >> 1);
        return x;
    }

    private void updateOverlayInfo(NavigationInfo info) {
        String timeTotalStr;
        String distTotalStr;
        String distTurnStr;
        String streetName;
        String velocity;
        Waypoint nextWpt;

        if(info != null) {
            UnitsFormatter formatter = this.getApp().getUnitsFormatter();
            int timeTotal = (!info.isFollowing() ? 0 : info.getTimeSecondsTotalRemaining());
            timeTotalStr = formatter.formatTimeWithUnitStrings(timeTotal, false);
            int distTotal = (!info.isFollowing() ? 0 : info.getDistanceMetersTotalRemaining());
            FormattingResult result = formatter.formatDistance(distTotal);
            distTotalStr = result.getRoundedValue() + " " + result.getUnitAbbr();
            int distTurn = (!info.isFollowing() ? 0 : info.getDistanceMetersToNextWpt());
            result = formatter.formatDistance(distTurn);
            distTurnStr = result.getRoundedValue() + " " + result.getUnitAbbr();
            nextWpt = info.getNextWpt();
            
            if(info.isSpeedCameraActive()) {
                streetName = this.getResources().getString(R.string.qtn_andr_speed_cam_ahead_txt);
            }
            else {
                streetName = info.getStreetName();
            }
            
            if(USERS_SPEED_ENABLED) {
                result = formatter.formatSpeedMPS(info.getSpeed());
            }
            else {
                result = formatter.formatSpeedMPS(info.getSpeedLimitKmh() / 3.6f);
            }
            velocity = result.getRoundedValue();
        }
        else {
            timeTotalStr = "-";
            distTotalStr = "-";
            distTurnStr = "-";
            nextWpt = null;
            streetName = "-";
            velocity = "??";
        }

        textTotalTime.setText(timeTotalStr); 
        textTotalTime.invalidate();
        
        textTotalDist.setText(distTotalStr);
        textTotalDist.invalidate();
        
        textTurnDist.setText(distTurnStr);
        textTurnDist.invalidate();
        
        if(nextWpt != null) {
            textNextStreet.setText(nextWpt.getRoadNameAfter());
            textNextStreet.invalidate();
        }
        else {
            textNextStreet.setText("");
            textNextStreet.invalidate();
        }

        textCurrStreet.setText(streetName);
        textCurrStreet.invalidate();
        
        Bitmap guideImage = getBitmap(info);
        imgGuide.setImageBitmap(guideImage);
        imgGuide.invalidate();
        
        if(nextWpt != null && nextWpt.getExitCount() > 0 && nextWpt.getTurn() == Turn.EXIT_ROUNDABOUT && !info.isSpeedCameraActive()){
        	textPictogram.setText(String.valueOf(nextWpt.getExitCount()));
        	textPictogramShadow.setText(String.valueOf(nextWpt.getExitCount()));
        }
        else{
        	textPictogram.setText("");
        	textPictogramShadow.setText("");
        }
        
        int transportMode = this.getTransportMode();
        if(transportMode == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
            layoutSpeed.setVisibility(View.GONE);
        }
        else {
            if(SPEED_SIGN_ENABLED) {
                layoutSpeed.setVisibility(View.VISIBLE);
            }
            else {
                layoutSpeed.setVisibility(View.GONE);
            }
            textSpeed.setText(velocity);
            textSpeed.invalidate();
        }
    }

    private Bitmap getBitmap(NavigationInfo info) {
        Waypoint nextWpt = null;
        if(info != null) {
            nextWpt = info.getNextWpt();
        }

        if(this.getRoute() == null) {
            Log.i("RouteActivity", "navigationInfoUpdated() route is null");
            this.pictogram = null;
            this.oldWpt = null;
        }
        else if(info == null || info.isOfftrack()) {
            if(!this.isOfftrackLoaded) {
                Log.i("RouteActivity", "navigationInfoUpdated() loading offtrack");
                this.pictogram = BitmapFactory.decodeResource(this.getResources(), R.drawable.offtrack);
                this.isOfftrackLoaded = true;
                this.isFinishLoaded = false;
                this.isSpeedCamLoaded = false;
            }
        }
        else if(!info.isFollowing() || nextWpt == null) {
            if(!this.isFinishLoaded) {
                Log.i("RouteActivity", "navigationInfoUpdated() loading finish_flag");
                this.pictogram = BitmapFactory.decodeResource(this.getResources(), R.drawable.finish_flag);
                this.isFinishLoaded = true;
                this.isOfftrackLoaded = false;
                this.isSpeedCamLoaded = false;
            }
        }
        else if(info.isSpeedCameraActive()) {
            if(!this.isSpeedCamLoaded) {
                Log.i("RouteActivity", "navigationInfoUpdated() loading speed_cam");
                this.pictogram = BitmapFactory.decodeResource(this.getResources(), R.drawable.speed_cam);
                this.isSpeedCamLoaded = true;
                this.isFinishLoaded = false;
                this.isOfftrackLoaded = false;
            }
        }
        else {
            if(this.isOfftrackLoaded || this.isFinishLoaded || this.isSpeedCamLoaded) {
                this.oldWpt = null;
                this.pictogram = null;
                this.isFinishLoaded = false;
                this.isOfftrackLoaded = false;
                this.isSpeedCamLoaded = false;
            }
            
            if(this.pictogram == null || this.oldWpt == null || !this.oldWpt.equals(nextWpt)) {
                if(nextWpt != null) {
                    Turn turn = nextWpt.getTurn();
                    if(turn != null) {
                        Log.i("RouteActivity", "navigationInfoUpdated() new waypoint");
                        this.pictogram = ResourceUtil.getDrawable(this, "p" + turn.getId());
                        this.oldWpt = nextWpt;
                    }
                }
            }
        }

        return this.pictogram;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if(this.getTransportMode() == RouteSettings.TRANSPORT_MODE_PEDESTRIAN || this.getApp().getUseSensorForRoute()) {
            int angle = (int) event.values[0];
            
            int deltaAngle = this.getAngle() - angle;
            if(Math.abs(deltaAngle) > MIN_DELTA_ANGLE) {
                this.setAngle(angle);
                VectorMapInterface map = this.mapView.getMapLayer().getMap();
                Position pos = map.getActivePosition();
                map.setGpsPosition(pos.getMc2Latitude(), pos.getMc2Longitude(), angle);
                Log.i("RouteActivity", "onSensorChanged() new angle: " + angle);
            }
        }
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
			Waypoint point = (this.oldWpt != null ? this.oldWpt : route.getFirstTurnWpt());
			int time = point.getTimeSecondsToEnd(); //seconds
	        int dist = point.getDistanceMetersToEnd(); //meters
	        UnitsFormatter formatter = getApp().getUnitsFormatter();
	        String timeStr = formatter.formatTimeWithUnitStrings(time, false);
	        FormattingResult result = formatter.formatDistance(dist);
	        String distStr = result.getRoundedValue() + " " + result.getUnitAbbr();
	        textTotalTime.setText(timeStr); 
	        textTotalTime.invalidate();
	        
	        textTotalDist.setText(distStr);
	        textTotalDist.invalidate();
	        
	        Waypoint nextPoint = point.getNext();
	        if(nextPoint != null){
		        int distToNextTurn = nextPoint.getDistanceMetersFromPrev(); //meters
		        result = formatter.formatDistance(distToNextTurn);
		        String distTurnStr = result.getRoundedValue() + " " + result.getUnitAbbr();
		        textTurnDist.setText(distTurnStr);
		        textTurnDist.invalidate();
	        }
	        
	        
	        textNextStreet.setText(point.getRoadNameAfter());
	        textCurrStreet.setText("");
	        this.pictogram = ResourceUtil.getDrawable(this, "p" + point.getTurn().getId());
	        imgGuide.setImageBitmap(pictogram);
	        
	        if(point.getExitCount() > 0 && point.getTurn() == Turn.EXIT_ROUNDABOUT && (info != null && !info.isSpeedCameraActive())) {
	            String exitCount = String.valueOf(point.getExitCount());
                textPictogram.setText(exitCount);
	            textPictogramShadow.setText(exitCount);
	        }
	        else{
	            textPictogram.setText("");
	            textPictogramShadow.setText("");
	        }
		}
	}
}	
