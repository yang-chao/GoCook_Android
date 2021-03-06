package com.m6.gocook.biz.account;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.TextUtils;

import com.m6.gocook.base.constant.PrefKeys;
import com.m6.gocook.base.protocol.Protocol;
import com.m6.gocook.util.File.StringUtils;
import com.m6.gocook.util.net.NetUtils;
import com.m6.gocook.util.preference.PrefHelper;
import com.m6.gocook.util.util.Base64;
import com.m6.gocook.util.util.SecurityUtils;

public class AccountModel {
	
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_PASSWORD = "password";
	
	public static final String RETURN_RESULT = "result";	
	public static final String RETURN_ERRORCODE = "errorcode";	
	public static final String RETURN_USERNAME = "username";	
	public static final String RETURN_ICON = "icon";
	public static final String RETURN_AVATAR = "avatar";
	public static final String RETURN_USERID = "user_id";
	
	/** 电话号码重复 */
	public static final int ERRORCODE_PHONE_REPEAT = 201;
	/** 昵称重复 */
	public static final int ERRORCODE_NICKNAME_REPEAT = 202;
	/** 甲方服务器错误 */
	public static final int ERRORCODE_NICKNAME = 203;
	/** 甲方服务器错误(逻辑错误，go_cook校验服务器返回结果错误) */
	public static final int ERRORCODE_LOGIC_ERROR = 204;
	/** 注册失败(甲方服务器未响应或错误) */
	public static final int ERRORCODE_NO_RESPONSE = 205;
	/** 账号已存在 */
	public static final int ERRORCODE_ACCOUNT_EXISTS = 206;
			
	
	private static ArrayList<OnAccountChangedListener> mAccountChangedListeners = new ArrayList<OnAccountChangedListener>();
	
	public static void registerOnAccountChangedListener(OnAccountChangedListener listener) {
		if(listener == null || mAccountChangedListeners.contains(listener)) {
			return;
		}
		mAccountChangedListeners.add(listener);
	}
	
	public static void unRegisterOnAccountChangedListener(OnAccountChangedListener listener) {
		if(listener == null || !mAccountChangedListeners.contains(listener)) {
			return;
		}
		mAccountChangedListeners.remove(listener);
	}
	
	public static void onLogin() {
		for(OnAccountChangedListener listener : mAccountChangedListeners) {
			listener.onLogin();
		}
	}
	
	public static void onLogout() {
		for(OnAccountChangedListener listener : mAccountChangedListeners) {
			listener.onLogout();
		}
	}
	
	public static void onRegister(String email, String avatarUrl, String userName) {
		for(OnAccountChangedListener listener : mAccountChangedListeners) {
			listener.onRegister(email, avatarUrl, userName);
		}
	}
	
//	public static String login(Context context, String phone, String password) {
//		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("login", phone));
//		params.add(new BasicNameValuePair("password", SecurityUtils.encryptMode(password)));
//		return NetUtils.httpPost(context, Protocol.URL_LOGIN, params, null);
//	}
	
	public static String login(Context context, String data, String rnd) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		try {
			params.add(new BasicNameValuePair("data", URLEncoder.encode(data, "UTF-8")));
			params.add(new BasicNameValuePair("rnd", rnd));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return NetUtils.httpPost(context, Protocol.URL_LOGIN_EX, params, null);
	}
	
	public static void logout(Context context) {
		PrefHelper.putString(context, PrefKeys.ACCOUNT_PHONE, "");
		PrefHelper.putString(context, PrefKeys.ACCOUNT_EMAIL, "");
		PrefHelper.putString(context, PrefKeys.ACCOUNT_AVATAR, "");
		PrefHelper.putString(context, PrefKeys.ACCOUNT_USERNAME, "");
		PrefHelper.putString(context, PrefKeys.ACCOUNT_PASSWORD, "");
		PrefHelper.putString(context, PrefKeys.ACCOUNT_COOKIE, "");
		PrefHelper.putString(context, PrefKeys.ACCOUNT_LOGIN_COOKIE, "");
		PrefHelper.putString(context, PrefKeys.PROFILE_INFO, "");
		onLogout();
	}
	
	public static String register(Context context, String phone, String email, String password, String rePassword, String nickname, File avatart) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tel", phone));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("nickname", nickname));
		params.add(new BasicNameValuePair("password", SecurityUtils.encryptMode(password)));
		params.add(new BasicNameValuePair("repassword", SecurityUtils.encryptMode(rePassword)));
		return NetUtils.httpPost(context, Protocol.URL_REGISTER, params, avatart, "avatar", null);
	}
	
	public static String getCookie(Context context) {
		return PrefHelper.getString(context, PrefKeys.ACCOUNT_COOKIE, "");
	}
	
	public static void saveCookie(Context context, String cookie) {
		PrefHelper.putString(context, PrefKeys.ACCOUNT_COOKIE, cookie);
	}
	
	public static String getLoginCookie(Context context) {
		return PrefHelper.getString(context, PrefKeys.ACCOUNT_LOGIN_COOKIE, "");
	}
	
	public static void saveLoginCookie(Context context, String cookie) {
		PrefHelper.putString(context, PrefKeys.ACCOUNT_LOGIN_COOKIE, cookie);
	}
	
	public static String getPhone(Context context) {
		return PrefHelper.getString(context, PrefKeys.ACCOUNT_PHONE, "");
	}
	
	public static void savePhone(Context context, String phone) {
		if (!TextUtils.isEmpty(phone)) {
			PrefHelper.putString(context, PrefKeys.ACCOUNT_PHONE, StringUtils.trimLineFeed(phone.trim()));
		}
	}
	
	public static String getUsername(Context context) {
		return PrefHelper.getString(context, PrefKeys.ACCOUNT_USERNAME, "");
	}
	
	public static void saveUsername(Context context, String username) {
		if (!TextUtils.isEmpty(username)) {
			PrefHelper.putString(context, PrefKeys.ACCOUNT_USERNAME, StringUtils.trimLineFeed(username.trim()));
		}
	}
	
	public static void saveAvatarPath(Context context, String avatar) {
		PrefHelper.putString(context, PrefKeys.ACCOUNT_AVATAR, avatar);
	}
	
	public static String getAvatarPath(Context context) {
		return PrefHelper.getString(context, PrefKeys.ACCOUNT_AVATAR, "");
	}
	
	public static void saveUserId(Context context, String userId) {
		PrefHelper.putString(context, PrefKeys.ACCOUNT_USERID, userId);
	}
	
	public static String getUserId(Context context) {
		return PrefHelper.getString(context, PrefKeys.ACCOUNT_USERID, "");
	}
	
	public static boolean isLogon(Context context) {
		return !TextUtils.isEmpty(PrefHelper.getString(context, PrefKeys.ACCOUNT_USERNAME, ""));
	}
	
	public static String getAccount(Context context) {
		return PrefHelper.getString(context, PrefKeys.ACCOUNT_EMAIL, "");
	}
	
	public static void saveAccount(Context context, String email) {
		PrefHelper.putString(context, PrefKeys.ACCOUNT_EMAIL, email);
	}
	
	public static void savePassword(Context context, String password) {
		String code = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
		PrefHelper.putString(context, PrefKeys.ACCOUNT_PASSWORD, code);
	}
	
	public static String getPassword(Context context) {
		String pwd = PrefHelper.getString(context, PrefKeys.ACCOUNT_PASSWORD, "");
		if (!TextUtils.isEmpty(pwd)) {
			return new String(Base64.decode(pwd.getBytes(), Base64.DEFAULT));
		}
		return null;
	}
}
