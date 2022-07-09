package snd.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentDatasheet extends Fragment
{
	private static final String TAG=FragmentDatasheet.class.getName();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
		View rootView = inflater.inflate(R.layout.datasheet, container, false);
		
		return rootView;
    }
}
