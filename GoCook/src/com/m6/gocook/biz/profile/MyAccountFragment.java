package com.m6.gocook.biz.profile;

import com.m6.gocook.R;
import com.m6.gocook.biz.account.AccountModel;
import com.m6.gocook.util.cache.util.ImageCache;
import com.m6.gocook.util.cache.util.ImageFetcher;
import com.m6.gocook.util.cache.util.ImageWorker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAccountFragment extends Fragment {
	
	public static final String TAG = "MyAccountFragment";
	
	private static final String IMAGE_CACHE_DIR = "images";
	private ImageFetcher mImageFetcher;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int imageThumbSize = getResources().getDimensionPixelSize(R.dimen.biz_profile_avatar);

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), imageThumbSize);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_myaccount, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View view = getView();
		
		view.findViewById(R.id.logout).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AccountModel.logout(getActivity());
			}
		});
		
		view.findViewById(R.id.profile).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), ProfileActivity.class));
			}
		});
		
		Bundle args = getArguments();
		if(args != null) {
			String url = args.getString(AccountModel.RETURN_ICON);
			if(!TextUtils.isEmpty(url)) {
				new AvatarTask().execute(url);
			}
			String userName = args.getString(AccountModel.RETURN_USERNAME);
			((TextView) getActivity().findViewById(R.id.actionbar_title)).setText(userName);
		}
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
	
	private class AvatarTask extends AsyncTask<String, Void, Void> {

		private ImageView mAvatar;
		@Override
		protected Void doInBackground(String... params) {
			if (params != null && params.length > 0) {
				mAvatar = (ImageView) getActivity().findViewById(R.id.avatar);
				String url = params[0];
				mImageFetcher.loadImage(url, mAvatar);
			}
			return null;
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			if(mAvatar != null) {
				ImageWorker.cancelWork(mAvatar);
				mAvatar.setImageDrawable(null);
			}
		}
		
	}
	
}
