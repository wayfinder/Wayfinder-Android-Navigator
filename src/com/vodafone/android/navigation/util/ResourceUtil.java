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
package com.vodafone.android.navigation.util;

import com.wayfinder.core.shared.geocoding.AddressInfo;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ResourceUtil {

    public static Bitmap scale(Bitmap bitmap, int maxWidth, int maxHeight) {
        if(bitmap == null) {
            return null;
        }
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float ratio = (float) width / (float) height;
           
        int dstWidth = maxWidth;
        int dstHeight = (int) (dstWidth / ratio);
        
        if(dstHeight > maxHeight) {
            dstHeight = maxHeight;
            dstWidth = (int) (dstHeight * ratio);
        }
        
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
        
        return scaled;
    }
    
    public static Bitmap getDrawable(Context context, String filename) {
        int pictogramId = getDrawableId(context, filename);

        Resources res = context.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, pictogramId);
        return bitmap;
    }

    public static int getDrawableId(Context context, String filename) {
        //remove suffix from filename: my_pic.png -> my_pic
        int index = filename.lastIndexOf('.');
        if(index >= 0) {
            filename = filename.substring(0, index);
        }
        
        Application app = (Application) context.getApplicationContext();
        Resources res = context.getResources();
        int id = res.getIdentifier(filename, "drawable", app.getPackageName());
        return id;
    }
    
    public static String getAddressAsString(AddressInfo addressInfo, boolean includeStateCountry) {
        StringBuffer s = new StringBuffer();

        String street = addressInfo.getStreet();
        String citypart = addressInfo.getCityPart();
        String municipal = addressInfo.getMunicipal();
        String city = addressInfo.getCity();
        String state = addressInfo.getCountryOrState();
        
        boolean added = false;
        if(street.length() > 0) {
            s.append(street);
            added = true;
        }
        
        if(city.length() > 0) {
            if(added) {
                s.append(", ");
            }
            s.append(city);
            added = true;
        }
        else if(citypart.length() > 0) {
            if(added) {
                s.append(", ");
            }
            s.append(citypart);
            added = true;
        }
        else if(municipal.length() > 0) {
            if(added) {
                s.append(", ");
            }
            s.append(municipal);
            added = true;
        }

        if(includeStateCountry) {
            if(state.length() > 0 
                    && !state.equalsIgnoreCase(street) 
                    && !state.equalsIgnoreCase(city)
                    && !state.equalsIgnoreCase(citypart)
                    && !state.equalsIgnoreCase(municipal)) {
                if(added) {
                    s.append("\n");
                }
                s.append(state);
                added = true;
            }
        }

        return s.toString();
    }
}
