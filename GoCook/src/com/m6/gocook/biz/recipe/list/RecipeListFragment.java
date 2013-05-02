package com.m6.gocook.biz.recipe.list;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.m6.gocook.R;
import com.m6.gocook.base.entity.RecipeListItem;
import com.m6.gocook.base.fragment.BaseFragment;
import com.m6.gocook.biz.recipe.RecipeModel;

public abstract class RecipeListFragment extends BaseFragment {
	
	private String mUrl;
	protected int mPage = 1;

	@Override
	public View onCreateFragmentView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_recipe_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mUrl = getURL();
		
		new RecipeListTask(getActivity()).execute((Void) null);
		showProgress(true);
	}
	
	/**
	 * 子类实现此方法返回业务的URL
	 * 
	 * @return
	 */
	abstract protected String getURL();
	
	/**
	 * 子类重写此方法给出需要的Adapter Item Layout，默认使用adapter_recipe_list_item
	 * 
	 * @return
	 */
	protected int getAdapterLayout() {
		return R.layout.adapter_recipe_list_item;
	}
	
	private class RecipeListTask extends AsyncTask<Void, Void, RecipeListItem> {

    	private FragmentActivity mActivity;
    	
    	public RecipeListTask(FragmentActivity activity) {
    		mActivity = activity;
    	}
    	
		@Override
		protected RecipeListItem doInBackground(Void... params) {
			return RecipeModel.getRecipeData(mUrl);
		}
    	
		@Override
		protected void onPostExecute(RecipeListItem result) {
			showProgress(false);
			if (result != null && mActivity != null) {
				RecipeListAdapter adapter = new RecipeListAdapter(mActivity, mImageFetcher, result, getAdapterLayout());
				ListView list = (ListView) mActivity.findViewById(R.id.list);
				list.setAdapter(adapter);
			}
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			showProgress(false);
		}
    }
}