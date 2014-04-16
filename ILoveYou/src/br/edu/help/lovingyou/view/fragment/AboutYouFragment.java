package br.edu.help.lovingyou.view.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.edu.help.lovingyou.R;
import br.edu.help.lovingyou.view.HomeFragmentView;

/**
 * @author hildon.lima
 *
 */
public class AboutYouFragment  extends Fragment implements HomeFragmentView  {

	private TextView aboutYou;
	
	/** {@inheritDoc} **/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View mainView = inflater.inflate(R.layout.about_you_fragment, null);
		aboutYou = (TextView) mainView.findViewById(R.id.txv_about_you);

		aboutYou.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/adinekir.ttf"));
		
		return mainView;
	}

	/**
	 * @param arguments
	 * @return the fragment instance
	 */
	public static AboutYouFragment newInstance(Bundle arguments) {
		final AboutYouFragment fragment = new AboutYouFragment();
		fragment.setArguments(arguments);
		return fragment;
	}

}
