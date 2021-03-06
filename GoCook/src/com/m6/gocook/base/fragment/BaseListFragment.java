package com.m6.gocook.base.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.m6.gocook.R;

public abstract class BaseListFragment extends BaseFragment implements OnScrollListener {

	/** Page Number */
	private int mPage = 1;
	
	/** 每页加载的数据条数 */  
    protected static final int COUNT_PER_PAGE = 10;
    
    /** 最后可见条目的索引  */
    private int mLastVisibleIndex;
    
    private ListView mListView;
    private View mFooterView;
    
    private BaseAdapter mAdapter;
    
    @Override
	public View onCreateFragmentView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
    	
    	LinearLayout root = (LinearLayout) inflater.inflate(R.layout.base_fragment_list, container, false);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
    			LinearLayout.LayoutParams.WRAP_CONTENT);
    	
    	View headerView = onCreateHeaderView(inflater, container);
		if (headerView != null) {
			root.addView(headerView, params);
		}
		
		ListView listView = (ListView) onCreateListView(inflater, root);
		root.addView(listView, params);
		
		return root;
	}
    
    public View onCreateHeaderView(LayoutInflater inflater, ViewGroup container) {
    	return null;
    }
    
    public View onCreateListView(LayoutInflater inflater, ViewGroup container) {
    	return inflater.inflate(R.layout.base_listview, container, false);
    }
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		showProgress(true);
		
		executeTask(mPage);
		
		View view = getView();
		mListView = (ListView) view.findViewById(R.id.list);
		mListView.setOnScrollListener(this);
		
		mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.base_more_footer, null);
		TextView loadingMoreFooter = (TextView) mFooterView.findViewById(R.id.text);
		loadingMoreFooter.setText(getString(R.string.base_loading_more));
		
		mAdapter = getAdapter();
		mListView.addFooterView(mFooterView);
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				onListItemClick(arg0, arg1, arg2, arg3);
			}
		});
    }
    
    @Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mLastVisibleIndex = firstVisibleItem + visibleItemCount - 1;
	}
    
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 滑到底部后自动加载，判断listview已经停止滚动并且最后可视的条目等于adapter的条目  
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && mLastVisibleIndex == mAdapter.getCount() 
        		&& doPaginate() && haveNext()) {  
        	mPage++;
        	mFooterView.setVisibility(View.VISIBLE);
        	executeTask(mPage);
        } else {
        	mFooterView.setVisibility(View.GONE);
        }
	}
	
	public void hideFooter() {
		if (mFooterView != null) {
			mFooterView.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 列表项点击事件
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void onListItemClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		mListView = null;
		mFooterView = null;
	}
	
	protected ListView getListView() {
		return mListView;
	}
	
	protected String getURLWithPageNum() {
		return getURL() + mPage;
	}
	
	/**
	 * 是否分页 
	 * 
	 * @return
	 */
	protected boolean doPaginate() {
		return true;
	}
	
	/**
	 * 是否还有下一页
	 * 
	 * @return
	 */
	protected boolean haveNext() {
		if (mAdapter != null && mAdapter.getCount() >= COUNT_PER_PAGE
				&& (mAdapter.getCount() % COUNT_PER_PAGE) == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 刷新
	 */
	protected void refresh() {
		showProgress(true);
		mPage = 1;
		executeTask(mPage);
	}
	
	/**
	 * 子类实现此方法返回业务的URL
	 * 
	 * @return
	 */
	protected String getURL() {
		return null;
	}
	
	/**
	 * 子类实现此方法执行业务所需的任务
	 * 
	 * @param pageIndex 当前页码
	 */
	abstract protected void executeTask(int pageIndex);
	
	
	/**
	 * 子类重写此方法返回自己需要的Adapter
	 * 
	 * @return
	 */
	abstract protected BaseAdapter getAdapter();

}
