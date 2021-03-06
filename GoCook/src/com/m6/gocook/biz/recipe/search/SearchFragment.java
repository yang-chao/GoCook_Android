package com.m6.gocook.biz.recipe.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.os.Bundle;

import com.m6.gocook.R;
import com.m6.gocook.base.protocol.Protocol;
import com.m6.gocook.base.view.ActionBar;
import com.m6.gocook.biz.recipe.list.RecipeListFragment;

public class SearchFragment extends RecipeListFragment {
	
	public static final String	PARAM_KEYWORDS = "param_keywords";

	private String mKeyWords;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args != null) {
			mKeyWords = args.getString(PARAM_KEYWORDS);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ActionBar action = getActionBar();
		action.setTitle(mKeyWords);
	}
	
	@Override
	protected String getURL() {
		try {
			return String.format(Protocol.URL_RECIPE_SEARCH, URLEncoder.encode(mKeyWords, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String getEmptyMessage() {
		return getString(R.string.biz_search_empty_message, mKeyWords);
	}

}
