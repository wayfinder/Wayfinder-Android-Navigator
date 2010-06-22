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
package com.vodafone.android.navigation.persistence;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.vodafone.android.navigation.NavigatorApplication;
import com.wayfinder.core.map.vectormap.PoiCategory;
import com.wayfinder.core.shared.route.RouteSettings;
import com.wayfinder.core.shared.settings.GeneralSettings;

public class ApplicationSettings {

	public static final int BACKLIGHT_NORMAL = 0;
	public static final int BACKLIGHT_ON_ROUTE = 1;
	public static final int BACKLIGHT_ALWAYS_ON = 2;

	private static final ApplicationSettings INSTANCE = new ApplicationSettings();

	private static final String SHARED_PREFS_NAME = "AndroidNavigatorSettings";
	private static final String KEY_BACKLIGHT = "key_backlight";
	private static final String KEY_ROUTE_USE_HIGHWAYS = "key_route_highway";
	private static final String KEY_ROUTE_USE_TOLLROADS = "key_route_tollroad";
	private static final String KEY_ROUTE_OPTIMIZATION = "key_route_optimization";
	private static final String KEY_ROUTE_USE_VOICE_GUIDANCE = "key_route_voice";
	private static final String KEY_DISPLAY_WARNING = "key_display_warning";
	private static final String KEY_MEASUREMENTS = "key_measurements";
	private static final String KEY_3D_MODE_PREFFERRED = "key_3d_mode_prefferred";
	private static final String KEY_IS_LOCAL_SEARCH = "key_is_local_search";
	private static final String KEY_SERVER_URL = "key_server_url";
	private static final String KEY_SERVER_PORT = "key_server_port";
	private static final String KEY_CLIENT_TYPE = "key_client_type";
	private static final String KEY_ALWAYS_ROUTE = "key_always_route";
	private static final String KEY_DISP_SETTINGS_AT_STARTUP = "key_disp_settings_at_startup";
	private static final String KEY_USE_FIXED_ZOOM_LEVELS = "key_use_fixed_zoom_levels";
	private static final String KEY_DISABLE_POSITIONING = "key_disable_positioning";

	public static final String URL_VERSION_BASE = "http://download.location.vodafone.com/resources/";
	public static final String URL_VERSION_CHECK ="/version";
	public static final String URL_MARKET_CHECK ="/market";	
	
	/**
	 * Default base URL for the Android Market
	 */
	public static final String DEFAULT_MARKET_BASE = "market://";

	/**
	 * Default search URL suffix that can be used to do a "package-search" on
	 * the Android Market
	 */
	public static final String DEFAULT_MARKET_PACKAGE_SEARCH = "search?q=pname:";

	/**
	 * Default search URL suffix that can be used to do a "name-search" on the
	 * Android Market
	 */
	public static final String DEFAULT_MARKET_NAME_SEARCH = "search?q=";
	
	private NavigatorApplication app;
	private int backlight = BACKLIGHT_ON_ROUTE;
	private int routeOptimization = RouteSettings.OPTIMIZE_TIME_AND_TRAFFIC;
	private boolean routeUseTollRoads = true;
	private boolean routeUseHighways = true;
	private boolean routeUseVoiceGuidance = true;
	private boolean displayWarningMessage;
	private boolean is3DModePrefferred = true;
	private boolean isLocalSearch = true;
	private HashMap<String, Boolean> enabledPoiCategoryNames;
	private String serverUrl;
	private int serverPort;
	private String clientId;
	private boolean alwaysRoute;
	private boolean dispSettingsAtStartup;
	private boolean useFixedZoomLevels;
	private boolean disablePositioning;

	private ApplicationSettings() {
	}

	public static final ApplicationSettings get() {
		return INSTANCE;
	}

	public void setBacklight(int backlight) {
		this.backlight = backlight;
	}

	public int getBacklight() {
		return this.backlight;
	}

	public void setRouteUseHighways(boolean useHighways) {
		this.routeUseHighways = useHighways;
	}

	public boolean getRouteUseHighways() {
		return this.routeUseHighways;
	}

	public void setRouteUseTollRoads(boolean useTollRoads) {
		this.routeUseTollRoads = useTollRoads;
	}

	public boolean getRouteUseTollRoads() {
		return this.routeUseTollRoads;
	}

	public void setRouteOptimization(int optimization) {
		this.routeOptimization = optimization;
	}

	public int getRouteOptimization() {
		return this.routeOptimization;
	}

	public void setRouteUseVoiceGuidance(boolean useVoiceGuidance) {
		this.routeUseVoiceGuidance = useVoiceGuidance;
		if (!this.app.isVoiceGuidanceMutedByPhoneCall()) {
			this.app.getCore().getSoundInterface().setMute(
					!this.routeUseVoiceGuidance);
		}
	}

	public boolean getRouteUseVoiceGuidance() {
		return this.routeUseVoiceGuidance;
	}

	public void setDisplayWarningMessage(boolean displayWarning) {
		this.displayWarningMessage = displayWarning;
	}

	public boolean getDisplayWarningMessage() {
		return this.displayWarningMessage;
	}

	public void setMeasurementSystem(int measurementsSettings) {
		GeneralSettings coreSettings = this.app.getCore().getGeneralSettings();
		coreSettings.setMeasurementSystem(measurementsSettings);
		coreSettings.commit();
		this.app.updateUnitsFormatter();
	}

	public int getMeasurementSystem() {
		return this.app.getCore().getGeneralSettings().getMeasurementSystem();
	}

	public void set3DModePrefferred(boolean is3DModePrefferred) {
		this.is3DModePrefferred = is3DModePrefferred;
	}

	public boolean get3DModePrefferred() {
		return this.is3DModePrefferred;
	}

	public void setIsLocalSearch(boolean isLocalSearch) {
		this.isLocalSearch = isLocalSearch;
	}

	public boolean getIsLocalSearch() {
		return this.isLocalSearch;
	}

	public void setServerAddress(String url, int port) {
		this.serverUrl = url;
		this.serverPort = port;
	}

	public String getServerUrl() {
		// if this is not a debug-version then disregard user�s settings and use
		// what is defined in NavigatorApplication
		if (!NavigatorApplication.DEBUG_ENABLED) {
			return NavigatorApplication.getServerUrl();
		}
		return this.serverUrl;
	}

	public int getServerPort() {
		// if this is not a debug-version then disregard user�s settings and use
		// what is defined in NavigatorApplication
		if (!NavigatorApplication.DEBUG_ENABLED) {
			return NavigatorApplication.getServerPort();
		}
		return this.serverPort;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		if (!NavigatorApplication.DEBUG_ENABLED) {
			return NavigatorApplication.getClientId();
		}
		return this.clientId;
	}

	public void setAlwaysRoute(boolean alwaysRoute) {
		this.alwaysRoute = alwaysRoute;
	}

	public boolean getAlwaysRoute() {
		if (!NavigatorApplication.DEBUG_ENABLED) {
			return false;
		}
		return this.alwaysRoute;
	}

	public void setDisplaySettingsAtStartup(boolean dispSettingsAtStartup) {
		this.dispSettingsAtStartup = dispSettingsAtStartup;
	}

	public boolean getDisplaySettingsAtStartup() {
		if (!NavigatorApplication.DEBUG_ENABLED) {
			return false;
		}
		return this.dispSettingsAtStartup;
	}

	public void setUseFixedZoomLevels(boolean useFixedZoomLevels) {
		this.useFixedZoomLevels = useFixedZoomLevels;
	}

	public boolean getUseFixedZoomLevels() {
		if (!NavigatorApplication.DEBUG_ENABLED) {
			return false;
		}
		return this.useFixedZoomLevels;
	}

	public void setDisablePositioning(boolean disablePositioning) {
		this.disablePositioning = disablePositioning;
	}

	public boolean getDisablePositioning() {
		if (!NavigatorApplication.DEBUG_ENABLED) {
			return false;
		}
		return this.disablePositioning;
	}

	public void setPoiCategoryEnabled(PoiCategory poiCategory, boolean enabled) {
		String name = poiCategory.getName().toLowerCase();

		if (this.enabledPoiCategoryNames == null) {
			this.enabledPoiCategoryNames = new HashMap<String, Boolean>();
		}
		this.enabledPoiCategoryNames.put(name, enabled);
	}

	public boolean getPoiCategoryEnabled(PoiCategory poiCategory) {
		String name = poiCategory.getName().toLowerCase();

		if (this.enabledPoiCategoryNames.containsKey(name)) {
			Boolean enabled = this.enabledPoiCategoryNames.get(name);
			return enabled;
		}

		SharedPreferences prefs = openPrefs();
		boolean enabled = prefs.getBoolean(name, false);
		return enabled;
	}

	public void setupPoiCategories(PoiCategory[] poiCats) {
		SharedPreferences prefs = openPrefs();

		for (PoiCategory poiCat : poiCats) {
			String name = poiCat.getName().toLowerCase();

			boolean enabled = false;
			if (this.enabledPoiCategoryNames != null
					&& this.enabledPoiCategoryNames.containsKey(name)) {
				enabled = this.enabledPoiCategoryNames.get(name);
			} else {
				enabled = prefs.getBoolean(name, false);
			}
			poiCat.setEnable(enabled);
		}
	}

	public void initBeforeCore(NavigatorApplication app) {
		this.app = app;

		SharedPreferences prefs = openPrefs();
		this.setBacklight(prefs.getInt(KEY_BACKLIGHT, BACKLIGHT_ON_ROUTE));
		this
				.setRouteUseHighways(prefs.getBoolean(KEY_ROUTE_USE_HIGHWAYS,
						true));
		this.setRouteUseTollRoads(prefs.getBoolean(KEY_ROUTE_USE_TOLLROADS,
				true));
		this.setRouteOptimization(prefs.getInt(KEY_ROUTE_OPTIMIZATION,
				RouteSettings.OPTIMIZE_TIME_AND_TRAFFIC));
		this.setDisplayWarningMessage(prefs.getBoolean(KEY_DISPLAY_WARNING,
				true));
		this
				.set3DModePrefferred(prefs.getBoolean(KEY_3D_MODE_PREFFERRED,
						true));
		this.setIsLocalSearch(prefs.getBoolean(KEY_IS_LOCAL_SEARCH, true));
		this.setServerAddress(prefs.getString(KEY_SERVER_URL,
				NavigatorApplication.getServerUrl()), prefs.getInt(
				KEY_SERVER_PORT, NavigatorApplication.getServerPort()));
		this.setClientId(prefs.getString(KEY_CLIENT_TYPE, NavigatorApplication
				.getClientId()));
		this.setAlwaysRoute(prefs.getBoolean(KEY_ALWAYS_ROUTE, false));
		this.setDisplaySettingsAtStartup(prefs.getBoolean(
				KEY_DISP_SETTINGS_AT_STARTUP, true));
		this.setUseFixedZoomLevels(prefs.getBoolean(KEY_USE_FIXED_ZOOM_LEVELS,
				false));
		this.setDisablePositioning(prefs.getBoolean(KEY_DISABLE_POSITIONING,
				false));
	}

	public void initAfterCore(NavigatorApplication app) {
		this.app = app;
		GeneralSettings coreSettings = this.app.getCore().getGeneralSettings();

		SharedPreferences prefs = openPrefs();
		this.setMeasurementSystem(prefs.getInt(KEY_MEASUREMENTS, coreSettings
				.getMeasurementSystem()));
		this.setRouteUseVoiceGuidance(prefs.getBoolean(
				KEY_ROUTE_USE_VOICE_GUIDANCE, true));
	}

	public void commit() {
		SharedPreferences prefs = openPrefs();
		Editor editor = prefs.edit();
		editor.putInt(KEY_BACKLIGHT, this.getBacklight());
		editor.putBoolean(KEY_ROUTE_USE_HIGHWAYS, this.getRouteUseHighways());
		editor.putBoolean(KEY_ROUTE_USE_TOLLROADS, this.getRouteUseTollRoads());
		editor.putInt(KEY_ROUTE_OPTIMIZATION, this.getRouteOptimization());
		editor.putBoolean(KEY_ROUTE_USE_VOICE_GUIDANCE, this
				.getRouteUseVoiceGuidance());
		editor.putBoolean(KEY_DISPLAY_WARNING, this.getDisplayWarningMessage());

		if (this.app.isInitialized()) {
			editor.putInt(KEY_MEASUREMENTS, this.getMeasurementSystem());
		}

		editor.putBoolean(KEY_3D_MODE_PREFFERRED, this.get3DModePrefferred());
		editor.putBoolean(KEY_IS_LOCAL_SEARCH, this.getIsLocalSearch());
		editor.putString(KEY_SERVER_URL, this.getServerUrl());
		editor.putInt(KEY_SERVER_PORT, this.getServerPort());
		editor.putString(KEY_CLIENT_TYPE, this.getClientId());
		editor.putBoolean(KEY_ALWAYS_ROUTE, this.getAlwaysRoute());
		editor.putBoolean(KEY_DISP_SETTINGS_AT_STARTUP, this
				.getDisplaySettingsAtStartup());
		editor.putBoolean(KEY_USE_FIXED_ZOOM_LEVELS, this
				.getUseFixedZoomLevels());
		editor
				.putBoolean(KEY_DISABLE_POSITIONING, this
						.getDisablePositioning());

		if (this.enabledPoiCategoryNames != null) {
			for (String enabledPoiCategoryName : this.enabledPoiCategoryNames
					.keySet()) {
				Boolean enabled = this.enabledPoiCategoryNames
						.get(enabledPoiCategoryName);
				editor.putBoolean(enabledPoiCategoryName, enabled);
			}
			this.enabledPoiCategoryNames.clear();
		}

		editor.commit();
	}

	private SharedPreferences openPrefs() {
		SharedPreferences prefs = this.app.getSharedPreferences(
				SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		return prefs;
	}
}
