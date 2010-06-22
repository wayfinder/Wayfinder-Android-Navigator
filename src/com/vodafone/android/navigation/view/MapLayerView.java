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

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.vodafone.android.navigation.NavigatorApplication;
import com.vodafone.android.navigation.R;
import com.vodafone.android.navigation.persistence.ApplicationSettings;
import com.wayfinder.core.map.MapKeyInterface;
import com.wayfinder.core.map.vectormap.MapCameraInterface;
import com.wayfinder.core.map.vectormap.MapDrawerInterface;
import com.wayfinder.core.map.vectormap.MapRenderer;
import com.wayfinder.core.map.vectormap.VectorMapInterface;
import com.wayfinder.core.positioning.LocationInformation;
import com.wayfinder.core.shared.Position;
import com.wayfinder.pal.android.graphics.AndroidGraphics;

public class MapLayerView extends SurfaceView implements MapDrawerInterface, SurfaceHolder.Callback {

	private static final float MAP_MIN_SCALE = 0.3f;
    private static final int MAP_MAX_SCALE = 24000;
    private static final int MAX_STEP_NUMBER = 40;
    
    private static final float[] FIXED_ZOOM_LEVELS = new float[] {
        MAP_MIN_SCALE, 1, 2, 3, 4, 5, 6, 7, 11, 12, 20, 30, 44, 100, 115, 170, 480, 600, 1000, 20000, MAP_MAX_SCALE
    };
    
    private static final boolean allowDoubleTapping = false;
    
    private boolean newMotionEvent;
    
    private float lastX;
    private float lastY;

    private static final Timer timer = new Timer("MapLayerView");

    private static final float MIN_SLIDING_MOVEMENT = 25;

    // smooth zoom ==========================
	private final static int ZOOM_DURATION = 750;
	// ======================================

	// disable map animations ==========================
	private final static boolean DISABLE_ANIMATED_TRANSLATION = false; //disables the animation when centering map to a position
	private final static boolean DISABLE_SLIDING_EFFECT = false; // disables the fling animation when panning
	// ======================================
	
	// map tapping parameters ================
	private long lastMapTapTime;
	private float lastMapTapX;
	private float lastMapTapY;
	boolean mapDoubleTapped;
	// =======================================     


    // slide effect parameters  ==============
	private float speedX;
	private float speedY;
	private VelocityTracker velocityTracker;
	
	private final int SLIDING_TIME_STEP = 40;
	// =======================================     
	
	// flying map effect parameters ==========
	private int flyDistanceX;
	private int flyDistanceY;
	
	private int flyStepNumber;
	
	private boolean centeringInProgress;
    // =======================================    
	
	private WayfinderMapView iOwnerMap;
	private SurfaceHolder iSurfaceHolder;
	private Handler handler;
    private boolean panningDisabled;
    private View mapOverlay;
    private NavigatorApplication application;
    private boolean isSliding;
    private long lastTouchReleasedTime = 0;
    private boolean rubberBandEffectEnabled;
    private Position rubberBandPosition;
    private Paint p;
    private SurfaceChangeListener surfaceChangeListener;
    protected AndroidGraphics g;
    private boolean lockMapUpdates;
    private Runnable mapUpdateRunnable;
    
    //These two variables SHOULT NOT be used outside of the mapUpdateRunnable-run 
    //method this they are invalid in between mapupdates
    private MapRenderer temporaryMapRenderer;
    private MapCameraInterface temporatyMapCamera;

	public MapLayerView(Context context, WayfinderMapView aOwnerMap) {
		super(context);
		this.init(context, aOwnerMap);
	}

	public MapLayerView(Context context, AttributeSet attrs,
			WayfinderMapView aOwnerMap) {
		super(context, attrs);
		this.init(context, aOwnerMap);
	}

	public MapLayerView(Context context, AttributeSet attrs, int defStyle,
			WayfinderMapView aOwnerMap) {
		super(context, attrs, defStyle);
		this.init(context, aOwnerMap);
	}

	private void init(Context context, WayfinderMapView aOwnerMap) {
	    this.p = new Paint();
	    this.p.setTextSize(17);
	    this.p.setAntiAlias(true);
	    
        this.setBackgroundColor(this.getResources().getColor(R.color.color_white));
	    
        iOwnerMap = aOwnerMap;
        this.application = (NavigatorApplication) iOwnerMap.getContext().getApplicationContext();
		handler = new Handler();
		iSurfaceHolder = getHolder();
		iSurfaceHolder.addCallback(this);
		try {
			getHolder().setType(SurfaceHolder.SURFACE_TYPE_HARDWARE);
		} catch (Exception e) {
			try {
				getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
			} catch (Exception e2) {
				try {
					getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
				} catch (Exception e3) {
				}
			}
		}
	}

    public VectorMapInterface getMap() {
        return this.application.getMapInterface();
    }

	public void updateMap() {
		VectorMapInterface vectorInterface = this.getMap();
        if(vectorInterface  != null && vectorInterface.isMapStarted()){
			vectorInterface.requestMapUpdate();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		iOwnerMap.mapInterationDetected();
	    if(this.panningDisabled) {
	        return false;
	    }
	    
	    VectorMapInterface map = this.getMap();

		newMotionEvent = true;
		if (iOwnerMap != null) {
			iOwnerMap.showControls();
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
		    this.isSliding = false;
			mapDoubleTapped = false;
			velocityTracker = VelocityTracker.obtain();
			if(!centeringInProgress){
				// double tap checking
				if (allowDoubleTapping && System.currentTimeMillis() - lastMapTapTime < 400
						&& (Math.abs(lastMapTapX - event.getX()) < 50)
						&& (Math.abs(lastMapTapY - event.getY()) < 50)) {
					map.getMapKeyInterface().pointerReleased(
							(int) event.getX(), (int) event.getY());
					onDoubleMapTap(event.getX(), event.getY());
					mapDoubleTapped = true;
					lastMapTapTime = 0;
				} else {
					lastMapTapTime = System.currentTimeMillis();
					lastMapTapX = event.getX();
					lastMapTapY = event.getY();
					lastX = event.getX();
					lastY = event.getY();
					if (map.isMapStarted()) {
						map.getMapKeyInterface().pointerPressed((int) event.getX(), (int) event.getY());
					}
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!mapDoubleTapped && !centeringInProgress) {
				velocityTracker.addMovement(event);
				processMapDragTo(event.getX(), event.getY());
			}
			
            if((Math.abs(this.lastMapTapX - event.getX()) > MIN_SLIDING_MOVEMENT) || (Math.abs(this.lastMapTapY - event.getY()) > MIN_SLIDING_MOVEMENT)) {
                this.isSliding = true;
            }
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
            if(!this.isSliding 
//            		&& (System.currentTimeMillis() - this.lastMapDownTouchEvent) < 750 
            		&& (System.currentTimeMillis() - this.lastTouchReleasedTime) > 1000) {
                this.iOwnerMap.setMapClicked((int) event.getX(), (int) event.getY());
            }
            this.lastTouchReleasedTime = System.currentTimeMillis();

            if(!this.mapDoubleTapped && !this.centeringInProgress) {
				velocityTracker.addMovement(event);
				if (map.isMapStarted()) {
					map.getMapKeyInterface().pointerReleased((int) event.getX(), (int) event.getY());
				}
				lastX = event.getX();
				lastY = event.getY();
				if(!DISABLE_SLIDING_EFFECT){
					performSlideEffect();
				}
			}
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}
		} else if(event.getAction() == MotionEvent.ACTION_CANCEL){
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
            }
		}
		return true;
	}

	private void performSlideEffect() {
        final VectorMapInterface vectorInterface = this.getMap();

        newMotionEvent = false;
		if (vectorInterface.isMapStarted()) {
			vectorInterface.getMapKeyInterface().pointerPressed((int) lastX, (int) lastY);
		}
		velocityTracker.computeCurrentVelocity(1);
		speedX = velocityTracker.getXVelocity() / 2;
		speedY = velocityTracker.getYVelocity() / 2;
		
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				if (newMotionEvent) {
				    this.cancel();
				    isSliding = false;
				} else if ((Math.abs(speedX) < 0.025 && Math.abs(speedY) < 0.025)) {
					if (vectorInterface.isMapStarted()) {
						vectorInterface.getMapKeyInterface().pointerReleased((int) lastX, (int) lastY);
					}
					this.cancel();
                    isSliding = false;

                    if(rubberBandEffectEnabled && rubberBandPosition != null) {
                        centerMapTo(rubberBandPosition.getMc2Latitude(), rubberBandPosition.getMc2Longitude(), true);
                    }
				}
				processMapDragTo(lastX + speedX * SLIDING_TIME_STEP, lastY + speedY * SLIDING_TIME_STEP);
				speedX *= 0.89;
				speedY *= 0.89;
			}
		};
		timer.schedule(tt, 0, SLIDING_TIME_STEP);
	}

	private void performMapFlyEffect(final int latitude, final int longitude, int duration, final Runnable finalTask) {
        final VectorMapInterface vectorInterface = this.getMap();

        this.centeringInProgress = true;
		final int flyStepDuration = duration / MAX_STEP_NUMBER;
		Position currPos = vectorInterface.getActivePosition();
		
		final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
		final int startLat = currPos.getMc2Latitude();
        final int startLon = currPos.getMc2Longitude();
        this.flyDistanceX = startLon - longitude;
        this.flyDistanceY = startLat - latitude;
        this.flyStepNumber = 0;

		vectorInterface.setCenter(startLat, startLon);

		final TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				if(flyStepNumber >= MAX_STEP_NUMBER) {
					vectorInterface.setCenter(latitude, longitude);
					centeringInProgress = false;
					if(finalTask != null) {
						finalTask.run();
					}
					this.cancel();
				} else {
					flyStepNumber++;
					float timePercent = (float) (flyStepDuration * flyStepNumber) / (float) (MAX_STEP_NUMBER * flyStepDuration);
					float spacePercent = interpolator.getInterpolation(timePercent);
                    if(vectorInterface.isMapStarted()) {
                        vectorInterface.setCenter((int) (startLat - spacePercent * flyDistanceY), (int) (startLon - spacePercent * flyDistanceX));
                    }
				}
			}
		};
		timer.schedule(tt, 0, flyStepDuration);
	}

    private void processMapDragTo(float currentX, float currentY) {
        VectorMapInterface vectorInterface = this.getMap();
        
        if (Math.abs(currentX - lastX) >= 1 || Math.abs(currentY - lastY) >= 1) {
            if (vectorInterface.isMapStarted()) {
                vectorInterface.getMapKeyInterface().pointerDragged((int) currentX, (int) currentY);
            }
            lastX = currentX;
            lastY = currentY;
        }
    }
	
	private void onDoubleMapTap(float x, float y) {
//		long[] centerCoords = iMapCamera.getWorldCoordinate((int)x, (int)y);
//		Runnable zoomTask = new Runnable(){
//			public void run() {
//				getHandler().post(new Runnable(){
//					public void run() {
//						iOwnerMap.zoomIn();	
//					}
//				});
//			}
//		};
//		performMapFlyEffect(centerCoords[0], centerCoords[1], 400, zoomTask);
	}
	
	public void startZoomIn() {
	    Log.i("MapLayerView", "startZoomIn()");

	    VectorMapInterface vectorInterface = this.getMap();
	    vectorInterface.setFollowGpsPosition(false);
	    if(ApplicationSettings.get().getUseFixedZoomLevels()) {
	        float scale = vectorInterface.getScale();
	        for(int i = 1; i < FIXED_ZOOM_LEVELS.length; i ++) {
	            float currScale = FIXED_ZOOM_LEVELS[i];
                if(scale <= currScale) {
	                float prevScale = FIXED_ZOOM_LEVELS[i - 1];
                    vectorInterface.setScale(prevScale);
	                break;
	            }
	        }
	    }
	    else {
	        vectorInterface.getMapKeyInterface().actionInvoked(MapKeyInterface.ACTION_ZOOM_IN);
	    }
        iOwnerMap.invalidate();
	}
	
	public void stopZoomIn() {
        Log.i("MapLayerView", "stopZoomIn()");

        VectorMapInterface vectorInterface = this.getMap();
        vectorInterface.getMapKeyInterface().actionStopped(MapKeyInterface.ACTION_ZOOM_IN);
        iOwnerMap.showControls();
        if (isMaxZoomReached()) {
            iOwnerMap.setZoomInControlEnabled(false);
        }
        
        if (!isMinZoomReached()) {
            iOwnerMap.setZoomOutControlEnabled(true);
        }
	}
	
	public void startZoomOut() {
        Log.i("MpLayerView", "startZoomOut()");

        VectorMapInterface vectorInterface = this.getMap();
        vectorInterface.setFollowGpsPosition(false);
        if(ApplicationSettings.get().getUseFixedZoomLevels()) {
            float scale = vectorInterface.getScale();
            for(int i = FIXED_ZOOM_LEVELS.length - 2; i >= 0; i --) {
                float currScale = FIXED_ZOOM_LEVELS[i];
                if(scale >= currScale) {
                    vectorInterface.setScale(FIXED_ZOOM_LEVELS[i + 1]);
                    break;
                }
            }
        }
        else {
            vectorInterface.getMapKeyInterface().actionInvoked(MapKeyInterface.ACTION_ZOOM_OUT);
        }

        iOwnerMap.invalidate();
	}
	
	public void stopZoomOut() {
        Log.i("MpLayerView", "stopZoomOut()");

        VectorMapInterface vectorInterface = this.getMap();
        vectorInterface.getMapKeyInterface().actionStopped(MapKeyInterface.ACTION_ZOOM_OUT);
        iOwnerMap.showControls();
        if (isMinZoomReached()) {
            iOwnerMap.setZoomOutControlEnabled(false);
        }
        
        if (!isMaxZoomReached()) {
            iOwnerMap.setZoomInControlEnabled(true);
        }
	}
	
	private boolean isMinZoomReached() {
        VectorMapInterface vectorInterface = this.getMap();
		return vectorInterface.getScale() == MAP_MAX_SCALE;
	}

	private boolean isMaxZoomReached() {
        VectorMapInterface vectorInterface = this.getMap();
		return vectorInterface.getScale() == MAP_MIN_SCALE;
	}

	public void lockMapUpdates(boolean lock) {
	    if(this.lockMapUpdates != lock) {
    	    Log.i("MapLayerView", "lockMapUpdates() lock: " + lock);
    	    this.lockMapUpdates = lock;
//    	    if(!this.lockMapUpdates) {
//                getMap().requestMapUpdate();
//    	    }
	    }
	}
	
	public void updateScreen(MapRenderer mapRenderer, MapCameraInterface mapCamera) {
	    if(this.lockMapUpdates) {
	        return;
	    }
	    
	    this.temporaryMapRenderer = mapRenderer;
	    this.temporatyMapCamera = mapCamera;
	    
        mapRenderer.lockMap();
        this.post(this.getMapUpdateRunnable());
	}

	private Runnable getMapUpdateRunnable() {
	    if(this.mapUpdateRunnable == null) {
	       this.mapUpdateRunnable = new Runnable() {
	            public void run() {
                    int colorTransparent = getResources().getColor(R.color.color_transparent);
                    int colorMap = getResources().getColor(R.color.color_map_bkg);
                    setBackgroundColor(colorTransparent);

	                Canvas c = null;
	                try {
	                    if(MapLayerView.this.isShown()) {
	                        c = iSurfaceHolder.lockCanvas();
	                        if(c != null) {
	                            if(g == null) {
	                                g = new AndroidGraphics(c);
	                            }
	                            else {
	                                g.setCanvas(c);
	                            }
	                            long startTime = System.currentTimeMillis();
	                            
	                            g.setColor(colorMap);
	                            g.fillRect(0, 0, c.getWidth(), c.getHeight());

	                            // drawMap
	                            try {
	                                temporaryMapRenderer.renderMap(g);
	                            } catch (Throwable e) {
	                                Log.e("MapLayerView", "[inner]" + e);
	                                e.printStackTrace();
	                            }
	                            
	                            drawDebugInformation(c, System.currentTimeMillis() - startTime);
	                            
	                            // draw overlay
	                            iOwnerMap.drawOverlays(c, temporaryMapRenderer, temporatyMapCamera, mapOverlay);
	                        }
	                    }
	                    else {
	                        Log.e("MapLayerView", "updateScreen() View is not visible");
	                    }
	                } catch (Throwable e) {
	                    Log.e("MapLayerView", "" + e);
	                    e.printStackTrace();
	                } finally {
	                    if (c != null) {
	                        iSurfaceHolder.unlockCanvasAndPost(c);
	                    }
	                    temporaryMapRenderer.unlockMap();
	                }
	            }
	        };
	    }
	    return this.mapUpdateRunnable;
	}
	
	/**
     * Is only be used in the debug versions. Remove any calls of this method before releases.
     */
	private void drawDebugInformation(Canvas c, long l) {
        if(NavigatorApplication.DEBUG_ENABLED) {
            int halfHeight = (c.getHeight() >> 1);
    		String text = l + " ms";
    		c.drawText(text, 10, halfHeight, this.p);
    		
    		String size = c.getWidth() + "x" + c.getHeight();
    		c.drawText(size, 10, halfHeight + 20, this.p);
    		
    		LocationInformation loc = this.application.getOwnLocationInformation();
    		if(loc != null) {
    		    String acc = loc.getAccuracy() + "m";
                c.drawText(acc, 10, halfHeight + 40, this.p);
    		}
    		
    		String zoomLevel = "zoom: " + String.format("%.1f", this.getMap().getScale());
    		c.drawText(zoomLevel, 10, halfHeight + 60, this.p);
        }
	}
	
	public void setVisibleMapArea(View mapOverlay) {
	    this.mapOverlay = mapOverlay;
	}

	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        VectorMapInterface vectorInterface = this.getMap();
        if(vectorInterface != null) {
    		vectorInterface.setDrawArea(0, 0, width, height);
    		if(this.surfaceChangeListener != null) {
    		    this.surfaceChangeListener.surfaceChanged(width, height);
    		}
        }
        else {
            Log.e("MapLayerView", "surfaceChanged() vectorInterface was null");
        }
	}
	
	public void setSurfaceChangeListener(SurfaceChangeListener listener) {
	    this.surfaceChangeListener = listener;
	}

	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		this.application.setMapDrawer(this);
        VectorMapInterface vectorInterface = this.getMap();
		if(vectorInterface != null) {
    		vectorInterface.setMapObjectListener(this.iOwnerMap.getPinsLayer());
    		vectorInterface.requestMapUpdate();
		}
		else {
		    Log.e("MapLayerView", "surfaceCreated() vectorInterface was null");
		}
	}

	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		if(this.application.getMapDrawer() == this){
			this.application.setMapDrawer(this.application);
		}
	}
	
	private void setMapCenter(float latitude, float longitude){
        VectorMapInterface vectorInterface = this.getMap();
		vectorInterface.setCenter((int)latitude, (int)longitude);
		handler.post(new Runnable(){
			public void run() {
				iOwnerMap.invalidate();
			}
		});
	}
    
    public void centerMapTo(int latitude, int longitude, boolean animatedMove){
        if(animatedMove && !DISABLE_ANIMATED_TRANSLATION){
            performMapFlyEffect(latitude, longitude, 500, null);
        } else {
            setMapCenter(latitude, longitude);
        }
    }
    
    public void centerMapTo(int latitude, int longitude, boolean animatedMove, Runnable finalTask){
        if(animatedMove && !DISABLE_ANIMATED_TRANSLATION){
            performMapFlyEffect(latitude, longitude, 1000, finalTask);
        } else {
            setMapCenter(latitude, longitude);
            finalTask.run();
        }
    }

    public void enablePanning(boolean enabled) {
        this.panningDisabled = !enabled;
    }

    public void enableRubberBandEffect(Position position) {
        this.rubberBandEffectEnabled = true;
        this.rubberBandPosition = position;
        this.enablePanning(true);
    }
    
    public void disableRubberBandEffect() {
        this.rubberBandEffectEnabled = false;
        this.rubberBandPosition = null;
    }

    public void updateCopyrightTextPosition(int y) {
        Log.i("MapLayerView", "updateCopyrightTextPosition() y: " + y);
        this.getMap().getMapDetailedConfigInterface().setCopyrightTextPositionY(y);
    }

    public void hideCopyrightText() {
//        int coords[] = {0,0};
//        iOwnerMap.getLocationOnScreen(coords);
//        int bottom = coords[1];
        int bottom = iOwnerMap.getHeight();
        bottom -= getContext().getResources().getDimensionPixelSize(R.dimen.minimized_landmark_list_height_offset);
        
        Log.i("MapLayerView", "hideCopyrightText() y: " + bottom);
        this.getMap().getMapDetailedConfigInterface().setCopyrightTextPositionY(bottom);
    }
    
    public interface SurfaceChangeListener {
        void surfaceChanged(int width, int height);
    }
}
