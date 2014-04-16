package br.edu.help.lovingyou;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import br.edu.help.lovingyou.app.view.component.CurlView;
import br.edu.help.lovingyou.app.view.component.music.MusicHandler;
import br.edu.help.lovingyou.view.MainActivityViewImpl;
import br.edu.help.lovingyou.view.fragment.AboutUsFragment;
import br.edu.help.lovingyou.view.fragment.AboutYouFragment;
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
	
	private Animation moveUp, moveup2;

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
		moveUp = AnimationUtils.loadAnimation(this, R.anim.move_up);
		moveup2 = AnimationUtils.loadAnimation(this, R.anim.move_up);
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.heart_animation) {
        	startHeartAnimation();
            return true;
        }
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 
	 */
	private void startHeartAnimation() {
		
		final ImageView myAnimationRight = new ImageView(this);
        FrameLayout.LayoutParams paramsImage = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        paramsImage.gravity = Gravity.RIGHT;
        myAnimationRight.setImageResource(R.drawable.some_hearts);
        addContentView(myAnimationRight, paramsImage);
        
		final ImageView myAnimationLeft = new ImageView(this);
        FrameLayout.LayoutParams paramsImage2 = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        paramsImage2.gravity = Gravity.LEFT;
        myAnimationLeft.setImageResource(R.drawable.some_hearts);
        addContentView(myAnimationLeft, paramsImage2);
        myAnimationLeft.setVisibility(View.GONE);
        moveUp.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
				Handler handler = new Handler();
				Runnable run = new Runnable() {
					
					@Override
					public void run() {
						myAnimationLeft.startAnimation(moveup2);
				        myAnimationLeft.setVisibility(View.VISIBLE);
					}
				};
				handler.postDelayed(run, 1500);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				myAnimationRight.startAnimation(moveUp);
			}
		});
        myAnimationRight.startAnimation(moveUp);
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
					.replace(R.id.container, AboutYouFragment.newInstance(null))
					.commit();
			break;

		case 2:
			fragmentManager
					.beginTransaction()
					.replace(R.id.container, AboutUsFragment.newInstance(null))
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

//	/** {@inheritDoc} **/
//	@Override
//	public boolean onOptionsItemSelected(final MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		final int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

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
