package br.edu.help.lovingyou.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

/**
 * The Class FileUtils.
 *
 * @author hildon.lima
 */
public class FileUtils {

	/** The sd card root. */
	private static String SD_CARD_ROOT;

	/**
	 * Find files.
	 *
	 * @param context the context
	 * @return the list
	 */
	public static List<String> findFiles(final Context context, final String initPath) {
		final File mFile = Environment.getExternalStorageDirectory();
//		SD_CARD_ROOT = mFile.toString();
		SD_CARD_ROOT = mFile.toString()+"/"+initPath;
		final List<String> tFileList = new ArrayList<String>();
		final Resources resources = context.getResources();
		// array of valid image file extensions
		final String[] imageTypes = resources.getStringArray(br.edu.help.lovingyou.R.array.images);
		final FilenameFilter[] filter = new FilenameFilter[imageTypes.length];

		int i = 0;
		for (final String type : imageTypes) {
			filter[i] = new FilenameFilter() {
				public boolean accept(final File dir, final String name) {
					return name.endsWith("." + type);
				}
			};
			i++;
		}

		final File[] allMatchingFiles = Utils.listFilesAsArray(
				new File(SD_CARD_ROOT), filter, -1);
		for (final File f : allMatchingFiles) {
			tFileList.add(f.getAbsolutePath());
		}
		return tFileList;
	}
	
	private static Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=70;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (FileNotFoundException e) {}
	    return null;
	}
	
	public static List<Drawable> getAllDrawableFromPath(final Context context, final String initPath) {
		final List<String> imagesPath = findFiles(context, initPath);
		final List<Drawable> imageDrawables = new ArrayList<Drawable>();
		
		for (final String imagePath : imagesPath) {
			Bitmap image = decodeFile(new File(imagePath));
			final Drawable mDrawable = new BitmapDrawable(context.getResources(), imagePath);
			if (mDrawable != null) {
				imageDrawables.add(mDrawable);
			}
		}
		
		return imageDrawables;
	} 
	
}