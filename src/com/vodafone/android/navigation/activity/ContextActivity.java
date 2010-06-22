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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.mapobject.UserMapObject;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.vodafone.android.navigation.util.ImageDownloader.ImageDownloadListener;

public class ContextActivity extends AbstractActivity {

    public static final String KEY_TITLE = "key_name";
    public static final String KEY_DESC = "key_desc";
    public static final String KEY_TYPE = "key_type";
    public static final String KEY_IMAGE_ID = "key_image_id";
    public static final String KEY_DISPLAY_INFO = "key_display_info";
	
	public static final int RESULT_NAVIGATE_TO = RESULT_FIRST_USER;
	public static final int RESULT_DISPLAY_DETAILS = RESULT_NAVIGATE_TO + 1;
	public static final int RESULT_SAVE_PLACE = RESULT_DISPLAY_DETAILS + 1;
	public static final int RESULT_EDIT_PLACE = RESULT_SAVE_PLACE + 1;
	
	public static final int TYPE_MAP = 0;
	public static final int TYPE_SEARCH = 1;
	public static final int TYPE_PLACES = 2;
	public static final int TYPE_DESTINATIONS = 3;
	public static final int TYPE_ADD = 4;
	public static final int TYPE_EDIT = 5;
	
	private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = this.getIntent();
        String title = intent.getStringExtra(KEY_TITLE);
        String desc = intent.getStringExtra(KEY_DESC);
        String imageId = intent.getStringExtra(KEY_IMAGE_ID);
        this.type = intent.getIntExtra(KEY_TYPE, 0);
        boolean displayInfo = intent.getBooleanExtra(KEY_DISPLAY_INFO, true);
        
        this.setContentView(R.layout.context);
        
        final ImageView imageTitle = (ImageView) this.findViewById(R.id.image_title);
        if(imageId == null || imageId.equals("") || imageId.equals(ImageDownloader.IMAGE_NAME_DEFAULT) || imageId.equals(ImageDownloader.IMAGE_NAME_EMPTY)) {
            imageTitle.setImageResource(R.drawable.cat_all);
        }
        else {
            ImageDownloader imageDownloader = ImageDownloader.get();
            Bitmap image = imageDownloader.getImage(this, imageId, true);
            if(image == null) {
                ImageDownloader.get().queueDownload(this, imageId, null, new ImageDownloadListener() {
                    public void onImageDownloaded(final Bitmap scaledBitmap, final Bitmap origBitmap, String imageName, AndroidMapObject[] mapObjects) {
                        imageTitle.getHandler().post(new Runnable() {
                            public void run() {
                                imageTitle.setImageBitmap(origBitmap);
                            }
                        });
                    }
                });
            }
            else {
                imageTitle.setImageBitmap(image);
            }
        }
        
        final TextView textTitle = (TextView) this.findViewById(R.id.text_title);
        textTitle.setText(title);
        
        UserMapObject userPin = this.getApp().getUserPin();
        if(userPin != null) {
            userPin.setOnTitleUpdateListener(new UserMapObject.OnTitleUpdateListener() {
                public void onUpdateTitle(String title) {
                    textTitle.setText(title);
                    textTitle.invalidate();
                }
            });
        }
        
        TextView textDesc = (TextView) this.findViewById(R.id.text_desc);
        textDesc.setText(desc);
        
        LinearLayout layoutRoute = (LinearLayout) this.findViewById(R.id.layout_route);
        layoutRoute.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_NAVIGATE_TO);
                finish();
            }
        });
        
        LinearLayout layoutDetails = (LinearLayout) this.findViewById(R.id.layout_details);
        layoutDetails.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_DISPLAY_DETAILS);
                finish();
            }
        });
        
        LinearLayout layoutAdd = (LinearLayout) this.findViewById(R.id.layout_add);
        layoutAdd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_SAVE_PLACE);
                finish();
            }
        });
        
        LinearLayout layoutEdit = (LinearLayout) this.findViewById(R.id.layout_edit);
        layoutEdit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_EDIT_PLACE);
                finish();
            }
        });

        if(type == TYPE_ADD){
			layoutAdd.setVisibility(View.VISIBLE);
			layoutEdit.setVisibility(View.GONE);
		}
        else if(type == TYPE_EDIT){
			layoutAdd.setVisibility(View.GONE);
			layoutEdit.setVisibility(View.VISIBLE);
		}
        
        if(!displayInfo) {
            layoutDetails.setVisibility(View.GONE);
        }
        else {
            layoutDetails.setVisibility(View.VISIBLE);
        }
    }
}
