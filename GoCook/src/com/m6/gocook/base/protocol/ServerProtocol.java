package com.m6.gocook.base.protocol;

public class ServerProtocol {

	public static final String URL_ROOT = "http://192.168.1.103";
	
	public static final String URL_LOGIN =  URL_ROOT + "/user/login";
	
	public static final String URL_REGISTER = URL_ROOT + "/user/register";
	
	public static final String URL_RECIPE = URL_ROOT + "/recipe?id=%s";
	
	
	
	public static final String KEY_RESULT = "result";
	public static final int VALUE_RESULT_OK = 0;
	public static final int VALUE_RESULT_ERROR = 0;

	
	
	/* Recipe JSON Protocol */
	
	public static final String KEY_RECIPE = "result_recipe";
	public static final String KEY_RECIPE_ID = "recipe_id";
	public static final String KEY_RECIPE_AUTHOR_ID = "author_id";
	public static final String KEY_RECIPE_AUTHOR_NAME = "author_name";
	public static final String KEY_RECIPE_NAME = "recipe_name";
	public static final String KEY_RECIPE_INTRO = "intro";
	public static final String KEY_RECIPE_COLLECTED_COUNT = "collected_count";
	public static final String KEY_RECIPE_DISH_COUNT = "dish_count";
	public static final String KEY_RECIPE_COMMENT_COUNT = "comment_count";
	public static final String KEY_RECIPE_COVER_IMAGE = "cover_image";
	public static final String KEY_RECIPE_MATERIALS = "materials";
	public static final String VALUE_RECIPE_MATERIALS_FLAG = "\\|";
	public static final String KEY_RECIPE_STEPS = "steps";
	public static final String KEY_RECIPE_STEPS_NO = "no";
	public static final String KEY_RECIPE_STEPS_CONTENT = "content";
	public static final String KEY_RECIPE_STEPS_IMG = "img";
	public static final String KEY_RECIPE_TIPS = "tips";
	
	
}