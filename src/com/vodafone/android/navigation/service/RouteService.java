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
package com.vodafone.android.navigation.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.activity.AbstractRouteActivity;
import com.vodafone.android.navigation.activity.SplashActivity;
import com.vodafone.android.navigation.util.ResourceUtil;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.shared.route.NavigationInfo;
import com.wayfinder.core.shared.route.NavigationInfoListener;
import com.wayfinder.core.shared.route.Turn;
import com.wayfinder.core.shared.route.Waypoint;

/**
 * This service will add a non-removable notification in the topbar. This will be
 * an easy way for the user to get back to the routingpart of the application. 
 *
 * This service broadcasts parts of the NnavigationInfo it receives from core.
 * Possible listeners to this is the GuideWidget, which displays next turn and 
 * distance on user's Homescreen
 */
public class RouteService extends Service implements NavigationInfoListener {

    public static final String ROUTE_EVENT              = "com.wayfinder.android.ROUTE";
    public static final String KEY_NEXT_STREET          = "key_next_street";
    public static final String KEY_NEXT_TURN_DISTANCE   = "key_next_turn_distance";
    public static final String KEY_GUIDE_IMAGE_ID       = "key_guide_image_id";
    public static final String KEY_EXIT_COUNT		    = "key_exit_count";
    public static final String KEY_ROUTE_REMOVED        = "key_route_removed";
    
    private static final int NOTIFICATION_ID = 0;
    
    private NavigatorApplication application;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.i("RouteService", "onCreate()");
        this.application = (NavigatorApplication) this.getApplicationContext();
        if(this.application.getCore() == null) {
            this.application.initiate(null);
        }
        
        this.application.getCore().getRouteInterface().addNavigationInfoListener(this);
        
        this.addNotification();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRouting(this);
        
        try {
            this.application = (NavigatorApplication) this.getApplicationContext();
            application.getCore().getRouteInterface().removeNavigationInfoListener(this);
        } catch(Exception e) {
            Log.e("RouteService", "onDestroy() " + e);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void navigationInfoUpdated(NavigationInfo info) {
        Waypoint nextWpt = null;
        if(info != null) {
            nextWpt = info.getNextWpt();
	        if(info.isOfftrack()) {
	            int pictogramId = R.drawable.offtrack;
	            String roadNameAfter = this.getResources().getString(R.string.qtn_andr_u_r_offtrack_txt);
	            addNotification(this, pictogramId, roadNameAfter, roadNameAfter);
	            broadcastRouteInfo(this, roadNameAfter, info.getDistanceMetersTotalRemaining(), pictogramId, -1);
	        }
	        else if(!info.isFollowing() || nextWpt == null) {
	            int pictogramId = R.drawable.finish_flag;
	            String roadNameAfter = this.getResources().getString(R.string.qtn_andr_dest_reached_txt);
	            addNotification(this, pictogramId, roadNameAfter, roadNameAfter);
	            broadcastRouteInfo(this, roadNameAfter, 0, pictogramId, -1);
	
	            AbstractRouteActivity.setNightMode(this.getApplicationContext(), false);
	
	            Log.i("RouteService", "Removing route");
	            this.application.removeRoute();
	
	            VectorMapInterface map = this.application.getMapInterface();
	            map.getMapDetailedConfigInterface().setRoute(null);
	            
	            RouteService.stopRouting(this);
	        }
	        else if(info.isSpeedCameraActive()) {
	            int pictogramId = R.drawable.speed_cam;
	            String roadNameAfter = this.getResources().getString(R.string.qtn_andr_speed_cam_ahead_txt);
	            addNotification(this, pictogramId, roadNameAfter, roadNameAfter);
	            broadcastRouteInfo(this, roadNameAfter, info.getDistanceMetersTotalRemaining(), pictogramId, -1);
	        }
	        else {
	            Turn turn = nextWpt.getTurn();
	            if(turn != null) {
	            	int exitCount = -1;
	                int pictogramId = ResourceUtil.getDrawableId(this, "p" + turn.getId());
	                String roadNameAfter = nextWpt.getRoadNameAfter();
	                addNotification(this, pictogramId, roadNameAfter, roadNameAfter);
	                if(nextWpt.getExitCount() > 0 && nextWpt.getTurn() == Turn.EXIT_ROUNDABOUT){
	                	exitCount = nextWpt.getExitCount();
	                }
	                else{
	                	exitCount = -1;
	                }
					broadcastRouteInfo(this, roadNameAfter, info.getDistanceMetersToNextWpt(), pictogramId, exitCount);
	            }
	        }
        }
    }

    public static void broadcastRouteInfo(Context context, String nextStreet, int nextTurnDistance, int guideImageId, int exitCount) {
        Intent intent = new Intent(ROUTE_EVENT);
        intent.putExtra(KEY_NEXT_STREET, nextStreet);
        intent.putExtra(KEY_NEXT_TURN_DISTANCE, nextTurnDistance);
        intent.putExtra(KEY_GUIDE_IMAGE_ID, guideImageId);
        intent.putExtra(KEY_EXIT_COUNT, exitCount);
        context.sendBroadcast(intent);
    }

    public static void broadcastPositionInfo(Context context) {
        Intent intent = new Intent(ROUTE_EVENT);
        context.sendBroadcast(intent);
    }

    public static void broadcastRemoveRoute(Context context) {
        Intent intent = new Intent(ROUTE_EVENT);
        intent.putExtra(KEY_ROUTE_REMOVED, true);
        context.sendBroadcast(intent);
    }
    
    public static void stopRouting(Context context) {
        Log.i("RouteService", "stopRouting()");
        
        removeNotification(context);
        
        broadcastRemoveRoute(context);

        context.stopService(new Intent(context, RouteService.class));
        
        NavigatorApplication app = ((NavigatorApplication) context.getApplicationContext());
        app.getCore().getRouteInterface().clearRoute();
    }
    
    private void addNotification() {
        addNotification(this, R.drawable.application_icon, "", "");
    }
    
    private static void addNotification(Context context, int iconId, String text, String tickerText) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra(SplashActivity.KEY_TARGET_ACTIVITY, SplashActivity.TARGET_ROUTE);
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        String title = res.getString(R.string.qtn_andr_applic_name_txt);

        Notification notification = new Notification(iconId, tickerText, System.currentTimeMillis());
        notification.setLatestEventInfo(context, title, text, pendingIntent);
        notification.flags |= Notification.FLAG_NO_CLEAR;
                    
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);         
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    private static void removeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);         
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
