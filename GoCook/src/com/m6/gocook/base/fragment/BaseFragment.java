package com.m6.gocook.base.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.m6.gocook.R;
import com.m6.gocook.base.constant.Constants;
import com.m6.gocook.base.protocol.ErrorCode;
import com.m6.gocook.base.view.ActionBar;
import com.m6.gocook.base.view.ActionBar.OnActionBarClick;
import com.m6.gocook.util.cache.util.ImageCache.ImageCacheParams;
import com.m6.gocook.util.cache.util.ImageFetcher;

public class BaseFragment extends Fragment implements OnActionBarClick {

	// UI references.
	private ActionBar mAction;
	private View mProgressView;
	private TextView mProgressMessageView;
	private View mEmptyView;
	private ProgressDialog mProgressDialog;
	
	// net result
	private int mErrorCode = ErrorCode.ILLEGAL;
	
	protected ImageFetcher mImageFetcher;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int imageThumbSize = getResources().getDimensionPixelSize(R.dimen.biz_popular_item_image_size);
	    ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), Constants.IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), imageThumbSize);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);
	}
	
	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.base_fragment, container, false);
		
		RelativeLayout.LayoutParams lp;
		
		View actionBarView = onCreateActionBarView(inflater, container);
		if(actionBarView != null) {
			mAction = new ActionBar(actionBarView);
			mAction.setActionBarClickListener(this);
			lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			root.addView(actionBarView, lp);
		}
		
		View content = onCreateFragmentView(inflater, container, savedInstanceState);
		if(content != null) {
			lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			if(actionBarView != null) {
				lp.addRule(RelativeLayout.BELOW, actionBarView.getId());
			} else {
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			}
			root.addView(content, lp);
		}
		
		View progress = onCreateProgressView(inflater, container);
		if(progress != null) {
			lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			if(actionBarView != null) {
				lp.addRule(RelativeLayout.BELOW, actionBarView.getId());
			} else {
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			}
			root.addView(progress, lp);
			
			mProgressView = progress.findViewById(R.id.progress_status);
			mProgressMessageView = (TextView) progress.findViewById(R.id.status_message);
		}
		
		mEmptyView = onCreateEmptyView(inflater, container);
		if(mEmptyView != null) {
			lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			mEmptyView.setVisibility(View.GONE);
			if(actionBarView != null) {
				lp.addRule(RelativeLayout.BELOW, actionBarView.getId());
			} else {
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			}
			root.addView(mEmptyView, lp);
		}
		
		return root;
	}
	
	/**
	 * Called to have the fragment instantiate its user interface view. This is optional.
	 * 
	 * @return Return the View for the fragment's UI, or null.
	 */
	public View onCreateFragmentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return null;
	}
	
	/**
	 * Create the ActionBar view, return null if use a fragment without ActionBar.
	 * 
	 * @param inflater
	 * @param container
	 * @return
	 */
	protected View onCreateActionBarView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.base_actionbar, container, false);
	}
	
	/**
	 * Called to have the fragment instantiate its progress interface view. This is optional.
	 * 
	 * @param inflater
	 * @param container
	 * @return
	 */
	public View onCreateProgressView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.fragment_progress_status, container, false);
	}
	
	/**
	 * Called to have the fragment instantiate its empty interface view. This is optional.
	 * 
	 * @param inflater
	 * @param container
	 * @return
	 */
	public View onCreateEmptyView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.base_empty_layout, container, false);
	}
	
	public ActionBar getActionBar() {
		return mAction;
	}
	
	/**
	 * Shows or hides the progress UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if(mProgressView == null || !isAdded()) {
			return;
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mProgressView.setVisibility(View.VISIBLE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	/**
	 * 设置显示空view的提示信息
	 * 
	 * @param message
	 */
	public void setEmptyMessage(String message) {
		if (mEmptyView != null) {
			((TextView) mEmptyView.findViewById(R.id.empty_tip)).setText(message);
		}
	}
	
	/**
	 * 设置显示空view的提示信息
	 * 
	 * @param message
	 */
	public void setEmptyMessage(int resId) {
		if (mEmptyView != null) {
			((TextView) mEmptyView.findViewById(R.id.empty_tip)).setText(resId);
		}
	}
	
	/**
	 * Shows or hides the empty UI.
	 */
	public void showEmpty(boolean show) {
		if (isAdded() && mEmptyView != null) {
			if (show) {
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mEmptyView.setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * 设置loading时的提示文字
	 * 
	 * @param message
	 */
	public void setProgressMessage(String message) {
		if(mProgressMessageView != null) {
			mProgressMessageView.setText(message);
		}
	}
	
	/**
	 * 设置loading时的提示文字
	 * 
	 * @param resId
	 */
	public void setProgressMessage(int resId) {
		if(mProgressMessageView != null) {
			mProgressMessageView.setText(resId);
		}
	}
	
	/**
	 * 是否正在加载
	 * 
	 * @return
	 */
	public boolean isOnProgressing() {
		return mProgressView.getVisibility() == View.VISIBLE ? true : false;
	}
	
	/**
	 * Show progress dialog
	 * 
	 * @param message
	 */
	public void showProgressDialog(String message) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(true);
		}
		mProgressDialog.setMessage(message);
		mProgressDialog.show();
	}
	
	/**
	 * Show progress dialog
	 * 
	 * @param resId
	 */
	public void showProgressDialog(int resId) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(true);
		}
		mProgressDialog.setMessage(getString(resId));
		mProgressDialog.show();
	}
	
	/**
	 * Dismiss progress dialog
	 */
	public void dismissProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	
	/**
	 * 是否正在显示空View
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return mEmptyView.getVisibility() == View.VISIBLE ? true : false;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
//		mProgressMessageView = null;
//		mProgressView = null;
	}
	
	
	@Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }
 
    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

	@Override
	public void onActionBarRightButtonClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActionBarLeftButtonClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 标示错误码
	 * 
	 * @param errorCode
	 */
	public void setError(int errorCode) {
		mErrorCode = errorCode;
	}
	
	/**
	 * 返回错误码
	 * 
	 * @return
	 */
	public int getError() {
		return mErrorCode;
	}
}
