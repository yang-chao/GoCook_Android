package com.m6.gocook.biz.recipe.hot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.m6.gocook.R;
import com.m6.gocook.base.entity.RecipeHot;
import com.m6.gocook.base.protocol.ProtocolUtils;
import com.m6.gocook.util.cache.util.ImageFetcher;

public class RecipeTopAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private RecipeHot mRecipeHot;
	private ImageFetcher mImageFetcher;
	
	public RecipeTopAdapter(Context context, ImageFetcher imageFetcher, RecipeHot recipeHot) {
		mContext = context;
		mRecipeHot = recipeHot;
		mInflater = LayoutInflater.from(mContext);
		mImageFetcher = imageFetcher;
	}
	
	@Override
	public int getCount() {
		if(mRecipeHot == null || mRecipeHot.getHotRecipes() == null) {
			return 0;
		}
		return mRecipeHot.getHotRecipes().size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_recipe_top_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.collectCount = (TextView) convertView.findViewById(R.id.collect_count);
			holder.material = (TextView) convertView.findViewById(R.id.material);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		RecipeHot.RecipeHotItem item = mRecipeHot.getHotRecipes().get(position);
		if(item != null) {
			mImageFetcher.loadImage(ProtocolUtils.getURL(item.getImage()), holder.image);
			holder.name.setText(item.getName());
			holder.collectCount.setText(String.format(mContext.getString(R.string.biz_recipe_hot_collect_count), item.getCollectCount()));
			holder.material.setText(item.getMaterial());
		}
		return convertView;
	}
	
	class ViewHolder {
		private ImageView image;
		private TextView name;
		private TextView collectCount;
		private TextView material;
	}
}
