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
package com.vodafone.android.navigation.adapter;

import java.util.Vector;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.android.navigation.R;
import com.wayfinder.core.shared.poiinfo.InfoField;

public class ChoosePhoneNumberAdapter extends ArrayAdapter<InfoField> {

	private Activity context;

    public ChoosePhoneNumberAdapter(Activity context, Vector<InfoField> poiInfo) {
		super(context, R.layout.choose_phone_number_lits_item, poiInfo);
		this.context = context;
	}

    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewWrapper viewWrapper;
		View row = convertView;
		if(row == null || row.getTag() == null) {
			LayoutInflater inflater = context.getLayoutInflater();
		    row = inflater.inflate(R.layout.choose_phone_number_lits_item, null);
		    viewWrapper = new ViewWrapper(row);
		    row.setTag(viewWrapper);
		}
		else {
		    viewWrapper = (ViewWrapper) row.getTag();
		}
		
		TextView phoneNumber = viewWrapper.getPhoneNumber();
		TextView phoneType = viewWrapper.getPhoneType();
		ImageView phoneTypeImage = viewWrapper.getPhoneIcon();
		
		InfoField poi = getItem(position);
		
		phoneNumber.setText(poi.getValue());
		
		if(poi.getType() == InfoField.TYPE_PHONE_NUMBER){
			phoneType.setText(R.string.qtn_andr_phone_type_phone_txt);
			phoneTypeImage.setImageResource(R.drawable.cmd_phone_landline);
		}
		else if(poi.getType() == InfoField.TYPE_MOBILE_PHONE){
			phoneType.setText(R.string.qtn_andr_phone_type_mobile_txt);
			phoneTypeImage.setImageResource(R.drawable.cmd_phone_mobile);
		}
		return row;
	}
	
    private static class ViewWrapper {
        private View view;
        private TextView phoneType;
        private TextView phoneNumber;
        private ImageView phoneIcon;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public ImageView getPhoneIcon() {
            if(this.phoneIcon == null) {
                this.phoneIcon = (ImageView) this.view.findViewById(R.id.img_phone_icon);
            }
            return this.phoneIcon;
        }

        public TextView getPhoneNumber() {
            if(this.phoneNumber == null) {
                this.phoneNumber = (TextView) this.view.findViewById(R.id.txt_phone_number);
            }
            return this.phoneNumber;
        }

        public TextView getPhoneType() {
            if(this.phoneType == null) {
                this.phoneType = (TextView) this.view.findViewById(R.id.txt_phone_type);
            }
            return this.phoneType;
        }
    }
}
