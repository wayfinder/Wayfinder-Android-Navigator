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
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vodafone.android.navigation.R;

public class SearchResultListView extends ListView {

    private int selected = ListView.INVALID_POSITION;
    private int oldSelected = ListView.INVALID_POSITION;
    private int oldFirstPosition = ListView.INVALID_POSITION;

    public SearchResultListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context);
    }

    public SearchResultListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public SearchResultListView(Context context) {
        super(context);
        this.init(context);
    }

    private void init(Context context) {
        this.setSelector(R.color.color_transparent);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        this.selected = position;
    }
    
    @Override
    public void setSelectionFromTop(int position, int y) {
        super.setSelectionFromTop(position, y);
        this.selected = position;
    }
    
    public void setHilighted(int position) {
        this.selected = position;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ListAdapter adapter = this.getAdapter();
        if(adapter != null){
            int firstPosition = this.getFirstVisiblePosition();
            int count = this.getCount();
            if(firstPosition != this.oldFirstPosition || this.selected != this.oldSelected) {
                Context context = this.getContext();
                long selectedItemId = adapter.getItemId(this.selected);
                for(int i = 0; i < count; i ++) {
                    View child = this.getChildAt(i);
                    if(child != null) {
                        View content = child.findViewById(R.id.layout_content);
                        TextView title = (TextView) child.findViewById(R.id.text_title);
                        TextView desc = (TextView) child.findViewById(R.id.text_description);
                        TextView dist = (TextView) child.findViewById(R.id.text_distance);
                        if(i + firstPosition == selectedItemId) {
                            if(content != null) {
                                content.setBackgroundResource(R.color.color_blue_dark);
                            }
                            if(title != null) {
                                title.setTextAppearance(context, R.style.title_white);
                            }
                            if(desc != null) {
                                desc.setTextAppearance(context, R.style.label_text_white_big);
                            }
                            if(dist != null) {
                                dist.setTextAppearance(context, R.style.label_text_white);
                            }
                        }
                        else {
                            if(content != null) {
                                content.setBackgroundResource(R.color.color_transparent);
                            }
                            if(title != null) {
                                title.setTextAppearance(context, R.style.title_black);
                            }
                            if(desc != null) {
                                desc.setTextAppearance(context, R.style.label_text_black_big);
                            }
                            if(dist != null) {
                                dist.setTextAppearance(context, R.style.label_text_black);
                            }
                        }
                    }
                }
                this.oldFirstPosition = firstPosition;
                this.oldSelected = selected;
            }
        }

        super.onDraw(canvas);
    }
    
    /**
	 * Resets the oldSelected and oldFirstPosition to default values. This can be used when list is reloaded.
	 */
    public void resetOldSavedIndexes(){
        oldSelected = ListView.INVALID_POSITION;
        oldFirstPosition = ListView.INVALID_POSITION;
    }

}
