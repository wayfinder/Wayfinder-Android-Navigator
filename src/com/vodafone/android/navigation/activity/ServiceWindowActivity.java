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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.dialog.AlertDialog;
import com.vodafone.android.navigation.servicewindow.PageHandler;
import com.vodafone.android.navigation.servicewindow.WFUriActionHandler;
import com.vodafone.android.navigation.servicewindow.WFWebClient;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.wayfinder.core.map.MapStartupListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.User;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.userdata.UserListener;

public class ServiceWindowActivity extends AbstractActivity implements
		WFUriActionHandler {

	public static final String KEY_SHOW_START_PAGE = "key_show_start_page";

	protected static final int DIALOG_LOADING = DIALOG_NEXT_AVAILABLE_ID;

	private static final String BUNDLE_URL = "URL";

	private WebView webView;
	private PageHandler iHandler;
	private WFWebClient iWebClient;

	protected NavigatorApplication application;

	@Override
	protected void onCreate(Bundle aSavedInstanceState) {
		super.onCreate(aSavedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView();
		application = (NavigatorApplication) getApplication();

		webView = (WebView) this.findViewById(R.id.web_view);
		iWebClient = new WFWebClient(this, this, this);
		webView.setWebViewClient(iWebClient);
		webView.getSettings().setJavaScriptEnabled(false);
		iHandler = new PageHandler(webView, iWebClient);

		Intent intent = this.getIntent();
		boolean showStartPage = intent.getBooleanExtra(KEY_SHOW_START_PAGE,
				false);

		if (showStartPage) {
			this.iHandler.openFirstPage();
		} else if (!application.getServiceWindowHistory().isEmpty()) {
			this.iWebClient.reload(webView);
		}
	}

	protected void setContentView() {
		this.setContentView(R.layout.service_window);
	}

	protected PageHandler getPageHandler() {
		return this.iHandler;
	}

	@Override
	protected void onSaveInstanceState(Bundle aOutState) {
		super.onSaveInstanceState(aOutState);
		aOutState.putString(BUNDLE_URL, webView.getUrl());
	}

	@Override
	protected void onRestoreInstanceState(Bundle aSavedInstanceState) {
		super.onRestoreInstanceState(aSavedInstanceState);
		iHandler.openServices(aSavedInstanceState.getString(BUNDLE_URL));
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		iHandler = null;

		webView.destroy();
		webView = null;
		iWebClient = null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean canGoBack = iWebClient.canGoBack();
		if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack) {
			iWebClient.goBack();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && !canGoBack) {
			application.getServiceWindowHistory().clear();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = this.getMenuInflater();
		menuInflater.inflate(R.menu.options_menu_service_window, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_LOADING:
			AlertDialog dialog = new AlertDialog(this);
			dialog.setMessage(R.string.qtn_andr_loading_txt);
			dialog.setProgressbarVisible(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if (iWebClient != null) {
						if (iWebClient.canGoBack()) {
							iWebClient.goBack();
						} else {
							application.getServiceWindowHistory().clear();
							finish();
						}
					}
				}
			});

			return dialog;

		default:
			return super.onCreateDialog(id);
		}

	}

	public void displayLoadingDialog() {
		showDialog(DIALOG_LOADING);
	}

	public void dismissLoadingDialog() {
		dismissDialog(DIALOG_LOADING);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.cmd_stop_service_window: {
			this.finish();
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	public boolean activated() {
		Log.e("ServiceWindowActivity", "activated() Implement me!!!");
		return false;
	}

	public boolean continueToMainMenu() {
		Intent intent = new Intent(this, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
		return true;
	}

	public boolean invokePhoneCall(String phoneNumber) {
		Intent dial = new Intent(Intent.ACTION_DIAL);
		dial.setData(Uri.parse("tel:" + phoneNumber));
		this.startActivity(dial);
		return true;
	}

	public boolean openEmailApplication(String anEmailAddress) {
		Intent email = new Intent(Intent.ACTION_VIEW);
		email.setData(Uri.parse("mailto:" + anEmailAddress));
		this.startActivity(email);
		return true;
	}

	public boolean openInExternalBrowser(String anUrl) {
		Intent web = new Intent(Intent.ACTION_VIEW);
		web.setData(Uri.parse(anUrl));
		this.startActivity(web);
		return true;
	}

	public boolean route(int waypoint, String name, int lat, int lon) {
		this.navigate(name, name, new Position(lat, lon), this.iHandler
				.getDetailsId(), ImageDownloader.IMAGE_NAME_DEFAULT);
		return true;
	}

	public boolean runTrial() {
		Log.e("ServiceWindowActivity", "runTrial() Implement me!!!");
		return false;
	}

	public boolean savefavorite(String name, String desc, int lat, int lon) {
		Log.e("ServiceWindowActivity", "saveFavorite() Implement me!!!");
		return false;
	}

	public boolean sendSMS(String phoneNumber, String text) {
		Intent sms = new Intent(this, SMSActivity.class);
		sms.putExtra(SMSActivity.KEY_PHONE_NUMBER, phoneNumber);
		sms.putExtra(SMSActivity.KEY_BODY, text);
		this.startActivity(sms);
		return true;
	}

	public boolean setNewServerList(String list) {
		Log.e("ServiceWindowActivity", "setNewServerList() Implement me!!!");
		return false;
	}

	public boolean setUin(String anUin) {
		this.getApp().getCore().getUserDataInterface().setUIN(anUin,
				new UserListener() {
					public void currentUser(User user) {
						Log.i("ServiceWindowActivity",
								"setUin() new user set: " + user);
						NavigatorApplication app = getApp();
						if (app.isMapInitiated()) {
							continueToMainMenu();
						} else {
							app.startMap(new MapStartupListener() {
								public void mapStartupComplete(int statusCode) {
									continueToMainMenu();
								}
							});
						}
					}

					public void error(RequestID requestID, CoreError error) {
						Log.e("ServiceWindowActivity", "setUin() Error: "
								+ error);
						exitApplication();
					}
				});
		return true;
	}

	public void setWFIDActivationHoldOffParams(int consecutiveStartups,
			int minDaysRequired, int maxDaysAllowed) {
		Log.e("ServiceWindowActivity",
				"setWFIDActivationHoldOffParams() Implement me!!!");
	}

	public boolean showOnMap(int lat, int lon, int zoomLevel) {
		Intent intent = new Intent(this, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(SearchActivity.KEY_START_LAT, lat);
		intent.putExtra(SearchActivity.KEY_START_LON, lon);
		intent.putExtra(SearchActivity.KEY_START_ZOOM, zoomLevel);
		this.startActivity(intent);
		return true;
	}

	public boolean upgradeApplication(String key, String name, String phoneNbr,
			boolean allowSpam, String email) {
		Log.e("ServiceWindowActivity", "upgradeApplication() Implement me!!!");
		return false;
	}

	public boolean upgradeClient(String anUpgradeUrl) {
		Log.e("ServiceWindowActivity", "upgradeClient() Implement me!!!");
		return false;
	}

	public boolean userTermsAccepted() {
		Log.e("ServiceWindowActivity", "userTermsAccepted() Implement me!!!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vodafone.android.navigation.activity.AbstractActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		new Handler().postDelayed(new Runnable() {
			public void run() {
				getApp().initWarnings();
			}
		}, 1000);
		
		if(iWebClient != null){
		    iWebClient.setServiceWindowVisible(true);
		}
	}
	
	@Override
	protected void onPause() {
	    if(iWebClient != null){
	    	iWebClient.setServiceWindowVisible(false);
	    }
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.vodafone.android.navigation.activity.AbstractActivity#
	 * shouldBeCloseOnError()
	 */
	@Override
	public boolean shouldBeCloseOnError() {
		return true;
	}
}
