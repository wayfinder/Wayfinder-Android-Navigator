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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.listeners.ViewSizeChangedListener;

public class ResizeHandleImageButton extends ImageButton {
	
	public static final int MENU_HEIGHT_STEP_MIN = 0;
	public static final int MENU_HEIGHT_STEP_MED = 1;
	public static final int MENU_HEIGHT_STEP_MAX = 2;
	
	private ViewSizeChangedListener listener;
    private boolean buttonResizeActive;
    private int menuHeightStep;
    
    private int menuHeightOffset;
    

	public ResizeHandleImageButton(Context context) {
		super(context);
		init(context);
	}
	
	public ResizeHandleImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public ResizeHandleImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void resizeView(int menuHeightStep){
	    this.menuHeightStep = menuHeightStep;

        this.setImageResource(getBackgroundImageId(false));

        View vg = (View) this.getParent().getParent();
		vg.getLayoutParams().height = this.getMenuHeight(this.menuHeightStep);
        vg.requestLayout();

        if(this.listener != null){
            this.listener.onViewSizeChanged(menuHeightStep);
        }
	}

    private void init(Context context) {
    	menuHeightOffset = context.getResources().getDimensionPixelSize(R.dimen.minimized_landmark_list_height_offset);
		this.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View button, MotionEvent event) {
				return ResizeHandleImageButton.this.onTouch(event);
			}
		});
	}

	public void setSizeChangedListener(ViewSizeChangedListener listener) {
		this.listener = listener;
	}

    private int getBackgroundImageId(boolean pressed) {
        Log.i("ResizeHandlerImageButton", "getBackgroundImageId() pressed=" + pressed);
        if(pressed) {
            switch(this.menuHeightStep) {
                case MENU_HEIGHT_STEP_MAX: 
                    Log.i("ResizeHandlerImageButton", "getBackgroundImageId() Max");
                    return R.drawable.handle_pressed_down;
                case MENU_HEIGHT_STEP_MIN: 
                    Log.i("ResizeHandlerImageButton", "getBackgroundImageId() Min");
                    return R.drawable.handle_pressed_up;
                default:
                    Log.i("ResizeHandlerImageButton", "getBackgroundImageId() Medium");
                    return R.drawable.handle_pressed_up;
            }
        }
        else {
            switch(this.menuHeightStep) {
                case MENU_HEIGHT_STEP_MAX: 
                    Log.i("ResizeHandlerImageButton", "getBackgroundImageId() Max");
                    return R.drawable.handle_normal_down;
                case MENU_HEIGHT_STEP_MIN: 
                    Log.i("ResizeHandlerImageButton", "getBackgroundImageId() Min");
                    return R.drawable.handle_normal_up;
                default:
                    Log.i("ResizeHandlerImageButton", "getBackgroundImageId() Medium");
                    return R.drawable.handle_normal_up;
            }
        }
    }

    private int getMinMenuHeight() {
        int height = this.getHeight() + menuHeightOffset;
        return height;
    }

    private int getMedMenuHeight() {
        View rootView = getRootView();
        int rootHeight = rootView.getHeight();
        
        int height = (rootHeight >> 1) + menuHeightOffset;
        return height;
    }

    private int getMaxMenuHeight() {
        View rootView = getRootView();
        int rootHeight = rootView.getHeight();

        int height = rootHeight - this.getHeight();
        return height;
    }
    
    public int getMenuHeight(int menuHeightStep) {
        switch(menuHeightStep){
            case MENU_HEIGHT_STEP_MIN: return this.getMinMenuHeight();
            case MENU_HEIGHT_STEP_MED: return this.getMedMenuHeight();
            case MENU_HEIGHT_STEP_MAX: return this.getMaxMenuHeight();
            default: return 0;
        }
    }
    
    public int getMenuHeight() {
        return this.getMenuHeight(this.menuHeightStep);
    }

    private boolean onTouch(MotionEvent event) {
        View vg = (View)ResizeHandleImageButton.this.getParent().getParent();
        int action = event.getAction();

        int minPos = getMinMenuHeight();
        int medPos = getMedMenuHeight();
        int maxPos = getMaxMenuHeight();
        int pos = medPos;

        if (action == MotionEvent.ACTION_DOWN) {
            buttonResizeActive = true;
            
            ResizeHandleImageButton.this.setImageResource(getBackgroundImageId(true));
            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            switch(this.menuHeightStep) {
                case MENU_HEIGHT_STEP_MIN: {
                    this.menuHeightStep = MENU_HEIGHT_STEP_MED;
                    pos = medPos;
                    break;
                }
                case MENU_HEIGHT_STEP_MED: {
                    this.menuHeightStep = MENU_HEIGHT_STEP_MAX;
                    pos = maxPos;
                    break;
                }
                case MENU_HEIGHT_STEP_MAX: {
                    this.menuHeightStep = MENU_HEIGHT_STEP_MIN;
                    pos = minPos;
                    break;
                }
            }

            ResizeHandleImageButton.this.setImageResource(getBackgroundImageId(false));

            vg.getLayoutParams().height = pos;
            vg.requestLayout();

            if(listener != null){
                listener.onViewSizeChanged(this.menuHeightStep);
            }
            
            return true;
        } else if (buttonResizeActive && action == MotionEvent.ACTION_MOVE) {
            return true;
        }
        return false;
    }
}
