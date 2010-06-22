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
package com.vodafone.android.navigation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.activity.AbstractActivity;
import com.vodafone.android.navigation.mapobject.AndroidMapObject;
import com.vodafone.android.navigation.util.ImageDownloader;

public class MapObjectView extends LinearLayout implements OnClickListener, OnKeyListener {

    private AndroidMapObject mapObject;
    private AbstractActivity activity;

	public MapObjectView(Context context, AndroidMapObject mapObject, AbstractActivity activity) {
		super(context);
		this.mapObject = mapObject;
		this.activity = activity;
		
		View.inflate(context, R.layout.map_object, this);
		
		ImageView imgOverlayView = (ImageView) this.findViewById(R.id.category_icon);
		
		Bitmap bitmap = ImageDownloader.get().getImage(context, mapObject.getImageName(), true);
		imgOverlayView.setImageBitmap(bitmap);
		
		TextView titleView = (TextView) this.findViewById(R.id.strTitle);
		titleView.setText(mapObject.getName());
		
		TextView textView = (TextView) this.findViewById(R.id.strText);
		textView.setText(mapObject.getDesc());
		
		this.setOnClickListener(this);
		this.setOnKeyListener(this);
	}

	public void onClick(View view) {
	    this.execute();
	}

    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
            this.execute();
            return true;
        }
        return false;
    }

    private void execute() {
        if(this.activity != null) {
            this.activity.displayContextMenu(this.mapObject);
        }
    }
}
