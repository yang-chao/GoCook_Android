package com.m6.gocook.biz.recipe;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.m6.gocook.R.string;
import com.m6.gocook.base.constant.Constants;
import com.m6.gocook.base.db.GoCookProvider;
import com.m6.gocook.base.db.table.RecipeMaterialPurchaseList;
import com.m6.gocook.base.db.table.RecipePurchaseList;
import com.m6.gocook.base.db.table.SearchHistory;
import com.m6.gocook.base.entity.RecipeEntity;
import com.m6.gocook.base.entity.RecipeListItem;
import com.m6.gocook.base.protocol.Protocol;
import com.m6.gocook.util.log.Logger;
import com.m6.gocook.util.net.NetUtils;

public class RecipeModel {
	
	private static final String TAG = RecipeModel.class.getCanonicalName();

	public static RecipeEntity getRecipe(Context context, String recipeId) {
		
		if(context == null || TextUtils.isEmpty(recipeId)) {
			Logger.e(TAG, "getRecipe failed, parameter is invalid");
			return null;
		}
		
		String result = NetUtils.httpGet(String.format(Protocol.URL_RECIPE, recipeId));
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(jsonObject != null) {
			RecipeEntity entity = new RecipeEntity();
			entity.parse(jsonObject);
			return entity;
		}

		return null;
	}
	
	public static RecipeListItem getRecipeTop(String url, int page) {
		return getRecipeData(NetUtils.httpGet(String.format(url, page)));
	}
	
	public static RecipeListItem getSearchData(String keyWords, int page) {
		String url = NetUtils.httpGet(String.format(Protocol.URL_RECIPE_SEARCH, keyWords, page));
		return getRecipeData(url);
	}
	
	public static RecipeListItem getRecipeData(String url) {
		String result = NetUtils.httpGet(url);
		if(TextUtils.isEmpty(result)) {
			return null;
		}
		
		try {
			JSONObject json = new JSONObject(result);
			RecipeListItem recipeListItem = new RecipeListItem();
			if(recipeListItem.parse(json)) {
				return recipeListItem;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
