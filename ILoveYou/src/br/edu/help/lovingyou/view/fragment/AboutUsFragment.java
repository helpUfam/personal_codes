package br.edu.help.lovingyou.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.edu.help.lovingyou.R;
import br.edu.help.lovingyou.view.AboutUsFragmentView;

/**
 * @author hildon.lima
 *
 */
public class AboutUsFragment extends Fragment implements AboutUsFragmentView  {
	
	private TextView aboutUsText1;
	
	/** {@inheritDoc} **/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View mainView = inflater.inflate(R.layout.about_us_fragment, null);
		
		return mainView;
	}

	/**
	 * @param arguments
	 * @return the fragment instance
	 */
	public static AboutUsFragment newInstance(Bundle arguments) {
		final AboutUsFragment fragment = new AboutUsFragment();
		fragment.setArguments(arguments);
		return fragment;
	}

}
