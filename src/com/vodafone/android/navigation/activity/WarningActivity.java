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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.provider.Settings;
import android.util.Log;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.dialog.AlertDialog;
import com.vodafone.android.navigation.listeners.WarningListener;

public class WarningActivity extends Activity implements WarningListener {

	protected static final int DIALOG_ROAMING_WARNING = 0;
    protected static final int DIALOG_ROAMING_IS_TURNED_OFF_WARNING = DIALOG_ROAMING_WARNING + 1;
    protected static final int DIALOG_SAFETY_WARNING = DIALOG_ROAMING_IS_TURNED_OFF_WARNING + 1;
    protected static final int DIALOG_GPS_WARNING = DIALOG_SAFETY_WARNING + 1;
    protected static final int DIALOG_WIFI_WARNING = DIALOG_GPS_WARNING + 1;
    protected static final int DIALOG_ROUTING = DIALOG_WIFI_WARNING + 1;
    protected static final int DIALOG_ROUTING_RETRY_REQUEST = DIALOG_ROUTING + 1;
    protected static final int DIALOG_WAITING_FOR_POSITION = DIALOG_ROUTING_RETRY_REQUEST + 1;
	protected static final int DIALOG_NO_SIM_WARNING = DIALOG_WAITING_FOR_POSITION + 1;
	protected static final int DIALOG_NO_INTERNET_CONNECTION = DIALOG_NO_SIM_WARNING + 1;
	protected static final int DIALOG_GENERAL_ERROR = DIALOG_NO_INTERNET_CONNECTION + 1;
	protected static final int DIALOG_SELECT_PHONE_NUMBER_ID = DIALOG_GENERAL_ERROR + 1;
	protected static final int DIALOG_NEW_VERSION_AVAILABLE = DIALOG_SELECT_PHONE_NUMBER_ID + 1;
	protected static final int DIALOG_NEXT_AVAILABLE_ID = DIALOG_NEW_VERSION_AVAILABLE + 1;
		
	public void displayGeneralErrorMessage(int messageId) {}
	
	public void displayGpsWarning() {}

	public void displayNoInternetConnectionWarning(int autoDismissTime) {}

	public void displayNoSIMWarning() {}

	public void displayRoamingIsTurnedOffWarning() {}

	public void displayRoamingWarning(int autoDismissTime) {}

	public void displaySafetyWarning() {}

	public void displaySynchronizationCompletedMessage() {}

	public void displayWifiWarning() {
			showDialog(DIALOG_WIFI_WARNING);
	}

	public void showNewVersionDialog(String newVersionID) {	}
	
	public void handleSoundMuteStateChanged() {}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_WIFI_WARNING: {
				AlertDialog dialog = new AlertDialog(this);
				dialog.setTitle(R.string.qtn_andr_note_txt);
				dialog.setMessage(R.string.qtn_andr_turn_off_wifi_txt);
				dialog.setNeutralButton(R.string.qtn_andr_settings_tk, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(
								Settings.ACTION_WIRELESS_SETTINGS);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
						dialog.dismiss();
					}
				});
				dialog.setPositiveButton(R.string.qtn_andr_close_tk, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					    Intent intent = new Intent(WarningActivity.this, SplashActivity.class);
				        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					    intent.putExtra(SplashActivity.KEY_EXIT, true);
					    WarningActivity.this.startActivity(intent);
					    finish();
						
					}
				});

				
				dialog.setOnDismissListener(new OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						// Works for bringing up the gps-settings
						try {
							
						} catch (Throwable t) {
							Log.e("AbstractActivity", "onDismiss() " + t);
						}
					}
				});
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				return dialog;
			}
				
			default: {
				return super.onCreateDialog(id);
			}
		}
	}
}
