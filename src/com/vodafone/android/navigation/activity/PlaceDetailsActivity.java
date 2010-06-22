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


import java.util.Vector;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.adapter.ChoosePhoneNumberAdapter;
import com.vodafone.android.navigation.components.RecentDestination;
import com.vodafone.android.navigation.dialog.AlertDialog;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.vodafone.android.navigation.util.ImageDownloader.ImageDownloadListener;
import com.wayfinder.core.poiinfo.PoiInfoInterface;
import com.wayfinder.core.poiinfo.PoiInfoListener;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.poiinfo.InfoFieldList;
import com.wayfinder.core.shared.poiinfo.PoiInfo;
import com.wayfinder.core.shared.poiinfo.InfoField;

public class PlaceDetailsActivity extends ServiceWindowActivity implements ImageDownloadListener, PoiInfoListener {

    public static final String KEY_ID = "key_id";
    public static final String KEY_LAT = "key_lat";
    public static final String KEY_LON = "key_lon";
    public static final String KEY_IMAGE_NAME = "key_image_name";
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_DESCRIPTION = "key_description";
    public static final String KEY_IS_ALREADY_SAVED = "key_is_already_saved";
	public static final String KEY_PHONE_NUMBER = "phone_number";

    private String srvString;
    private int lat;
    private int lon;
    private ImageView imageView;
    private TextView titleView;
    private TextView descView;
    private String imageName;
    private String description;
    private String title;
    private Handler handler;
    private boolean isAlreadySaved;
	private Vector<InfoField> poiInfo;

	private PoiInfoInterface poiInfoInterface;
	private NavigatorApplication application;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.handler = new Handler();
        this.imageView = (ImageView) this.findViewById(R.id.image_title);
        this.titleView = (TextView) this.findViewById(R.id.text_title);
        this.descView = (TextView) this.findViewById(R.id.text_desc);
        
        Intent intent = this.getIntent();
        String id = intent.getStringExtra(KEY_ID);
        this.isAlreadySaved = intent.getBooleanExtra(KEY_IS_ALREADY_SAVED, false);
        this.lat = intent.getIntExtra(KEY_LAT, 0);
        this.lon = intent.getIntExtra(KEY_LON, 0);
        this.title = intent.getStringExtra(KEY_TITLE);
        this.description = intent.getStringExtra(KEY_DESCRIPTION);
        this.imageName = intent.getStringExtra(KEY_IMAGE_NAME);
        
        this.application = (NavigatorApplication) this.getApplicationContext();
        poiInfoInterface = application.getCore().getPoiInfoInterface();
        poiInfoInterface.requestInfo(id, this);
        
        if(this.imageName != null && !this.imageName.equals("")){
	        if(this.imageName.equals(ImageDownloader.IMAGE_NAME_EMPTY)) {
	            this.imageName = ImageDownloader.IMAGE_NAME_DEFAULT;
	        }
	        
	        ImageDownloader imageDownloader = ImageDownloader.get();
	        Bitmap bitmap = imageDownloader.getImage(this, this.imageName, true);
	        if(bitmap == null) {
	            imageDownloader.queueDownload(this, this.imageName, null, this);
	        }
	        else {
	            this.imageView.setImageBitmap(bitmap);
	        }
        }

        if(title.trim().length() > 0){
        	this.titleView.setText(title);
        } 
        else {
        	this.titleView.setText(R.string.qtn_andr_view_details_txt);
        }
        this.descView.setText(description);
        
        Log.i("PlaceDetailsActivity", "Id: " + id);
        this.srvString = id;
        this.getPageHandler().openPlaceDetails(srvString);
    }
    
    @Override
    protected void setContentView() {
        this.setContentView(R.layout.service_window_details);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.options_menu_place_details, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem cmdRoute = menu.findItem(R.id.cmd_route);
        if(cmdRoute != null) {
            if(this.lat == 0 && this.lon == 0) {
                cmdRoute.setVisible(false);
            }
            else {
                cmdRoute.setVisible(true);
            }
        }
        
        MenuItem cmdSave = menu.findItem(R.id.cmd_add);
        if(cmdSave != null) {
            if(this.isAlreadySaved) {
                cmdSave.setVisible(false);
            }
            else {
                cmdSave.setVisible(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.cmd_route: {
            	RecentDestination fav = new RecentDestination(title, description, new Position(lat, lon), System.currentTimeMillis(), imageName, null); // TODO add poiInfo
            	navigate(fav);
                return true;
            }
            case R.id.cmd_add: {
                Intent intent = new Intent(this, EditPlaceActivity.class);
                intent.putExtra(EditPlaceActivity.TYPE_KEY, EditPlaceActivity.TYPE_SAVE_PLACE);
                intent.putExtra(EditPlaceActivity.NAME_KEY, this.title);
                intent.putExtra(EditPlaceActivity.DESCRIPTION_KEY, this.description);
                intent.putExtra(EditPlaceActivity.SRVSTRING_KEY, this.srvString);
                intent.putExtra(EditPlaceActivity.LAT_KEY, this.lat);
                intent.putExtra(EditPlaceActivity.LON_KEY, this.lon);
                intent.putExtra(EditPlaceActivity.IMAGENAME_KEY, this.imageName);
                this.startActivity(intent);
                return true;
            }
        }
        
        return super.onOptionsItemSelected(item);
    }

    public void onImageDownloaded(final Bitmap scaledBitmap, final Bitmap origBitmap, String imageName, AndroidMapObject[] mapObjects) {
        if(imageName.equals(this.imageName)) {
            this.handler.post(new Runnable() {
                public void run() {
                    imageView.setImageBitmap(origBitmap);
                }
            });
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_CALL){
    		int size = this.poiInfo.size();
            if(size == 1) {
                String number = this.poiInfo.get(0).getValue();
                startActivityForResult(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number)), 1);
            }
            else if(size > 1){
    			this.showDialog(DIALOG_SELECT_PHONE_NUMBER_ID);
    		}
    		else{
    			startActivity(new Intent(Intent.ACTION_DIAL));
    			Toast.makeText(application, R.string.qtn_andr_no_number_2_call_txt, Toast.LENGTH_LONG).show();
    		}
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }

	public void requestInfoDone(RequestID arg0, PoiInfo[] poiInfoIn) {
	    if(this.poiInfo == null) {
	        this.poiInfo = new Vector<InfoField>();
	    }
	    else {
	        this.poiInfo.clear();
	    }
	    for(PoiInfo info: poiInfoIn) {
	        InfoFieldList list = info.getInfoFieldList();
	        int count = list.getNbrInfoFields();
	        for(int i = 0; i < count; i ++) {
	            InfoField infoField = list.getInfoFieldAt(i);
                int type = infoField.getType();
                if(type == InfoField.TYPE_MOBILE_PHONE || type == InfoField.TYPE_PHONE_NUMBER){
                    this.poiInfo.add(infoField); 
	            }
	        }
	    }
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == DIALOG_SELECT_PHONE_NUMBER_ID){
			View choosePhoneView = View.inflate(this, R.layout.choose_phone_number_dialog, null);
			AlertDialog dialog = new AlertDialog(this);
			dialog.setContentView(choosePhoneView);
	        return dialog;
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		if(id == DIALOG_SELECT_PHONE_NUMBER_ID){
			final ListView numbersList = (ListView)dialog.findViewById(R.id.choose_phone_number_list);
			ChoosePhoneNumberAdapter adapter = new ChoosePhoneNumberAdapter(this, poiInfo);
			numbersList.setAdapter(adapter);
			numbersList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View view, int arg2,long arg3) {
		    		TextView number = (TextView) view.findViewById(R.id.txt_phone_number);
		    		startActivityForResult(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number.getText())), 1);
					dialog.dismiss();
				}
			});
			numbersList.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_CALL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        View focusedView = numbersList.getSelectedView();
                        if(focusedView != null) {
                            TextView number = (TextView) focusedView.findViewById(R.id.txt_phone_number);
                            startActivityForResult(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number.getText())), 1);
                            dialog.dismiss();
                        }
                        return true;
                    }
                    return false;
                }
            });
		}
		else {
		    super.onPrepareDialog(id, dialog);
		}
	}
	
	@Override
	public boolean invokePhoneCall(String phoneNumber) {
	    //091118 MM: Latest spec from SebastianW says that whenever we click a phonenumber the dialog with all numbers should be displayed.
	    //But, if the user presses the green call-button, and there�s only one number the phonecall is initiated. If more than one number the dialog is displayed
        if(this.poiInfo.size() > 0) {
            this.showDialog(DIALOG_SELECT_PHONE_NUMBER_ID);
            return true;
        }
        else {
            return super.invokePhoneCall(phoneNumber);
        }
	}
}
