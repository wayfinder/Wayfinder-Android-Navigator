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

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.contentprovider.ContentProviderPlaces;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.wayfinder.core.Core;
import com.wayfinder.core.map.MapStartupListener;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.User;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.userdata.UserListener;

/**
 * Note: This class should NOT extend AbstractActivity since that class will
 * initiate Core and this will make the splash not to be displayed correctly
 */
public class SplashActivity extends WarningActivity implements UserListener {

	public static final String KEY_TARGET_ACTIVITY = "key_target_activity";
	public static final String KEY_EXIT = "key_exit";

	public static final int TARGET_LOCAL_SEARCH = 0;
	public static final int TARGET_ROUTE = TARGET_LOCAL_SEARCH + 1;
	public static final int TARGET_SAVED_PLACES = TARGET_ROUTE + 1;

	private static final int REQUEST_CODE_DEBUG_SETTINGS = 0;

	private boolean userRetrived;
	private boolean query_server;
	private int target;
	private long contentId;
	private NavigatorApplication application;
	private boolean coreStarted;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.splash);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		application = (NavigatorApplication) this.getApplicationContext();

		FrameLayout layoutSplash = (FrameLayout) this
				.findViewById(R.id.layout_splash);
		if (NavigatorApplication.DEBUG_ENABLED) {
			layoutSplash.setBackgroundResource(R.drawable.splash_test);
		} else {
			layoutSplash.setBackgroundResource(R.drawable.splash);
		}

		Intent intent = this.getIntent();
		final boolean exit = intent.getBooleanExtra(KEY_EXIT, false);
		if (exit) {
			this.finish();
			return;
		}

		application.setWarningListener(this);
		String version = "Version " + application.getVersion();
		if (NavigatorApplication.DEBUG_ENABLED) {
			version += "\nInternal " + application.getBuild();
		}

		TextView textVersion = (TextView) this.findViewById(R.id.text_version);
		textVersion.setText(version);

		this.target = intent.getIntExtra(KEY_TARGET_ACTIVITY,
				TARGET_LOCAL_SEARCH);
		Log.i("SplashActivity", "onCreate() target: " + this.target);

		Uri uri = intent.getData();
		if (uri != null) {
			try {
				this.contentId = Long.parseLong(uri.getPathSegments().get(1));
				String auth = uri.getAuthority();
				if (ContentProviderPlaces.AUTHORITY.equals(auth)) {
					this.target = TARGET_SAVED_PLACES;
				}
			} catch (Exception e) {
				Log.e("SplashActivity", "onCreate() " + e);
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onResume() {
		if (application.isWifiEnabled()) {
			this.showDialog(DIALOG_WIFI_WARNING);
		} else {
			if (!coreStarted) {
				coreStarted = true;
				// The reason for us to create a HandlerThread is that core
				// needs a handler
				// that is prepared with looper
				// Then, the reson for us to do the init in another thread is
				// that the
				// onCreate method should return as quickly as possible, so that
				// the
				// Splash-image can be shown while we init core
				HandlerThread handlerThread = new HandlerThread(
						"SplashActivity-HandlerThread");
				handlerThread.start();

				final Handler handler = new Handler(handlerThread.getLooper());
				handler.post(new Runnable() {
					public void run() {
						NavigatorApplication app = (NavigatorApplication) getApplicationContext();
						if (!app.isInitialized()) {
							initApplication(app);
						} else {
							getUserInternal();
						}
						handler.getLooper().quit();
					}
				});
			}
		}

		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_DEBUG_SETTINGS) {
			getUserInternal();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void initApplication(NavigatorApplication app) {
		app.initiatePreCore();
		if (ApplicationSettings.get().getDisplaySettingsAtStartup()) {
			this.startActivityForResult(new Intent(this,
					SettingsDebugActivity.class), REQUEST_CODE_DEBUG_SETTINGS);
		} else {
			// initiate core and wait for map to finish loading before moving
			// on...
			getUserInternal();
		}
	}

	private void startServiceWindow() {
		/*
		Intent intent = new Intent(this, ServiceWindowActivity.class);
		intent.putExtra(ServiceWindowActivity.KEY_SHOW_START_PAGE, true);
		this.startActivity(intent);
		this.finish();*/
	}
	
	
	private void getUserInternal() {
		if(query_server) {
			application.getCore().getUserDataInterface().obtainUserFromServer(this);
		} else {
			application.getCore().getUserDataInterface().getUser(this);
		}
	}
	

	private void startMain() {
		switch (this.target) {
		case TARGET_ROUTE: {
			Intent intent = new Intent(this, SearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(KEY_TARGET_ACTIVITY, TARGET_ROUTE);
			this.startActivity(intent);
			break;
		}
		case TARGET_SAVED_PLACES: {
			Intent intent = new Intent(this, SearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(KEY_TARGET_ACTIVITY, TARGET_SAVED_PLACES);
			intent.putExtra(PlacesActivity.KEY_PLACE_ID, this.contentId);
			this.startActivity(intent);
			break;
		}
		default: {
			Intent intent = new Intent(this, SearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		}
		}

		this.finish();
	}

	public void currentUser(User user) {
		if (user != null && user.isActivated()) {
			((NavigatorApplication) getApplication())
					.startMap(new MapStartupListener() {

						public void mapStartupComplete(int statusCode) {
							Log.i("mapStartupComplete(int statusCode)",
									"map loaded (" + statusCode + ")");

						}
					});
			application.loadDataRequiredFromServer();
			userRetrived = true;
			this.startMain();
		} else if(!query_server){
			query_server = true;
			getUserInternal();
		} else {
			Toast.makeText(this, "Sorry, could not activate client", Toast.LENGTH_LONG).show();
			Runnable r = new Runnable() {
				public void run() {
					finish();
				}
			};
			new Handler().postDelayed(r, 3000);
		}
	}

	public void error(RequestID requestID, CoreError error) {
		Log.e("SplashActivity", "error() error: " + error);
		// TODO: check type of error and handle the different cases.
		getUserInternal();
	}

	public void displayRoamingIsTurnedOffWarning() {
	}

	public void displayGeneralErrorMessage(int messageId) {
	}

	public void displayGpsWarning() {
	}

	public void displayWifiWarning() {
	}

	public void displayNoSIMWarning() {
	}

	public void displayRoamingWarning(int autoDismissTime) {
	}

	public void displaySafetyWarning() {
	}

	public void handleSoundMuteStateChanged() {
	}

	public void displaySynchronizationCompletedMessage() {
	}

	public void displayNoInternetConnectionWarning(int autoDismissTime) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				if (!userRetrived) {
					getUserInternal();
				}
			}
		}, 3000);
	}
}
