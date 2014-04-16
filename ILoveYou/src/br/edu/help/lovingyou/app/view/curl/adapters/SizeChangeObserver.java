package br.edu.help.lovingyou.app.view.curl.adapters;

import br.edu.help.lovingyou.app.view.component.CurlView;

/**
 * An asynchronous update interface for receiving notifications
 * about SizeChange information as the SizeChange is constructed.
 *
 * @author hildon.lima
 */
public class SizeChangeObserver implements CurlView.SizeChangedObserver {
	
	/** The m curl view. */
	private CurlView mCurlView;
	
	/**
	 * This method is called when information about an SizeChange
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @param mCurlView the mCurlView to set
	 */
	public void setCurlView(CurlView mCurlView) {
		this.mCurlView = mCurlView;
	}
	
	/** {@inheritDoc} **/
	public void onSizeChanged(int w, int h) {
		if (w > h) {
			mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
			mCurlView.setMargins(.1f, .05f, .1f, .05f);
		} else {
			mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
			mCurlView.setMargins(.1f, .1f, .1f, .1f);
		}
	}
}