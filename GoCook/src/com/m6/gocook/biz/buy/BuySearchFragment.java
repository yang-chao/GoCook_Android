package com.m6.gocook.biz.buy;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.m6.gocook.R;
import com.m6.gocook.base.activity.BaseActivity;
import com.m6.gocook.base.entity.response.CKeywordQueryResult;
import com.m6.gocook.base.fragment.BaseListFragment;
import com.m6.gocook.base.fragment.FragmentHelper;
import com.m6.gocook.base.view.ActionBar;
import com.m6.gocook.biz.main.MainActivityHelper;

public class BuySearchFragment extends BaseListFragment {

	public static final String PARAM_KEYWORD = "param_keyword";
	public static final String PARAM_RECORD_ID = "param_record_id";
	
	private BuySearchTask mBuySearchTask;
	private BuySearchAdapter mAdapter;
	
	private String mKeyword;
	private String mRecordId;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		if (args != null) {
			mKeyword = args.getString(PARAM_KEYWORD);
			mRecordId = args.getString(PARAM_RECORD_ID);
		}
		mAdapter = new BuySearchAdapter(getActivity(), null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(getString(R.string.biz_buy_search_fragment_title, mKeyword));
	}
	
	@Override
	protected void executeTask(int pageIndex) {
		if (mBuySearchTask == null) {
			mBuySearchTask = new BuySearchTask(getActivity(), mKeyword, pageIndex);
			mBuySearchTask.execute((Void) null); 
		}
	}
	
	@Override
	protected BaseAdapter getAdapter() {
		return mAdapter;
	}
	
	@Override
	public void onListItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mAdapter != null) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(BuyDetailsFragment.PARAM_RESULT, mAdapter.getItem(arg2));
			bundle.putString(PARAM_RECORD_ID, mRecordId);
			Intent intent = FragmentHelper.getIntent(getActivity(), BaseActivity.class,
					BuyDetailsFragment.class.getName(), BuyDetailsFragment.class.getName(), bundle);
			startActivityForResult(intent, MainActivityHelper.REQUEST_CODE_INPUT);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == MainActivityHelper.RESULT_CODE_INPUT) {
			getActivity().setResult(MainActivityHelper.RESULT_CODE_INPUT, data);
			getActivity().finish();
		}
	}
	
	private class BuySearchTask extends AsyncTask<Void, Void, CKeywordQueryResult> {
		
		private String mKeyword;
		private int mPageIndex;
		
		private Context mContext;
		
		public BuySearchTask(Context context, String keyword, int pageIndex) {
			mContext = context.getApplicationContext();
			mKeyword = keyword;
			mPageIndex = pageIndex;
		}

		@Override
		protected CKeywordQueryResult doInBackground(Void... params) {
			return BuyModel.getBuySearchResult(mContext, mKeyword, mPageIndex, COUNT_PER_PAGE);
		}
		
		@Override
		protected void onPostExecute(CKeywordQueryResult result) {
			mBuySearchTask = null;
			if (isAdded()) {
				showProgress(false);
				if (result != null && result.getRows() != null && !result.getRows().isEmpty() && mAdapter != null) {
					mAdapter.setData(result);
					mAdapter.notifyDataSetChanged();
					
				} else {
					if (mAdapter != null && mAdapter.getCount() == 0) {
						setEmptyMessage(R.string.biz_buy_search_fragment_empty);
						showEmpty(true);
					}
				}
			}
		}
		
		@Override
		protected void onCancelled() {
			mBuySearchTask = null;
			if (isAdded()) {
				showProgress(false);
			}
		}
	}

}
