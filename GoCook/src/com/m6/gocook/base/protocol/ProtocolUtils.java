package com.m6.gocook.base.protocol;

import android.text.TextUtils;

public class ProtocolUtils {

	public static String getURL(String url) {
		if(url.startsWith("http")){
			return url;
		} else {
			return Protocol.URL_ROOT + "/" + url;
		}
	}
	
	public static String getStepImageURL(String url) {
		if(url.startsWith("http")) {
			return url;
		} else {
			return Protocol.URL_ROOT + "/images/recipe/step/" + url;
		}
	}
	
	public static String getImageFileNameFromURL(String url) {
		if(url.startsWith("http")) {
			return url.substring(url.lastIndexOf("/") + 1, url.length());
		} else {
			return url;
		}
	}
	
	public static String getAvatarURL(String url) {
		if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
			return url;
		} else {
			return Protocol.URL_ROOT + "/images/avatars/" + url;
		}
	}
}
