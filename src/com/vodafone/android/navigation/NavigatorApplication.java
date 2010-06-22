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
package com.vodafone.android.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.vodafone.android.navigation.activity.AbstractActivity;
import com.vodafone.android.navigation.activity.ServicesActivity;
import com.vodafone.android.navigation.components.AndroidCallbackHandler;
import com.vodafone.android.navigation.components.RecentDestination;
import com.vodafone.android.navigation.components.SearchHistoryItem;
import com.vodafone.android.navigation.listeners.SearchCategoriesListUpdatedListener;
import com.vodafone.android.navigation.listeners.SearchProvidersUpdateListener;
import com.vodafone.android.navigation.listeners.SearchResultsListener;
import com.vodafone.android.navigation.listeners.TopRegionsListUpdatedListener;
import com.vodafone.android.navigation.listeners.WarningListener;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.UserMapObject;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.persistence.DatabaseAdapter;
import com.vodafone.android.navigation.persistence.PredictiveWritingDatabaseAdapter;
import com.vodafone.android.navigation.util.AndroidUnitsFormatter;
import com.vodafone.android.navigation.util.AutoUpdater;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.wayfinder.core.Core;
import com.wayfinder.core.CoreFactory;
import com.wayfinder.core.ModuleData;
import com.wayfinder.core.ServerData;
import com.wayfinder.core.favorite.Favorite;
import com.wayfinder.core.favorite.FavoriteInterface;
import com.wayfinder.core.favorite.FavoriteLoadListener;
import com.wayfinder.core.favorite.FavoriteSynchListener;
import com.wayfinder.core.favorite.ListModel;
import com.wayfinder.core.map.MapStartupListener;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapDrawerInterface;
import com.wayfinder.core.map.vectormap.MapInitialConfig;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.network.NetworkError;
import com.wayfinder.core.network.ServerError;
import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationInterface;
import com.wayfinder.core.positioning.LocationListener;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.route.RouteListener;
import com.wayfinder.core.route.RouteRequest;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.CategoryCollection;
import com.wayfinder.core.search.CategoryListener;
import com.wayfinder.core.search.SearchError;
import com.wayfinder.core.search.SearchHistory;
import com.wayfinder.core.search.SearchInterface;
import com.wayfinder.core.search.SearchProvider;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.SearchReply;
import com.wayfinder.core.search.TopRegion;
import com.wayfinder.core.search.TopRegionCollection;
import com.wayfinder.core.search.TopRegionListener;
import com.wayfinder.core.search.onelist.OneListSearchListener;
import com.wayfinder.core.search.onelist.OneListSearchReply;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.error.RouteError;
import com.wayfinder.core.shared.error.UnexpectedError;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.RouteSettings;
import com.wayfinder.core.shared.settings.GeneralSettings;
import com.wayfinder.core.shared.util.ListenerList;
import com.wayfinder.core.shared.util.UnitsFormatter;
import com.wayfinder.pal.PAL;
import com.wayfinder.pal.android.AndroidPAL;
import com.wayfinder.pal.android.graphics.AndroidGraphicsFactory;
import com.wayfinder.pal.android.network.http.HttpConfigurationInterface;

public class NavigatorApplication extends Application implements
		LocationListener, CategoryListener, TopRegionListener,
		MapDrawerInterface, RouteListener,
		FavoriteSynchListener, FavoriteLoadListener, OneListSearchListener {

	public static final boolean DEBUG_ENABLED = false;
	public static final int DISMISS_TIME_WARNING_DIALOG = 10;

	private static final int MAX_NUMBER_OF_RECENT_DESTINATIONS = 50;
	private static final int MAX_NUMBER_OF_PREVIOUS_SEARCHES = 50;

	// HEAD-server
	private static String CLIENT_ID;
	private static String SERVER_URL;
	private static int SERVER_PORT;

	private static final boolean VERSION_CHECK_ENABLED = false;

	static {
		CLIENT_ID = "wf-android-demo";
		SERVER_URL = "[MY SERVER]";
		SERVER_PORT = 80;
	}

	private LocationInformation ownLocationInformation;
	private LocationInformation ownFirstLocationInformation;
	private LocationProvider locationProvider;
	private VectorMapInterface map;
	private MapDrawerInterface drawerInterface;
	private Core core;
	private AndroidGraphicsFactory androidFactory;
	private CategoryCollection categoryCollection;
	private Category selectedSearchCategory;
	private ListenerList searchCategoriesListUpdatedListeners = new ListenerList();
	private ListenerList locationListeners = new ListenerList();
	private TopRegionCollection topRegionCollections;
	private TopRegion selectedTopRegion;
	private ListenerList topRegionsListUpdatedListeners = new ListenerList();
	private Stack<TopRegion> lastUsedTopRegions = new Stack<TopRegion>();
	private SearchResultsListener searchResultsListener;
	private String searchWhatStr;
	private String searchWhereStr;
	private SearchHistoryItem previousSearchQuery;
	private SearchQuery preparedSearchQuery;
	private SearchReply searchReply;
	private boolean searchQueryPending;
	private Vector<AndroidMapObject> pressedMapObjects = new Vector<AndroidMapObject>();
	private AndroidMapObject[] activityMapObjects;
	private Route route;
	private RouteListener internalRouteListener;
	private RouteListener routeListener;
	private boolean isRoutePending;
	private Vector<RequestID> routeRequestIds = new Vector<RequestID>();
	private RequestID searchRequestId;
	private SearchProvider[] searchProviders;
	private SearchProvidersUpdateListener searchProvidersUpdateListener;
	private boolean temporaryTopRegionSet;
	private int routeTransportMode;
	private Criteria criteria;
	private boolean isFirstPosition = true;
	private boolean followRoute;
	private boolean displayWarning = true;
	private PhoneStateListener phoneStateListener;
	private WarningListener warningListener;
	private TelephonyManager telefonyManager;
	private WifiManager wifiManager;
	private boolean isRoaming;
	private ArrayList<RecentDestination> previousDestinations;
	private ArrayList<SearchHistoryItem> previousSearches;
	private DatabaseAdapter placesDBAdapter;
	private Vector<Integer> handledErrors = new Vector<Integer>();
	private UnitsFormatter unitsFormatter;
	private Handler handler;
	private boolean voiceGuidanceMutedByPhoneCall;
	private boolean warningsInitialized;
	private boolean roamingWarningDisplayed;
	private UserMapObject userPin;
	private boolean useSensorForRoute;
	private Timer timer;
	private TimerTask displayNetworkErrorTask;
	private TimerTask checkVersionTask;
	private boolean isMapInitiated;
	private PredictiveWritingDatabaseAdapter predictiveWritingDBAdapter;
	private ArrayList<String> predictiveWritingTable;
	private Favorite favoriteToBeUpdated;
	private long startupTime = System.currentTimeMillis();
	private boolean shouldRecheckWifi;
	private RouteRequest[] routeRequests;
	private boolean isReroute;
	private Stack<String> iServiceWindowHistory = new Stack<String>();
	private AutoUpdater updater;

	@Override
	public void onCreate() {
		super.onCreate();
		this.handler = new Handler();

		if (VERSION_CHECK_ENABLED) {
			new Thread("NavigatorApplication.VersionCheckThread") {
				public void run() {
					Looper.prepare();
					checkVersion();
				}
			}.start();
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public static boolean isEmulator() {
		return android.os.Build.MODEL.contains("sdk");
	}

	public String getVersion() {
		String version = "0.0.0";
		try {
			PackageManager packageManager = this.getPackageManager();
			if (packageManager != null) {
				PackageInfo packageInfo = packageManager.getPackageInfo(this
						.getPackageName(), 0);
				if (packageInfo != null) {
					version = packageInfo.versionName;
				}
			}
		} catch (NameNotFoundException e) {
		}
		return version;
	}

	public String getBuild() {
		String version = "#0";
		try {
			PackageManager packageManager = this.getPackageManager();
			if (packageManager != null) {
				PackageInfo packageInfo = packageManager.getPackageInfo(this
						.getPackageName(), 0);
				if (packageInfo != null) {
					version = "#" + packageInfo.versionCode;
				}
			}
		} catch (NameNotFoundException e) {
		}
		return version;
	}

	public void initiatePreCore() {
		ApplicationSettings.get().initBeforeCore(this);
		readPredictStrings();
	}

	public void initiateCore(final MapStartupListener mapStartupListener) {
		if (this.core == null) {
			this.startCore();
		}

		this.readPlaces();

		ApplicationSettings.get().initAfterCore(this);
	}

	public void initiate(MapStartupListener mapStartupListener) {
		if (this.core == null) {
			this.initiatePreCore();
			this.initiateCore(mapStartupListener);
		}
	}

	private void startCore() {
		Log.i("NavigatorApplication", "startCore()");

		String version = "0.0.0";
		PackageManager packageManager = this.getPackageManager();
		if (packageManager != null) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(this
						.getPackageName(), 0);
				if (packageInfo != null) {
					version = packageInfo.versionName;
				}
			} catch (NameNotFoundException e) {
			}
		}

		ApplicationSettings settings = ApplicationSettings.get();
		String serverUrl = settings.getServerUrl();
		int serverPort = settings.getServerPort();
		String clientId = settings.getClientId();

		final ServerData bootData = new ServerData(clientId, version,
				new String[] { serverUrl }, new int[] { serverPort });

		PAL pal = initPal();

		if (DEBUG_ENABLED) {
			pal.enableFileLoggingAsynch();
		}

		HttpConfigurationInterface httpConfig = getHttpConfig(pal);
		ImageDownloader.create(httpConfig);
		AndroidCallbackHandler callbackHandler = new AndroidCallbackHandler(
				this.handler);
		final ModuleData modData = new ModuleData(pal, bootData,
				callbackHandler);

		this.core = CoreFactory.createFullCore(modData);

		this.updateUnitsFormatter();

		LocationInterface locationInterface = this.core.getLocationInterface();
		locationInterface.initLocationSystem();

		this.pausePositioning(false);

		// have to do the below to point the Core towards the sounds
		this.core.getNavigationSoundInterface().setSoundsAndSyntaxPath("", "");


	}
	
	
	public void loadDataRequiredFromServer() {
		SearchInterface searchInterface = this.core.getSearchInterface();
		searchInterface.loadCategories(this);
		searchInterface.loadTopRegions(this);
		
		FavoriteInterface favoritesInterface = this.core.getFavoriteInterface();
		favoritesInterface.preload(this);
		// Toast.makeText(this, R.string.qtn_andr_synchronization_message,
		// Toast.LENGTH_LONG).show();
		favoritesInterface.synchronizeFavorites(null);
	}
	

	protected HttpConfigurationInterface getHttpConfig(PAL pal) {
		// in this class its an androidPal.
		return ((AndroidPAL) pal).getHttpConfiguration();
	}

	protected PAL initPal() {
		return AndroidPAL.createAndroidPAL(this);
	}

	private Criteria getCriteria() {
		if (this.criteria == null) {
			this.criteria = new Criteria.Builder().accuracy(
					Criteria.ACCURACY_NONE).costAllowed().build();
		}
		return this.criteria;
	}

	public boolean isMapInitiated() {
		return this.isMapInitiated;
	}

	public void startMap(MapStartupListener mapStartupListener) {
		Log.i("NavigatorApplication", "startMap()");

		this.map = this.core.getVectorMapInterface();

		WindowManager windowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		Display defaultDisplay = windowManager.getDefaultDisplay();
		int width = defaultDisplay.getWidth();
		int height = defaultDisplay.getHeight();
		MapInitialConfig mapInitialConfig = new MapInitialConfig(0, 0, width,
				height, true);
		mapInitialConfig.enableRouteTileDownloader(true);
		mapInitialConfig.connectFileCache(
				MapInitialConfig.CACHE_PRIORITY_PRIMARY, true);
		this.map
				.initializeMap(this.getAndroidFactory(), this, mapInitialConfig);

		if (mapStartupListener != null) {
			this.map.setStartupListener(mapStartupListener);
		}

		// Let's provide a first position for the user, before he gets a
		// cell-id-position or a real gps-position.
		// This position points out Wayfinder's office in Lund
		// Latitude: 55.718197
		// Longitude: 13.190884
		// And this one Arc de Triumph
		// Latitude: 48.8738
		// Longitude: 2.2950
		int startLat = Position.decimalDegresToMc2(48.8738f);
		int startLon = Position.decimalDegresToMc2(2.2950f);
		Log.i("NavigatorApplication", "startMap() map initiated at position ["
				+ startLat + ", " + startLon + "]");
		this.map.setCenter(startLat, startLon);

		// Set time to 0 (to indicate that this is a really old position)
		this.ownFirstLocationInformation = new LocationInformation(startLat,
				startLon, 500, (short) 0, (short) 0, (short) 0, 0);
		this.isFirstPosition = true;

		// for testing network connectivity
		// this.map.getMapDetailedConfigInterface().setOfflineMode(true);
		this.map.startMapComponent(CLIENT_ID);
		// this.map.setFollowGpsPosition(true);
		this.map.setVisible(true);

		this.isMapInitiated = true;
	}

	public UnitsFormatter getUnitsFormatter() {
		if (this.unitsFormatter == null) {
			this.updateUnitsFormatter();
		}
		return this.unitsFormatter;
	}

	public void updateUnitsFormatter() {
		int measurementSystem = GeneralSettings.UNITS_METRIC;
		if (this.core != null && this.core.getGeneralSettings() != null) {
			measurementSystem = this.core.getGeneralSettings()
					.getMeasurementSystem();
		}
		this.unitsFormatter = new AndroidUnitsFormatter(measurementSystem, this);
	}

	public void setDisplaySafetyWarning(boolean displayWarning) {
		this.displayWarning = displayWarning;
	}

	public boolean displaySafetyWarning() {
		return this.displayWarning;
	}

	/* ++++++++++++ MAP AND LOCATION RELATED SECTION +++++++++++++ */
	public VectorMapInterface getMapInterface() {
		if (this.map == null) {
			if (this.core == null) {
				this.initiate(null);
			}
			this.map = this.core.getVectorMapInterface();
		}
		return this.map;
	}

	public void mapInteractionDetected() {
		this.isFirstPosition = false;
	}

	public void locationUpdate(LocationInformation locationInformation,
			LocationProvider locationProvider) {
		boolean isPositioningEnabled = !ApplicationSettings.get()
				.getDisablePositioning();

		this.locationProvider = locationProvider;
		if (this.locationProvider.getState() == LocationProvider.PROVIDER_STATE_AVAILABLE) {
			if (isPositioningEnabled) {
				this.ownLocationInformation = locationInformation;
			}
			Log.i("NavigatorApplication",
					"locationUpdate() Updating ownLocationInformation");
		} else {
			if (this.ownLocationInformation == null) {
				LocationInformation oldPosition = locationProvider
						.getLastKnownLocation();
				if (!isPositioningEnabled) {
					Log.i("NavigatorApplication",
							"locationUpdate() Positioning is disabled");
				} else if (oldPosition == null) {
					Log
							.i(
									"NavigatorApplication",
									"locationUpdate() First startup, no old location is available, defaulting to static position");
					this.ownLocationInformation = this.ownFirstLocationInformation;
				} else {
					Log
							.i("NavigatorApplication",
									"locationUpdate() Using old position for ownLocationInformation");
					this.ownLocationInformation = oldPosition;
				}
			} else {
				Log.i("NavigatorApplication",
						"locationUpdate() Not updating ownLocationInformation");
			}
		}

		if (ownLocationInformation != null) {
			Position position = ownLocationInformation.getMC2Position();
			if (position.isValid()) {
				if (!this.followRoute || this.isFirstPosition) {
					int gpsLat = position.getMc2Latitude();
					int gpsLon = position.getMc2Longitude();
					if (this.map != null) {
						this.map.setGpsPosition(gpsLat, gpsLon, 0);
					}
				}
				int radius = ownLocationInformation.getAccuracy();

				// Map will continue updating until eother the positioning is
				// good enough for routing with, or the user has started
				// interacting with the map
				if (this.isFirstPosition) {
					this.core.getSearchInterface()
							.determineTopRegionForPosition(position, this);
					if (radius <= Criteria.ACCURACY_BAD) {
						this.isFirstPosition = false;
					}

					if (this.map != null) {
						int gpsLat = position.getMc2Latitude();
						int gpsLon = position.getMc2Longitude();
						this.map.setCenter(gpsLat, gpsLon);

						Resources resources = this.getResources();

						WindowManager windowManager = (WindowManager) this
								.getSystemService(Context.WINDOW_SERVICE);
						Display defaultDisplay = windowManager
								.getDefaultDisplay();
						int width = defaultDisplay.getWidth();
						int height = defaultDisplay.getHeight();
						int cellIdPadding = resources
								.getDimensionPixelSize(R.dimen.cell_id_padding);
						int cellIdMinScaleRadious = resources
								.getDimensionPixelSize(R.dimen.cell_id_min_scale_radious);
						float toScale = Math.min(width, height) - cellIdPadding;

						if (radius > cellIdMinScaleRadious) {
							this.map.setScale((radius / toScale) * 2);
						} else {
							this.map
									.setScale((cellIdMinScaleRadious / toScale) * 2);
						}
					}
				}
			}
		}

		for (int i = 0; i < locationListeners.getListenerInternalArray().length; i++) {
			((LocationListener) locationListeners.getListenerInternalArray()[i])
					.locationUpdate(locationInformation, locationProvider);
		}
	}

	public void addLocationListener(LocationListener listener) {
		this.locationListeners.add(listener);
	}

	public void removeLocationListener(LocationListener listener) {
		this.locationListeners.remove(listener);
	}

	public void setFollowRoute(boolean followRoute) {
		this.followRoute = followRoute;
	}

	public void providerStateChanged(LocationProvider provider) {
	}

	public LocationInformation getOwnLocationInformation() {
		if (this.ownLocationInformation != null) {
			return this.ownLocationInformation;
		}
		return ownFirstLocationInformation;
	}

	public boolean isOldAndInvalidLocation(LocationInformation info) {
		return (info.getPositionTime() < this.startupTime);
	}

	public void setOwnLocationInformation(int mc2Lat, int mc2Lon,
			int radiusInMeters) {
		this.ownLocationInformation = new LocationInformation(mc2Lat, mc2Lon,
				radiusInMeters, (short) 0, (short) 0, (short) 0, 0);
	}

	public void addPressedMapObject(AndroidMapObject mapObject) {
		this.pressedMapObjects.add(mapObject);
	}

	public AndroidMapObject[] getPressedMapObjects() {
		return this.pressedMapObjects
				.toArray(new AndroidMapObject[this.pressedMapObjects.size()]);
	}

	public void movePressedMapObjectToActivity() {
		this.activityMapObjects = this.getPressedMapObjects();
	}

	public AndroidMapObject[] getPressedMapObjectsForActivity() {
		return this.activityMapObjects;
	}

	public void clearPressedMapObjects() {
		this.pressedMapObjects.clear();
	}

	/* ----------- END OF MAP AND LOCATION RELATED SECTION ------------ */

	/* ++++++++++++++ CORE RELATED SECTION +++++++++++++++++ */
	public MapDrawerInterface getMapDrawer() {
		return drawerInterface;
	}

	public void setMapDrawer(MapDrawerInterface drawerInterface) {
		this.drawerInterface = drawerInterface;
		if (this.map != null) {
			this.map.setMapDrawerInterface(this.drawerInterface);
		} else {
			Log.e("State", "setMapDrawer() VectorInterface was null");
		}
	}

	public Core getCore() {
		if (this.core == null) {
			this.initiate(null);
		}
		return core;
	}

	public boolean isInitialized() {
		return (this.core != null);
	}

	public AndroidGraphicsFactory getAndroidFactory() {
		if (this.androidFactory == null) {
			float density = this.getResources().getDisplayMetrics().density;
			this.androidFactory = new AndroidGraphicsFactory(density);
		}
		return this.androidFactory;
	}

	public void updateScreen(MapRenderer mapRenderer,
			MapCameraInterface mapCamera) {
	}

	public void pausePositioning(boolean pause) {
		if (this.core != null) {
			LocationInterface loc = this.core.getLocationInterface();
			if (pause) {
				if (this.route == null && !this.isRoutePending) {
					Log.i("NavigationApplication",
							"pausePositioning() suspending positioning");
					loc.removeLocationListener(this);
					loc.suspend();
				}
			} else {
				Log.i("NavigationApplication",
						"pausePositioning() resuming positioning");
				loc.addLocationListener(this.getCriteria(), this);
				loc.resume();
			}
		}
	}

	public static String getServerUrl() {
		return SERVER_URL;
	}

	public static int getServerPort() {
		return SERVER_PORT;
	}

	public static String getClientId() {
		return CLIENT_ID;
	}

	public String getServerAddress() {
		ApplicationSettings settings = ApplicationSettings.get();
		return "http://" + settings.getServerUrl() + ":"
				+ settings.getServerPort() + "/";
	}

	/* -------------- END OF CORE RELATED SECTION -------------- */

	/* +++++++++++++++ SEARCH CATEGORIES RELATED SECTION */
	public CategoryCollection getCategoryCollection() {
		return categoryCollection;
	}

	public Category getSelectedSearchCategory() {
		return selectedSearchCategory;
	}

	public void setSelectedSearchCategory(Category selectedSearchCategory) {
		this.selectedSearchCategory = selectedSearchCategory;
	}

	public void searchCategoriesUpdated(RequestID requestID,
			CategoryCollection catCollection) {
		categoryCollection = catCollection;
		for (int i = 0; i < searchCategoriesListUpdatedListeners
				.getListenerInternalArray().length; i++) {
			SearchCategoriesListUpdatedListener listener = (SearchCategoriesListUpdatedListener) searchCategoriesListUpdatedListeners
					.getListenerInternalArray()[i];
			if (listener != null) {
				listener.onSearchCategoriesListUpdated(catCollection);
			}
		}
	}

	public void error(RequestID requestID, CoreError error) {
		if (this.handledErrors.contains(requestID.getRequestID())) {
			Log.i("NavigatorApplication", "error() This error["
					+ requestID.getRequestID() + "] has already been handled!");
			return;
		}
		this.handledErrors.add(requestID.getRequestID());

		if (this.routeRequestIds.contains(requestID)) {
			if (this.routeListener != null) {
				this.routeListener.error(requestID, error);
			}
		}

		if (requestID != null && requestID.equals(this.searchRequestId)) {
			if (this.searchResultsListener != null) {
				this.searchResultsListener.error(requestID, error);
			}
		}
		String requestIDString = "null";
		if (requestID != null) {
			requestIDString = "" + requestID.getRequestID();
		}
		int type = error.getErrorType();
		switch (type) {
		case CoreError.ERROR_GENERAL: {
			if (warningListener != null) {
				warningListener
						.displayGeneralErrorMessage(R.string.qtn_andr_error_try_again_txt);
			}
			Log.e("NavigatorApplication", "GeneralError[id:" + requestIDString
					+ "]: " + error.getInternalMsg());
			break;
		}
		case CoreError.ERROR_NETWORK: {
			NetworkError netError = (NetworkError) error;
			if (warningListener != null) {
				warningListener
						.displayNoInternetConnectionWarning(DISMISS_TIME_WARNING_DIALOG);
			}
			Log.e("NavigatorApplication", "NetworkError[id:" + requestIDString
					+ "]: " + netError.getInternalMsg());
			break;
		}
		case CoreError.ERROR_SEARCH: {
			SearchError searchError = (SearchError) error;
			if (warningListener != null) {
				// this should be improved in the future
				warningListener.displayNoInternetConnectionWarning(-1);
			}
			Log.e("NavigatorApplication", "SearchError[id:" + requestIDString
					+ "]: " + searchError.getInternalMsg());
			break;
		}
		case CoreError.ERROR_SERVER: {
			ServerError serverError = (ServerError) error;
			Log.e("NavigatorApplication", "ServerError[id:" + requestIDString
					+ "]: " + serverError.getInternalMsg());

			final String uri = serverError.getStatusUri();
			if (uri != null && uri.length() > 0) {
				Intent intent = new Intent(NavigatorApplication.this,
						ServicesActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(ServicesActivity.KEY_URI, uri);
				startActivity(intent);
			} else {
				switch (serverError.getStatusCode()) {
				case RouteError.ERRROUTE_ORIGIN_PROBLEM:
					if (warningListener != null) {
						warningListener
								.displayGeneralErrorMessage(R.string.qtn_andr_route_cant_b_calc_from_ur_pos_txt);
					}
					break;
				case RouteError.ERRROUTE_DESTINATION_PROBLEM:
					if (warningListener != null) {
						warningListener
								.displayGeneralErrorMessage(R.string.qtn_andr_route_cant_b_calc_2_req_pos_txt);
					}
					break;
				case RouteError.ERRROUTE_NOT_FOUND:
					if (warningListener != null) {
						warningListener
								.displayGeneralErrorMessage(R.string.qtn_andr_route_cant_b_calc_2_req_pos_txt);
					}
					break;
				case RouteError.ERRROUTE_TOO_FAR:
					if (warningListener != null) {
						warningListener
								.displayGeneralErrorMessage(R.string.qtn_andr_too_long_route_txt);
					}
					break;
				}
			}
			break;
		}
		case CoreError.ERROR_UNEXPECTED: {
			UnexpectedError unexpectedError = (UnexpectedError) error;
			if (warningListener != null) {
				warningListener
						.displayGeneralErrorMessage(R.string.qtn_andr_error_try_again_txt);
			}
			Log.e("NavigatorApplication", "UnexpectedError[id:"
					+ requestIDString + "]: "
					+ unexpectedError.getInternalMsg());
			break;
		}
		default: {
			if (warningListener != null) {
				warningListener
						.displayGeneralErrorMessage(R.string.qtn_andr_error_try_again_txt);
			}
			Log.e("NavigatorApplication", "Error[id:" + requestIDString + "]: "
					+ error.getInternalMsg());
			break;
		}
		}
	}

	public void addSearchCategoriesListUpdatedListener(
			SearchCategoriesListUpdatedListener listener) {
		searchCategoriesListUpdatedListeners.add(listener);
	}

	public void removeSearchCategoriesListUpdatedListener(
			SearchCategoriesListUpdatedListener listener) {
		searchCategoriesListUpdatedListeners.remove(listener);
	}

	/*
	 * ---------------- END OF SEARCH CATEGORIES RELATED SECTION
	 * -------------------
	 */

	/* +++++++++++++++++++++ TOP REGION RELATED SECTION +++++++++++++++++++++ */
	public TopRegion getSelectedTopRegion() {
		return selectedTopRegion;
	}

	public void setSelectedTopRegion(TopRegion aTopRegion, boolean addToHistory) {
		this.selectedTopRegion = aTopRegion;
		this.temporaryTopRegionSet = false;
		if (addToHistory && aTopRegion != null) {
			addSelectedTopRegion(aTopRegion);
		}
	}

	public void setSelectedTopRegion(String aTopRegionName) {
		for (int i = 0; i < topRegionCollections.getNbrOfRegions(); i++) {
			if (aTopRegionName.equals(topRegionCollections.getTopRegion(i)
					.getRegionName())) {
				setSelectedTopRegion(topRegionCollections.getTopRegion(i), true);
				break;
			}
		}
	}

	public TopRegionCollection getTopRegionCollection() {
		return topRegionCollections;
	}

	public boolean isCurrentTopRegionTemporary() {
		return (this.temporaryTopRegionSet || this.selectedTopRegion == null);
	}

	public void topregionsUpdated(RequestID requestID,
			TopRegionCollection topRegionCollection) {
		topRegionCollections = topRegionCollection;
		int nbrOfRegions = topRegionCollections.getNbrOfRegions();
		if (nbrOfRegions > 0) {
			this.temporaryTopRegionSet = true;
			selectedTopRegion = topRegionCollections.getTopRegion(0);
		}

		for (int i = 0; i < topRegionsListUpdatedListeners
				.getListenerInternalArray().length; i++) {
			TopRegionsListUpdatedListener listener = (TopRegionsListUpdatedListener) topRegionsListUpdatedListeners
					.getListenerInternalArray()[i];
			if (listener != null) {
				listener.onTopRegionsListUpdated(topRegionCollections);
			}
		}
	}

	public void currentTopRegion(RequestID requestID, Position position,
			TopRegion region) {
		if (this.selectedTopRegion == null || this.temporaryTopRegionSet) {
			this.selectedTopRegion = region;
			this.temporaryTopRegionSet = false;
		}

		for (int i = 0; i < topRegionsListUpdatedListeners
				.getListenerInternalArray().length; i++) {
			TopRegionsListUpdatedListener listener = (TopRegionsListUpdatedListener) topRegionsListUpdatedListeners
					.getListenerInternalArray()[i];
			if (listener != null) {
				listener.onTopRegionsListUpdated(topRegionCollections);
			}
		}
	}

	public void addTopRegionsListUpdatedListener(
			TopRegionsListUpdatedListener listener) {
		topRegionsListUpdatedListeners.add(listener);
	}

	public void removeTopRegionsListUpdatedListener(
			TopRegionsListUpdatedListener listener) {
		topRegionsListUpdatedListeners.remove(listener);
	}

	private void addSelectedTopRegion(TopRegion aTopRegion) {
		if (aTopRegion != null) {
			lastUsedTopRegions.remove(aTopRegion);
			lastUsedTopRegions.insertElementAt(aTopRegion, 0);
			if (lastUsedTopRegions.size() > 3) {
				lastUsedTopRegions.setSize(3);
			}
		}
	}

	public Stack<TopRegion> getLastUsedTopRegions() {
		return lastUsedTopRegions;
	}

	/* ---------------- END OF TOP REGION RELATED SECTION ------------------- */

	/* +++++++++++++++++++++ SEARCH RELATED SECTION +++++++++++++++++++++ */
	public void setSearchResultsListener(SearchResultsListener listener) {
		this.searchResultsListener = listener;
	}

	public String getSearchWhatStr() {
		return searchWhatStr;
	}

	public void setSearchWhatStr(String searchWhatStr) {
		this.searchWhatStr = searchWhatStr;
	}

	public String getSearchWhereStr() {
		return searchWhereStr;
	}

	public void setSearchWhereStr(String searchWhereStr) {
		this.searchWhereStr = searchWhereStr;
	}

	public void searchHistoryUpdated(SearchHistory aSearchHistory) {
		// XXX not used since there is no support in core for search history
	}

	public void usingExternalProviders(RequestID requestId,
			SearchProvider[] providers) {
		this.searchProviders = providers;
		if (this.searchProvidersUpdateListener != null) {
			this.searchProvidersUpdateListener.updateSearchProviders(providers);
		}
	}

	public void setSearchProvidersUpdateListener(
			SearchProvidersUpdateListener listener) {
		this.searchProvidersUpdateListener = listener;
	}

	public SearchProvider[] getSearchProviders() {
		return this.searchProviders;
	}
	
	public void searchDone(RequestID id, OneListSearchReply reply) {
		searchReply = reply;
		if (this.searchResultsListener != null) {
			this.searchResultsListener.searchDone(id, reply);
		}
	}

	public void searchUpdated(RequestID id, OneListSearchReply reply) {
		searchReply = reply;
		if (this.searchResultsListener != null) {
			this.searchResultsListener.searchUpdated(id, reply);
		}
	}

	public void clearSearch() {
		this.searchReply = null;
	}

	public SearchHistoryItem getPreviousSearchQuery() {
		return previousSearchQuery;
	}

	public void setPreviousSearchQuery(SearchHistoryItem previousSearchQuery) {
		this.previousSearchQuery = previousSearchQuery;
	}

	public SearchQuery getPreparedSearchQuery() {
		return preparedSearchQuery;
	}

	public void setPreparedSearchQuery(SearchQuery preparedSearchQuery) {
		this.preparedSearchQuery = preparedSearchQuery;
	}

	public SearchReply getSearchReply() {
		return searchReply;
	}

	public LocationProvider getLocationProvider() {
		return locationProvider;
	}

	public boolean isSearhQueryPending() {
		return searchQueryPending;
	}

	public void setSearchQueryPending(boolean searchQueryPending) {
		this.searchQueryPending = searchQueryPending;
	}

	public void setSearchRequestId(RequestID requestId) {
		this.searchRequestId = requestId;
	}

	/* ---------------- END OF SEARCH RELATED SECTION ------------------- */

	/* +++++++++++++++++++++ ROUTING RELATED SECTION +++++++++++++++++++++ */
	public RequestID navigate(RouteRequest routeRequest) {
		this.route = null;
		this.setRouteIsPending(true);
		this.setUseSensorForRoute(true);
		this.routeRequestIds.clear();
		this.isReroute = false;
		RequestID requestId = this.core.getRouteInterface().navigate(
				routeRequest, this.getInternalRouteListener());
		this.routeRequestIds.add(requestId);
		this.routeTransportMode = routeRequest.getRouteSettings()
				.getTransportMode();
		return requestId;
	}

	public RequestID[] requestRoutes(RouteRequest[] routeRequests) {
		this.route = null;
		this.routeRequests = routeRequests;
		return this.requestRoutes(false);
	}

	public RequestID[] reroute() {
		return this.requestRoutes(true);
	}

	private RequestID[] requestRoutes(boolean isReroute) {
		if (this.routeRequests != null) {
			this.setRouteIsPending(true);
			this.setUseSensorForRoute(true);

			this.isReroute = isReroute;
			this.routeRequestIds.clear();
			for (RouteRequest req : routeRequests) {
				this.routeRequestIds.add(this.core.getRouteInterface()
						.newRoute(req, this.getInternalRouteListener()));
				this.routeTransportMode = req.getRouteSettings()
						.getTransportMode();
			}

			return this.routeRequestIds
					.toArray(new RequestID[this.routeRequestIds.size()]);
		}
		return null;
	}

	public boolean isReroute(RequestID requestID) {
		return (this.hasRequestedRoutes() && (this.isReroute || !routeRequestIds
				.contains(requestID)));
	}

	public boolean hasRequestedRoutes() {
		return (this.routeRequestIds.size() > 0);
	}

	private RouteListener getInternalRouteListener() {
		if (this.internalRouteListener == null) {
			this.internalRouteListener = new RouteListener() {
				public void routeDone(RequestID requestID, Route route) {
					NavigatorApplication.this.routeDone(requestID, route);
				}

				public void error(RequestID requestID, CoreError error) {
					Log.e("NavigatorApplication",
							"getInternalRouteListener().error() "
									+ error.getInternalMsg());
					if (isReroute(requestID) && routeListener != null) {
						// The requestID is not one of our route-requests.
						// This means that we have had a reroute that has failed
						// somehow
						Log.e("NavigatorApplication",
								"getInternalRouteListener().error() This is a new Route-RequestID: "
										+ requestID.getRequestID());
						routeListener.error(requestID, error);
					} else {
						Log.e("NavigatorApplication",
								"getInternalRouteListener().error() This is a known Route-RequestID: "
										+ requestID.getRequestID());
						NavigatorApplication.this.error(requestID, error);
					}
				}
			};
		}

		return this.internalRouteListener;
	}

	public void setRouteIsPending(boolean isRoutePendgin) {
		this.isRoutePending = isRoutePendgin;
	}

	public boolean isRoutePending() {
		return this.isRoutePending;
	}

	public void setRouteListener(RouteListener listener) {
		this.routeListener = listener;
	}

	public void routeDone(RequestID requestID, Route route) {
		if (route == null) {
			Log.e("NavigatorApplication", "routeDone() returned route is NULL");
		} else {
			Log.i("NavigatorApplication", "routeDone() new route: " + route);
		}
		if (this.routeListener != null) {
			this.routeListener.routeDone(requestID, route);
		}
		this.setRouteIsPending(false);
	}

	public void removeRoute() {
		this.route = null;
		this.routeRequests = null;
		this.routeRequestIds.clear();
	}

	public void setRoute(Route route) {
		this.route = route;
		this.core.getRouteInterface().follow(/* route */);
	}

	public Route getRoute() {
		return this.route;
	}

	public boolean getUseSensorForRoute() {
		return this.useSensorForRoute;
	}

	public void setUseSensorForRoute(boolean useSensorForRoute) {
		this.useSensorForRoute = useSensorForRoute;
	}

	public int getRouteTransportMode() {
		return this.routeTransportMode;
	}

	public RouteSettings getRouteSettings(int transportMode) {
		ApplicationSettings settings = ApplicationSettings.get();
		int optimization = settings.getRouteOptimization();
		return this.getRouteSettings(transportMode, optimization);
	}

	public RouteSettings getRouteSettings(int transportMode, int optimization) {
		ApplicationSettings settings = ApplicationSettings.get();
		boolean avoidHighway = !settings.getRouteUseHighways();
		boolean avoidTollRoad = !settings.getRouteUseTollRoads();
		RouteSettings routeSettings = new RouteSettings(transportMode,
				avoidHighway, avoidTollRoad, optimization);
		return routeSettings;
	}

	/* ---------------- END OF ROUTING RELATED SECTION ------------------- */

	public void initWarnings() {
		if (warningsInitialized) {
			return;
		}
		warningsInitialized = true;
		if (this.phoneStateListener == null) {
			this.phoneStateListener = this.createPhoneStateListener();
		}

		if (this.telefonyManager == null) {
			this.telefonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		}
		this.telefonyManager.listen(this.phoneStateListener,
				PhoneStateListener.LISTEN_SERVICE_STATE
						| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
						| PhoneStateListener.LISTEN_CALL_STATE);
		if (isSIMCardAbsent()) {
			warningListener.displayNoSIMWarning();
		}

		boolean gpsFound = this.isGPSActive();
		if (!gpsFound) {
			if (this.warningListener != null) {
				this.warningListener.displayGpsWarning();
			}
		}
	}

	public boolean isGPSActive() {
		String stringValue = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		StringTokenizer st = new StringTokenizer(stringValue, ",");
		boolean gpsFound = false;
		while (st.hasMoreTokens()) {
			String provider = st.nextToken();
			if ("gps".equals(provider)) {
				gpsFound = true;
				break;
			}
		}
		return gpsFound;
	}

	private PhoneStateListener createPhoneStateListener() {
		return new PhoneStateListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.telephony.PhoneStateListener#onServiceStateChanged(android
			 * .telephony.ServiceState)
			 */
			@Override
			public void onServiceStateChanged(ServiceState serviceState) {
				isRoaming = serviceState.getRoaming();
				super.onServiceStateChanged(serviceState);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.telephony.PhoneStateListener#onDataConnectionStateChanged
			 * (int)
			 */
			@Override
			public void onDataConnectionStateChanged(int state) {
				if (state == TelephonyManager.DATA_CONNECTED) {
					if (displayNetworkErrorTask != null) {
						displayNetworkErrorTask.cancel();
						displayNetworkErrorTask = null;
					}
				}

				if (state == TelephonyManager.DATA_DISCONNECTED
						&& warningListener != null && isWifiEnabled()) {
					warningListener.displayWifiWarning();
				}

				if (isRoaming) {
					if (state == TelephonyManager.DATA_DISCONNECTED
							|| state == TelephonyManager.DATA_CONNECTING) {
						if (checkVersionTask != null) {
							checkVersionTask.cancel();
							checkVersionTask = null;
						}

						if (warningListener != null
								&& AbstractActivity.isApplicationActive()) {
							displayNoInternetConnectionDialog();
						}
					} else if (state == TelephonyManager.DATA_CONNECTED) {
						if (!isDataRoamingEnabled()) {
							warningListener.displayRoamingIsTurnedOffWarning();
						} else {
							if (!roamingWarningDisplayed
									&& warningListener != null
									&& AbstractActivity.isApplicationActive()) {
								warningListener
										.displayRoamingWarning(DISMISS_TIME_WARNING_DIALOG);
								roamingWarningDisplayed = true;
							}
						}
					}
				} else {
					if (state == TelephonyManager.DATA_DISCONNECTED
							|| state == TelephonyManager.DATA_CONNECTING) {
						if (checkVersionTask != null) {
							checkVersionTask.cancel();
							checkVersionTask = null;
						}

						if (warningListener != null
								&& AbstractActivity.isApplicationActive()) {
							displayNoInternetConnectionDialog();
						}
					}
				}
				super.onDataConnectionStateChanged(state);
			}

			private void displayNoInternetConnectionDialog() {
				// When roaming "No internet connection"-dialog is displayed
				// alot when starting the application.
				// A Timer that displays this only if network has been down more
				// than X seconds has been added
				if (timer == null) {
					timer = new Timer("NavigatorApplication-Timer");
				}
				if (displayNetworkErrorTask == null) {
					displayNetworkErrorTask = new TimerTask() {
						public void run() {
							handler.post(new Runnable() {
								public void run() {
									warningListener
											.displayNoInternetConnectionWarning(DISMISS_TIME_WARNING_DIALOG);
								}
							});
						}
					};
					timer.schedule(displayNetworkErrorTask, 10000);
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
			 * java.lang.String)
			 */
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				ApplicationSettings appSettings = ApplicationSettings.get();
				if (state == TelephonyManager.CALL_STATE_OFFHOOK
						|| state == TelephonyManager.CALL_STATE_RINGING) {
					core.getSoundInterface().setMute(true);
					voiceGuidanceMutedByPhoneCall = true;
				} else if (state == TelephonyManager.CALL_STATE_IDLE) {
					core.getSoundInterface().setMute(
							!appSettings.getRouteUseVoiceGuidance());
					voiceGuidanceMutedByPhoneCall = false;
				}
				warningListener.handleSoundMuteStateChanged();
				super.onCallStateChanged(state, incomingNumber);
			}
		};
	}

	public boolean isRoaming() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return ni.isRoaming();
	}

	public boolean isDataRoamingEnabled() {
		String stringValue = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.DATA_ROAMING);
		int intValue = Integer.parseInt(stringValue);
		return intValue == 1;
	}

	public boolean isSIMCardAbsent() {
		if (this.telefonyManager == null) {
			this.telefonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		}
		int simState = this.telefonyManager.getSimState();
		return simState == TelephonyManager.SIM_STATE_ABSENT;
	}

	public void setWarningListener(WarningListener listener) {
		this.warningListener = listener;
	}

	public String getUrlVersionApk() {
		return updater.getMarketUrlForPackageSearch();
	}

	private void checkVersion() {
		Map<Integer, String> settings = new HashMap<Integer, String>();

		settings.put(AutoUpdater.SETTING_CLIENT_ID, CLIENT_ID);
		settings.put(AutoUpdater.SETTING_VERSION_BASE,
				ApplicationSettings.URL_VERSION_BASE);
		settings.put(AutoUpdater.SETTING_VERSION_CHECK,
				ApplicationSettings.URL_VERSION_CHECK);
		settings.put(AutoUpdater.SETTING_MARKET_CHECK,
				ApplicationSettings.URL_MARKET_CHECK);

		updater = new AutoUpdater(this, settings);

		final String version = updater.checkVersion();

		if (version != null) {
			if (warningListener != null) {
				handler.post(new Runnable() {
					public void run() {
						warningListener.showNewVersionDialog(version);
					}
				});
			}
		}
	}

	/* ---------------- PLACE RELATED SECTION ------------------- */

	public RecentDestination[] getPreviousDestinations() {
		return previousDestinations
				.toArray(new RecentDestination[previousDestinations.size()]);
	}

	public ListModel getSavedPlaces() {
		if (this.core == null) {
			Looper.prepare();
			initiate(null);
		}
		return this.core.getFavoriteInterface().getFavoriteListModel();
	}

	// public AndroidFavorite getSavedPlace(String favId) {
	// int
	// return this.savedPlaces.get(favId);
	// return null;
	// }

	// XXX this method uses list index as id
	public Favorite getSavedPlace(int favId) {
		ListModel favList = this.core.getFavoriteInterface()
				.getFavoriteListModel();
		if (favId >= 0 && favId < favList.getSize()) {
			return (Favorite) this.core.getFavoriteInterface()
					.getFavoriteListModel().getElementAt(favId);
		}
		return null;
	}

	public void addSavedPlace(String name, String description, String iconName,
			Position position, InfoFieldList infoFieldList) {
		this.core.getFavoriteInterface().addFavorite(name, description,
				iconName, position, infoFieldList);
	}

	public void removeSavedPlace(Favorite fav) {
		this.core.getFavoriteInterface().removeFavorite(fav);
	}

	public void removePreviousDestination(RecentDestination destination) {
		placesDBAdapter.deletePreviousDestination(destination);
		previousDestinations.remove(destination);
	}

	public void removePreviousSearch(SearchHistoryItem item) {
		placesDBAdapter.deletePreviousSearch(item);
		previousSearches.remove(item);
	}

	public void updateSavedPlace(Favorite oldFavorite, String name,
			String description) {
		if (oldFavorite != null) {
			this.core.getFavoriteInterface().replaceFavorite(oldFavorite, name,
					description);
		}
	}

	public void updatePreviousDestination(RecentDestination destination,
			String name, String description) {
		if (destination != null) {
			destination.setName(name);
			destination.setDescription(description);
			placesDBAdapter.updatePreviusDestinations(destination);
		}
	}

	public void addPreviousDestination(RecentDestination newDestination) {
		// if a fav with an already existing id is used, copy the fav. The id is
		// set when adding to db
		if (newDestination.getId() != RecentDestination.INVALID_ID) {
			newDestination = new RecentDestination(newDestination);
		}
		for (int i = previousDestinations.size() - 1; i >= 0; i--) {
			final RecentDestination dest = previousDestinations.get(i);
			if (dest.isSameAs(newDestination)) {
				removePreviousDestination(dest);
			}
		}
		placesDBAdapter.addPreviousDestination(newDestination);
		previousDestinations.add(0, newDestination);
		while (previousDestinations.size() > MAX_NUMBER_OF_RECENT_DESTINATIONS) {
			Iterator<RecentDestination> it = previousDestinations.iterator();
			if (it.hasNext()) {
				RecentDestination oldestFav = it.next();

				while (it.hasNext()) {
					RecentDestination temp = it.next();
					if (oldestFav.getTimestamp() > temp.getTimestamp()) {
						oldestFav = temp;
					}
				}
				previousDestinations.remove(oldestFav);
				placesDBAdapter.deletePreviousDestination(oldestFav);
			}
		}
	}

	public void addPreviousSearch(SearchHistoryItem item) {
		for (int i = previousSearches.size() - 1; i >= 0; i--) {
			SearchHistoryItem shi = previousSearches.get(i);
			if (shi.hasSameValues(item)) {
				removePreviousSearch(shi);
			}
		}
		placesDBAdapter.addPreviousSearch(item);
		previousSearches.add(0, item);
		while (previousSearches.size() > MAX_NUMBER_OF_PREVIOUS_SEARCHES) {
			SearchHistoryItem oldestSearchItem = previousSearches.get(0);
			for (int i = 1; i < previousSearches.size(); i++) {
				if (oldestSearchItem.getTimestamp() > previousSearches.get(i)
						.getTimestamp()) {
					oldestSearchItem = previousSearches.get(i);
				}
			}

			previousSearches.remove(oldestSearchItem);
			placesDBAdapter.deletePreviousSearch(oldestSearchItem);
		}
	}

	public void removeAllPreviousDestionations() {
		placesDBAdapter.deleteAllPreviousDestinations();
		previousDestinations.clear();
	}

	private void readPlaces() {
		placesDBAdapter = new DatabaseAdapter(this);
		readSavedPlaces();
		readPreviousDestinations();
		readPreviousSearches();
	}

	private void readPreviousSearches() {
		previousSearches = placesDBAdapter.readPreviousSearches();
	}

	public ArrayList<SearchHistoryItem> getPreviousSearches() {
		return previousSearches;
	}

	private void readPreviousDestinations() {
		previousDestinations = placesDBAdapter.readPreviousDestionations();
	}

	private void readSavedPlaces() {
		ArrayList<RecentDestination> oldPlaces = placesDBAdapter
				.readSavedPlaces();
		for (RecentDestination oldPlace : oldPlaces) {
			addSavedPlace(oldPlace.getName(), oldPlace.getDescription(),
					oldPlace.getIconName(), oldPlace.getPosition(), null);
		}
		placesDBAdapter.deleteSavedPlacesTable();
	}

	public void removeAllPreviousSearches() {
		placesDBAdapter.deleteAllPreviousSearches();
		previousSearches.clear();

	}

	public void setFavoriteToBeUpdated(Favorite updateFav) {
		favoriteToBeUpdated = updateFav;
	}

	public Favorite getFavoriteToBeUpdated() {
		return favoriteToBeUpdated;
	}

	public void readPredictStrings() {
		predictiveWritingDBAdapter = new PredictiveWritingDatabaseAdapter(this);
		predictiveWritingTable = predictiveWritingDBAdapter
				.readPredictStrings();
	}

	public String[] getPredicStrings() {
		if (predictiveWritingTable != null) {
			String[] toReturn = new String[predictiveWritingTable.size()];
			int i = 0;
			for (Iterator<String> iterator = predictiveWritingTable
					.listIterator(); iterator.hasNext();) {
				toReturn[i++] = (String) iterator.next();
			}
			return toReturn;
		} else
			return new String[0];
	}

	public boolean addPredictString(String searchString) {
		if (predictiveWritingDBAdapter.addPredictString(searchString)) {
			predictiveWritingTable.add(searchString);
			return true;
		}
		return false;
	}

	public boolean isVoiceGuidanceMutedByPhoneCall() {
		return voiceGuidanceMutedByPhoneCall;
	}

	/**
	 * @return the pin
	 */
	public UserMapObject getUserPin() {
		return userPin;
	}

	/**
	 * @param pin
	 *            the pin to set
	 */
	public void setUserPin(UserMapObject pin) {
		this.userPin = pin;
	}

	public boolean isSoundMuted() {
		ApplicationSettings appSettings = ApplicationSettings.get();
		return isVoiceGuidanceMutedByPhoneCall()
				|| !appSettings.getRouteUseVoiceGuidance();
	}

	public void synchronizeDone(RequestID reqID, boolean hasChanges) {
		if (warningListener != null) {
			warningListener.displaySynchronizationCompletedMessage();
		}
	}

	public void loaded(RequestID id, int numberOfFavorites) {
		// TODO Auto-generated method updater

	}

	public boolean isWifiEnabled() {
		if (this.wifiManager == null) {
			this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		}
		return wifiManager.isWifiEnabled();
	}

	/**
	 * @return the shouldRecheckWifi
	 */
	public boolean shouldRecheckWifi() {
		return shouldRecheckWifi;
	}

	/**
	 * @param shouldRecheckWifi
	 *            the shouldRecheckWifi to set
	 */
	public void setShouldRecheckWifi(boolean shouldRecheckWifi) {
		this.shouldRecheckWifi = shouldRecheckWifi;
	}

	/**
	 * @return the iServiceWindowHistory
	 */
	public Stack<String> getServiceWindowHistory() {
		return iServiceWindowHistory;
	}

}
