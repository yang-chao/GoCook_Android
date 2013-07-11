package com.m6.gocook.base.entity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.m6.gocook.base.protocol.Protocol;
import com.m6.gocook.biz.account.AccountModel;

import android.os.Parcel;
import android.os.Parcelable;

public class RecipeEntity implements IParseable<JSONObject> {

	public class Material {
		private String name;
		private String remark;
		private boolean isMain;

		public Material(String name, String remark, boolean isMain) {
			this.name = name;
			this.remark = remark;
			this.isMain = isMain;
		}

		public String getName() {
			return name;
		}
		
		public String getName(int maxLength) {
			if(name != null && name.length() > maxLength) {
				return name.substring(0, maxLength - 1) + "...";
			} else {
				return name;
			}
			
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRemark() {
			return remark;
		}
		
		public String getRemark(int maxLength) {
			if(remark != null && remark.length() > maxLength) {
				return remark.substring(0, maxLength - 1) + "...";
			} else {
				return remark;
			}
			
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		public boolean isMain() {
			return isMain;
		}

		public void setMain(boolean isMain) {
			this.isMain = isMain;
		}
	}

	public class Procedure {
		private String desc;
		private String imageURL;

		public Procedure(String desc, String imageURL) {
			this.desc = desc;
			this.imageURL = imageURL;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getImageURL() {
			return imageURL;
		}

		public void setImageURL(String imageURL) {
			this.imageURL = imageURL;
		}
	}

	private int id;

	private String name;

	private String coverImgURL;

	private String desc;

	private String author;

	private int dishCount;

	private int collectCount;

	private ArrayList<Material> materials;

	private ArrayList<Procedure> procedures;

	private String tips;

	private int commentCount;

	private String commentStrs;
	
	private boolean isCollected;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCoverImgURL() {
		return coverImgURL;
	}

	public void setCoverImgURL(String coverImgURL) {
		this.coverImgURL = coverImgURL;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getDishCount() {
		return dishCount;
	}

	public void setDishCount(int dishCount) {
		this.dishCount = dishCount;
	}

	public int getCollectCount() {
		return collectCount;
	}

	public void setCollectCount(int collectCount) {
		this.collectCount = collectCount;
	}

	public ArrayList<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(ArrayList<Material> materials) {
		this.materials = materials;
	}

	public ArrayList<Procedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(ArrayList<Procedure> procedures) {
		this.procedures = procedures;
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public String getCommentStrs() {
		return commentStrs;
	}

	public void setCommentStrs(String commentStrs) {
		this.commentStrs = commentStrs;
	}

	public boolean isCollected() {
		return isCollected;
	}

	public void setCollected(boolean isCollected) {
		this.isCollected = isCollected;
	}

	@Override
	public boolean parse(JSONObject object) {

		if(object == null) {
			return false;
		}
		
		try {
			if(object.optInt(Protocol.KEY_RESULT) != Protocol.VALUE_RESULT_OK) {
				return false;
			}

			JSONObject recipe = object.optJSONObject(Protocol.KEY_RECIPE);
			if(recipe == null) {
				return false;
			}
			this.id = recipe.optInt(Protocol.KEY_RECIPE_ID);
			this.name = recipe.optString(Protocol.KEY_RECIPE_NAME);
			this.author = recipe.optString(Protocol.KEY_RECIPE_AUTHOR_NAME);
			this.desc = recipe.optString(Protocol.KEY_RECIPE_INTRO);
			this.coverImgURL = Protocol.URL_ROOT + "/" + recipe.optString(Protocol.KEY_RECIPE_COVER_IMAGE);
			this.dishCount = recipe.optInt(Protocol.KEY_RECIPE_DISH_COUNT);
			this.collectCount = recipe.optInt(Protocol.KEY_RECIPE_COLLECTED_COUNT);
			this.isCollected = recipe.optInt(Protocol.KEY_RECIPE_ISCOLLECTED) == 0;
			
			String materialStr = recipe.optString(Protocol.KEY_RECIPE_MATERIALS);
			String[] materials = materialStr.split(Protocol.VALUE_RECIPE_MATERIALS_FLAG);
			this.materials = new ArrayList<RecipeEntity.Material>();
			for (int index = 0; index < materials.length / 2; index++) {
				this.materials.add(new Material(materials[index * 2], materials[index * 2 + 1], true));
			}
			if(materials.length % 2 == 1) {
				this.materials.add(new Material(materials[materials.length - 1], "", true));
			}
			
			JSONArray steps = recipe.optJSONArray(Protocol.KEY_RECIPE_STEPS);
			this.procedures = new ArrayList<RecipeEntity.Procedure>();
			for(int index=0;index<steps.length();index++) {
				JSONObject step = steps.getJSONObject(index);
				this.procedures
				.add(new Procedure(step.optString(Protocol.KEY_RECIPE_STEPS_CONTENT),
						step.optString(Protocol.KEY_RECIPE_STEPS_IMG)));
			}
			
			this.tips = recipe.optString(Protocol.KEY_RECIPE_TIPS);
			
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}


}
