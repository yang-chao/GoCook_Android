package com.m6.gocook.biz.account;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.m6.gocook.R;
import com.m6.gocook.base.activity.BaseActivity;
import com.m6.gocook.base.fragment.BaseFragment;
import com.m6.gocook.base.fragment.FragmentHelper;
import com.m6.gocook.base.protocol.Protocol;
import com.m6.gocook.base.view.ActionBar;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginFragment extends BaseFragment {

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mPhone;
//	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mPhoneView;
//	private EditText mEmailView;
	private EditText mPasswordView;
	
	public static final String PARAM_JUMP_LOGIN = "param_jump_login";
	private boolean mJumpLogin = false;
	
	
	/**
	 * 跳转到登录页面
	 * 
	 * @param context
	 */
	public static void JumpToLoginFragment(Context context) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(LoginFragment.PARAM_JUMP_LOGIN, true);
		Intent intent = FragmentHelper.getIntent(context, BaseActivity.class,
				LoginFragment.class.getName(), 
				LoginFragment.class.getName()
				,bundle);
		context.startActivity(intent);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getArguments();
		if(bundle != null) {
			mJumpLogin = bundle.getBoolean(PARAM_JUMP_LOGIN, false);
		}
	}
	
	@Override
	public View onCreateFragmentView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_login, container, false);
	}
	
	@Override
	protected View onCreateActionBarView(LayoutInflater inflater,
			ViewGroup container) {
		if(mJumpLogin) {
			return super.onCreateActionBarView(inflater, container);
		}
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		FragmentActivity activity = getActivity();
		// Set up the login form.\
		mPhoneView = (EditText) activity.findViewById(R.id.phone);
		mPhoneView.setText(mPhone);
//		mEmailView = (EditText) activity.findViewById(R.id.email);
//		mEmailView.setText(mEmail);

		mPasswordView = (EditText) activity.findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		activity.findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		if (mJumpLogin) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle(R.string.biz_account_tab_login);
		}
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mPhoneView.setError(null);
//		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mPhone = mPhoneView.getText().toString();
//		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_password_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
//		if (TextUtils.isEmpty(mEmail)) {
//			mEmailView.setError(getString(R.string.error_email_required));
//			focusView = mEmailView;
//			cancel = true;
//		} else if (!mEmail.contains("@")) {
//			mEmailView.setError(getString(R.string.error_invalid_email));
//			focusView = mEmailView;
//			cancel = true;
//		}
		
		if (TextUtils.isEmpty(mPhone)) {
			mPhoneView.setError(getString(R.string.error_phone_required));
			focusView = mPhoneView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			setProgressMessage(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(getActivity());
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Map<String, Object>> {
		
		private Context mContext;
		
		public UserLoginTask(Context context) {
			if(context != null) {
				mContext = context.getApplicationContext();
			} else {
				mContext = getActivity();
			}
		}
		
		@Override
		protected Map<String, Object> doInBackground(Void... params) {
			
			String result = AccountModel.login(mContext, mPhone, mPassword);
			if (!TextUtils.isEmpty(result)) {
				try {
					JSONObject json = new JSONObject(result);
					int responseCode = json.optInt(AccountModel.RETURN_RESULT);
					if (responseCode == Protocol.VALUE_RESULT_OK) {
						String icon = json.optString(AccountModel.RETURN_ICON);
						String userName = json.optString(AccountModel.RETURN_USERNAME);
						String userId = json.optString(AccountModel.RETURN_USERID);
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put(AccountModel.RETURN_ICON, icon);
						map.put(AccountModel.RETURN_USERNAME, userName);
						
//						AccountModel.saveAccount(mContext, mEmail);
						AccountModel.savePhone(mContext, mPhone);
						AccountModel.saveUsername(mContext, userName);
						AccountModel.savePassword(mContext, mPassword);
						AccountModel.saveAvatarPath(mContext, icon);
						AccountModel.saveUserId(mContext, userId);
						return map;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Map<String, Object> result) {
			mAuthTask = null;
			showProgress(false);

			if (result != null && !result.isEmpty()) {
				Toast.makeText(mContext, R.string.biz_account_login_success, Toast.LENGTH_LONG).show();
				getActivity().finish();
//				if(mJumpLogin) {
//					getActivity().getSupportFragmentManager().popBackStackImmediate();
//				} else {
//				}
			} else {
				Toast.makeText(mContext, R.string.biz_account_login_failure, Toast.LENGTH_SHORT).show();
//				mPasswordView.setError(getString(R.string.error_incorrect_password));
//				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
}
