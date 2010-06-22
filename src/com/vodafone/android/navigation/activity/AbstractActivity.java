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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.components.RecentDestination;
import com.vodafone.android.navigation.dialog.AlertDialog;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.LandmarkMapObject;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.vodafone.android.navigation.service.RouteService;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.Criteria;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.positioning.LocationListener;
import com.wayfinder.core.positioning.LocationProvider;
import com.wayfinder.core.route.RouteListener;
import com.wayfinder.core.route.RouteRequest;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.route.Route;
import com.wayfinder.core.shared.route.RoutePointRequestable;
import com.wayfinder.core.shared.route.RouteSettings;

public abstract class AbstractActivity extends WarningActivity implements
		RouteListener, LocationListener {

	// If false the "Choose transportation mode"-dialog is not displayed and
	// car-mode is selected automatically
	private static final boolean PEDESTRIAN_MODE_ENABLED = false;
	private static final String LOGTAG = "AbstractActivity";

	private static int createdActivities = 0;
	private static int activeActivities = 0;
	private static AbstractActivity activityOnTop;

	public static final int REQUEST_CODE_CONTEXT_MENU = 0;
	public static final int REQUEST_CODE_TRANSPORT = REQUEST_CODE_CONTEXT_MENU + 1;
	public static final int REQUEST_CODE_NEXT_AVAILABLE_ID = REQUEST_CODE_TRANSPORT + 1;

	public static final int ACCURACY_MAX = Criteria.ACCURACY_GOOD;

	private NavigatorApplication application;
	private int transportMode;
	private boolean waitingForPositionDialogDisplayed;
	private boolean waitingForPosition;
	private Position destination;
	private int errorMessageId;
	private AndroidMapObject mapObject;
	private boolean contextMenuDisplayed;
	protected AudioManager audioManager;
	protected boolean searchWaitingForPosition;
	private long savePlacesDBId;
	private int autoDismissTime;
	private String newVersionId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		this.application = (NavigatorApplication) this.getApplicationContext();
		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void onStart() {
		super.onStart();

		createdActivities++;
		if (createdActivities == 1) {
			// This is the first activity started.
			Log.i(LOGTAG, "onStart() starting positioning");
			this.application.pausePositioning(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.application.setWarningListener(this);

		activeActivities++;
		activityOnTop = this;

		this.application.setRouteListener(this);
		this.application.addLocationListener(this);

		if (this.canInitiateCore()) {
			this
					.setBrightness(this.application.getMapInterface()
							.isNightMode());
		}

		if (application.shouldRecheckWifi()) {
			if (this.application.isWifiEnabled()) {
				displayWifiWarning();
			} else {
				application.setShouldRecheckWifi(false);
			}
		}
	}

	protected boolean canInitiateCore() {
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();

		activeActivities--;
		if (activeActivities <= 0) {
			// None of the WayfinderActivities are interacting with the user
			// right now.
			// Beware, this could mean that we have a dialog on top of the
			// WayfinderActivity.
		}

		this.application.setRouteListener(null);
		this.application.removeLocationListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		createdActivities--;
		if (createdActivities <= 0) {
			createdActivities = 0;
			// No activities are visible on the screen. Be aware that this
			// method may never be called from the system
			Log.i(LOGTAG, "onStop() Last activity stopped[" + this + "]");
			this.application.pausePositioning(true);
		}
	}

	protected NavigatorApplication getApp() {
		if (this.application == null) {
			this.application = (NavigatorApplication) this
					.getApplicationContext();
		}
		return this.application;
	}

	protected void stopRoute() {
		NavigatorApplication app = this.getApp();
		app.removeRoute();

		setNightMode(this, false);

		Log.i("AbstractActivity", "Removing route");
		VectorMapInterface map = app.getMapInterface();
		map.getMapDetailedConfigInterface().setRoute(null);

		RouteService.stopRouting(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.vodafone.android.navigation.listeners.WarningListener#
	 * displayDataRoamingWarning()
	 */
	public void displayRoamingIsTurnedOffWarning() {
		NavigatorApplication app = getApp();
		if (app.isSIMCardAbsent()) {
			displayNoSIMWarning();
			return;
		}

		if (isApplicationActive()) {
			showDialog(DIALOG_ROAMING_IS_TURNED_OFF_WARNING);
		} else {
			Toast.makeText(this, R.string.qtn_andr_roaming_off_warning_txt,
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.vodafone.android.navigation.listeners.WarningListener#
	 * displayRoamingWarning()
	 */
	public void displayRoamingWarning(int autoDismissTime) {
		if (isApplicationActive()) {
			this.autoDismissTime = autoDismissTime;
			showDialog(DIALOG_ROAMING_WARNING);
		} else {
			Toast.makeText(this, R.string.qtn_andr_roaming_warning_mess_txt,
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.vodafone.android.navigation.listeners.WarningListener#
	 * displaySafetyWarning()
	 */
	public void displaySafetyWarning() {
		if (this.application.displaySafetyWarning()) {
			if (ApplicationSettings.get().getDisplayWarningMessage()) {
				showDialog(DIALOG_SAFETY_WARNING);
				this.application.setDisplaySafetyWarning(false);
			}
		}
	}

	public void displayGpsWarning() {
		if (isApplicationActive()) {
			showDialog(DIALOG_GPS_WARNING);
		} else {
			Toast.makeText(this, R.string.qtn_andr_set_gps_txt,
					Toast.LENGTH_LONG).show();
		}
	}

	public void displayWifiWarning() {
		application.setShouldRecheckWifi(true);
		if (isApplicationActive()) {
			super.showDialog(DIALOG_WIFI_WARNING);
		}
	}

	public void displaySynchronizationCompletedMessage() {
		Toast.makeText(this, R.string.qtn_andr_sync_completed_txt,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void showNewVersionDialog(String newVersionID) {
		this.newVersionId = newVersionID;
		showDialog(DIALOG_NEW_VERSION_AVAILABLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ROAMING_WARNING: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_note_txt);
			dialog.setMessage(R.string.qtn_andr_roaming_warning_mess_txt);
			dialog.setNeutralButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							AbstractActivity.this
									.dismissDialog(DIALOG_ROAMING_WARNING);
						}
					});
			dialog.setNegativeButton(R.string.qtn_andr_close_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							exitApplication();
						}
					});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			return dialog;
		}
		case DIALOG_ROAMING_IS_TURNED_OFF_WARNING: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_note_txt);
			dialog.setMessage(R.string.qtn_andr_roaming_off_warning_txt);
			dialog.setNeutralButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							AbstractActivity.this
									.dismissDialog(DIALOG_ROAMING_IS_TURNED_OFF_WARNING);
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					// Works for bringing up the settings
					try {
						Intent i = new Intent(Settings.ACTION_SETTINGS);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					} catch (Throwable t) {
						Log.e("AbstractActivity", "onDismiss() " + t);
					}
				}
			});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			return dialog;
		}
		case DIALOG_SAFETY_WARNING: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_safety_warning_txt);

			View content = View.inflate(this, R.layout.dialog_warning_message,
					null);
			dialog.setView(content);

			final CheckBox checkbox = (CheckBox) dialog
					.findViewById(R.id.check_box);
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					ApplicationSettings settings = ApplicationSettings.get();
					settings.setDisplayWarningMessage(!isChecked);
					settings.commit();
					checkbox.invalidate();
				}
			});

			dialog.setNeutralButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			return dialog;
		}
		case DIALOG_GPS_WARNING: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_note_txt);
			dialog.setMessage(R.string.qtn_andr_set_gps_txt);
			dialog.setNeutralButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					// Works for bringing up the gps-settings
					try {
						Intent i = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					} catch (Throwable t) {
						Log.e("AbstractActivity", "onDismiss() " + t);
					}
				}
			});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			return dialog;
		}
		case DIALOG_ROUTING: {
			View routeView = View.inflate(this, R.layout.route_dialog, null);
			TextView transportMessage = (TextView) routeView
					.findViewById(R.id.text_transport);

			Resources res = getResources();
			String transportModeStr = null;
			int transportMode = this.transportMode;
			if (transportMode == RouteSettings.TRANSPORT_MODE_PEDESTRIAN) {
				transportModeStr = res.getString(R.string.qtn_andr_walking_txt);
			} else if (transportMode == RouteSettings.TRANSPORT_MODE_CAR) {
				transportModeStr = res.getString(R.string.qtn_andr_driving_txt);
			}

			transportMessage.setText(this.getResources().getString(
					R.string.qtn_andr_route_for_txt, transportModeStr));

			AlertDialog dialog = new AlertDialog(this);
			dialog.setView(routeView);
			dialog.setTitle(R.string.qtn_andr_loading_txt);
			dialog.setIcon(R.drawable.search_dialog_icon);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					getApp().setRouteListener(null);
					getApp().setRouteIsPending(false);
				}
			});
			return dialog;
		}
		case DIALOG_ROUTING_RETRY_REQUEST: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle("");
			dialog.setMessage(R.string.qtn_andr_rerouting_failes_retry_qtxt);
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setAutoDismissTime(10,
					R.string.qtn_andr_time_until_retry_txt);
			dialog.setCancelable(true);
			dialog.setPositiveButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.setNegativeButton(R.string.qtn_andr_edit_cancel_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					// User cancelled the dialog; close this activity
					stopRoute();
				}
			});
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					// User wants to retry; request the route again
					reroute();
				}
			});
			return dialog;
		}
		case DIALOG_WAITING_FOR_POSITION: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_waiting_for_pos_txt);
			dialog.setMessage(R.string.qtn_andr_weak_gps_signal_txt);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					waitingForPositionDialogDisplayed = false;
					waitingForPosition = false;
					searchWaitingForPosition = false;
					getApp().setRouteIsPending(false);
				}
			});
			return dialog;
		}
		case DIALOG_NO_SIM_WARNING: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_note_txt);
			dialog.setMessage(R.string.qtn_andr_insert_sim_txt);
			dialog.setNeutralButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					AbstractActivity.this.finish();
				}
			});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			return dialog;
		}
		case DIALOG_NO_INTERNET_CONNECTION: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_note_txt);
			dialog.setMessage(R.string.qtn_andr_set_conn_txt);
			dialog.setPositiveButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.setNeutralButton(R.string.qtn_andr_settings_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							AbstractActivity.this
									.dismissDialog(DIALOG_NO_INTERNET_CONNECTION);
							// Works for bringing up the settings
							try {
								Intent i = new Intent(Settings.ACTION_SETTINGS);
								i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(i);
							} catch (Throwable t) {
								Log.e("AbstractActivity", "onDismiss() " + t);
							}
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					if (shouldBeCloseOnError()) {
						AbstractActivity.this.finish();
					}
				}
			});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			return dialog;
		}
		case DIALOG_GENERAL_ERROR: {
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(R.string.qtn_andr_note_txt);
			dialog.setMessage(errorMessageId);
			dialog.setNeutralButton(R.string.qtn_andr_ok_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							AbstractActivity.this
									.dismissDialog(DIALOG_GENERAL_ERROR);
						}
					});
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					if (shouldBeCloseOnError()) {
						AbstractActivity.this.finish();
					}
				}
			});
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			return dialog;
		}
		case DIALOG_NEW_VERSION_AVAILABLE:
			AlertDialog dialog = new AlertDialog(this);
			dialog.setTitle(getString(
					R.string.qtn_andr_update_app_2_version_txt, newVersionId));
			dialog.setMessage(R.string.qtn_andr_new_version_available_txt);
			dialog.setNegativeButton(R.string.qtn_andr_edit_cancel_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.setPositiveButton(R.string.qtn_andr_upgrade_tk,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							try {
								Intent i = new Intent(
										Intent.ACTION_VIEW,
										Uri
												.parse(application.getUrlVersionApk()));
								startActivity(i);
								finish();
							} catch (Throwable t) {
								Log.e(LOGTAG, "Download intent failed " + t);
							}
						}
					});
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
				}
			});
			return dialog;
		default: {
			return super.onCreateDialog(id);
		}
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_ROAMING_WARNING: {
			AlertDialog alert = (AlertDialog) dialog;
			alert.setAutoDismissTime(this.autoDismissTime);
			break;
		}
		case DIALOG_NO_INTERNET_CONNECTION: {
			AlertDialog alert = (AlertDialog) dialog;
			alert.setAutoDismissTime(this.autoDismissTime);
			break;
		}
		case DIALOG_ROUTING_RETRY_REQUEST: {
			AlertDialog alert = (AlertDialog) dialog;
			alert
					.setAutoDismissTime(10,
							R.string.qtn_andr_time_until_retry_txt);
			break;
		}
		default: {
			super.onPrepareDialog(id, dialog);
		}
		}
	}

	/**
	 * This method can be overriden if extending classes wants special behaviour
	 * when cancelling the waiting-for-position-dialog. For instance
	 * AbstractRouteActivity wants to stop the current route.
	 */
	protected void onCancelWaitingForPositionDialog() {
	}

	public static boolean isApplicationActive() {
		Log.i("AbstractActivity", "created activities: " + createdActivities);
		return createdActivities > 0;
	}

	public static boolean isApplicationVisible() {
		Log.i("AbstractActivity", "active activities: " + activeActivities);
		return activeActivities > 0;
	}

	/**
	 * 
	 * @param id
	 * @param title
	 * @param position
	 * @param imageName
	 * @param contentMenuType
	 *            - possible values ContextActivity.TYPE_MAP,
	 *            ContextActivity.TYPE_SEARCH, ContextActivity.TYPE_PLACES,
	 *            ContextActivity.TYPE_DESTINATIONS.
	 */

	public void displayContextMenu(AndroidMapObject mapObject) {
		if (!this.contextMenuDisplayed) {
			this.contextMenuDisplayed = true;
			this.mapObject = mapObject;

			Intent intent = new Intent(this, ContextActivity.class);
			String title = this.mapObject.getName();
			String desc = this.mapObject.getDesc();
			intent.putExtra(ContextActivity.KEY_TITLE, title);
			intent.putExtra(ContextActivity.KEY_DESC, desc);
			int contextType = ContextActivity.TYPE_ADD;

			if (this.mapObject.getContextMenuType() == ContextActivity.TYPE_PLACES) {
				intent.putExtra(ContextActivity.KEY_DISPLAY_INFO, false);
				contextType = ContextActivity.TYPE_EDIT;
				application
						.setFavoriteToBeUpdated(((LandmarkMapObject) mapObject)
								.getLandmark().getFavorite());
			}

			if (this.mapObject.getContextMenuType() == ContextActivity.TYPE_DESTINATIONS) {
				intent.putExtra(ContextActivity.KEY_DISPLAY_INFO, false);
				contextType = ContextActivity.TYPE_ADD;
			}

			intent.putExtra(ContextActivity.KEY_TYPE, contextType);
			intent.putExtra(ContextActivity.KEY_IMAGE_ID, this.mapObject
					.getImageName());
			this.startActivityForResult(intent, REQUEST_CODE_CONTEXT_MENU);
		}
	}

	protected void setMapObject(AndroidMapObject mapObject) {
		this.mapObject = mapObject;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String imageName = null;
		if (this.mapObject != null) {
			imageName = this.mapObject.getImageName();
		}
		if (imageName == null
				|| imageName.equals(ImageDownloader.IMAGE_NAME_EMPTY)) {
			imageName = ImageDownloader.IMAGE_NAME_DEFAULT;
		}

		if (requestCode == REQUEST_CODE_CONTEXT_MENU && this.mapObject != null) {
			this.contextMenuDisplayed = false;
			String id = this.mapObject.getId();
			String title = this.mapObject.getName();
			String description = this.mapObject.getDesc();
			Position position = this.mapObject.getPosition();
			long placeDBId = this.mapObject.getPlaceDBId();

			if (resultCode == ContextActivity.RESULT_NAVIGATE_TO) {
				Log.i("SERVER_STRING", id);
				this.navigate(title, description, position, id, imageName);
			} else if (resultCode == ContextActivity.RESULT_DISPLAY_DETAILS) {
				Intent intent = new Intent(this, PlaceDetailsActivity.class);
				intent.putExtra(PlaceDetailsActivity.KEY_ID, id);
				intent.putExtra(PlaceDetailsActivity.KEY_LAT, position
						.getMc2Latitude());
				intent.putExtra(PlaceDetailsActivity.KEY_LON, position
						.getMc2Longitude());
				intent.putExtra(PlaceDetailsActivity.KEY_IMAGE_NAME, imageName);
				intent.putExtra(PlaceDetailsActivity.KEY_TITLE, title);
				intent.putExtra(PlaceDetailsActivity.KEY_DESCRIPTION,
						description);
				intent.putExtra(PlaceDetailsActivity.KEY_IS_ALREADY_SAVED,
						placeDBId > 0);
				this.startActivity(intent);
			} else if (resultCode == ContextActivity.RESULT_SAVE_PLACE) {
				Intent intent = new Intent(this, EditPlaceActivity.class);
				intent.putExtra(EditPlaceActivity.TYPE_KEY,
						EditPlaceActivity.TYPE_SAVE_PLACE);
				intent.putExtra(EditPlaceActivity.NAME_KEY, title);
				intent.putExtra(EditPlaceActivity.DESCRIPTION_KEY, description);
				intent.putExtra(EditPlaceActivity.SRVSTRING_KEY, id);
				intent.putExtra(EditPlaceActivity.LAT_KEY, position
						.getMc2Latitude());
				intent.putExtra(EditPlaceActivity.LON_KEY, position
						.getMc2Longitude());
				intent.putExtra(EditPlaceActivity.IMAGENAME_KEY, imageName);
				this.startActivity(intent);
			} else if (resultCode == ContextActivity.RESULT_EDIT_PLACE) {
				application
						.setFavoriteToBeUpdated(((LandmarkMapObject) mapObject)
								.getLandmark().getFavorite());
				Intent intent = new Intent(this, EditPlaceActivity.class);
				intent.putExtra(EditPlaceActivity.TYPE_KEY,
						EditPlaceActivity.TYPE_EDIT_PLACE);
				intent.putExtra(EditPlaceActivity.SRVSTRING_KEY, id);
				intent.putExtra(EditPlaceActivity.ID_KEY, savePlacesDBId);
				intent.putExtra(EditPlaceActivity.NAME_KEY, title);
				intent.putExtra(EditPlaceActivity.DESCRIPTION_KEY, description);
				intent.putExtra(EditPlaceActivity.RECENT_PLACE_ID_KEY,
						placeDBId);
				this.startActivity(intent);
			} else {
				// if none was chosen (ie. back was pressed) then we abort
				return;
			}
		} else if (requestCode == REQUEST_CODE_TRANSPORT
				&& this.mapObject != null) {
			int transportMode = RouteSettings.TRANSPORT_MODE_CAR;
			if (resultCode == TransportActivity.RESULT_PEDESTRIAN_MODE) {
				transportMode = RouteSettings.TRANSPORT_MODE_PEDESTRIAN;
			} else if (resultCode == TransportActivity.RESULT_CAR_MODE) {
				transportMode = RouteSettings.TRANSPORT_MODE_CAR;
			} else {
				// if none was chosen (ie. back was pressed) then we abort
				// routing
				return;
			}

			Position position = this.mapObject.getPosition();
			this.navigate(transportMode, position);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	protected void navigate(String title, String desc,
			Position contextPosition, String contextId, String imageName) {
		RecentDestination recentDestination = new RecentDestination(title,
				desc, contextPosition, System.currentTimeMillis(), imageName,
				null); // TODO add poiInfo
		this.navigate(recentDestination);
	}

	protected void navigate(RecentDestination recentDestination) {
		if (PEDESTRIAN_MODE_ENABLED) {
			Intent intent = new Intent(this, TransportActivity.class);
			this.startActivityForResult(intent, REQUEST_CODE_TRANSPORT);
		} else {
			this.navigate(RouteSettings.TRANSPORT_MODE_CAR, recentDestination
					.getPosition());
		}

		// Only add favorite to previous destinations if it has either a name or
		// a description
		String name = recentDestination.getName();
		String desc = recentDestination.getDescription();
		if ((name != null && name.length() > 0)
				|| (desc != null && desc.length() > 0)) {
			this.application.addPreviousDestination(recentDestination);
		}
	}

	private void navigate(int transportMode, Position destination) {
		NavigatorApplication app = this.getApp();
		this.destination = destination;
		this.transportMode = transportMode;

		if (!app.isGPSActive()) {
			this.displayGpsWarning();
		} else {
			LocationInformation loc = app.getOwnLocationInformation();
			if (loc == null || this.shouldDisplayWaitingForPositionDialog(loc)) {
				this.waitingForPosition = true;
			} else {
				Position origin = loc.getMC2Position();
				navigate(origin, this.destination);
			}
		}
	}

	private void navigate(RoutePointRequestable origin,
			RoutePointRequestable destination) {
		NavigatorApplication app = this.getApp();

		RouteRequest[] routeRequests = new RouteRequest[] { RouteRequest
				.createRequest(origin, destination, app
						.getRouteSettings(this.transportMode)) };
		// RouteRequest[] routeRequests = new RouteRequest[] {
		// RouteRequest.createRequest(origin, destination,
		// app.getRouteSettings(this.transportMode,
		// RouteSettings.OPTIMIZE_DISTANCE)),
		// RouteRequest.createRequest(origin, destination,
		// app.getRouteSettings(this.transportMode,
		// RouteSettings.OPTIMIZE_TIME_AND_TRAFFIC))
		// };

		app.requestRoutes(routeRequests);
		this.showDialog(DIALOG_ROUTING);
	}

	private void reroute() {
		NavigatorApplication app = this.getApp();
		app.reroute();
		this.showDialog(DIALOG_ROUTING);
	}

	public void error(RequestID requestID, CoreError error) {
		Log.e("AbstractActivity", "error() " + error);

		NavigatorApplication app = this.getApp();
		if (app.hasRequestedRoutes()) {
			try {
				this.dismissDialog(DIALOG_ROUTING);
			} catch (IllegalArgumentException e) {
			}

			if (app.isReroute(requestID)) {
				// Ok, so we got an error after we have requested a route.
				// If this is a network error we will display a dialog asking
				// the user to retry
				if (error.getErrorType() == CoreError.ERROR_NETWORK) {
					this.showDialog(DIALOG_ROUTING_RETRY_REQUEST);
				}
			}
		}
	}

	public void routeDone(RequestID requestID, Route route) {
		try {
			this.dismissDialog(DIALOG_ROUTING);
		} catch (IllegalArgumentException e) {
		}

		this.application.setRoute(route);

		Log.i("AbstractActivity", "routeDone() RouteId: " + route.getRouteID());

		Intent service = new Intent(this, RouteService.class);
		this.startService(service);

		if (this.application.isRoutePending()) {
			// This should not be done if the route is a reroute...
			// Whenever the user tries to navigate away from routeactivity he
			// will be transported back.
			Intent intent = new Intent(this, RouteOverviewActivity.class);
			intent.putExtra(RouteOverviewActivity.KEY_AUTOMATIC_ROUTING, true);
			this.startActivity(intent);
		}
	}

	/**
	 * returns true if Waiting-for-valid-position dialog should/is shown,
	 * otherwise false
	 * 
	 * @param accuracy
	 * @return
	 */
	protected boolean shouldDisplayWaitingForPositionDialog(
			LocationInformation info) {
		if (NavigatorApplication.isEmulator()
				|| ApplicationSettings.get().getAlwaysRoute()) {
			return false;
		} else {
			int accuracy = info.getAccuracy();
			Log.i("AbstractActivity",
					"shouldDisplayWaitingForPosition() accuracy: " + accuracy);
			if (accuracy > ACCURACY_MAX
					|| this.application.isOldAndInvalidLocation(info)) {
				// This position is not good enough for navigation
				if (!this.waitingForPositionDialogDisplayed) {
					this.showDialog(DIALOG_WAITING_FOR_POSITION);
					this.waitingForPositionDialogDisplayed = true;
				}
				return true;
			} else {
				// finally a good position, let's remove the waiting dialog
				if (this.waitingForPositionDialogDisplayed) {
					// remove waiting dialog;
					this.dismissDialog(DIALOG_WAITING_FOR_POSITION);
					this.waitingForPositionDialogDisplayed = false;
				}
				return false;
			}
		}
	}

	public void locationUpdate(LocationInformation locationInformation,
			LocationProvider locationProvider) {
		if (this.waitingForPosition) {
			boolean display = this
					.shouldDisplayWaitingForPositionDialog(locationInformation);
			if (!display) {
				Position origin = locationInformation.getMC2Position();
				this.navigate(origin, this.destination);
				this.waitingForPosition = false;
			}
		}
	}

	public static void setNightMode(Context context, boolean nightMode) {
		NavigatorApplication app = ((NavigatorApplication) context
				.getApplicationContext());
		VectorMapInterface map = app.getMapInterface();
		map.setNightMode(nightMode);

		if (activityOnTop != null) {
			Log.e("AbstractActivity", activityOnTop.getClass().getName());
			activityOnTop.setBrightness(nightMode);
		}
	}

	private void setBrightness(boolean nightMode) {
		float brightness;
		if (nightMode) {
			brightness = 0.2f;
		} else {
			try {
				brightness = (float) Settings.System.getInt(this
						.getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS) / 256f;
			} catch (SettingNotFoundException e) {
				Log.e("AbstractActivity",
						"onResume() error when getting brightness, setting value to 60%. Error: "
								+ e);
				brightness = 0.6f;
			}
		}
		Window window = this.getWindow();
		LayoutParams lp = window.getAttributes();
		lp.screenBrightness = brightness; // 0 - 1
		window.setAttributes(lp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.vodafone.android.navigation.listeners.WarningListener#
	 * displayNoInternetConnectionWarning()
	 */
	public void displayNoInternetConnectionWarning(int autoDismissTime) {
		this.autoDismissTime = autoDismissTime;
		NavigatorApplication app = getApp();
		if (app.isSIMCardAbsent()) {
			displayNoSIMWarning();
			return;
		}

		if (app.isWifiEnabled()) {
			displayWifiWarning();
			return;
		}

		if (app.isRoaming() && !app.isDataRoamingEnabled()) {
			displayRoamingIsTurnedOffWarning();
			return;
		}

		if (isApplicationActive()) {
			// Let?s disable the dialog for now...
			showDialog(DIALOG_NO_INTERNET_CONNECTION);
		} else {
			Toast.makeText(this, R.string.qtn_andr_set_conn_txt,
					Toast.LENGTH_LONG);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vodafone.android.navigation.listeners.WarningListener#displayNoSIMWarning
	 * ()
	 */
	public void displayNoSIMWarning() {
		showDialog(DIALOG_NO_SIM_WARNING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vodafone.android.navigation.listeners.WarningListener#displayErrorMessage
	 * (java.lang.String)
	 */
	public void displayGeneralErrorMessage(int messageId) {
		errorMessageId = messageId;
		if (isApplicationActive()) {
			showDialog(DIALOG_GENERAL_ERROR);
		} else {
			Toast.makeText(this, messageId, Toast.LENGTH_LONG);
		}
	}

	public boolean shouldBeCloseOnError() {
		return false;
	}

	public boolean exitApplication() {
		Log.e("ServiceWindowActivity",
				"exitApplication() Application will exit!");

		Intent intent = new Intent(this, SplashActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(SplashActivity.KEY_EXIT, true);
		this.startActivity(intent);
		finish();
		return true;
	}

	public void handleSoundMuteStateChanged() {
	}

}
