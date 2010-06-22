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

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vodafone.android.navigation.R;

public class ContextAdapter extends ArrayAdapter<Object[]> {

	public ContextAdapter(Activity context, ArrayList<Object[]> data) {
		super(context, R.layout.context_item, data);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View row = convertView;
	    ViewWrapper vw;
	    if(row == null) {
	        row = View.inflate(this.getContext(), R.layout.context_item, null);
	        vw = new ViewWrapper(row);
	        row.setTag(vw);
	    }
	    else {
	        vw = (ViewWrapper) row.getTag();
	    }
	    
		TextView labelView = vw.getLabel();
		ImageView imageView = vw.getImage();
		Object[] data = getItem(position);
		if(data != null){
            imageView.setImageResource((Integer) data[0]);
			labelView.setText((Integer) data[1]);
		}
		
		return row;
	}
	
	private static class ViewWrapper {

        private View view;
        private TextView label;
        private ImageView image;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public ImageView getImage() {
            if(this.image == null) {
                this.image = (ImageView) this.view.findViewById(R.id.image);
            }
            return this.image;
        }

        public TextView getLabel() {
            if(this.label == null) {
                this.label = (TextView) this.view.findViewById(R.id.text);
            }
            return this.label;
        }
	}
}
