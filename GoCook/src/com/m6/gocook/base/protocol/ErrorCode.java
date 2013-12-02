package com.m6.gocook.base.protocol;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.m6.gocook.R;
import com.m6.gocook.biz.account.AccountModel;
import com.m6.gocook.biz.account.WebLoginFragment;

public class ErrorCode {

	public static final int ILLEGAL = -1;
	
	/** 未授权用户 */
	public static final int UNAUTHORIZED = 102;
	/** 商品不存在或无效错误 */
	public static final int ORDER_NO_GOODS = 301;
	/** 订购失败,客户不存在或无效 */
	public static final int ORDER_NO_CUSTOMER = 302;
	/** 订购失败,订单已经存在且订单状态错误 */
	public static final int ORDER_EXIST_OR_ERROR = 303;
	/** 已经赞过该菜谱 */
	public static final int RECIPE_PRAISED = 407;
	/** 该菜谱本人未赞过 */
	public static final int RECIPE_UNPRAISED = 408;
	
	
	public static int getErrorCode(JSONObject json) {
		return json.optInt(Protocol.KEY_ERROR_CODE, ErrorCode.ILLEGAL);
	}
	
	public static void toast(Context context, int errorCode) {
		String msg = null;
		switch (errorCode) {
		case ILLEGAL:
			return;
		case UNAUTHORIZED:
			msg = context.getString(R.string.error_code_unauthorized);
			break;
		case ORDER_NO_GOODS:
			msg = context.getString(R.string.error_code_no_goods);
			break;
		case ORDER_NO_CUSTOMER:
			msg = context.getString(R.string.error_code_no_customer);
			break;
		case ORDER_EXIST_OR_ERROR:
			msg = context.getString(R.string.error_code_exist_or_error);
			break;
		case RECIPE_PRAISED:
			msg = context.getString(R.string.error_code_recipe_praised);
		case RECIPE_UNPRAISED:
			msg = context.getString(R.string.error_code_recipe_unpraised);
			break;
		}
		
		if (!TextUtils.isEmpty(msg)) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void handleError(Context context, int errorCode) {
		toast(context, errorCode);
		switch (errorCode) {
		case ILLEGAL:
			return;
		case UNAUTHORIZED:
			WebLoginFragment.jumpToLogin(context);
			AccountModel.logout(context);
			break;
		}
	}
}
