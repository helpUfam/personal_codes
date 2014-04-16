package br.edu.help.lovingyou.app.view.component;

/**
 * Observer interface for handling CurlView size changes.
 */
public interface SizeChangedObserver {

	/**
	 * Called once CurlView size changes.
	 */
	public void onSizeChanged(int width, int height);
}