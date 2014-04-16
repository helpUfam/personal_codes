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

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

/**
 * Actual renderer class.
 * 
 * @author harism
 */
public class CurlRenderer implements GLSurfaceView.Renderer {

	/**
	 * Constant that retains the value 10.
	 */
	private static final int MAX_PAGE_RAIO = 10;

	/**
	 * Constant for requesting left page rect.
	 */
	public static final int PAGE_LEFT = 1;

	/**
	 * Constant for requesting right page rect.
	 */
	public static final int PAGE_RIGHT = 2;

	/**
	 * Constants for changing view mode.
	 */
	public static final int SHOW_ONE_PAGE = 1;
	public static final int SHOW_TWO_PAGES = 2;

	/**
	 * Set to true for checking quickly how perspective projection looks.
	 */
	private static final boolean USE_PERSP_PROJECT = true;

	/**
	 * Background fill color.
	 */
	private transient int mBackgroundColor = 0xFFFFFFFF;

	/**
	 * Curl meshes used for static and dynamic rendering.
	 */
	private transient final Vector<CurlMesh> mCurlMeshes;

	/**
	 * Retains an instance of {@link RectF}
	 */
	private transient final RectF mMargins = new RectF();

	/**
	 * Retains an instance of {@link CurlRenderer.Observer}
	 */
	private transient final CurlRenderer.Observer mObserver;

	/**
	 * Page rectangles.
	 */
	private transient final RectF mPageRectLeft;
	private transient final RectF mPageRectRight;

	/**
	 * View mode.
	 */
	private transient int mViewMode = SHOW_ONE_PAGE;

	/**
	 * Screen size.
	 */
	private transient int mViewportWidth, mViewportHeight;

	/**
	 * {@link Rect} for render area.
	 */
	private transient final RectF mViewRect = new RectF();

	/**
	 * Retains the textures ids.
	 */
	private int[] mTextureIds;

	/**
	 * Retains the progress index.
	 */
	private int progressIndex = -1;

	/**
	 * Controls this state.
	 */
	private boolean mUsePerspectiveCoordinates;

	/**
	 * Retains the Height.
	 */
	private int intHeight;

	/**
	 * Basic constructor.
	 */
	public CurlRenderer(final CurlRenderer.Observer observer,
			boolean mLargeScreen) {
		mObserver = observer;
		mCurlMeshes = new Vector<CurlMesh>();
		mPageRectLeft = new RectF();
		mPageRectRight = new RectF();
		mUsePerspectiveCoordinates = mLargeScreen;
	}

	/**
	 * Adds CurlMesh to this renderer.
	 */
	public synchronized void addCurlMesh(final CurlMesh mesh) {
		removeCurlMesh(mesh);
		mCurlMeshes.add(mesh);
	}

	/**
	 * Returns rect reserved for left or right page. Value page should be
	 * PAGE_LEFT or PAGE_RIGHT.
	 */
	public RectF getPageRect(final int page) {
		RectF rec = mPageRectRight;
		if (page == PAGE_LEFT) {
			rec = mPageRectLeft;
		}
		return rec;
	}

	/**
	 * checkGlError
	 * 
	 * @param op
	 */
	private void checkGlError(String op) {
		int error;
		while ((error = GLES10.glGetError()) != GLES10.GL_NO_ERROR) {
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	/**
	 * Listen when pageCurl need to Draw the Frame
	 * @param gL10
	 */
	@Override
	public synchronized void onDrawFrame(final GL10 gL10) {
		if (processLoadTextures()) {

			gL10.glClearColor(0, 0, 0, 0);
			gL10.glClear(GL10.GL_COLOR_BUFFER_BIT);

			return;
		}

		mObserver.onDrawFrame();

		gL10.glClearColor(Color.red(mBackgroundColor) / 255f,
				Color.green(mBackgroundColor) / 255f,
				Color.blue(mBackgroundColor) / 255f,
				Color.alpha(mBackgroundColor) / 255f);
		gL10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gL10.glLoadIdentity();

		if (USE_PERSP_PROJECT) {
			// gL10.glTranslatef(0, -.1f, -7.8f);
			if (mUsePerspectiveCoordinates) {
				gL10.glTranslatef(0f, 0f, -7.6f);
			} else {
				gL10.glTranslatef(0, 0, -7f);
			}
		}

		for (int i = 0; i < mCurlMeshes.size(); ++i) {
			mCurlMeshes.get(i).getTexturePage()
					.setRectTexture(mObserver.getRectF(i));
			mCurlMeshes.get(i).onDrawFrame(gL10);
		}
	}

	/**
	 * Listen when surface is changed
	 * @param gL10
	 * @param width
	 * @param height
	 */
	@Override
	public void onSurfaceChanged(final GL10 gL10, final int width,
			final int height) {
		// gL10.glViewport(0, 0, width, height);
		if (mUsePerspectiveCoordinates) {
			gL10.glViewport(0, -17, width, height + 36);
		} else {
			gL10.glViewport(0, 0, width, height);
		}
		mViewportWidth = width;
		mViewportHeight = height;

		final float ratio = (float) width / height;

		// changing top/bottom margins, so the curl animation is
		// screen-centered..
		if (mUsePerspectiveCoordinates) {
			mViewRect.top = 0.98f; // 1.0f
			mViewRect.bottom = -1.180f; // -1.0f
		} else {
			mViewRect.top = 0.844f; // 1.0f
			mViewRect.bottom = -0.983f; // -1.0f
		}

		mViewRect.left = -ratio;
		mViewRect.right = ratio;

		// adjusting left/right boundaries according to new top/bottom margins..
		float newHeight = mViewRect.top - mViewRect.bottom;
		float newWidth = (ratio * newHeight);
		float horizontalDelta = mViewRect.width() - newWidth;
		mViewRect.left = mViewRect.left + (horizontalDelta / 2);
		mViewRect.right = mViewRect.right - (horizontalDelta / 2);

		updatePageRects();

		gL10.glMatrixMode(GL10.GL_PROJECTION);
		gL10.glLoadIdentity();
		if (USE_PERSP_PROJECT) {
			GLU.gluPerspective(gL10, 20f, (float) width / height, .1f, 100f);
		} else {
			GLU.gluOrtho2D(gL10, mViewRect.left, mViewRect.right,
					mViewRect.bottom, mViewRect.top);
		}

		gL10.glMatrixMode(GL10.GL_MODELVIEW);
		gL10.glLoadIdentity();
	}

	/***
	 * Listen when Surface is created
	 * @param gL10
	 * @param config
	 */
	@Override
	public void onSurfaceCreated(final GL10 gL10, final EGLConfig config) {
		gL10.glClearColor(1f, 1f, 1f, 1f); // changed to white
		gL10.glShadeModel(GL10.GL_SMOOTH);
		gL10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gL10.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
		gL10.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
		gL10.glEnable(GL10.GL_LINE_SMOOTH);
		gL10.glDisable(GL10.GL_DEPTH_TEST);
		gL10.glDisable(GL10.GL_CULL_FACE);

		gL10.glClearColor(Color.red(mBackgroundColor) / 255f,
				Color.green(mBackgroundColor) / 255f,
				Color.blue(mBackgroundColor) / 255f,
				Color.alpha(mBackgroundColor) / 255f);

		createTextureIDs(gL10);

		mObserver.onSurfaceCreated();
	}

	/**
	 * Creates the texture ids.
	 * 
	 * @param gL10
	 */
	private void createTextureIDs(final GL10 gL10) {
		// Generate texture.
		int countTextures = mObserver.countTextures();

		if (mTextureIds == null || mTextureIds.length != countTextures) {
			mTextureIds = new int[countTextures];
			gL10.glGenTextures(mTextureIds.length, mTextureIds, 0);

			for (int page = 0; page < countTextures; page++) {
				int textureId = mTextureIds[page];

				// Set texture attributes.
				gL10.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
				gL10.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
				gL10.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
				gL10.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
				gL10.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

				pageMap.put(page, textureId);
				textureMap.put(textureId, page);
				mObserver.registerTextureID(page, mTextureIds[page]);

			}
		}
	}

	/**
	 * Map for number of pages.
	 */
	private SparseArray<Integer> pageMap = new SparseArray<Integer>();

	/**
	 * Map for number of textures.
	 */
	private SparseArray<Integer> textureMap = new SparseArray<Integer>();

	/**
	 * Map for booleans, that controls the state of load maps.
	 */
	private SparseBooleanArray loadMap = new SparseBooleanArray();

	/**
	 * Flag that controls this state.
	 */
	private boolean goFront = true;

	/**
	 * Retains the number of the last page.
	 */
	private int lastBackPage = 0;

	/**
	 * Verifies this state.
	 */
	private boolean firedComplete = false;

	/**
	 * Remove the selected page
	 * @param page
	 */
	private synchronized void removePage(int page) {
		Integer idTex = pageMap.get(page);
		if (idTex != null && loadMap.get(page)) {

			GLES10.glDeleteTextures(1, new int[] { idTex }, 0);
			loadMap.put(page, false);

		}
	}

	/**
	 * Creates the page.
	 * 
	 * @param page
	 * @return
	 */
	private synchronized boolean createPage(final int page) {
		// Generate texture.

		int countTextures = mObserver.countTextures();
		if (page >= 0 && page < countTextures && !loadMap.get(page)) {

			int textureId = mTextureIds[page];

			// Set texture attributes.
			GLES10.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
			GLES10.glTexParameterf(GL10.GL_TEXTURE_2D,
					GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			GLES10.glTexParameterf(GL10.GL_TEXTURE_2D,
					GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
			GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
					GL10.GL_CLAMP_TO_EDGE);
			GLES10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
					GL10.GL_CLAMP_TO_EDGE);

			Bitmap texture = mObserver.createBitmapTexture(page);
			GLES10.glBindTexture(GL10.GL_TEXTURE_2D, mTextureIds[page]);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, texture, 0);
			texture.recycle();

			mObserver.registerTextureID(page, textureId);
			mObserver.onLoadingTick();

			loadMap.put(page, true);

			try {
				checkGlError("Loading Page: " + progressIndex);
			} catch (Exception e) {
				// Until now, there is no handling action.
			}

			return true;
		}

		return false;
	}

	/**
	 * Reallocates the pages.
	 * 
	 * @param page
	 * @return
	 */
	private synchronized boolean createPageRealloc(final int page) {
		if (goFront) {
			removePage(page - MAX_PAGE_RAIO);
		} else {
			removePage(page + MAX_PAGE_RAIO);
		}
		return createPage(page);
	}

	/**
	 * Calls the reallocator of pages.
	 * 
	 * @param page
	 * @return
	 */
	private synchronized boolean processLoadCreatePage(final int page) {

		boolean result = false;

		if (firedComplete) {
			if (mObserver.isTouching()) {
				mObserver.onLoadingTick();
			} else if (!createPageRealloc(page)) {
				for (int i = page - MAX_PAGE_RAIO / 2 + 2; i < page
						+ MAX_PAGE_RAIO / 2 - 2; i++) {
					
					result = createPageRealloc(i);
					
					if ( result ) {
						break;
					}
				}
			}
		} else {
			result = createPageRealloc(page);
		}

		return result;
	}

	/**
	 * Loads the textures by pages.
	 * 
	 * @param idTex
	 * @return
	 */
	private synchronized boolean processLoadTexturesByPage(int idTex) {
		Integer page = textureMap.get(idTex);
		boolean loading = false;

		if (processLoadCreatePage(page)) {
			loading = true;
		}
		;

		return loading;
	}

	/**
	 * Calls the loader of textures.
	 * 
	 * @return
	 */
	private synchronized boolean processLoadTextures() {

		boolean loading = false;

		for (int i = 0; i < mCurlMeshes.size(); ++i) {
			CurlPage texture = mCurlMeshes.get(i).getTexturePage();

			if (texture == null) {
				mObserver.onLoadingTick();
				continue;
			}

			final Integer idTex1 = texture.getTextureIdBack();
			final Integer idTex2 = texture.getTextureIdFront();

			if (idTex1 == null || idTex2 == null) {
				//mObserver.onLoadingTick();
				continue;
			}

			final Integer page1 = textureMap.get(idTex1);
			final Integer page2 = textureMap.get(idTex2);

			goFront = (page1 - lastBackPage) > 0;
			lastBackPage = page1;

			if (processLoadTexturesByPage(idTex1)) {

				loading = true;
				break;

			} else if (processLoadTexturesByPage(idTex2)) {

				loading = true;
				break;

			}

			mObserver.waitNeeds(!loadMap.get(page1) || !loadMap.get(page2));
		}

		if (!loading) {
			if (!firedComplete) {
				if (loadMap.size() >= MAX_PAGE_RAIO / 4 - 1) 
				{
					firedComplete = true;
					mObserver.onLoadingComplete();
				}

			}
		}

		return loading && !firedComplete;
	}

	/**
	 * Sets the reload textures.
	 */
	public synchronized void setReloadTextures() {
		progressIndex = -1;

		int countTextures = mObserver.countTextures();
		for (int i = 0; i < countTextures; i++) {
			removePage(i);
		}

		firedComplete = false;
	}

	/**
	 * Sets the fire complete.
	 */
	public void setReFireComplete() {
		firedComplete = false;
	}

	/**
	 * Removes CurlMesh from this renderer.
	 */
	public synchronized void removeCurlMesh(final CurlMesh mesh) {
		while (mCurlMeshes.remove(mesh))
			;
	}

	/**
	 * Change background/clear color.
	 */
	public void setBackgroundColor(final int color) {
		mBackgroundColor = color;
	}

	/**
	 * Set margins or padding. Note: margins are proportional. Meaning a value
	 * of .1f will produce a 10% margin.
	 */
	public synchronized void setMargins(final float left, final float top,
			final float right, final float bottom) {
		mMargins.left = left;
		mMargins.top = top;
		mMargins.right = right;
		mMargins.bottom = bottom;
		updatePageRects();
	}

	/**
	 * Sets visible page count to one or two. Should be either SHOW_ONE_PAGE or
	 * SHOW_TWO_PAGES.
	 */
	public synchronized void setViewMode(final int viewmode) {
		if (viewmode == SHOW_ONE_PAGE) {
			mViewMode = viewmode;
			updatePageRects();
		} else if (viewmode == SHOW_TWO_PAGES) {
			mViewMode = viewmode;
			updatePageRects();
		}
	}

	/**
	 * Translates screen coordinates into view coordinates.
	 */
	public void translate(final PointF point) {
		point.x = mViewRect.left
				+ (mViewRect.width() * point.x / mViewportWidth);
		point.y = mViewRect.top
				- (-mViewRect.height() * point.y / mViewportHeight);
	}

	/**
	 * Recalculates page rectangles.
	 */
	private void updatePageRects() {
		if (mViewRect.width() == 0 || mViewRect.height() == 0) {
			return;
		} else if (mViewMode == SHOW_ONE_PAGE) {
			mPageRectRight.set(mViewRect);
			mPageRectRight.left += mViewRect.width() * mMargins.left;
			mPageRectRight.right -= mViewRect.width() * mMargins.right;
			mPageRectRight.top += mViewRect.height() * mMargins.top;
			mPageRectRight.bottom -= mViewRect.height() * mMargins.bottom;

			mPageRectLeft.set(mPageRectRight);
			mPageRectLeft.offset(-mPageRectRight.width(), 0);

			final int bitmapW = (int) ((mPageRectRight.width() * mViewportWidth) / mViewRect
					.width());
			final int bitmapH = (int) ((mPageRectRight.height() * mViewportHeight) / mViewRect
					.height());

			setIntHeight(bitmapH);

			mObserver.onPageSizeChanged(bitmapW, bitmapH);
		} else if (mViewMode == SHOW_TWO_PAGES) {
			mPageRectRight.set(mViewRect);
			mPageRectRight.left += mViewRect.width() * mMargins.left;
			mPageRectRight.right -= mViewRect.width() * mMargins.right;
			mPageRectRight.top += mViewRect.height() * mMargins.top;
			mPageRectRight.bottom -= mViewRect.height() * mMargins.bottom;

			mPageRectLeft.set(mPageRectRight);
			mPageRectLeft.right = (mPageRectLeft.right + mPageRectLeft.left) / 2;
			mPageRectRight.left = mPageRectLeft.right;

			final int bitmapW = (int) ((mPageRectRight.width() * mViewportWidth) / mViewRect
					.width());
			final int bitmapH = (int) ((mPageRectRight.height() * mViewportHeight) / mViewRect
					.height());

			setIntHeight(bitmapH);

			mObserver.onPageSizeChanged(bitmapW, bitmapH);
		}
	}

	/**
	 * Returns the value of the intHeight
	 * 
	 * @return
	 */
	public int getIntHeight() {
		return this.intHeight;
	}

	/**
	 * Sets the value of the intHeight
	 */
	public void setIntHeight(int height) {
		this.intHeight = height;
	}

	/**
	 * Observer for waiting render engine/state updates.
	 */
	public interface Observer {
		/**
		 * Called from onDrawFrame called before rendering is started. This is
		 * intended to be used for animation purposes.
		 */
		void onDrawFrame();

		void waitNeeds(boolean b);
		
		/**
		 * Returns true if curlPage is been touched
		 * @return
		 */
		boolean isTouching();

		/**
		 * Return the Rect of the selected item
		 * @param i
		 * @return
		 */
		RectF getRectF(int i);

		void onLoadingTick();

		/**
		 * Called once page size is changed. Width and height tell the page size
		 * in pixels making it possible to update textures accordingly.
		 */
		void onPageSizeChanged(int width, int height);

		/**
		 * Called from onSurfaceCreated to enable texture re-initialization etc
		 * what needs to be done when this happens.
		 */
		void onSurfaceCreated();

		void registerTextureID(int index, int i);

		/**
		 * Create the Bitmap Texture
		 * @param page
		 * @return
		 */
		Bitmap createBitmapTexture(int page);

		int countTextures();

		void onLoadingComplete();

		void onResize();

	}
}
