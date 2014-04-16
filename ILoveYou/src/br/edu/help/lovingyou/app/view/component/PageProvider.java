package br.edu.help.lovingyou.app.view.component;

import android.graphics.Bitmap;
import android.graphics.RectF;

/**
 * Provider for feeding 'book' with bitmaps which are used for rendering
 * pages.
 */
public interface PageProvider {

	/**
	 * Return number of pages available.
	 */
	int getPageCount();

	void waitNeeds(boolean needs);

	RectF getRectF(int i);

	int countTextures();

	void registerTextureID(int index, int mTextureIds);

	/**
	 * Called once new bitmaps/textures are needed. Width and height are in
	 * pixels telling the size it will be drawn on screen and following them
	 * ensures that aspect ratio remains. But it's possible to return bitmap
	 * of any size though. You should use provided CurlPage for storing page
	 * information for requested page number.<br/>
	 * <br/>
	 * Index is a number between 0 and getBitmapCount() - 1.
	 */
	void updatePage(CurlPage page, int index);

	Bitmap createPage(int page, int width, int height);

	void showLastPageOverlay();

	boolean isNumberPagesOdd();

	void loadComplete();

	public void recycleBitmaps();

	public void setFavorite();

	public int getIndexCount();
}