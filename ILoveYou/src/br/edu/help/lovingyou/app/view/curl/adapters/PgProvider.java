package br.edu.help.lovingyou.app.view.curl.adapters;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.util.SparseArray;
import br.edu.help.lovingyou.R;
import br.edu.help.lovingyou.app.view.component.CurlPage;
import br.edu.help.lovingyou.app.view.component.CurlUtil;
import br.edu.help.lovingyou.app.view.component.CurlView;

/**
 * @author hildon.lima
 *
 */
public class PgProvider implements CurlView.PageProvider {

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
	 * The percent size of the button with the image.
	 */
	private static final int BUTTONSIZEPERCENT = 117;

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

	/**
	 * Retains an instance of {@link RectF}
	 */
	private Rect rect;
	
	/** List of file names */
	private List<String> fileNameList;
	
	private Context context;
	
	private CurlView mCurlView;
	
	/** Retains the value 2. */
	private int marginup = 2;

	/** Retains the value 2. */
	private int margindown = 2;

	/** Retains the value 2. */
	private int marginleft = 2;

	/** Retains the value 2. */
	private int marginrigth = 2;

	/**
	 * Constructor of the class
	 * 
	 * @param magazine
	 * @param iconBitmap
	 * @param paths list of paths of drawables
	 */
	public PgProvider(Bitmap iconBitmap, Context context, CurlView curlView, List<String> paths) {
		this.iconBitmapOriginal = iconBitmap;
		this.context = context;
		this.mCurlView = curlView;
		this.fileNameList = paths;
	}

	/**
	 * Initialize the paint object.
	 */
	public void initPaint() {
		paint = new Paint();
		paint.setColor(COLOR);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);
	}


	/**
	 * Returns the size of the names list
	 * 
	 * @return
	 */
	public int getIndexCount() {
		return fileNameList.size();
	}

	/**
	 * Returns the pages count
	 * 
	 * @return
	 */
	@Override
	public int getPageCount() {
		final int pageCount = fileNameList.size();

		Integer res = null;
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			res = pageCount;
		} else if (pageCount % 2 == 0) {
			isNumberPagesEven = true;
			res = (pageCount / 2) + 1;
		} else {
			isNumberPagesOdd = true;
			res = (pageCount / 2) + 1;
		}
		return res;
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
			margin = (int) context.getResources().getDimension(
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

		int intHeight = mCurlView.getPageHeight();
		if (isWidth) {
			rect.left += ((rect.width() - imageWidth) / 2) - border
					+ (marginSet / 2);
			rect.right = rect.left + imageWidth + border + border
					- marginSet;
			rect.top = (int) context.getResources().getDimension(
					R.dimen.border_margin);
			rect.bottom = intHeight - rect.top;
		} else {
			rect.left += ((rect.width() - imageWidth) / 2) - border;
			rect.right = rect.left + imageWidth + border + border;
			rect.top = (int) context.getResources().getDimension(
					R.dimen.border_margin)
					+ marginSet;
			rect.bottom = intHeight - rect.top;
		}

		rect.left += border;
		rect.right -= border;
		rect.top += border;
		rect.bottom -= border;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.org.sidi.coquetel.curl.CurlView.PageProvider#countTextures()
	 */
	@Override
	public int countTextures() {
		return fileNameList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.org.sidi.coquetel.curl.CurlView.PageProvider#isNumberPagesOdd()
	 */
	@Override
	public boolean isNumberPagesOdd() {
		return isNumberPagesOdd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.org.sidi.coquetel.curl.CurlView.PageProvider#registerTextureID
	 * (int, int)
	 */
	@Override
	public void registerTextureID(int page, int textureId) {
		pageMap.put(page, textureId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.org.sidi.coquetel.curl.CurlView.PageProvider#createPage(int,
	 * int, int)
	 */
	@Override
	public Bitmap createPage(int page, int width, int height) {
		rectMap.put(page, new RectF(0, 0, 1, 1));

		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			return CurlUtil.getTexture(
					loadBitmap(width, height, page, false),
					rectMap.get(page),
					context.getApplicationContext(),
					R.dimen.border_margin);
		} else {
			if (page >= 1 && page <= getPageCount() * 2 - 2) {
				return CurlUtil.getTexture(
						loadBitmap(width, height, page, page % 2 == 1),
						rectMap.get(page), context.getApplicationContext(),
						R.dimen.densityImageCurlview);
			} else {
				return CurlUtil.getTexture(
						loadBitmap(width, height, page, false), rectMap
								.get(page), context.getApplicationContext(),
						R.dimen.densityImageCurlview);
			}
		}
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
		final String filename = fileNameList.get(index >= fileNameList
				.size() ? fileNameList.size() - 1 : index);
		Drawable draw;

		Bitmap bm = CurlUtil.getPageSampledBitmap(filename,
				bitmap.getWidth(), bitmap.getHeight());

		if (bm == null) {
//			finish();
			return null;
		}

//		adjustIconSizeToMagazineWidth(bm);

		draw = new BitmapDrawable(context.getResources(), bm);
//		draw = addIconToImage(iconBitmap, draw);
		draw = addNumberPage(index, ((BitmapDrawable) draw).getBitmap());

		setRects(width, height, draw);

		if (isBackPage
				&& mCurlView.getViewMode() == CurlView.SHOW_TWO_PAGES) {
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
				* (int) context.getResources()
						.getDimension(R.dimen.percentImageSize) / PERCENT;
		int marginCircle = (int) context.getResources()
				.getDimension(R.dimen.circleNumberMargin);
		int marginNumber = (int) (bitmap.getWidth()
				* (int) context.getResources()
						.getDimension(R.dimen.percentNumberMargin) / PERCENT)
				+ marginCircle;
		int sizeNumber = bitmap.getWidth()
				* (int) context.getResources()
						.getDimension(R.dimen.percentNumberSize) / PERCENT;
		int positionText = bitmap.getWidth()
				* (int) context.getResources()
						.getDimension(R.dimen.percentTextPosition)
				/ PERCENT;

		double eightyPercent = (double) context.getResources().getDimension(R.dimen.percentCirclePosition);
		Drawable drawable = context.getResources()
				.getDrawable(R.drawable.ic_launcher);

//		if (gameView.isTablet()) {
//			drawable = context.getResources().getDrawable(
//					R.drawable.page_circle_tab);
//			marginCircle = SIZECIRCLE;
//			marginNumber = (int) (bitmap.getWidth() * 1.5 / PERCENT)
//					+ marginCircle;
//			sizeCircle = (int) (bitmap.getWidth() * 8 / PERCENT);
//			sizeNumber = bitmap.getWidth() * 4 / PERCENT;
//			eightyPercent = 88;
//			positionText = (int) (bitmap.getWidth() * 89.8 / PERCENT);
//		}

		int number = index + 1; //gameView.getMagazineItem(index).getOriginalOrder() + 1;
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

//		Typeface robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
//		paint.setTypeface(robotoRegular);

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
	public void update(final CurlPage page, int index, int side) {
		page.setTexture(side, index, this.pageMap.get(index),
				rectMap.get(index));
	}

//	/**
//	 * Adds the play button on magazine view.
//	 * 
//	 * @param iconBitmap
//	 * @param image
//	 */
//	public Drawable addIconToImage(Bitmap iconBitmap, Drawable image) {
//		final Bitmap iconAndImage;
//		final Canvas canvas;
//		final int imageWidth;
//		final int imageHeight;
//
//		imageWidth = image.getIntrinsicWidth();
//		imageHeight = image.getIntrinsicHeight();
//		iconAndImage = Bitmap.createBitmap(imageWidth, imageHeight,
//				Bitmap.Config.ARGB_8888);
//		canvas = new Canvas(iconAndImage);
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setDither(true);
//		paint.setFilterBitmap(true);
//		canvas.drawBitmap(((BitmapDrawable) image).getBitmap(), 0f, 0f,
//				paint);
//		final Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
//		final float s = (iconBitmap.getWidth() / (float) bitmap.getWidth()) / 3f;
//		final int w = (int) (iconBitmap.getWidth() * s);
//		final int h = (int) (iconBitmap.getHeight() * s);
//		final int x1 = (imageWidth / 2) - (w / 2);
//		final int y1 = (imageHeight / 2) - (h / 2);
//		final int x2 = (imageWidth / 2) - (w / 2) + w;
//		final int y2 = (imageHeight / 2) - (h / 2) + h;
//
//		canvas.drawBitmap(iconBitmap, new Rect(0, 0, iconBitmap.getWidth(),
//				iconBitmap.getHeight()), new Rect(x1, y1, x2, y2), paint);
//
//		return new BitmapDrawable(iconAndImage);
//	}

	/**
	 * Changes the state of a page on curlView.
	 * 
	 * @param page
	 * @param width
	 * @param height
	 * @param index
	 */
	@Override
	public void updatePage(final CurlPage page, final int index) {

		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

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

	/** {@inheritDoc} **/
	@Override
	public void waitNeeds(boolean needs) {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} **/
	@Override
	public void showLastPageOverlay() {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} **/
	@Override
	public void loadComplete() {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} **/
	@Override
	public void recycleBitmaps() {
		iconBitmap.recycle();
		
	}
}
