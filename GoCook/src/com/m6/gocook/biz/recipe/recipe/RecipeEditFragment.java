package com.m6.gocook.biz.recipe.recipe;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.m6.gocook.R;
import com.m6.gocook.base.activity.BaseActivity;
import com.m6.gocook.base.entity.RecipeEntity;
import com.m6.gocook.base.entity.RecipeEntity.Material;
import com.m6.gocook.base.entity.RecipeEntity.Procedure;
import com.m6.gocook.base.fragment.BaseFragment;
import com.m6.gocook.base.fragment.FragmentHelper;
import com.m6.gocook.base.fragment.OnActivityAction;
import com.m6.gocook.base.fragment.OnKeyDown;
import com.m6.gocook.base.protocol.Protocol;
import com.m6.gocook.base.protocol.ProtocolUtils;
import com.m6.gocook.base.view.ActionBar;
import com.m6.gocook.biz.common.PhotoPickDialogFragment;
import com.m6.gocook.biz.common.PhotoPickDialogFragment.OnPhotoPickCallback;
import com.m6.gocook.biz.main.MainActivityHelper;
import com.m6.gocook.biz.recipe.RecipeModel;
import com.m6.gocook.util.File.ImgUtils;
import com.m6.gocook.util.log.Logger;

public class RecipeEditFragment extends BaseFragment implements OnKeyDown, OnClickListener, OnActivityAction, OnPhotoPickCallback {

	private final String TAG = RecipeFragment.class.getCanonicalName();
	
	public static final String ARGUMENT_KEY_ACTION = "argument_key_action";
	public static final String ARGUMENT_KEY_RECIPE_ID = "argument_key_recipe_id";

	private Context mContext;
	private View mRootView;
	private String mRecipeId;
	private RecipeEntity mRecipeEntity;
	private LayoutInflater mInflater;
	private Mode mMode;
	
	private ImageView mCurrentImageView;
	private ProgressDialog mProgressDialog;
	
	public enum Mode {
		RECIPE_NEW, RECIPE_EDIT
	}
	
	public static void startInActivity(Context context, Mode mode, String recipeId) {
		Bundle argument = new Bundle();
		argument.putSerializable(RecipeEditFragment.ARGUMENT_KEY_ACTION,
				mode);
		argument.putString(RecipeEditFragment.ARGUMENT_KEY_RECIPE_ID, recipeId);
        Intent intent = FragmentHelper.getIntent(context, BaseActivity.class, 
        		RecipeEditFragment.class.getName(), 
        		RecipeEditFragment.class.getName(), argument);
        context.startActivity(intent);
	}
	
	public static void startInActivityForResult(Activity context, Mode mode, String recipeId) {
		Bundle argument = new Bundle();
		argument.putSerializable(RecipeEditFragment.ARGUMENT_KEY_ACTION,
				mode);
		argument.putString(RecipeEditFragment.ARGUMENT_KEY_RECIPE_ID, recipeId);
        Intent intent = FragmentHelper.getIntent(context, BaseActivity.class, 
        		RecipeEditFragment.class.getName(), 
        		RecipeEditFragment.class.getName(), argument);
        context.startActivityForResult(intent, MainActivityHelper.REQUEST_CODE_RECIPE_EDIT);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).registerOnkeyDownListener(this);
		// set softinputmode for activity
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	
	@Override
	public void onDestroy() {
		((BaseActivity) getActivity()).unRegisterOnkeyDownListener(this);
		super.onDestroy();
	}
	
	@Override
	public View onCreateFragmentView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mRootView = inflater.inflate(R.layout.fragment_recipe_edit, null, false);
		mRootView.findViewById(R.id.cover_imageview).setOnClickListener(this);
		return mRootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(mContext == null) {
			mContext = getActivity();
			doCreate();
		}
	}
	
	private View findViewById(int id) {
		if(mRootView != null) {
			return mRootView.findViewById(id);
		} else {
			return null;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();

		Fragment f = getChildFragmentManager().findFragmentByTag(PhotoPickDialogFragment.class.getName());
		if (f != null) {
			DialogFragment df = (DialogFragment) f;
			df.dismiss();
			getFragmentManager().beginTransaction().remove(f).commit();
		}
	}
	
	private class MyEditTextOnTouchListener implements OnTouchListener {

		private int mLineNumber = Integer.MAX_VALUE;
		
		public MyEditTextOnTouchListener(int lineNumber) {
			mLineNumber = lineNumber;
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(((EditText)v).getLineCount() > mLineNumber) {
				v.getParent().requestDisallowInterceptTouchEvent(true);
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
			}
			return false;
		}
	}

	private void doCreate() {
		
		ActionBar action = getActionBar();
		
		action.setRightButton(R.string.biz_recipe_edit_post, R.drawable.actionbar_btn_selector);
		
		Bundle arg = getArguments();
		if(arg != null) {
			//set action mode and title
			mMode= (Mode)arg.getSerializable(ARGUMENT_KEY_ACTION);
			if(mMode == Mode.RECIPE_NEW) {
				action.setTitle(R.string.biz_recipe_edit_title_new);
			} else if (mMode == Mode.RECIPE_EDIT) {
				action.setTitle(R.string.biz_recipe_edit_title_edit);
				action.setRightButton(R.string.biz_recipe_edit_title_edit_post, R.drawable.actionbar_btn_selector);
				mRecipeId = arg.getString(RecipeEditFragment.ARGUMENT_KEY_RECIPE_ID);
			}
		}
		
		if(mMode == Mode.RECIPE_EDIT) {
			new AchieveRecipeTask().execute();
		}

		final EditText descEditText = (EditText) findViewById(R.id.recipe_introduction_edittext);
		descEditText.setOnTouchListener(new MyEditTextOnTouchListener(8));
		
		final EditText tipsEditText = (EditText) findViewById(R.id.recipe_tips_edittext);
		tipsEditText.setOnTouchListener(new MyEditTextOnTouchListener(8));

		final LinearLayout materialLayout = (LinearLayout) findViewById(R.id.material_layout);
		for (int i = 0; i < 5; i++) {
			materialLayout.addView(createMaterialView());
		}

		((Button) findViewById(R.id.material_addmore_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				materialLayout.addView(createMaterialView());
			}
		});

		final LinearLayout procedureLayout = (LinearLayout) findViewById(R.id.procedure_layout);
		for (int i = 0; i < 5; i++) {
			procedureLayout.addView(createProcedureView());
		}

		((Button) findViewById(R.id.procedure_addmore_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				procedureLayout.addView(createProcedureView());
			}
		});
	}
	
	private void applyData() {
		
		ImageView cover = (ImageView) findViewById(R.id.cover_imageview);
		mImageFetcher.loadImage(mRecipeEntity.getCoverImgURL(), cover);
		cover.setTag(ProtocolUtils.getImageFileNameFromURL(mRecipeEntity.getCoverImgURL()));
		
		EditText titleEditText = (EditText) findViewById(R.id.recipe_title_edittext);
		titleEditText.setText(mRecipeEntity.getName());
		
		EditText intrEditText = (EditText) findViewById(R.id.recipe_introduction_edittext);
		intrEditText.setText(mRecipeEntity.getDesc());
		
		EditText tipEditText = (EditText) findViewById(R.id.recipe_tips_edittext);
		tipEditText.setText(mRecipeEntity.getTips());
		
		applyMaterial();
		applyProcedure();
		
	}
	
	private View createMaterialView() {
		return mInflater.inflate(R.layout.adapter_recipe_edit_material_item, null);
	}
	
	private View createProcedureView() {

		final View view = mInflater.inflate(R.layout.adapter_recipe_edit_procedure_item, null);
		
		final ImageView imageView = (ImageView) view.findViewById(R.id.image);
		imageView.setOnClickListener(this);
		
		view.findViewById(R.id.button_upload).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new UploadImageTask(imageView).execute();
				mCurrentImageView = imageView;
			}
		});
		
		view.findViewById(R.id.button_remove).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				view.findViewById(R.id.button_layout).setVisibility(View.GONE);
				imageView.setImageResource(R.drawable.landscape_photo);
				mCurrentImageView = imageView;
			}
		});
		
		return view;
	}
	
	private View applyMaterial() {
		final LinearLayout materialLayout = (LinearLayout) findViewById(R.id.material_layout);
		
		ArrayList<Material> materials = mRecipeEntity.getMaterials();
		int materialCount = materials.size();
		int materialFlag = 0;

		for (int i = 0; i < materialLayout.getChildCount(); i++, materialFlag++) {

			if (materialFlag < materialCount) {

				Material material = materials.get(materialFlag);
				View view = materialLayout.getChildAt(i);

				applyDataToMaterialItem(view, material);
			}
		}

		for (; materialFlag < materialCount; materialFlag++) {
			
			Material material = materials.get(materialFlag);
			View view = createMaterialView();
			
			applyDataToMaterialItem(view, material);
			materialLayout.addView(view);
		}
		return null;
	}
	
	private void applyDataToMaterialItem(View view, Material material) {
		
		EditText name = (EditText) view.findViewById(R.id.name);
		name.setText(material.getName());

		EditText remark = (EditText) view.findViewById(R.id.remark);
		remark.setText(material.getRemark());
	}
	
	private View applyProcedure() {
		final LinearLayout procedureLayout = (LinearLayout) findViewById(R.id.procedure_layout);
		
		ArrayList<Procedure> procedures = mRecipeEntity.getProcedures();
		int procedureCount = procedures.size();
		int procedureFlag = 0;

		for (int i = 0; i < procedureLayout.getChildCount(); i++, procedureFlag++) {

			if (procedureFlag < procedureCount) {

				Procedure procedure = procedures.get(procedureFlag);
				View view = procedureLayout.getChildAt(i);

				applyDataToProcedurelItem(view, procedure);
			}
		}

		for (; procedureFlag < procedureCount; procedureFlag++) {
			
			Procedure procedure = procedures.get(procedureFlag);
			View view = createProcedureView();
			
			applyDataToProcedurelItem(view, procedure);
			procedureLayout.addView(view);
		}
		return null;
	}
	
	private void applyDataToProcedurelItem(View view, Procedure procedure) {
		
		EditText desc = (EditText) view.findViewById(R.id.desc);
		desc.setText(procedure.getDesc());

		if(!TextUtils.isEmpty(procedure.getImageURL())) {
			ImageView image = (ImageView) view.findViewById(R.id.image);
			mImageFetcher.loadImage(ProtocolUtils.getStepImageURL(procedure.getImageURL()), image);
			image.setTag(procedure.getImageURL());
			view.findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
			view.findViewById(R.id.button_upload).setVisibility(View.GONE);
		}
	}
	
	private void showUploadingProgressBar(boolean show) {
		showUploadingProgressBar(show, null);
	}
	private void showUploadingProgressBar(boolean show, String message) {
		
		if(mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity());  
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
		
		if(message != null) {
			mProgressDialog.setMessage(message);
		}
			
		if(show) {
			mProgressDialog.show();
		} else {
			mProgressDialog.dismiss();
		}
	}
	
	private class AchieveRecipeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			showProgress(true);
		}

		@Override
		protected Void doInBackground(Void... params) {

			mRecipeEntity = RecipeModel.getRecipe(getActivity(), mRecipeId, true);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if(isAdded()) {
				showProgress(false);
				if(mRecipeEntity != null) {
					applyData();
				} else {
					getActivity().finish();
				}
			}
			super.onPostExecute(result);
		}

	}

	@Override
	public void onClick(View v) {
		mCurrentImageView = (ImageView) v;
		PhotoPickDialogFragment.startForResult(getChildFragmentManager(), this);
	}

	@Override
	public void onPhotoPickResult(final Uri uri, final Bitmap bitmap) {
		if(mCurrentImageView != null) {
			mCurrentImageView.setImageBitmap(bitmap);
			if(bitmap != null) {
				mCurrentImageView.setImageBitmap(bitmap);
			} else if (uri != null) {
				if (Build.VERSION.SDK_INT >= 19) {
					mCurrentImageView.setImageURI(uri);
				} else {
					int size = getResources().getDimensionPixelSize(R.dimen.biz_recipe_edit_step_image_height);
					mCurrentImageView.setImageBitmap(
							ImgUtils.decodeSampledBitmapFromFile(ImgUtils.getPath(getActivity(), uri), size, size));
				}
			} else {
				mCurrentImageView.setImageResource(R.drawable.register_photo);
			}
			
			// After select new picture, set ImageView tag to null
			mCurrentImageView.setTag(null);
			
			if(mCurrentImageView.getId() == R.id.cover_imageview) {
				new UploadImageTask(mCurrentImageView).execute();
			} else {
				LinearLayout parent = (LinearLayout) mCurrentImageView.getParent();
				parent.findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
				parent.findViewById(R.id.button_upload).setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	public void onActionBarRightButtonClick(View v) {
		postRecipe();
	}
	
	private void postRecipe() {
		
		mRecipeEntity = new RecipeEntity();
		
		if(mMode == Mode.RECIPE_EDIT) {
			mRecipeEntity.setId(mRecipeId);
		}
		
		if(findViewById(R.id.cover_imageview).getTag() != null) {
			mRecipeEntity.setCoverImgURL(findViewById(R.id.cover_imageview).getTag().toString());
		} else {
			Toast.makeText(mContext, R.string.biz_recipe_edit_nocover, Toast.LENGTH_SHORT).show();
			return;
		}
		
		EditText title = (EditText) findViewById(R.id.recipe_title_edittext);
		String recipeName = title.getText().toString().trim();
		Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-{\u4e00-\u9fa5}{\uff01-\uff5e}{\u2014}{\u2013}]{2,30}$");
		Matcher matcher = pattern.matcher(recipeName);
		if (!matcher.find()) {
			Toast.makeText(mContext, R.string.biz_recipe_edit_titleerror, Toast.LENGTH_SHORT).show();
			return;
		} else if(!TextUtils.isEmpty(recipeName)) {
			if(title.getText().toString().trim().length() < 2) {
				Toast.makeText(mContext, R.string.biz_recipe_edit_titleshort, Toast.LENGTH_SHORT).show();
				return;
			} else if (title.getText().toString().trim().length() > 30) {
				Toast.makeText(mContext, R.string.biz_recipe_edit_titlelong, Toast.LENGTH_SHORT).show();
				return;
			}
			mRecipeEntity.setName(title.getText().toString().trim());
		} else {
			Toast.makeText(mContext, R.string.biz_recipe_edit_notitle, Toast.LENGTH_SHORT).show();
			title.setSelected(true);
			return;
		}
		
		EditText desc = (EditText) findViewById(R.id.recipe_introduction_edittext);
		if(desc.getText().toString().trim().length() > 0 && desc.getText().toString().trim().length() < 5) {
			Toast.makeText(mContext, R.string.biz_recipe_edit_descshort, Toast.LENGTH_SHORT).show();
			return;
		}
		mRecipeEntity.setDesc(desc.getText().toString().trim());
		
		final LinearLayout materialLayout = (LinearLayout) findViewById(R.id.material_layout);

		for (int i = 0; i < materialLayout.getChildCount(); i++) {

			View view = materialLayout.getChildAt(i);
			EditText name = (EditText) view.findViewById(R.id.name);
			if(!TextUtils.isEmpty(name.getText().toString().trim())) {
				EditText remark = (EditText) view.findViewById(R.id.remark);
				Material material = new Material(name.getText().toString().trim(),
						remark.getText().toString().trim(), false);
				mRecipeEntity.getMaterials().add(material);
			}
		}
		if(mRecipeEntity.getMaterials().size() == 0) {
			Toast.makeText(mContext, R.string.biz_recipe_edit_nomaterial, Toast.LENGTH_SHORT).show();
			return;
		}
		
		final LinearLayout procedureLayout = (LinearLayout) findViewById(R.id.procedure_layout);
		
		for (int i = 0; i < procedureLayout.getChildCount(); i++) {
			
			View view = procedureLayout.getChildAt(i);
			
			EditText proceduDesc = (EditText) view.findViewById(R.id.desc);
			if(!TextUtils.isEmpty(proceduDesc.getText().toString().trim())) {
				
				String imageURL = "";
				if(view.findViewById(R.id.image).getTag() != null) {
					imageURL = view.findViewById(R.id.image).getTag().toString();
				}
				
				Procedure procedure = new Procedure(proceduDesc.getText().toString().trim(), imageURL);
				mRecipeEntity.getProcedures().add(procedure);
			}
		}
		
		if(mRecipeEntity.getProcedures().size() == 0) {
			Toast.makeText(mContext, R.string.biz_recipe_edit_noprocedure, Toast.LENGTH_SHORT).show();
			return;
		}
		
		EditText tips = (EditText) findViewById(R.id.recipe_tips_edittext);
		if(tips.getText().toString().trim().length() > 0 && tips.getText().toString().trim().length() < 5) {
			Toast.makeText(mContext, R.string.biz_recipe_edit_tipsshort, Toast.LENGTH_SHORT).show();
			return;
		}
		mRecipeEntity.setTips(tips.getText().toString().trim());
		
	
		new PostRecipeTask().execute();
	}
	
	private void exit() {
		new AlertDialog.Builder(getActivity())
        .setMessage(R.string.biz_recipe_exit_message)
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
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return false;
	}
	
	public class PostRecipeTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			showUploadingProgressBar(true, getResources().getString(R.string.biz_recipe_edit_posting));
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			String result = (mMode == Mode.RECIPE_NEW) ?
					RecipeModel.postRecipe(mContext, mRecipeEntity) :
					RecipeModel.editRecipe(mContext, mRecipeEntity);
			
			if (!TextUtils.isEmpty(result)) {
				try {
					JSONObject json = new JSONObject(result);
					int responseCode = json.optInt(Protocol.KEY_RESULT);
					if (responseCode == Protocol.VALUE_RESULT_OK) {
						return true;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			
			super.onPostExecute(result);
			if(isAdded()) {
				showUploadingProgressBar(false);
			}
			
			if(result) {
				Toast.makeText(mContext, R.string.biz_recipe_edit_post_ok, Toast.LENGTH_SHORT).show();
				getActivity().setResult((mMode == Mode.RECIPE_NEW) ?
						MainActivityHelper.RESULT_CODE_RECIPE_EDIT_CREATED :
						MainActivityHelper.RESULT_CODE_RECIPE_EDIT_UPDATED);
				getActivity().finish();
			} else {
				Toast.makeText(mContext, R.string.biz_recipe_edit_post_failed, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	public class UploadImageTask extends AsyncTask<Void, Void, String> {

		private ImageView mImageView;
		public UploadImageTask(ImageView imageView) {
			mImageView = imageView;
		}
		
		@Override
		protected void onPreExecute() {
			showUploadingProgressBar(true, getResources().getString(R.string.biz_recipe_edit_progressbar));
		}
		
		@Override
		protected String doInBackground(Void... params) {
			
			if(mImageView == null) {
				Logger.e(TAG, "mImageView is null");
				return null;
			}
			
			File file = ImgUtils.createBitmapFile("pic" + System.currentTimeMillis(),
					((BitmapDrawable)mImageView.getDrawable()).getBitmap());
			
			if(file != null) {
				if(mImageView.getId() == R.id.cover_imageview) {
					return RecipeModel.uploadRecipeCoverImage(mContext, file);
				} else {
					return RecipeModel.uploadRecipeStepImage(mContext, file);
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(isAdded()){
				showUploadingProgressBar(false);
				
				mCurrentImageView.setTag(null);
				if(mCurrentImageView != null && result != null) {
					mCurrentImageView.setTag(result);
				}
				
				if(mImageView.getId() != R.id.cover_imageview) {
					LinearLayout parent = (LinearLayout) mCurrentImageView.getParent();
					
					if(TextUtils.isEmpty(result)) {
						parent.findViewById(R.id.button_upload).setVisibility(View.VISIBLE);
					} else {
						parent.findViewById(R.id.button_upload).setVisibility(View.GONE);
					}
				} else {
					if(result == null) {
						mCurrentImageView.setImageResource(R.drawable.landscape_photo);
					}
				}
				
				if(TextUtils.isEmpty(result)) {
					Toast.makeText(mContext, R.string.biz_recipe_edit_upload_failed, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, R.string.biz_recipe_edit_upload_ok, Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		@Override
		protected void onCancelled() {
			if(isAdded()){
				showUploadingProgressBar(false);
			}
		}
	}

	@Override
	public void onCustomActivityResult(int requestCode, int resultCode,
			Intent data) {
	}

}
