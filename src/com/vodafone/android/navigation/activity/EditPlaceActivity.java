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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.mapobject.UserMapObject;
import com.wayfinder.core.shared.Position;


public class EditPlaceActivity extends AbstractActivity implements TextWatcher{
	
	public static final int TYPE_SAVE_PLACE = 0;
	public static final int TYPE_EDIT_PLACE = 1;
	public static final int TYPE_EDIT_recent = 1;
	
	public static final String TYPE_KEY = "type_key";
	public static final String NAME_KEY = "name_key";
	public static final String DESCRIPTION_KEY = "description_key";
	public static final String LAT_KEY = "lat_key";
	public static final String LON_KEY = "lon_key";
	public static final String SRVSTRING_KEY = "srvstring_key";
	public static final String IMAGENAME_KEY = "imagename_key";
	public static final String ID_KEY = "id_key";
	public static final String RECENT_PLACE_ID_KEY = "previus_place_id_key";
	
	private int type;
	
	private Button cancelButton;
	private Button saveButton;
	
	private EditText nameField;
	private EditText descriptionField;
	
	private String name;
	private String description;
	private int lat;
	private int lon;
	private String srvString;
	private String imageName;
	private long previusDestinationId;

	/* (non-Javadoc)
	 * @see com.vodafone.android.navigation.activity.AbstractActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_place_dialog);
		
		Intent intent =  this.getIntent();
		name = intent.getStringExtra(NAME_KEY);
		description = intent.getStringExtra(DESCRIPTION_KEY);
		type = intent.getIntExtra(TYPE_KEY, 0);
		if(type == TYPE_SAVE_PLACE){
			imageName = intent.getStringExtra(IMAGENAME_KEY);
			srvString = intent.getStringExtra(SRVSTRING_KEY);
			lat = intent.getIntExtra(LAT_KEY, 0);
			lon = intent.getIntExtra(LON_KEY, 0);
		} else if(type == TYPE_EDIT_PLACE){
			setTitle(R.string.qtn_andr_edit_place_det_txt);
			srvString = intent.getStringExtra(SRVSTRING_KEY);
			previusDestinationId = intent.getLongExtra(RECENT_PLACE_ID_KEY, -1);
		}
		
		nameField = (EditText) findViewById(R.id.name_field);
		nameField.setText(name);
		nameField.addTextChangedListener(this);
		
        UserMapObject userPin = this.getApp().getUserPin();
        if(userPin != null) {
            userPin.setOnTitleUpdateListener(new UserMapObject.OnTitleUpdateListener() {
                public void onUpdateTitle(String title) {
                    nameField.setText(title);
                    nameField.invalidate();
                }
            });
        }
		
		descriptionField = (EditText) findViewById(R.id.description_field);
		descriptionField.setHint(R.string.qtn_andr_map_pos_txt);
		if(type == TYPE_EDIT_PLACE){
			descriptionField.setText(description);
		} 
		saveButton = (Button) findViewById(R.id.save_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);
		setSaveButtonState();
		
		saveButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(type == TYPE_SAVE_PLACE){
					EditPlaceActivity.this.getApp().
					addSavedPlace(nameField.getText().toString(), descriptionField.getText().toString(), imageName ,new Position(lat,lon), null );
					Toast.makeText(EditPlaceActivity.this, R.string.qtn_andr_placemark_is_saved_txt, Toast.LENGTH_SHORT).show();
				} else if(type == TYPE_EDIT_PLACE){
					if(srvString != null){
						EditPlaceActivity.this.getApp().updateSavedPlace(getApp().getFavoriteToBeUpdated(), nameField.getText().toString(), descriptionField.getText().toString());
					}
//					if(previusDestinationId != -1){
//						EditPlaceActivity.this.getApp().updatePreviousDestination(, nameField.getText().toString(), descriptionField.getText().toString());
//					}
					Toast.makeText(EditPlaceActivity.this, R.string.qtn_andr_place_is_edited_txt, Toast.LENGTH_SHORT).show();
				}
				EditPlaceActivity.this.finish();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditPlaceActivity.this.finish();
			}
		});
		
	}

	public void afterTextChanged(Editable s) {
		setSaveButtonState();
	}
	
	private void setSaveButtonState(){
		if(nameField.getText().length() == 0){
			if(saveButton.isEnabled()){
				saveButton.setEnabled(false);
			}
		} else {
			if(!saveButton.isEnabled()){
				saveButton.setEnabled(true);
			}
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
	
}
