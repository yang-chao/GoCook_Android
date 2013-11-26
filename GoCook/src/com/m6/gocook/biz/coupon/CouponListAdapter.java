package com.m6.gocook.biz.coupon;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.m6.gocook.R;
import com.m6.gocook.base.entity.Coupon;

public class CouponListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private Resources mResources;
	private List<Coupon> mData;
	
	public CouponListAdapter(Context context, List<Coupon> data) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mResources = context.getResources();
		mData = data;
	}
	
	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public Coupon getItem(int position) {
		return mData == null ? null : mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_coupon_list, parent, false);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			holder.go = (ImageView) convertView.findViewById(R.id.go);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final Coupon coupon = mData.get(position);
		
		final boolean isInvalid = coupon.getStatus() == Coupon.STATUS_INVALID;
		final boolean isDelay = coupon.isDelay();
		final boolean isCoupon = coupon.getKtype() == Coupon.KTYPE_COUPON;
		final boolean isAd = coupon.getKtype() == Coupon.KTYPE_AD;
		
		if (!isDelay && isCoupon) { // 优惠券，包括过期和未过期的优惠券
			if (isInvalid) { // 过期
				holder.content.setText(mContext.getString(R.string.biz_coupon_list_content_normal, 
						coupon.getcTime(), coupon.getSupplier(), coupon.getVal(), coupon.getName(), 
						coupon.getCoupon(), coupon.getEffDay(), coupon.getExpDay(), 
						coupon.getStores(), coupon.getCouponRemark()));
				
				holder.go.setBackgroundResource(R.drawable.coupon_list_go_selector);
				convertView.setBackgroundColor(mResources.getColor(R.color.biz_coupon_invalid));
			} else { // 未过期
				holder.content.setText(mContext.getString(R.string.biz_coupon_list_content_normal, 
						coupon.getcTime(), coupon.getSupplier(), coupon.getVal(), coupon.getName(), 
						coupon.getCoupon(), coupon.getEffDay(), coupon.getExpDay(), 
						coupon.getStores(), coupon.getCouponRemark()));
				holder.go.setBackgroundResource(R.drawable.coupon_list_go_selector);
				convertView.setBackgroundColor(mResources.getColor(R.color.biz_coupon_normal));
			}
		} else { // 延期记录
			holder.content.setText(mContext.getString(R.string.biz_coupon_list_content_delay,
					coupon.getEffDay(), coupon.getExpDay()));
			holder.go.setBackgroundResource(R.drawable.coupon_list_shake_selector);
			convertView.setBackgroundColor(mResources.getColor(R.color.biz_coupon_delay));
		}
		
		
//		holder.go.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (isDelay) { // 延期优惠券
//					Bundle bundle = new Bundle();
//			    	bundle.putString(ShakeFragment.PARAM_COUPON_ID, coupon.getCouponId());
//					Intent intent = FragmentHelper.getIntent(mContext, BaseActivity.class, 
//							ShakeFragment.class.getName(), ShakeFragment.class.getName(), bundle);
//					((FragmentActivity) mContext).startActivityForResult(intent, MainActivityHelper.REQUEST_CODE_COUPON);
//				} else {
//					FragmentHelper.startActivity(mContext, BaseWebFragment.newInstance(coupon.getUrl(), 
//							mContext.getString(R.string.biz_coupon_details_title)));
//				}
//			}
//		});
		
		return convertView;
	}

	class ViewHolder {
		private TextView content;
		private ImageView go;
	}
}
