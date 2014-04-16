package br.edu.help.lovingyou;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import br.edu.help.lovingyou.app.view.adapters.SizeChangeObserver;
import br.edu.help.lovingyou.app.view.component.CurlView;
import br.edu.help.lovingyou.app.view.component.music.MusicHandler;
import br.edu.help.lovingyou.view.MainActivityViewImpl;
import br.edu.help.lovingyou.view.fragment.HomeFragment;
import br.edu.help.lovingyou.view.fragment.NavigationDrawerFragment;

/**
 * The Class MainActivity.
 * 
 * @author hildon.lima
 */
public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, MainActivityViewImpl {
	
	private MusicHandler musicManagerHandler;
	
	private final int FADE_TIME = 1000;
	
	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	/** {@inheritDoc} **/
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		musicManagerHandler = new MusicHandler(getApplicationContext());//MusicHandler.newInstance(getApplicationContext());// 
		
		musicManagerHandler.load(R.raw.jota_quest_so_hoje, true);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setMainActivity(this);
		
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}
	
	/** {@inheritDoc} **/
	@Override
	protected void onDestroy() {
		musicManagerHandler.release();
		super.onDestroy();
		
	}
	
	/** {@inheritDoc} **/
	@Override
	protected void onPause() {
		musicManagerHandler.pause(FADE_TIME);
		super.onPause();
	}
	
	/** {@inheritDoc} **/
	@Override
	protected void onResume() {
		musicManagerHandler.play(4000);
		super.onResume();
	}
	
	
	/** {@inheritDoc} **/
	@Override
	public void onNavigationDrawerItemSelected(final int position) {
		// update the main content by replacing fragments
		final FragmentManager fragmentManager = getSupportFragmentManager();
		switch (position) {
		case 0:
			fragmentManager.beginTransaction()
					.replace(R.id.container, HomeFragment.newInstance(null))
					.commit();
			break;
		case 1:
			fragmentManager
					.beginTransaction()
					.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
					.commit();
			break;

		case 2:
			fragmentManager
					.beginTransaction()
					.replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
					.commit();
			break;

		default:
			break;
		}

	}
	
	/**
	 * On section attached.
	 * 
	 * @param number
	 *            the number
	 */
	public void onSectionAttached(final int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	/**
	 * Restore action bar.
	 */
	public void restoreActionBar() {
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	/** {@inheritDoc} **/
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	/** {@inheritDoc} **/
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		final int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/** The curl view. */
		static CurlView curlView;

		/** The size change observer. */
		private SizeChangeObserver sizeChangeObserver;

		/**
		 * Returns a new instance of this fragment for the given section number.
		 * 
		 * @param sectionNumber
		 *            the section number
		 * @return the placeholder fragment
		 */
		public static PlaceholderFragment newInstance(final int sectionNumber) {
			final PlaceholderFragment fragment = new PlaceholderFragment();
			final Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		/**
		 * Instantiates a new placeholder fragment.
		 */
		public PlaceholderFragment() {
		}

		/** {@inheritDoc} **/
		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			// TextView textView = (TextView) rootView
			// .findViewById(R.id.section_label);
			// textView.setText(Integer.toString(getArguments().getInt(
			// ARG_SECTION_NUMBER)));

			return rootView;
		}

		/** {@inheritDoc} **/
		@Override
		public void onAttach(final Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

	/** {@inheritDoc} **/
	@Override
	public boolean playMusic() {
		return musicManagerHandler.play(FADE_TIME);
	}

	/** {@inheritDoc} **/
	@Override
	public boolean pauseMusic() {
		return musicManagerHandler.pause(FADE_TIME);
	}

}
