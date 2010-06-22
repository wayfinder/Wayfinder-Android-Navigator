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
package com.vodafone.android.navigation.mapobject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.Display;
import android.view.WindowManager;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.util.ImageDownloader;
import com.vodafone.android.navigation.util.ImageDownloader.ImageDownloadListener;
import com.wayfinder.core.map.MapObject;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.shared.Position;
import com.wayfinder.core.shared.util.UnitsFormatter.FormattingResult;
import com.wayfinder.pal.android.graphics.AndroidGraphicsFactory;
import com.wayfinder.pal.graphics.WFFont;
import com.wayfinder.pal.graphics.WFGraphics;
import com.wayfinder.pal.graphics.WFImage;

public abstract class AndroidMapObject extends MapObject implements ImageDownloadListener {

    protected static final int PADDING = 6;
    protected static int bkgColorGrayBorderLight;
    protected static int bkgColorGrayBorderDark;
    protected static int bkgColorWhite;
    protected static int bkgColorDark;
    protected static int bkgColorLight;
    protected static int fontColor;
    protected static WFFont titleFont;
    protected static WFFont textFont;
    
    private WFImage image;
    private WFImage inflatedImage;
    private boolean isSelected;
    private int oldWidth;
    private int oldHeight;
    private String distance;
    private String title;
    private int textContainerWidth;
    private int containerHeight;
    private int arrowHeight = 14;
    private NavigatorApplication application;
    private Position position;
    private Context context;
    private int contextMenuType;
    private AndroidGraphicsFactory androidFactory;
    private int width;
    private int height;
//	private boolean isInFavorites;
    private String imageName;
    private VectorMapInterface map;

    public AndroidMapObject(Context context, VectorMapInterface map, String imageName, String title, Position position, int maxVisibleZoomLevel, int contextMenuType) {
        super(imageName, maxVisibleZoomLevel);
        this.context = context;
        this.map = map;
        this.position = position;
        this.contextMenuType = contextMenuType;
        
        this.application = (NavigatorApplication) context.getApplicationContext();
        androidFactory = this.application.getAndroidFactory();
        Resources resources = context.getResources();
        if(titleFont == null) {
            bkgColorGrayBorderDark = resources.getColor(R.color.color_gray_border_dark);
            bkgColorGrayBorderLight = resources.getColor(R.color.color_gray_border_light);
            bkgColorWhite = resources.getColor(R.color.color_white);
            bkgColorDark = resources.getColor(R.color.color_blue_dark);
            bkgColorLight = resources.getColor(R.color.color_blue_light);
            fontColor = resources.getColor(R.color.color_white);
            titleFont = androidFactory.getWFFont(WFFont.SIZE_SMALL, WFFont.STYLE_BOLD);
            textFont = androidFactory.getWFFont(WFFont.SIZE_SMALL, WFFont.STYLE_PLAIN);
        }
        
        NavigatorApplication application = (NavigatorApplication) context.getApplicationContext();

        LocationInformation ownLocationInformation = application.getOwnLocationInformation();
        if(ownLocationInformation != null) {
            FormattingResult result = application.getUnitsFormatter().formatDistance(position.distanceTo(ownLocationInformation.getMC2Position()));
            this.distance = result.getRoundedValue() + " " + result.getUnitAbbr();
        }

        this.updateTitle(title);
        this.containerHeight = this.calculateContainerHeight();
    }
    
    public void updateTitle(String title) {
        Paint paint = new Paint();
        paint.setTextSize(textFont.getFontHeight());
        int textWidth = (this.distance != null ? (int) paint.measureText(this.distance) + (PADDING * 3) : 0);
        paint.setTextSize(titleFont.getFontHeight());
        this.title = title;

        WindowManager windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int screenWidth = defaultDisplay.getWidth();
        
        int maxContainerWidth = (int) (((float) screenWidth * 0.8) - this.containerHeight - 12);
        this.textContainerWidth = (int) paint.measureText(this.title) + (PADDING * 3);
        if(this.textContainerWidth > maxContainerWidth) {
            this.textContainerWidth = maxContainerWidth;
        }
        else if(this.textContainerWidth < textWidth) {
            this.textContainerWidth = textWidth;
        }
    }
    
    protected Context getContext() {
        return this.context;
    }

    protected NavigatorApplication getApplication() {
        return this.application;
    }
    
    public int getLatitude() {
        return this.position.getMc2Latitude();
    }
    
    public int getLongitude() {
        return this.position.getMc2Longitude();
    }
    
    public Position getPosition() {
        return this.position;
    }
    
    public abstract String getId();
    public abstract String getDesc();
    
    public long getPlaceDBId(){
    	return -1;
    }
    
    public String getName() {
        return this.title;
    }

    protected int calculateContainerHeight() {
        int textHeight = this.calculateStringHeight();
        int imageHeight = (this.image != null ? this.image.getHeight() : 0);
        if(imageHeight > textHeight) {
            textHeight = imageHeight;
        }
        return textHeight + 6;
    }

    private int calculateStringHeight() {
        return titleFont.getFontHeight() + textFont.getFontHeight() + PADDING * 3;
    }

    public WFImage setBitmap(String imageName, Bitmap bitmap) {
        if(bitmap == null) {
            return null;
        }
        
        this.imageName = imageName;
        this.image = this.application.getAndroidFactory().createWFImage(bitmap);
        this.containerHeight = this.calculateContainerHeight();
        
        return this.image;
    }

    public void setAsSelected(boolean isSelected) {
        if(this.isSelected != isSelected) {
            this.setForcedAsSelected(isSelected);
        }
    }

    private void setForcedAsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if(this.isSelected && this.drawInflatedWhenSelected()) {
            this.oldWidth = this.width;
            this.oldHeight = this.height;
            this.setSelectedSize();
        }
        else {
            this.setUnselectedSize();
        }
    }

    private void setSelectedSize() {
        int touchPadding = this.getTouchPadding();
        int halfTouchPadding = (touchPadding >> 1);
        this.width = this.textContainerWidth + this.containerHeight - 12 + (PADDING * 3) + touchPadding;
        this.height = this.containerHeight + this.arrowHeight + touchPadding;

        int imageMaxSize = this.containerHeight - 12;
        Bitmap scaledImage = ImageDownloader.get().getScaledImage(this.context, this.imageName, imageMaxSize, imageMaxSize);
        if(scaledImage != null) {
            this.inflatedImage = this.application.getAndroidFactory().createWFImage(scaledImage);
        }
        else {
            this.inflatedImage = this.image;
        }
        
        int halfWidth = ((this.width - touchPadding) >> 1);
        int left = -(halfWidth + halfTouchPadding);
        int right = halfWidth + halfTouchPadding;
        int top = -(this.height - halfTouchPadding);
        int bottom = halfTouchPadding;
        this.setDrawArea(left, right, top, bottom);
    }

    private void setUnselectedSize() {
        int touchPadding = this.getTouchPadding();
        int halfTouchPadding = (touchPadding >> 1);
        this.width = this.oldWidth;
        this.height = this.oldHeight;
        
        if(this.inflatedImage != null) {
            Bitmap inflatedBitmap = (Bitmap) this.inflatedImage.getNativeImage();
            if(inflatedBitmap != null && !inflatedBitmap.isRecycled()) {
                inflatedBitmap.recycle();
            }
        }
        
        int halfWidth = ((this.width - touchPadding) >> 1);
        int left = -(halfWidth + halfTouchPadding);
        int right = halfWidth + halfTouchPadding;
        int top = -(this.height - halfTouchPadding);
        int bottom = halfTouchPadding;
        this.setDrawArea(left, right, top, bottom);
    }
    
    protected int getTouchPadding() {
        return this.context.getResources().getDimensionPixelSize(R.dimen.map_object_padding);
    }
    
    protected int getInternalWidth() {
        return this.getMaxX() - this.getMinX();
    }
    
    protected int getInternalHeight() {
        return this.getMaxY() - this.getMinY();
    }
    
    @Override
    public void draw(WFGraphics g, WFImage i, int x, int y, int anchor) {

        if(!this.isImageValid() && this.imageName != null && this.imageName.length() > 0) {
            Bitmap bitmap = ImageDownloader.get().queueDownload(this.context, this.imageName, this, this);
            if(bitmap != null) {
                this.setBitmap(this.imageName, bitmap);
            }
        }

        int touchPadding = this.getTouchPadding();
        int halfTouchPadding = (touchPadding >> 1);
        
        if(this.width == 0 && this.height == 0) {
            this.width = image.getWidth() + touchPadding;
            this.height = image.getHeight() + touchPadding;
            this.oldWidth = this.width;
            this.oldHeight = this.height;
            this.setForcedAsSelected(this.isSelected);
        }

        int tmpMinX = this.getMinX();
        int tmpMaxX = this.getMaxX();
        int tmpMinY = this.getMinY();
        int tmpMaxY = this.getMaxY();
        int minX = Math.min(tmpMinX, tmpMaxX);
        int minY = Math.min(tmpMinY, tmpMaxY);
        int maxX = Math.max(tmpMinX, tmpMaxX);
        int maxY = Math.max(tmpMinY, tmpMaxY);
        int width = maxX - minX;
        int height = maxY - minY;

        int leftContent = x + minX + halfTouchPadding;
        int topContent = y + minY + halfTouchPadding;
        
        int widthContent = width - touchPadding;
        int heightContent = height - touchPadding;
        
        if(!this.isSelected || !this.drawInflatedWhenSelected()) {
            this.drawNormalPOI(g, image, leftContent, topContent, widthContent, heightContent);
        }
        else {
            this.drawInflatedPOI(g, leftContent, topContent, widthContent, heightContent);
        }

        //Used only for debugging purpose, to see where the touch-areas are, and where the content is painted
//        g.setColor(0xFF0000);
//        g.drawRect(x + minX, y + minY, width, height);
    }

    protected abstract void drawNormalPOI(WFGraphics g, WFImage img, int left, int top, int width, int height);

    protected boolean drawInflatedWhenSelected() {
        return true;
    }

    protected void drawInflatedPOI(WFGraphics g, int left, int top, int width, int height) {
        this.drawPlaceMarker(g, left, top, width, height);
        
        int titleHeight = titleFont.getFontHeight() + PADDING + (PADDING >> 1);

        g.setColor(bkgColorLight);
        g.fillRect(left + 3, top + 3, width - 5, titleHeight);
        
        g.setColor(bkgColorDark);
        g.fillRect(left + 3, top + titleHeight + 3, width - 5, height - titleHeight - this.arrowHeight - 6);
        
        int anchorLeftTop = WFGraphics.ANCHOR_LEFT | WFGraphics.ANCHOR_TOP;
        g.setColor(bkgColorWhite);
        int imageMaxSize = this.containerHeight - 12;
        g.fillRect(left + 6, top + 6, imageMaxSize, imageMaxSize);
        if(this.isImageValid()) {
            g.drawImage(this.inflatedImage, left + 6 + (imageMaxSize >> 1), top + 6 + (imageMaxSize >> 1), WFGraphics.ANCHOR_HCENTER | WFGraphics.ANCHOR_VCENTER);
        }
        
        g.setColor(fontColor);
        g.setFont(titleFont);
        int maxTextWidth = this.textContainerWidth;
        g.drawText(this.title, left + this.containerHeight - 6 + PADDING, top + PADDING, maxTextWidth, anchorLeftTop, "..");
         
        if(this.distance != null) {
            g.setFont(textFont);
            g.drawText(this.distance, left + this.containerHeight - 6 + PADDING, top + PADDING + titleFont.getFontHeight() + PADDING + 3, anchorLeftTop);
        }
    }

    protected int getArrowHeight() {
        return this.arrowHeight;
    }
    
    protected void drawPlaceMarker(WFGraphics g, int left, int top, int width, int height) {
        int triX1 = left + ((width - this.arrowHeight) >> 1);
        int rectBottom = top + height - this.arrowHeight - 1;
        int rectRight = left + width;
        int triY1 = rectBottom;
        int triX2 = triX1 + this.arrowHeight;
        int triY2 = triY1;
        int triX3 = triX2;
        int triY3 = top + height;
        
        g.setColor(bkgColorWhite);
        g.fillRect(left, top, width, height - this.arrowHeight);
        g.fillTriangle(triX1, triY1, triX2, triY2, triX3, triY3);

        g.setColor(bkgColorGrayBorderLight);
        g.drawLine(left, top, rectRight, top, 1);
        g.drawLine(left, top, left, rectBottom, 1);
        g.drawLine(triX1, triY1, triX3, triY3, 1);

        g.setColor(bkgColorGrayBorderDark);
        g.drawLine(rectRight, top, rectRight, rectBottom, 1);
        g.drawLine(left, rectBottom, triX1, rectBottom, 1);
        g.drawLine(triX2, rectBottom, rectRight, rectBottom, 1);
        g.drawLine(triX2, triY2, triX3, triY3, 1);
    }
    
    private boolean isImageValid() {
        boolean isImageValid = (this.inflatedImage != null && !((Bitmap) this.inflatedImage.getNativeImage()).isRecycled());
        return isImageValid;
    }

	/**
	 * @return the contextMenuType
	 */
	public int getContextMenuType() {
		return contextMenuType;
	}

	
	public void onImageDownloaded(Bitmap scaledBitmap, Bitmap origBitmap, String imageName, AndroidMapObject[] mapObjects) {
	    this.setBitmap(imageName, scaledBitmap);
	    this.map.requestMapUpdate();
	}
}
