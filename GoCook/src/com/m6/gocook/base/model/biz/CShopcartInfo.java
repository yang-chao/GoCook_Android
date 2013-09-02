package com.m6.gocook.base.model.biz;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.m6.gocook.base.model.BaseData;

public class CShopcartInfo extends BaseData {
	
	private int custId;
	private List<CShopcartWareInfo> wares;
	
	public CShopcartInfo(int custId, List<CShopcartWareInfo> wares) {
		this.custId = custId;
		this.wares = wares;
	}
	
	@Override
	public String getJsonData() {
		try {
			JSONObject postJsonObject = new JSONObject();
			postJsonObject.put("CustId", custId);
			JSONArray wareArray = new JSONArray();
			for (CShopcartWareInfo ware : wares) {
				wareArray.put(ware.getJsonObject());
			}
			postJsonObject.put("Wares", wareArray.toString());
			return postJsonObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public class CShopcartWareInfo {
		private int wareId; //商品编号
		private double quantity; //商品数量
		private String remark; //加工说明
		
		public JSONObject getJsonObject() {
			try {
				JSONObject postJsonObject = new JSONObject();
				postJsonObject.put("WareId", wareId);
				postJsonObject.put("Quantity", quantity);
				postJsonObject.put("Remark", remark);
				return postJsonObject;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
