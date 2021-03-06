package com.m6.gocook.biz.buy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.m6.gocook.R;
import com.m6.gocook.base.activity.BaseActivity;
import com.m6.gocook.base.db.table.RecipeMaterialPurchaseList;
import com.m6.gocook.base.entity.request.CShopcartInfo;
import com.m6.gocook.base.entity.request.CShopcartInfo.CShopcartWareInfo;
import com.m6.gocook.base.entity.response.CShopCartResult;
import com.m6.gocook.base.entity.response.CWareItem;
import com.m6.gocook.base.fragment.BaseFragment;
import com.m6.gocook.base.fragment.BaseWebFragment;
import com.m6.gocook.base.fragment.FragmentHelper;
import com.m6.gocook.base.fragment.OnKeyDown;
import com.m6.gocook.base.protocol.ErrorCode;
import com.m6.gocook.base.protocol.Protocol;
import com.m6.gocook.base.view.ActionBar;
import com.m6.gocook.biz.account.AccountModel;
import com.m6.gocook.biz.account.WebLoginFragment;
import com.m6.gocook.biz.main.MainActivityHelper;
import com.m6.gocook.biz.order.OrderListFragment;
import com.m6.gocook.biz.order.OrderModel;
import com.m6.gocook.biz.order.OrderWebFragment;
import com.m6.gocook.util.model.ModelUtils;

public class BuyListFragment extends BaseFragment implements OnKeyDown {

	public static final String PARAM_RECIPE_ID = "param_recipe_id";
	
	private BuyListAdapter mAdapter;
	private String mRecipeId;
	private List<Map<String, Object>> mData;
	
	private int mOrderedCount = 0;
	
	private OrderTask mTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).registerOnkeyDownListener(this);
		
		Bundle args = getArguments();
		if (args != null) {
			mRecipeId = args.getString(PARAM_RECIPE_ID);
		}
		
		mData = BuyModel.getBuyList(getActivity(), mRecipeId);
	}
	
	@Override
	public View onCreateFragmentView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_buylist, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.biz_buy_title);
		actionBar.setRightButton(R.string.biz_buy_list_actionbar_right, R.drawable.actionbar_btn_selector);
		
		mAdapter = new BuyListAdapter(this, mData);
		((ListView) getView().findViewById(R.id.list)).setAdapter(mAdapter);
		
		view.findViewById(R.id.add_material).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LayoutInflater factory = LayoutInflater.from(getActivity());
	            final View textEntryView = factory.inflate(R.layout.fragment_buy_dialog_material, null);
	            new AlertDialog.Builder(getActivity())
	                .setTitle(R.string.biz_buy_dialog_material_title)
	                .setView(textEntryView)
	                .setPositiveButton(R.string.biz_buy_dialog_material_positive, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	if (mData != null && !mData.isEmpty() && mAdapter != null) {
	                    		String materialName = ((EditText) textEntryView.findViewById(R.id.material)).getText().toString();
	                    		if (!TextUtils.isEmpty(materialName) && !TextUtils.isEmpty(materialName.trim())) {
	                    			mData.add(0, BuyModel.getManualMaterial(materialName));
	                    			mAdapter.notifyDataSetChanged();
	                    		} else {
	                    			Toast.makeText(getActivity(), R.string.biz_buy_dialog_material_empty, Toast.LENGTH_SHORT).show();
	                    		}
	                    	}
	                    }
	                })
	                .setNegativeButton(R.string.cancel, null)
	                .create()
	                .show();
			}
		});
		
		view.findViewById(R.id.orders).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!AccountModel.isLogon(getActivity())) {
					FragmentHelper.startActivity(getActivity(), new WebLoginFragment());
				} else {
					
					FragmentHelper.startActivity(getActivity(), 
							BaseWebFragment.newInstance(getActivity(), OrderWebFragment.class.getName(), 
									Protocol.URL_BUY_ORDERS, getString(R.string.biz_buy_order_list_title, AccountModel.getUsername(getActivity()))));
				}
			}
			
		});
	}
	
	private void saveOrder(String recordId, CWareItem wareItem) {
		if (mData == null || mData.isEmpty()) {
			return;
		}
		
		final List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		data.addAll(mData);
		for (Map<String, Object> map : data) {
			String recId = ModelUtils.getStringValue(map, RecipeMaterialPurchaseList._ID);
			if (!TextUtils.isEmpty(recordId) && recordId.equals(recId)) {
				map.put(BuyModel.NAME, wareItem.getName());
				map.put(BuyModel.UNIT, wareItem.getUnit());
				map.put(BuyModel.NORM, wareItem.getNorm());
				map.put(BuyModel.QUANTITY, wareItem.getQuantity());
				map.put(BuyModel.PRICE, wareItem.getPrice());
				map.put(BuyModel.METHOD, wareItem.getDealMethod().get(0));
				map.put(BuyModel.REMARK, wareItem.getRemark());
				map.put(BuyModel.WAREID, wareItem.getId());
				mOrderedCount++;
			}
		}
		
		if (mAdapter != null) {
			mData.clear();
			mData.addAll(data);
			mAdapter.notifyDataSetChanged();
		}
	}

    @Override
	public void onDestroy() {
		super.onDestroy();
		((BaseActivity) getActivity()).unRegisterOnkeyDownListener(this);
	}
	
	@Override
	public void onActionBarRightButtonClick(View v) {
		if (!AccountModel.isLogon(getActivity())) {
			FragmentHelper.startActivity(getActivity(), new WebLoginFragment());
		} else {
			if (mOrderedCount > 0) {
				if (mTask == null) {
					showProgress(true);
					setProgressMessage(R.string.biz_buy_list_progress_message);
					
					mTask = new OrderTask(getActivity(), mData);
					mTask.execute((Void) null); 
				}
			} else {
				if (isAdded()) {
					Toast.makeText(getActivity(), R.string.biz_buy_list_no_order, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == MainActivityHelper.RESULT_CODE_INPUT) {
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					String recordId = data.getStringExtra(BuySearchFragment.PARAM_RECORD_ID);
					CWareItem wareItem = (CWareItem) bundle.getSerializable(BuyDetailsFragment.PARAM_RESULT_DATA);
					saveOrder(recordId, wareItem);
				}
			}
		}
	}

	public void removeBuyInfo(Map<String, Object> pureMap, int position) {
		if (mData != null && !mData.isEmpty() && pureMap != null && position >= 0 && position < mData.size()) {
			mData.remove(position);
			mData.add(position, pureMap);
			if (mOrderedCount > 0) {
				mOrderedCount--;
			}
			
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	
	public class OrderTask extends AsyncTask<Void, Void, CShopCartResult> {

		private Context mContext;
		private List<Map<String, Object>> mItems;
		
		public OrderTask(Context context, List<Map<String, Object>> items) {
			mContext = context.getApplicationContext();
			mItems = items;
		}
		
		@Override
		protected CShopCartResult doInBackground(Void... params) {
			if (mItems != null && !mItems.isEmpty()) {
				List<CShopcartWareInfo> cart = new ArrayList<CShopcartWareInfo>();
				for (Map<String, Object> item : mItems) {
					int wareId = ModelUtils.getIntValue(item, BuyModel.WAREID, 0);
					double quantity = ModelUtils.getDoubleValue(item, BuyModel.QUANTITY, 0.00d);
					String remark = ModelUtils.getStringValue(item, BuyModel.REMARK);
					
					if (quantity != 0 && wareId != 0) {
						CShopcartWareInfo shopcartWareInfo = new CShopcartWareInfo(wareId, quantity, remark);
						cart.add(shopcartWareInfo);
					}
				}
				
				CShopCartResult resutl = OrderModel.orderRequest(mContext, new CShopcartInfo(cart));
				return resutl;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(CShopCartResult result) {
			mTask = null;
			
			if (isAdded()) {
				showProgress(false);
				if (result != null && result.getResult() == Protocol.VALUE_RESULT_OK
						&& !TextUtils.isEmpty(result.getOrderId())) {
					// 下单成功，跳转到订单web页面
//					Toast.makeText(getActivity(), R.string.biz_buy_list_order_success, Toast.LENGTH_SHORT).show();
					FragmentHelper.startActivity(getActivity(), 
							BaseWebFragment.newInstance(getActivity(), OrderWebFragment.class.getName(), 
									String.format(Protocol.URL_BUY_SINGLE_ORDER, result.getOrderId()), 
									getString(R.string.biz_buy_single_order_title, AccountModel.getUsername(getActivity()))));
					getActivity().finish();
				} else {
					if (result != null && result.getResult() == Protocol.VALUE_RESULT_ERROR) {
						ErrorCode.toast(mContext, result.getErrorCode());
					} else {
						Toast.makeText(getActivity(), R.string.biz_buy_list_order_failure, Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		
		@Override
		protected void onCancelled() {
			mTask = null;
			
			if (isAdded()) {
				showProgress(false);
			}
		}
	}
	
	private void exit() {
		new AlertDialog.Builder(getActivity())
        .setMessage(R.string.biz_buy_exit_message)
        .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	getActivity().finish();
            }
        })
        .setNegativeButton(R.string.cancel, null)
        .create()
        .show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mOrderedCount > 0) {
				exit();
				return true;
			}
		}
		return false;
	}

}
