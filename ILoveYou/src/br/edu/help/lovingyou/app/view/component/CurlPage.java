/*
   Copyright 2012 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package br.edu.help.lovingyou.app.view.component;

import android.graphics.Color;
import android.graphics.RectF;

/**
 * Storage class for page textures, blend colors and possibly some other values
 * in the future.
 * 
 * @author harism
 */
/**
 * @author douglas.soares
 * 
 */
public final class CurlPage {

	/**
	 * Constant that retains the value 2.
	 */
	public static final int SIDE_BACK = 2;

	/**
	 * Constant that retains the value 3.
	 */
	public static final int SIDE_BOTH = 3;

	/**
	 * Constant that retains the value 2.
	 */
	public static final int SIDE_FRONT = 1;

	/**
	 * Retains the color back.
	 */
	private transient int mColorBack;

	/**
	 * Retains the color front.
	 */
	private transient int mColorFront;

	/**
	 * Verifies the state of textures.
	 */
	private transient boolean mTexturesChanged;

	/**
	 * Retains the number of the page.
	 */
	private int page;

	/**
	 * Retains the id of texture.
	 */
	private Integer textureIdFront;

	/**
	 * Retains the id of texture.
	 */
	private Integer textureIdBack;

	/**
	 * Retains an instance of {@link RectF}
	 */
	private RectF rectTexture;

	/**
	 * Default constructor.
	 */
	public CurlPage() {
		reset();
	}

	/**
	 * Getter for color.
	 */
	public int getColor(final int side) {
		int res = mColorBack;
		if (side == SIDE_FRONT) {
			res = mColorFront;
		}
		return res;
	}

	/**
	 * Returns true if textures have changed.
	 */
	public boolean isTexturesChanged() {
		return mTexturesChanged;
	}

	/**
	 * Returns true if back siding texture exists and it differs from front
	 * facing one.
	 */
	public boolean hasBackTexture() {
		if (textureIdBack == null || textureIdFront == null) {
			return true;
		}

		return !textureIdFront.equals(textureIdBack);
	}

	/**
	 * Recycles and frees underlying Bitmaps.
	 */
	public void recycle() {
		mTexturesChanged = false;
	}

	/**
	 * Resets this CurlPage into its initial state.
	 */
	public void reset() {
		mColorBack = Color.WHITE;
		mColorFront = Color.WHITE;
		recycle();
	}

	/**
	 * Setter blend color.
	 */
	public void setColor(final int color, final int side) {
		switch (side) {
		case SIDE_FRONT:
			mColorFront = color;
			break;
		case SIDE_BACK:
			mColorBack = color;
			break;
		default:
			mColorFront = mColorBack = color;
			break;
		}
	}

	/**
	 * Setter for textures.
	 * 
	 * @param textureId
	 * @param page
	 * @param rect
	 */
	public void setTexture(final int side, int page, Integer textureId,
			RectF rect) {
		this.page = page;
		this.rectTexture = rect;

		switch (side) {
		case SIDE_FRONT:
			this.textureIdFront = textureId;
			break;

		case SIDE_BACK:
			this.textureIdBack = textureId;
			break;

		case SIDE_BOTH:
			this.textureIdFront = textureId;
			this.textureIdBack = textureId;
			break;
		}
		mTexturesChanged = true;
	}

	/**
	 * Gets the texture of the rectangle.
	 * 
	 * @return {@link RectF}
	 */
	public RectF getRectTexture() {
		return rectTexture;
	}

	/**
	 * Sets the texture of the rectangle.
	 * 
	 * @param rectTexture
	 */
	public void setRectTexture(RectF rectTexture) {
		this.rectTexture = rectTexture;
	}

	/**
	 * Gets the id of the texture.
	 * 
	 * @return {@link Integer}
	 */
	public Integer getTextureIdFront() {
		return textureIdFront;
	}

	/**
	 * Gets the id of the texture.
	 * 
	 * @return {@link Integer}
	 */
	public Integer getTextureIdBack() {
		return textureIdBack;
	}

	/**
	 * Gets the number of page.
	 * 
	 * @return int.
	 */
	public int getPage() {
		return page;
	}

}
