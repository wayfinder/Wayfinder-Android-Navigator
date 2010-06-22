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

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.SearchWhereAdapter;
import com.vodafone.android.navigation.components.SearchHistoryItem;
import com.vodafone.android.navigation.dialog.AlertDialog;
import com.vodafone.android.navigation.listeners.MapInterationDetectedListener;
import com.vodafone.android.navigation.listeners.SearchCategoriesListUpdatedListener;
import com.vodafone.android.navigation.listeners.TopRegionsListUpdatedListener;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.util.ResourceUtil;
import com.vodafone.android.navigation.view.MapLayerView;
import com.vodafone.android.navigation.view.WayfinderMapView;
import com.vodafone.android.navigation.view.MapLayerView.SurfaceChangeListener;
import com.wayfinder.core.geocoding.GeocodeListener;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.search.Category;
import com.wayfinder.core.search.CategoryCollection;
import com.wayfinder.core.search.SearchQuery;
import com.wayfinder.core.search.TopRegion;
import com.wayfinder.core.search.TopRegionCollection;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.geocoding.AddressInfo;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;

public class SearchActivity extends AbstractMapActivity implements
		View.OnFocusChangeListener, MapInterationDetectedListener,
		TopRegionsListUpdatedListener, SearchCategoriesListUpdatedListener,
		SurfaceChangeListener {

	public static final String KEY_START_LAT = "key_start_lat";
	public static final String KEY_START_LON = "key_start_lon";
	public static final String KEY_START_ZOOM = "key_start_zoom";
	public static final String KEY_SHOW_FIELDS = "show_fields";

	public static final int LOCAL_SEARCH_RADIUS = 50000;

	private static final int MENU_ITEM_PLACES_ID = 1;
	private static final int MENU_ITEM_SETTINGS_ID = 3;
	private static final int MENU_ITEM_SEARCH_HISTORY_ID = 4;
	private static final int MENU_ITEM_OWN_POSITION_ID = 5;
	private static final int MENU_ITEM_HELP_ID = 6;
	private static final int MENU_ITEM_ROUTE_ID = 7;
	private static final int MENU_ITEM_ABOUT_ID = 8;
	private static final int MENU_ITEM_PLAN_ROUTE_ID = 9;
	private static final int MENU_ITEM_SEARCH = 10;

	private static final int REQUEST_CODE_CATEGORIES = REQUEST_CODE_NEXT_AVAILABLE_ID;
	private static final int REQUEST_CODE_SEARCH_RESULTS = REQUEST_CODE_CATEGORIES + 1;
	private static final int REQUEST_CODE_SEARCH_HISTORY = REQUEST_CODE_SEARCH_RESULTS + 1;
	private static final int REQUEST_CODE_COUNTRIES = REQUEST_CODE_SEARCH_HISTORY + 1;
	private static final int REQUEST_CODE_PLAN_ROUTE_ID = REQUEST_CODE_COUNTRIES + 1;

	private static final int DIALOG_NO_POSITION = DIALOG_NEXT_AVAILABLE_ID;
	private static final int DIALOG_ENTER_DATA = DIALOG_NO_POSITION + 1;
	private static final int DIALOG_EXIT_APPLICATION = DIALOG_ENTER_DATA + 1;

	private int copyrightTextDrawingOffset;

	protected RequestID requestId;

	private WayfinderMapView mapView;
	private ImageButton searchButton;
	private Button selectCountry;
	private EditText searchWhat;
	private AutoCompleteTextView searchCity;
	private Button selectCategory;
	private Runnable hideCategoryPost;
	private NavigatorApplication application;
	private MenuItem cmdRoute;
	private CheckBox chkboxLocalSearch;
	private boolean disregardFocusChange;
	private boolean isShowFields;
	private int copyrightTextPosition = 30;
	private boolean isFieldsVisible;
	private SearchWhereAdapter searchWhereAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_activity);

		this.application = (NavigatorApplication) this.getApplicationContext();

		copyrightTextDrawingOffset = getResources().getDimensionPixelSize(
				R.dimen.copyright_text_drawing_offset);
		this.mapView = (WayfinderMapView) findViewById(R.id.map);
		this.mapView.setMapInterationListener(this);

		this.mapView.getMapLayer().setSurfaceChangeListener(this);

		Intent intent = this.getIntent();
		final int lat = intent.getIntExtra(KEY_START_LAT, 0);
		final int lon = intent.getIntExtra(KEY_START_LON, 0);
		final int zoom = intent.getIntExtra(KEY_START_ZOOM, 0);

		int target = intent.getIntExtra(SplashActivity.KEY_TARGET_ACTIVITY,
				SplashActivity.TARGET_LOCAL_SEARCH);
		Log.i("SearchActivity", "onCreate() target: " + target);
		if (target == SplashActivity.TARGET_ROUTE) {
			Intent targetIntent = new Intent(this, RouteActivity.class);
			this.startActivity(targetIntent);
		} else if (target == SplashActivity.TARGET_SAVED_PLACES) {
			Intent targetIntent = new Intent(this, PlacesActivity.class);
			long contentId = intent.getLongExtra(PlacesActivity.KEY_PLACE_ID,
					-1);
			targetIntent.putExtra(PlacesActivity.KEY_PLACE_ID, contentId);
			this.startActivity(targetIntent);
		}

		if (lat != 0 && lon != 0 && zoom != 0) {
			mapView.getMapLayer().centerMapTo(lat, lon, true, new Runnable() {
				public void run() {
					mapView.getMapLayer().getMap().setScale(zoom);
				}
			});
		}

		searchWhat = (EditText) findViewById(R.id.edittext_search);
		searchWhat.setText(this.application.getSearchWhatStr());
		searchWhat.setOnFocusChangeListener(this);
		searchWhat
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							startSearch();
						}
						return false;
					}
				});

		searchWhat.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					showFields(true);
				}
				return false;
			}
		});

		this.selectCategory = (Button) findViewById(R.id.select_cateory);
		Category selectedSearchCategory = this.application
				.getSelectedSearchCategory();
		if (selectedSearchCategory != null) {
			selectCategory.setText(selectedSearchCategory.getCategoryName());
		} else {
			selectCategory
					.setText(getString(R.string.qtn_andr_no_categories_txt));
		}
		this.selectCategory.setOnFocusChangeListener(this);
		this.selectCategory.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SearchActivity.this,
						CategorySelectionActivity.class);
				startActivityForResult(intent, REQUEST_CODE_CATEGORIES);
			}
		});

		searchCity = (AutoCompleteTextView) findViewById(R.id.edittext_city);
		searchCity.setText(this.application.getSearchWhereStr());
		searchCity.setOnFocusChangeListener(this);
		searchCity
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							startSearch();
						}
						return false;
					}
				});

		searchWhereAdapter = new SearchWhereAdapter(this,
				R.layout.auto_complete_item, application.getPredicStrings());

		searchCity.setAdapter(searchWhereAdapter);

		selectCountry = (Button) findViewById(R.id.select_country);
		selectCountry.setOnFocusChangeListener(this);
		TopRegion selectedTopRegion = this.application.getSelectedTopRegion();
		if (selectedTopRegion != null) {
			selectCountry.setText(selectedTopRegion.getRegionName());
		}
		selectCountry.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SearchActivity.this,
						CountrySelectionActivity.class);
				SearchActivity.this.startActivityForResult(intent,
						SearchActivity.REQUEST_CODE_COUNTRIES);
			}
		});

		searchButton = (ImageButton) findViewById(R.id.search_button);
		searchButton.setEnabled(true);
		searchButton.setOnFocusChangeListener(this);
		searchButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startSearch();
			}
		});

		boolean isLocalSearch = ApplicationSettings.get().getIsLocalSearch();
		chkboxLocalSearch = (CheckBox) findViewById(R.id.chkbox_search_around_me);
		FormattingResult result = this.getApp().getUnitsFormatter()
				.formatDistance(SearchActivity.LOCAL_SEARCH_RADIUS);
		String distance = result.getRoundedValue() + " " + result.getUnit();
		String text = this.getResources().getString(
				R.string.qtn_andr_search_around_me_txt, distance);
		chkboxLocalSearch.setText(text);
		chkboxLocalSearch.setChecked(isLocalSearch);
		chkboxLocalSearch.setOnFocusChangeListener(this);
		chkboxLocalSearch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton checkbox,
							boolean checked) {
						updateFields();
					}
				});

		if (application.isSIMCardAbsent()) {
			this.displayNoSIMWarning();
		} else {
			this.displaySafetyWarning();
		}

		if (this.application.isInitialized()) {
			if (!this.chkboxLocalSearch.isChecked()) {
				ArrayList<SearchHistoryItem> searchHistory = this.application
						.getPreviousSearches();
				if (searchHistory != null && searchHistory.size() > 0) {
					SearchHistoryItem item = searchHistory.get(0);
					item.setupInternalData(this.application
							.getTopRegionCollection(), this.application
							.getCategoryCollection());
					// this.populateSearchQuery(this.application, item);
				}
			}
		} else {
			this.application.getCore();
		}

		this.application.addTopRegionsListUpdatedListener(this);
		this.application.addSearchCategoriesListUpdatedListener(this);

		this.showFields(false);
		this.updateFields();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		this.saveViewData();
		super.onSaveInstanceState(outState);
	}

	private void saveViewData() {
		this.application.setSearchWhatStr(searchWhat.getText().toString());
		this.application.setSearchWhereStr(searchCity.getText().toString());
	}

	@Override
	protected WayfinderMapView getMap() {
		return this.mapView;
	}

	private void startSearch() {
		if (searchButton.isEnabled()) {
			String searchString = this.searchWhat.getText().toString();
			Category category = this.application.getSelectedSearchCategory();
			if ((searchString != null && searchString.length() > 0)
					|| category != null) {
				if (this.chkboxLocalSearch.isChecked()) {
					Position position = this.application
							.getOwnLocationInformation().getMC2Position();
					LocationProvider provider = this.application
							.getLocationProvider();
					if ((provider != null && position.isValid())
							|| NavigatorApplication.isEmulator()
							|| ApplicationSettings.get().getAlwaysRoute()) {
						SearchQuery query = SearchQuery.createPositionalQuery(
								searchString, category, position,
								LOCAL_SEARCH_RADIUS);
						this.application.setPreparedSearchQuery(query);
						this.application.setSearchQueryPending(true);
						Intent intent = new Intent(this,
								SearchResultsActivity.class);
						intent.putExtra(
								SearchResultsActivity.KEY_SEARCH_AROUND_ME,
								true);
						startActivityForResult(intent,
								REQUEST_CODE_SEARCH_RESULTS);
					} else {
						searchWaitingForPosition = true;
						this.showDialog(DIALOG_WAITING_FOR_POSITION);
					}
				} else {
					String city = searchCity.getText().toString();
					if (city.length() > 0) {
						if (application.addPredictString(city)) {
							searchWhereAdapter.add(city);
						}
					}
					TopRegion topRegion = this.application
							.getSelectedTopRegion();
					if ((searchString != null && searchString.length() > 0)
							|| category != null) {
						SearchQuery query = SearchQuery.createRegionalQuery(
								searchString, category, city, topRegion);
						this.application.setPreparedSearchQuery(query);
						this.application
								.addPreviousSearch(new SearchHistoryItem(query,
										System.currentTimeMillis()));
						this.application.setSearchQueryPending(true);
						Intent intent = new Intent(SearchActivity.this,
								SearchResultsActivity.class);
						intent.putExtra(
								SearchResultsActivity.KEY_SEARCH_AROUND_ME,
								false);
						startActivityForResult(intent,
								REQUEST_CODE_SEARCH_RESULTS);
					} else {
						Log.e("SearchActivity", "No data entered");
						this.showDialog(DIALOG_ENTER_DATA);
					}
				}
			} else {
				Log.e("SearchActivity", "No data entered");
				this.showDialog(DIALOG_ENTER_DATA);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_PLAN_ROUTE_ID, Menu.NONE,
				R.string.qtn_andr_plan_route_tk).setIcon(
				R.drawable.cmd_plan_route);

		this.cmdRoute = menu.add(Menu.NONE, MENU_ITEM_ROUTE_ID, Menu.NONE,
				R.string.qtn_andr_return_drive_tk)
				.setIcon(R.drawable.cmd_route);

		menu.add(Menu.NONE, MENU_ITEM_OWN_POSITION_ID, Menu.NONE,
				R.string.qtn_andr_my_position_tk).setIcon(
				R.drawable.cmd_my_position);

		menu.add(Menu.NONE, MENU_ITEM_SEARCH, Menu.NONE,
				R.string.qtn_andr_search_tk).setIcon(R.drawable.cmd_search);

		menu.add(Menu.NONE, MENU_ITEM_PLACES_ID, Menu.NONE,
				R.string.qtn_andr_places_tk).setIcon(R.drawable.cmd_my_places);

		menu.add(Menu.NONE, MENU_ITEM_SETTINGS_ID, Menu.NONE,
				R.string.qtn_andr_settings_tk).setIcon(R.drawable.cmd_settings);

		menu.add(Menu.NONE, MENU_ITEM_HELP_ID, Menu.NONE,
				R.string.qtn_andr_help_tk).setIcon(R.drawable.cmd_help);

		menu.add(Menu.NONE, MENU_ITEM_ABOUT_ID, Menu.NONE,
				R.string.qtn_andr_about_tk).setIcon(R.drawable.cmd_about);

		menu.add(Menu.NONE, MENU_ITEM_SEARCH_HISTORY_ID, Menu.NONE,
				R.string.qtn_andr_prev_searches_tk).setIcon(
				R.drawable.cmd_previous_search);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.cmdRoute.setVisible(this.getApp().getRoute() != null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Intent intent;

		switch (item.getItemId()) {
		case MENU_ITEM_PLAN_ROUTE_ID: {
			intent = new Intent(this, PlanRouteActivity.class);
			startActivityForResult(intent, REQUEST_CODE_PLAN_ROUTE_ID);
			return true;
		}
		case MENU_ITEM_ROUTE_ID: {
			intent = new Intent(this, RouteActivity.class);
			startActivity(intent);
			return true;
		}
		case MENU_ITEM_SEARCH: {
			clearFields();
			this.showFields(true);
			return true;
		}
		case MENU_ITEM_SEARCH_HISTORY_ID: {
			if (getApp().getPreviousSearches().isEmpty()) {
				Toast.makeText(this, R.string.qtn_andr_no_prev_searches_txt,
						Toast.LENGTH_LONG).show();
				return true;
			}
			intent = new Intent(this, PreviousSearchesActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SEARCH_HISTORY);
			return true;
		}
		case MENU_ITEM_PLACES_ID: {
			intent = new Intent(this, PlacesActivity.class);
			intent.putExtra(PlacesActivity.KEY_TYPE,
					PlacesActivity.TYPE_SAVED_PLACES);
			getApp().getMapInterface().setFollowGpsPosition(false);
			startActivityForResult(intent, -1);
			return true;
		}
		case MENU_ITEM_SETTINGS_ID: {
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		case MENU_ITEM_OWN_POSITION_ID: {
			LocationInformation locInfo = this.application
					.getOwnLocationInformation();
			LocationProvider provider = this.application.getLocationProvider();
			if (provider != null) {
				final Position position = locInfo.getMC2Position();
				mapView.centerMapTo(position.getMc2Latitude(), position
						.getMc2Longitude(), true, new Runnable() {
					public void run() {
						mapView.getMapLayer().getMap().setGpsPosition(
								position.getMc2Latitude(),
								position.getMc2Longitude(), 0);
						try {
							SearchActivity.this.requestId = application
									.getCore().getGeocodeInterface()
									.reverseGeocode(position,
											new GeocodeListener() {
												public void reverseGeocodeDone(
														RequestID requestID,
														AddressInfo addressInfo) {
													if (requestID
															.equals(requestId)) {
														String address = ResourceUtil
																.getAddressAsString(
																		addressInfo,
																		true);
														Toast
																.makeText(
																		SearchActivity.this,
																		address,
																		Toast.LENGTH_LONG)
																.show();
													}
												}

												public void error(
														RequestID requestID,
														CoreError error) {
													Log
															.e(
																	"SearchActivity.GeocodeListener",
																	"error() "
																			+ error
																					.getInternalMsg());
												}
											});

						} catch (IllegalArgumentException e) {
							Log.e("SearchActivity", "centerMapTo() " + e);
							e.printStackTrace();
						}
						mapView.getMapLayer().getMap().setFollowGpsPosition(
								true);
					}
				});
			} else {
				Toast.makeText(this, R.string.qtn_andr_no_loc_warn_mess_txt,
						Toast.LENGTH_LONG).show();
			}
			return true;
		}
		case MENU_ITEM_HELP_ID: {
			intent = new Intent(this, HelpActivity.class);
			startActivity(intent);
			return true;
		}
		case MENU_ITEM_ABOUT_ID: {
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		NavigatorApplication app = this.getApp();
		switch (requestCode) {
		case -1:
			return;
		case REQUEST_CODE_CATEGORIES: {
			Category category = this.application.getSelectedSearchCategory();
			if (category != null) {
				selectCategory.setText(category.getCategoryName());
			} else {
				selectCategory
						.setText(getString(R.string.qtn_andr_no_categories_txt));
			}
			this.showFields(true);
			this.disregardFocusChange = true;
			selectCountry.setFocusable(true);
			selectCategory.requestFocus();
			break;
		}
		case REQUEST_CODE_SEARCH_RESULTS: {
			if (resultCode == SearchResultsActivity.RESULT_NO_RESULTS) {
				Toast.makeText(this, R.string.qtn_andr_no_result_found_txt,
						Toast.LENGTH_LONG).show();
			} else if (resultCode == SearchResultsActivity.RESULT_ERROR) {
				// TODO: save information about this and make sure to trigger
				// the search if this error was due to billing
				Toast.makeText(this, R.string.qtn_andr_no_result_found_txt,
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		case REQUEST_CODE_SEARCH_HISTORY: {
			if (resultCode == Activity.RESULT_OK) {
				SearchHistoryItem searchHistoryItem = app
						.getPreviousSearchQuery();
				if (searchHistoryItem == null) {
					ArrayList<SearchHistoryItem> searchHistory = app
							.getPreviousSearches();
					if (searchHistory != null && searchHistory.size() > 0) {
						searchHistoryItem = searchHistory.get(0);
					}
				}
				this.populateSearchQuery(app, searchHistoryItem);
				this.chkboxLocalSearch.setChecked(false);
			}

			this.showFields(true);
			this.updateFields();
			this.disregardFocusChange = true;
			return;
		}
		case REQUEST_CODE_COUNTRIES: {
			TopRegion selectedTopRegion = app.getSelectedTopRegion();
			if (selectedTopRegion != null) {
				selectCountry.setText(selectedTopRegion.getRegionName());
			}
			this.disregardFocusChange = true;
			selectCountry.setFocusable(true);
			selectCountry.requestFocus();
			return;
		}
		case REQUEST_CODE_PLAN_ROUTE_ID: {
			if (resultCode == PlanRouteActivity.RESULT_SEARCH) {
				this.showFields(true);
				this.disregardFocusChange = true;
			} else if (resultCode == PlanRouteActivity.RESULT_MYPLACES) {
				Intent intent = new Intent(this, PlacesActivity.class);
				intent.putExtra(PlacesActivity.KEY_TYPE,
						PlacesActivity.TYPE_SAVED_PLACES);
				getApp().getMapInterface().setFollowGpsPosition(false);
				startActivity(intent);
			}
			return;
		}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void populateSearchQuery() {
		NavigatorApplication app = this.getApp();
		TopRegion topRegion = app.getSelectedTopRegion();
		if (topRegion == null) {
			TopRegionCollection topRegions = app.getTopRegionCollection();
			if (topRegions != null) {
				topRegion = topRegions.getTopRegion(0);
			}
		}
		if (topRegion != null) {
			selectCountry.setText(topRegion.getRegionName());
		}

		selectCategory.setText(getString(R.string.qtn_andr_no_categories_txt));

		app.setSelectedTopRegion(topRegion, false);
	}

	private void populateSearchQuery(NavigatorApplication app,
			SearchHistoryItem searchHistoryItem) {
		if (searchHistoryItem != null) {
			if (searchHistoryItem.getCategory() != null) {
				selectCategory.setText(searchHistoryItem.getCategory()
						.getCategoryName());
			} else {
				selectCategory
						.setText(getString(R.string.qtn_andr_no_categories_txt));
			}
			app.setSelectedSearchCategory(searchHistoryItem.getCategory());
			if (searchHistoryItem.getTopRegion() != null) {
				selectCountry.setText(searchHistoryItem.getTopRegion()
						.getRegionName());
			}
			app.setSelectedTopRegion(searchHistoryItem.getTopRegion(), false);
			searchWhat.setText(searchHistoryItem.getWhatText());
			app.setSearchWhatStr(searchHistoryItem.getWhatText());
			searchCity.setText(searchHistoryItem.getWhereText());
			app.setSearchWhereStr(searchHistoryItem.getWhereText());
		} else {
			selectCategory
					.setText(getString(R.string.qtn_andr_no_categories_txt));
			app.setSelectedSearchCategory(null);

			TopRegion topRegion = app.getSelectedTopRegion();
			if (topRegion == null) {
				TopRegionCollection topRegions = app.getTopRegionCollection();
				topRegion = topRegions.getTopRegion(0);
			}
			selectCountry.setText(topRegion.getRegionName());
			app.setSelectedTopRegion(topRegion, false);

			searchWhat.setText("");
			app.setSearchWhatStr("");

			searchCity.setText("");
			app.setSearchWhereStr("");
		}
	}

	public void onFocusChange(View view, boolean hasFocus) {
		// we're not checking what view is getting/loosing focus here, since we
		// know what views are listening for the focus-change
		Handler handler = this.selectCategory.getHandler();
		if (hasFocus) {
			if (view instanceof EditText) {
				((EditText) view).selectAll();
			}
			this.showFields(true);

			if (handler != null && this.hideCategoryPost != null) {
				handler.removeCallbacks(this.hideCategoryPost);
			}
		} else if (!disregardFocusChange) {
			// There seems to be no way of getting a callback when the current
			// view
			// looses focus. So instead we delay the setVisibility(Gone) on the
			// categoryButton. If we get focus on some of the buttons listening
			// to
			// focus-change, we cancel the post
			if (this.hideCategoryPost == null) {
				this.hideCategoryPost = new Runnable() {
					public void run() {
						showFields(false);
					}
				};
			}

			if (handler != null) {
				handler.postDelayed(this.hideCategoryPost, 500);
			}
		}
		this.disregardFocusChange = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		this.mapView = (WayfinderMapView) findViewById(R.id.map);
		this.setupMap();
		this.mapView.getPinsLayer().setPositionPinEnabled(true);

		Category category = this.application.getSelectedSearchCategory();
		if (category != null) {
			this.selectCategory.setText(category.getCategoryName());
		} else {
			this.selectCategory
					.setText(getString(R.string.qtn_andr_no_categories_txt));
		}
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout_focus_holder);
		layout.requestFocus();

		new Handler().postDelayed(new Runnable() {
			public void run() {
				getApp().initWarnings();
			}
		}, 1000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		this.mapView.getPinsLayer().setPositionPinEnabled(false);
		super.onPause();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_NO_POSITION: {
			AlertDialog alert = new AlertDialog(this);
			alert.setTitle(R.string.qtn_andr_note_txt);
			alert.setMessage(R.string.qtn_andr_no_loc_warn_mess_txt);
			return alert;
		}
		case DIALOG_ENTER_DATA: {
			AlertDialog alert = new AlertDialog(this);
			alert.setTitle(R.string.qtn_andr_note_txt);
			alert.setMessage(R.string.qtn_andr_search_incomplete_txt);
			return alert;
		}
		case DIALOG_EXIT_APPLICATION: {
			AlertDialog alert = new AlertDialog(this);
			alert.setTitle(R.string.qtn_andr_exit_nav_title);
			alert.setMessage(R.string.qtn_andr_exit_nav_txt);
			alert.setPositiveButton(R.string.qtn_andr_yes_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							exitApplication();
						}
					});

			alert.setNegativeButton(R.string.qtn_andr_no_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(DIALOG_EXIT_APPLICATION);
						}
					});
			alert.setIcon(android.R.drawable.ic_dialog_alert);
			return alert;
		}
		}

		return super.onCreateDialog(id);
	}

	protected void updateActiveScreenPoint() {
		if (getMap() != null) {
			MapLayerView mapLayer = getMap().getMapLayer();
			int mapHeight = mapLayer.getHeight();
			int centerVisibleY = mapHeight >> 1;
			int centerVisibleX = (mapLayer.getWidth() >> 1);
			VectorMapInterface map = mapLayer.getMap();
			Log.i("SearchActivity", "Setting active screen[" + centerVisibleX
					+ ", " + centerVisibleY + "]");
			map.setActiveScreenPoint(centerVisibleX, centerVisibleY);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			LinearLayout content = (LinearLayout) this
					.findViewById(R.id.content);
			MapLayerView mapLayer = mapView.getMapLayer();
			mapLayer.setVisibleMapArea(content);
			updateActiveScreenPoint();

			if (!this.isShowFields) {
				this.copyrightTextPosition = mapLayer.getHeight()
						- searchWhat.getHeight() - copyrightTextDrawingOffset;
			}
			this.getMap().updateCopyrightTextPosition(
					this.copyrightTextPosition);
		}
	}

	public void mapInterationDetected() {
		if (this.isShowFields) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.layout_focus_holder);
			layout.requestFocus();
			this.showFields(false);
		}
		this.application.mapInteractionDetected();
	}

	public void onTopRegionsListUpdated(TopRegionCollection topRegions) {
		if (!this.application.isCurrentTopRegionTemporary()) {
			this.getApp().removeTopRegionsListUpdatedListener(this);
			this.populateSearchQuery();
		}
	}

	public void onSearchCategoriesListUpdated(CategoryCollection catCollection) {
		this.getApp().removeSearchCategoriesListUpdatedListener(this);
		this.populateSearchQuery();
	}

	private void showFields(boolean visible) {
		if (!this.isShowFields) {
			MapLayerView mapLayer = mapView.getMapLayer();
			this.copyrightTextPosition = mapLayer.getHeight()
					- searchWhat.getHeight() - copyrightTextDrawingOffset;
		}
		this.getMap().updateCopyrightTextPosition(this.copyrightTextPosition);

		this.isShowFields = visible;
		int visibility = (visible ? View.VISIBLE : View.GONE);

		boolean isLocalSearch = this.chkboxLocalSearch.isChecked();

		this.selectCategory.setVisibility(visibility);
		this.chkboxLocalSearch.setVisibility(visibility);
		this.searchButton.setVisibility(visibility);
		this.searchCity.setVisibility(visibility);
		this.selectCountry.setVisibility(visibility);

		this.searchCity.setEnabled(!isLocalSearch);
		this.searchCity.setFocusable(!isLocalSearch);
		this.searchCity.setFocusableInTouchMode(!isLocalSearch);

		this.selectCountry.setEnabled(!isLocalSearch);

		int hintWhatId = R.string.qtn_andr_search_POI_txt;
		if (!this.isShowFields) {
			hintWhatId = R.string.qtn_andr_tap_2_enter_search_txt;
		} else if (isLocalSearch) {
			hintWhatId = R.string.qtn_andr_find_around_me_txt;
		}
		this.searchWhat.setHint(hintWhatId);

		if (!isFieldsVisible && visible) {
			searchWhat.requestFocus();
		}
		this.isFieldsVisible = visible;
	}

	private void updateFields() {
		boolean isLocalSearch = this.chkboxLocalSearch.isChecked();

		ApplicationSettings settings = ApplicationSettings.get();
		settings.setIsLocalSearch(isLocalSearch);
		settings.commit();

		this.searchCity.setEnabled(!isLocalSearch);
		this.searchCity.setFocusable(!isLocalSearch);
		this.searchCity.setFocusableInTouchMode(!isLocalSearch);

		this.selectCountry.setEnabled(!isLocalSearch);

		int hintWhatId = R.string.qtn_andr_search_POI_txt;

		if (isLocalSearch) {
			if (!this.isShowFields) {
				hintWhatId = R.string.qtn_andr_tap_2_enter_search_txt;
			} else {
				hintWhatId = R.string.qtn_andr_find_around_me_txt;
			}
		} else {
			if (!this.isShowFields) {
				hintWhatId = R.string.qtn_andr_tap_2_enter_search_txt;
			} else {
				hintWhatId = R.string.qtn_andr_search_POI_txt;
			}
		}

		this.searchWhat.setHint(hintWhatId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isFieldsVisible) {
				showFields(false);
				return true;
			} else {
				this.showDialog(DIALOG_EXIT_APPLICATION);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			startSearch();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (isFieldsVisible) {
				startSearch();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void clearFields() {
		searchWhat.setText("");
		searchCity.setText("");
		selectCategory.setText(getString(R.string.qtn_andr_no_categories_txt));
		application.setSelectedSearchCategory(null);

		LocationInformation locInfo = application.getOwnLocationInformation();
		Position position = locInfo.getMC2Position();
		application.getCore().getGeocodeInterface().reverseGeocode(position,
				new GeocodeListener() {
					public void reverseGeocodeDone(RequestID requestID,
							AddressInfo adressInfo) {
						String topRegion = adressInfo.getCountryOrState();
						application.setSelectedTopRegion(topRegion);
						selectCountry.setText(topRegion);
					}

					public void error(RequestID requestID, CoreError error) {
						Log.e("SearchActivity",
								"GeocodeListener.error() Using old topRegion. Error msg: "
										+ error.getInternalMsg());
						TopRegion selectedTopRegion = application
								.getSelectedTopRegion();
						if (selectedTopRegion != null) {
							selectCountry.setText(selectedTopRegion
									.getRegionName());
						}
					}
				});
	}

	public void surfaceChanged(int width, int height) {
		WayfinderMapView map = this.getMap();
		if (!this.isShowFields) {
			this.copyrightTextPosition = height - searchWhat.getHeight()
					- copyrightTextDrawingOffset;
		}
		map.updateCopyrightTextPosition(this.copyrightTextPosition);
		map.getMapLayer().getMap().requestMapUpdate();
	}

	public void locationUpdate(LocationInformation locationInformation,
			LocationProvider locationProvider) {
		if (searchWaitingForPosition
				&& locationInformation.getMC2Position().isValid()) {
			this.dismissDialog(DIALOG_WAITING_FOR_POSITION);
			this.searchWaitingForPosition = false;
			startSearch();
		}
		super.locationUpdate(locationInformation, locationProvider);
	}
}
