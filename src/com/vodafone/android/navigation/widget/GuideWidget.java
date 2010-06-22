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
package com.vodafone.android.navigation.widget;

import java.util.Timer;
import java.util.TimerTask;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.activity.AbstractActivity;
import com.vodafone.android.navigation.activity.CancelRouteActivity;
import com.vodafone.android.navigation.activity.SplashActivity;
import com.vodafone.android.navigation.service.RouteService;
import com.vodafone.android.navigation.util.ResourceUtil;
import com.wayfinder.core.geocoding.GeocodeListener;
import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationListener;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.geocoding.AddressInfo;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;

public class GuideWidget extends AppWidgetProvider implements GeocodeListener {

    private static final String KEY_REFRESH = "key_refresh";
    
    //static declaration needed since a new widget-object is created each time the widget updates
    private static Timer timer;
    private static TimerTask timerTask;
    private static LocationInformation loc;
    private static LocationListener locationListener;
    private static Criteria criteria;
    private static String streetAddress;
    private static RequestID requestId;
    
    private RemoteViews remoteViews;
    private ComponentName thisWidget;
    private AppWidgetManager manager;
    private NavigatorApplication app;

    public GuideWidget() {
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.updateWidget(context, null);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        this.updateWidget(context, intent);
    }

    private void updateWidget(Context context, Intent intent) {
        if(this.remoteViews == null) {
            this.remoteViews = new RemoteViews(context.getPackageName(), R.layout.guide_widget);
        }
        
        this.app = (NavigatorApplication) context.getApplicationContext();
        
        //Set action to launch the SplashActivity when clicking the Widget
        boolean hasRoute = this.app.getRoute() != null;
        if(hasRoute) {
            Intent topRightIntent = new Intent(context, CancelRouteActivity.class);
            topRightIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent topRightPendingIntent = PendingIntent.getActivity(context, R.id.layout_top_right, topRightIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            this.remoteViews.setOnClickPendingIntent(R.id.layout_top_right, topRightPendingIntent);
            
            Intent contentIntent = new Intent(context, SplashActivity.class); 
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentIntent.putExtra(SplashActivity.KEY_TARGET_ACTIVITY, SplashActivity.TARGET_ROUTE);
            PendingIntent contentPendingIntent = PendingIntent.getActivity(context, R.id.layout_route_content, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            this.remoteViews.setOnClickPendingIntent(R.id.layout_route_content, contentPendingIntent);
        }
        else {
            Intent topRightIntent = new Intent(RouteService.ROUTE_EVENT);
            topRightIntent.putExtra(KEY_REFRESH, true);
            PendingIntent topRightPendingIntent = PendingIntent.getBroadcast(context, R.id.layout_top_right, topRightIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            this.remoteViews.setOnClickPendingIntent(R.id.layout_top_right, topRightPendingIntent);

            Intent contentIntent = new Intent(context, SplashActivity.class); 
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentPendingIntent = PendingIntent.getActivity(context, R.id.layout_position_content, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            this.remoteViews.setOnClickPendingIntent(R.id.layout_position_content, contentPendingIntent);
        }

        if(intent != null) {
            if(hasRoute) {
                Log.i("GuideWidget", "updateWidget() Has route");
                String nextStreetName = intent.getStringExtra(RouteService.KEY_NEXT_STREET);
                int nextTurnDistance = intent.getIntExtra(RouteService.KEY_NEXT_TURN_DISTANCE, -1);
                int guideImageId = intent.getIntExtra(RouteService.KEY_GUIDE_IMAGE_ID, -1);
                int exitCount =  intent.getIntExtra(RouteService.KEY_EXIT_COUNT, -1);
                this.updateRouteData(context, this.remoteViews, nextStreetName, nextTurnDistance, guideImageId, exitCount);
            }
            else {
                Log.i("GuideWidget", "updateWidget() No route!");
                boolean routeRemoved = intent.getBooleanExtra(RouteService.KEY_ROUTE_REMOVED, false);
                boolean refresh = intent.getBooleanExtra(KEY_REFRESH, false);
                if(!refresh) {
                    //refresh if either refresh-flag is set to true, or ir route has recently been removed
                    refresh = routeRemoved;
                }
                Log.i("GuideWidget", "updateWidget() refresh: " + refresh);
                this.updatePositionData(context, this.remoteViews, refresh);
            }
        }
        
        // Push update for this widget to the home screen
        this.thisWidget = new ComponentName(context, GuideWidget.class);
        this.manager = AppWidgetManager.getInstance(context);
        this.pushUpdate();
    }

    private void pushUpdate() {
        if(this.manager != null && this.thisWidget != null && this.remoteViews != null) {
            this.manager.updateAppWidget(this.thisWidget, this.remoteViews);
        }
    }

    private void updateRouteData(Context context, RemoteViews remoteViews, String nextStreetName, int nextTurnDistance, int guideImageId, int exitCount) {
        remoteViews.setViewVisibility(R.id.layout_route_content, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.layout_position_content, View.GONE);
        
        remoteViews.setImageViewResource(R.id.image_top_right, R.drawable.button_stop);

        if(guideImageId != -1) {
            remoteViews.setImageViewResource(R.id.img_guide, guideImageId);
            remoteViews.setViewVisibility(R.id.img_guide, View.VISIBLE);
        }
        else {
            remoteViews.setViewVisibility(R.id.img_guide, View.INVISIBLE);
        }
        
        if(nextTurnDistance >= 0) {
            NavigatorApplication app = (NavigatorApplication) context.getApplicationContext();
            FormattingResult result = app.getUnitsFormatter().formatDistance(nextTurnDistance);
            String distance = result.getRoundedValue() + " " + result.getUnitAbbr();
            remoteViews.setTextViewText(R.id.text_dist_to_next_turn, distance);
        }
        else {
            remoteViews.setTextViewText(R.id.text_dist_to_next_turn, "");
        }
        
        if(exitCount > 0){
        	remoteViews.setTextViewText(R.id.text_pictogram, ""+exitCount);
        	remoteViews.setTextViewText(R.id.text_pictogram_shadow, ""+exitCount);
        }
        else{
        	remoteViews.setTextViewText(R.id.text_pictogram, "");
        	remoteViews.setTextViewText(R.id.text_pictogram_shadow, "");
        }
    }

    private void updatePositionData(Context context, RemoteViews remoteViews, boolean refresh) {
        Log.i("GuideWidget", "updatePositionData() Updating position data...");

        remoteViews.setViewVisibility(R.id.layout_route_content, View.GONE);
        remoteViews.setViewVisibility(R.id.layout_position_content, View.VISIBLE);
        
        remoteViews.setImageViewResource(R.id.image_top_right, R.drawable.button_refresh);
        
        Resources res = context.getResources();
        String currentPosition = null;

        if(loc == null) {
            loc = this.app.getOwnLocationInformation();
        }
        
        int accuracy = Criteria.ACCURACY_NONE;
        if(loc != null) {
            accuracy = loc.getAccuracy();
        }
        
        if(refresh) {
            streetAddress = null;
        }
        
        if(!refresh && accuracy <= AbstractActivity.ACCURACY_MAX) { //good enough position
            if(streetAddress != null) {
                currentPosition = res.getString(R.string.qtn_andr_u_r_close_txt, "\n" + streetAddress);
            }
            shutdownLocation(this.app);
        }
        else {
            if(refresh) {
                currentPosition = res.getString(R.string.qtn_andr_loading_txt);
            }
            else if(streetAddress != null && loc != null) {
                FormattingResult result = this.app.getUnitsFormatter().formatDistance(accuracy);
                String distance = result.getRoundedValue() + " " + result.getUnit();
                if(currentPosition == null) {
                    if(accuracy <= Criteria.ACCURACY_GOOD) {
                        currentPosition = res.getString(R.string.qtn_andr_u_r_close_txt, "\n" + streetAddress);
                    }
                    else {
                        currentPosition = res.getString(R.string.qtn_andr_u_r_close_dist_txt, "\n" + streetAddress, distance);
                    }
                }
            }
            
            //Start GPS and make updates when you get better positions. 
            //After the 60 seconds close the gps and use what you have.
            if(refresh) {
                if(locationListener == null) {
                    locationListener = new WidgetLocationListener();
                }
                this.app.getCore().getLocationInterface().addLocationListener(getCriteria(), locationListener);
                this.app.pausePositioning(false);
                
                if(timer == null) {
                    timer = new Timer("GuideWidget-timer");
                }
                
                if(timerTask == null) {
                    timerTask = new TimerTask() {
                        public void run() {
                            shutdownLocation(app);
                        }
                    };
                    timer.schedule(timerTask, 60000);
                }
            }
        }
        
        if(currentPosition != null) {
            Log.i("GuideWidget", "updatePositionData() current position: " + currentPosition);
            remoteViews.setTextViewText(R.id.text_position, currentPosition);
        }
    }

    private static void shutdownLocation(NavigatorApplication app) {
        if(timerTask != null) {
            timerTask.cancel();
        }
        timerTask = null;
        if(!AbstractActivity.isApplicationVisible()) {
            app.pausePositioning(true);
            app.getCore().getLocationInterface().removeLocationListener(locationListener);
        }
    }

    private static void updateWidget(NavigatorApplication app) {
        Intent updateIntent = new Intent(RouteService.ROUTE_EVENT);
        updateIntent.putExtra(KEY_REFRESH, false);
        app.sendBroadcast(updateIntent);
    }

    private static Criteria getCriteria() {
        if(criteria == null) {
            criteria = new Criteria.Builder()
                .accuracy(Criteria.ACCURACY_NONE)
                .costAllowed()
                .build();
        }
        return criteria;
    }

    public void reverseGeocodeDone(RequestID requestID, AddressInfo addressInfo) {
        String address = ResourceUtil.getAddressAsString(addressInfo, false);
        Log.i("GuideWidget", "reverseGeocodeDone() new reverse geocoding received: " + address);
        if(requestID.equals(GuideWidget.requestId)) {
            streetAddress = address;
            updateWidget(this.app);
        }
        else {
            Log.i("GuideWidget", "reverseGeocodeDone() old request, discarding this one and awaits the newer one");
        }
    }

    public void error(RequestID requestID, CoreError error) {
        Log.e("GuideWidget", "error() " + error.getInternalMsg());
    }

    private void requestReverseGeocode() {
        if(loc != null) {
            Log.i("GuideWidget", "requestReverseGeocode() requesting new reverseGeocode");
            requestId = app.getCore().getGeocodeInterface().reverseGeocode(loc.getMC2Position(), this);
        }
    }

    private class WidgetLocationListener implements LocationListener  {
        public WidgetLocationListener() {
        }

        public void locationUpdate(LocationInformation locationInformation, LocationProvider locationProvider) {
            Log.i("GuideWidget", "locationUpdate() new location");
            loc = locationInformation;
            if(locationInformation.getAccuracy() <= AbstractActivity.ACCURACY_MAX) {
                new Thread("GuideWidget-Thread") {
                    public void run() {
                        requestReverseGeocode();
                        updateWidget(app);
                        shutdownLocation(app);
                    }
                }.start();
            }
            else {
                requestReverseGeocode();
                updateWidget(app);
            }
        }
    }
}
