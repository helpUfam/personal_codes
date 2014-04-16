package br.edu.help.lovingyou.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import br.edu.help.lovingyou.R;
import br.edu.help.lovingyou.app.view.adapters.PgProvider;
import br.edu.help.lovingyou.app.view.adapters.SizeChangeObserver;
import br.edu.help.lovingyou.app.view.component.CurlUtil;
import br.edu.help.lovingyou.app.view.component.curl.CurlPage;
import br.edu.help.lovingyou.app.view.component.curl.CurlView;
import br.edu.help.lovingyou.util.FileUtils;
import br.edu.help.lovingyou.view.HomeFragmentView;

/**
 * The Class HomeFragment.
 *
 * @author hildon.lima
 */
public class HomeFragment extends Fragment implements HomeFragmentView {
	
	/** The curl view. */
	private CurlView curlView;
	
	/** The pages provider. */
	private PgProvider pagesProvider;
	
	/** The size change observer. */
	private SizeChangeObserver sizeChangeObserver;
	
	private PageProvider provider;
	
	/**
	 * New instance.
	 *
	 * @param args the args
	 * @return the home fragment
	 */
	public static HomeFragment newInstance(final Bundle args) {
		final HomeFragment fragment = new HomeFragment();
		fragment.setArguments(args);
		return fragment;
	}
	
	/** {@inheritDoc} **/
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		final View mainView = inflater.inflate(R.layout.home_fragment, null);
		
		curlView = (CurlView) mainView.findViewById(R.id.curl_view);
//		pagesProvider = new PgProvider(null, getActivity(), curlView, FileUtils.findFiles(getActivity(), "Anne_Hildon"));
		
		final int index = 0;
		sizeChangeObserver = new SizeChangeObserver();
		
		provider = new PageProvider(FileUtils.findFiles(getActivity(), "Anne_Hildon"));
		curlView.setPageProvider(provider);
		curlView.setSizeChangedObserver(new SizeChangedObserver());
		curlView.setCurrentIndex(index);
		curlView.setBackgroundColor(Color.TRANSPARENT);
		curlView.setAllowLastPageCurl(false);
		
		return mainView;
	}
	
	/**
	 * Bitmap provider.
	 */
	private class PageProvider implements CurlView.PageProvider {

		// Bitmap resources.
//		private int[] mBitmapIds = { R.drawable.obama, R.drawable.road_rage,
//				R.drawable.taipei_101, R.drawable.world };
		
		/**
		 * Retains the color of page number's circle.
		 */
		private static final int COLOR = 0xFFC0C0C0;

		/**
		 * Map that retains the textures ids.
		 */
		SparseArray<Integer> pageMap = new SparseArray<Integer>();

		/**
		 * Map that retains {@link RectF}
		 */
		SparseArray<RectF> rectMap = new SparseArray<RectF>();

		/**
		 * Constant to get the hundred percent
		 */
		private static final int PERCENT = 100;

		/**
		 * Constant that retains the value 255.
		 */
		private static final int ALPHA = 255;

		/**
		 * Constant that retains the value 10.
		 */
		private static final int DECIMAL = 10;

		/**
		 * Constant that retains the value 11.
		 */
		private static final int SIZECIRCLE = 11;

		/**
		 * Retains the iconBitmap
		 */
		private Bitmap iconBitmapOriginal;

		/**
		 * Retains an instance of {@link Bitmap}
		 */
		private Bitmap iconBitmap;

		/**
		 * Set the state of pages Numbers
		 */
		private transient boolean isNumberPagesEven = false;

		/**
		 * Verifies this state.
		 */
		private transient boolean isNumberPagesOdd = false;

		/**
		 * Retains the margin.
		 */
		private int margin;

		/**
		 * Verifies this state.
		 */
		private boolean isWidth;

		/**
		 * Retains an instance of {@link Paint}
		 */
		private Paint paint;
		
		/** Retains the value 2. */
		private int marginup = 2;

		/** Retains the value 2. */
		private int margindown = 2;

		/** Retains the value 2. */
		private int marginleft = 2;

		/** Retains the value 2. */
		private int marginrigth = 2;

		/**
		 * Retains an instance of {@link RectF}
		 */
		private Rect rect;
		
		private List<String> fileList = new ArrayList<String>();
		
		public PageProvider(final List<String> list) {
			this.fileList = list;
		}
		
		@Override
		public int getPageCount() {
			return fileList.size();
		}
		
		/**
		 * Sets the right Page border
		 * 
		 * @param width
		 * @param height
		 * @param bitmap
		 * @return
		 */
		public int setBorder(int width, int height, Bitmap bitmap) {
			if (margin != -1)
				return margin;
			int percentHeight = bitmap.getHeight() * PERCENT / height;
			int percentWidth = bitmap.getWidth() * PERCENT / width;
			int dif = percentHeight - percentWidth;
			if (dif < 25 && dif > -4) {
				isWidth = true;
				margin = (int) getResources().getDimension(
						R.dimen.border_margin) - 5;
			} else if (percentHeight > percentWidth) {
				isWidth = true;
				int percentOpposite = PERCENT - percentHeight;
				int defaultPercent = bitmap.getWidth() * percentOpposite / 100;
				int increased = bitmap.getWidth() + defaultPercent;
				int diference = width - increased;
				margin = (int) (diference / 6);

				if ((increased >= width) || percentOpposite < 1) {

					margin = 0;
				}
			} else if (percentHeight < percentWidth) {
				isWidth = false;
				int percentOpposite = PERCENT - percentWidth;
				int defaultPercent = bitmap.getHeight() * percentOpposite
						/ PERCENT;
				int increased = bitmap.getHeight() + defaultPercent;
				int diference = height - increased;
				margin = (int) (diference / 6) - 10;
				if ((increased >= height) || dif > -6) {
					margin = 0;

				}
			}

			return margin;

		}
		
		/**
		 * Sets the rectangle.
		 * 
		 * @param width
		 * @param height
		 * @param draw
		 */
		public void setRects(int width, int height, Drawable draw) {
			if (rect != null)
				return;

			int border = 0;
			int marginSet = setBorder(width, height,
					((BitmapDrawable) draw).getBitmap());
			rect = new Rect(marginleft, marginup, width - marginrigth, height
					- margindown);

			int imageWidth = rect.width() - (border * 2);
			int imageHeight = imageWidth * draw.getIntrinsicHeight()
					/ draw.getIntrinsicWidth();
			if (imageHeight > rect.height() - (border * 2)) {
				imageHeight = rect.height() - (border * 2);
				imageWidth = imageHeight * draw.getIntrinsicWidth()
						/ draw.getIntrinsicHeight();
			}

			int intHeight = curlView.getPageHeight();
			if (isWidth) {
				rect.left += ((rect.width() - imageWidth) / 2) - border
						+ (marginSet / 2);
				rect.right = rect.left + imageWidth + border + border
						- marginSet;
				rect.top = (int) getResources().getDimension(
						R.dimen.margin_curlview);
				rect.bottom = intHeight - rect.top;
			} else {
				rect.left += ((rect.width() - imageWidth) / 2) - border;
				rect.right = rect.left + imageWidth + border + border;
				rect.top = (int) getResources().getDimension(
						R.dimen.margin_curlview)
						+ marginSet;
				rect.bottom = intHeight - rect.top;
			}

			rect.left += border;
			rect.right -= border;
			rect.top += border;
			rect.bottom -= border;
		}

		/**
		 * Loads the selected bitmap
		 * 
		 * @param width
		 * @param height
		 * @param index
		 * @param isBackPage
		 * @return
		 */
		private Bitmap loadBitmap(final int width, final int height,
				final int index, final boolean isBackPage) {

			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			bitmap.eraseColor(0xFFFFFFFF);
			final Canvas canvas = new Canvas(bitmap);

			// TODO: Em teste
			final String filename = fileList.get(index >= fileList
					.size() ? fileList.size() - 1 : index);
			Drawable draw;

			Bitmap bm = CurlUtil.getPageSampledBitmap(filename,
					bitmap.getWidth(), bitmap.getHeight());

			if (bm == null) {
//				finish();
				return null;
			}

//			adjustIconSizeToMagazineWidth(bm);

			draw = new BitmapDrawable(getResources(), bm);
//			draw = addIconToImage(iconBitmap, draw);
			draw = addNumberPage(index, ((BitmapDrawable) draw).getBitmap());

			setRects(width, height, draw);

			if (isBackPage
					&& curlView.getViewMode() == CurlView.SHOW_TWO_PAGES && paint != null) {
				canvas.drawLine(width - 1, 0, width - 1, height, paint);
			}

			draw.setBounds(rect);
			draw.draw(canvas);

			if (isBackPage) {
				final Matrix matrix = new Matrix();
				matrix.preScale(-1, 1);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
						matrix, false);
			}
			return bitmap;
		}
//		private Bitmap loadBitmap(final int width, final int height, final int index) {
//			final Bitmap b = Bitmap.createBitmap(width, height,
//					Bitmap.Config.ARGB_8888);
//			b.eraseColor(0xFFFFFFFF);
//			final Canvas c = new Canvas(b);
////			Drawable d = getResources().getDrawable(mBitmapIds[index]);
//			
//			final Bitmap bitmap = Bitmap.createBitmap(width, height,
//					Bitmap.Config.ARGB_8888);
//			bitmap.eraseColor(0xFFFFFFFF);
//			final Canvas canvas = new Canvas(bitmap);
//
//			// TODO: Em teste
//			final String filename = fileList.get(index >= fileList
//					.size() ? fileList.size() - 1 : index);
//			
//			Drawable d = new BitmapDrawable(getResources(), bitmap);
//
//			final Bitmap bm = CurlUtil.getPageSampledBitmap(filename,
//					bitmap.getWidth(), bitmap.getHeight());
//
//			if (bm == null) {
////				finish();
//				return null;
//			}
//
////			adjustIconSizeToMagazineWidth(bm);
//
//			d = new BitmapDrawable(getResources(), bm);
////			draw = addIconToImage(iconBitmap, draw);
//
//			d.draw(canvas);
//
//			final int margin = 7;
//			final int border = 3;
//			final Rect r = new Rect(margin, margin, width - margin, height - margin);
//
//			int imageWidth = r.width() - (border * 2);
//			int imageHeight = imageWidth * d.getIntrinsicHeight()
//					/ d.getIntrinsicWidth();
//			if (imageHeight > r.height() - (border * 2)) {
//				imageHeight = r.height() - (border * 2);
//				imageWidth = imageHeight * d.getIntrinsicWidth()
//						/ d.getIntrinsicHeight();
//			}
//
//			r.left += ((r.width() - imageWidth) / 2) - border;
//			r.right = r.left + imageWidth + border + border;
//			r.top += ((r.height() - imageHeight) / 2) - border;
//			r.bottom = r.top + imageHeight + border + border;
//
//			final Paint p = new Paint();
//			p.setColor(0xFFC0C0C0);
//			c.drawRect(r, p);
//			r.left += border;
//			r.right -= border;
//			r.top += border;
//			r.bottom -= border;
//
//			d.setBounds(r);
//			d.draw(c);
//
//			return b;
//		}
		
		/**
		 * Insert the number on the page
		 * 
		 * @param index
		 * @param bitmap
		 * @return
		 */
		@SuppressLint("ResourceAsColor")
		private Drawable addNumberPage(int index, Bitmap bitmap) {

			int sizeCircle = bitmap.getWidth()
					* (int) getResources()
							.getDimension(R.dimen.percentImageSize) / PERCENT;
			int marginCircle = (int) getResources()
					.getDimension(R.dimen.circleNumberMargin);
			int marginNumber = (int) (bitmap.getWidth()
					* (int) getResources()
							.getDimension(R.dimen.percentNumberMargin) / PERCENT)
					+ marginCircle;
			int sizeNumber = bitmap.getWidth()
					* (int) getResources()
							.getDimension(R.dimen.percentNumberSize) / PERCENT;
			int positionText = bitmap.getWidth()
					* (int) getResources()
							.getDimension(R.dimen.percentTextPosition)
					/ PERCENT;

			double eightyPercent = (double) getResources().getDimension(R.dimen.percentCirclePosition);
			Drawable drawable = getResources()
					.getDrawable(R.drawable.ic_drawer);

//			if (gameView.isTablet()) {
//				drawable = getApplicationContext().getResources().getDrawable(
//						R.drawable.page_circle_tab);
//				marginCircle = SIZECIRCLE;
//				marginNumber = (int) (bitmap.getWidth() * 1.5 / PERCENT)
//						+ marginCircle;
//				sizeCircle = (int) (bitmap.getWidth() * 8 / PERCENT);
//				sizeNumber = bitmap.getWidth() * 4 / PERCENT;
//				eightyPercent = 88;
//				positionText = (int) (bitmap.getWidth() * 89.8 / PERCENT);
//			}

			int number = index+1;
			String text = number < DECIMAL ? ("0" + number) : (number + "");
			
			Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
			Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

			Canvas canvas = new Canvas(mutableBitmap);
			Bitmap bit = Bitmap.createScaledBitmap(
					((BitmapDrawable) drawable).getBitmap(), sizeCircle,
					sizeCircle, false);

			Paint paint = new Paint();
			paint.setTextSize(sizeNumber);
			paint.setAlpha(ALPHA);
			paint.setAntiAlias(false);
			paint.setDither(true);
			paint.setFilterBitmap(true);

//			Typeface robotoRegular = Typeface.createFromAsset(getContext()
//					.getAssets(), "fonts/Roboto-Bold.ttf");
//			paint.setTypeface(robotoRegular);

			canvas.drawBitmap(bit,
					(int) (bitmap.getWidth() * eightyPercent / PERCENT),
					marginCircle, paint);
			paint.setColor((int) R.color.color_number);
			canvas.drawText(text, positionText, sizeNumber + marginNumber,
					paint);

			return new BitmapDrawable(bitmap);
		}

		/**
		 * Returns the page Texture
		 * 
		 * @param index
		 */
		@Override
		public RectF getRectF(int index) {
			return rectMap.get(index);
		}

		/**
		 * Updates the pages.
		 * 
		 * @param page
		 * @param index
		 * @param side
		 */
		public void update(final br.edu.help.lovingyou.app.view.component.CurlPage page, int index, int side) {
			page.setTexture(side, index, this.pageMap.get(index),
					rectMap.get(index));
		}
		
		/**
		 * Changes the state of a page on curlView.
		 * 
		 * @param page
		 * @param width
		 * @param height
		 * @param index
		 */
		@Override
		public void updatePage(final br.edu.help.lovingyou.app.view.component.CurlPage page, final int index) {

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				update(page, index, CurlPage.SIDE_FRONT);
				update(page, index, CurlPage.SIDE_BACK);

				page.setColor(Color.argb(200, 200, 200, 200),
						CurlPage.SIDE_BACK);

			} else {
				if (index > 1 && index <= getPageCount() - 2) {

					update(page, index * 2, CurlPage.SIDE_FRONT);
					update(page, index * 2 + 1, CurlPage.SIDE_BACK);

				} else if (index == 0) {

					update(page, index, CurlPage.SIDE_FRONT);
					update(page, index + 1, CurlPage.SIDE_BACK);

				} else if (index == 1) {

					update(page, index + 1, CurlPage.SIDE_FRONT);
					update(page, index + 2, CurlPage.SIDE_BACK);

				} else {

					if (index == getPageCount() - 1 && isNumberPagesEven) {

						update(page, index * 2, CurlPage.SIDE_FRONT);
						update(page, index * 2, CurlPage.SIDE_BACK);

						page.setColor(Color.argb(0, 255, 255, 255),
								CurlPage.SIDE_FRONT);

					} else {

						update(page, index * 2, CurlPage.SIDE_FRONT);
						update(page, index * 2 + 1, CurlPage.SIDE_BACK);
					}
				}

			}
		}

		@Override
		public void recycleBitmaps() {
			iconBitmap.recycle();
		}

		/**
		 * Unused
		 */
		@Override
		public void showLastPageOverlay() {
		}

		/**
		 * Unused
		 */
		@Override
		public void waitNeeds(boolean needs) {
		}

		@Override
		public int countTextures() {
			return fileList.size();
		}

		@Override
		public boolean isNumberPagesOdd() {
			return isNumberPagesOdd;
		}

		@Override
		public void registerTextureID(int page, int textureId) {
			pageMap.put(page, textureId);
		}
		
		@Override
		public Bitmap createPage(int page, int width, int height) {
			rectMap.put(page, new RectF(0, 0, 1, 1));

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				return CurlUtil.getTexture(
						loadBitmap(width, height, page, false),
						rectMap.get(page),
						getActivity().getApplicationContext(),
						R.dimen.densityImageCurlview);
			} else {
				if (page >= 1 && page <= getPageCount() * 2 - 2) {
					return CurlUtil.getTexture(
							loadBitmap(width, height, page, page % 2 == 1),
							rectMap.get(page), getActivity().getApplicationContext(),
							R.dimen.densityImageCurlview);
				} else {
					return CurlUtil.getTexture(
							loadBitmap(width, height, page, false), rectMap
									.get(page),getActivity().getApplicationContext(),
							R.dimen.densityImageCurlview);
				}
			}
		}

		/** {@inheritDoc} **/
		@Override
		public void loadComplete() {
			// TODO Auto-generated method stub
			
		}

		/** {@inheritDoc} **/
		@Override
		public int getIndexCount() {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	/**
	 * CurlView size changed observer.
	 */
	private class SizeChangedObserver implements CurlView.SizeChangedObserver {
		@Override
		public void onSizeChanged(final int w, final int h) {
			if (w > h) {
				curlView.setViewMode(CurlView.SHOW_TWO_PAGES);
				curlView.setMargins(.1f, .05f, .1f, .05f);
			} else {
				curlView.setViewMode(CurlView.SHOW_ONE_PAGE);
				curlView.setMargins(.1f, .1f, .1f, .1f);
			}
		}
	}

}
