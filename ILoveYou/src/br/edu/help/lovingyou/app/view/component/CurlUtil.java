/*
 * Copyright (c) 2012 Samsung Electronics Co., Ltd.
 * All rights reserved.
 *
 * This software is a confidential and proprietary information of Samsung
 * Electronics, Inc. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Samsung Electronics.
 */

package br.edu.help.lovingyou.app.view.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Provides some resources to Page Curl.
 */
public final class CurlUtil {

	/**
	 * Public constructor
	 */
	private CurlUtil() {
	}

	/**
	 * Calculates the next highest power of two for a given integer.
	 */
	private static int getNextHighestPO2(int n) {
		n -= 1;
		n = n | (n >> 1);
		n = n | (n >> 2);
		n = n | (n >> 4);
		n = n | (n >> 8);
		n = n | (n >> 16);
		return n + 1;

	}

	/**
	 * Generates nearest power of two sized Bitmap for give Bitmap. Returns this
	 * new Bitmap using default return statement + original texture coordinates
	 * are stored into RectF.
	 */
	public static Bitmap getTexture(Bitmap bitmap, RectF textureRect,
			Context applicationContext, int densityImageMagazine) {
		
		if(bitmap == null) {
			bitmap = Bitmap.createBitmap(1,1,
					Bitmap.Config.ARGB_8888);
			bitmap.eraseColor(0xFFFFFFFF);
		}
			
		// Bitmap original size.
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		// forcing a lower density screen device to a higher resolution setup..
		int density = (int) applicationContext.getResources().getDimension(
				densityImageMagazine);
		w = (w * density / applicationContext.getResources()
				.getDisplayMetrics().densityDpi);
		h = (h * density / applicationContext.getResources()
				.getDisplayMetrics().densityDpi);

		// Bitmap size expanded to next power of two. This is done due to
		// the requirement on many devices, texture width and height should
		// be power of two.
		int newW = getNextHighestPO2(w);
		int newH = getNextHighestPO2(h);

		// TODO: Is there another way to create a bigger Bitmap and copy
		// original Bitmap to it more efficiently? Immutable bitmap anyone?
		Bitmap bitmapTex = Bitmap.createBitmap(newW, newH, bitmap.getConfig());
		Canvas c = new Canvas(bitmapTex);

		// forcing a lower density screen device to a higher resolution setup..
		c.setDensity(density);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);

		c.drawBitmap(bitmap, 0, 0, paint);

		// Calculate final texture coordinates.
		float texX = (float) w / newW;
		float texY = (float) h / newH;
		textureRect.set(0f, 0f, texX, texY);

		return bitmapTex;
	}

	/** 
	 * Loads bitmap scaling it to fit approximately the given dimensions. 
	 */
	public static Bitmap getPageSampledBitmap(final String filePath,
			final int reqWidth, final int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		int inSampleSize = 1;
		if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
			if (options.outWidth > options.outHeight) {
				inSampleSize = Math.round((float) options.outHeight
						/ (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) options.outWidth
						/ (float) reqWidth);
			}
		}
		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

}
