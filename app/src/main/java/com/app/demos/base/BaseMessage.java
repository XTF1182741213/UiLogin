package com.app.demos.base;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.app.demos.util.AppUtil;

public class BaseMessage {
	
	private String code;
	private String message;
	private String resultSrc;
	private Map<String, BaseModel> resultMap;
	private Map<String, ArrayList<? extends BaseModel>> resultList;
	
	public BaseMessage () {
		this.resultMap = new HashMap<String, BaseModel>();
		this.resultList = new HashMap<String, ArrayList<? extends BaseModel>>();
	}
	
	@Override
	public String toString () {
		return code + " | " + message + " | " + resultSrc;
	}
	
	public String getCode () {
		return this.code;
	}
	
	public void setCode (String code) {
		this.code = code;
	}
	
	public String getMessage () {
		return this.message;
	}
	
	public void setMessage (String message) {
		this.message = message;
	}
	
	public String getResult () {
		return this.resultSrc;
	}
	
	public Object getResult (String modelName) throws Exception {
		Object model = this.resultMap.get(modelName);
		// catch null exception
		if (model == null) {
			throw new Exception("Message data is empty");
		}
		return model;
	}
	
	public ArrayList<? extends BaseModel> getResultList (String modelName) throws Exception {
		ArrayList<? extends BaseModel> modelList = this.resultList.get(modelName);
		// catch null exception
		if (modelList == null || modelList.size() == 0) {
			throw new Exception("Message data list is empty");
		}
		return modelList;
	}
	
	@SuppressWarnings("unchecked")
	public void setResult (String result) throws Exception {

		//result="{\"Customer\":{\"sign\":\"Happying\",\"id\":1,\"sid\":\"nbrj7qiau4o65ugkupjfep8ugq2hh1ts\",\"face\":\"1\",\"fanscount\":0,\"name\":\"james\",\"uptime\":\"2011-11-29 18:11:24\",\"blogcount\":0}}";
		this.resultSrc = result;
		if (result.length() > 0) {
			Log.d("MessageResult",result);
			JSONObject jsonObject = null;
			//result="{\"Customer\":{\"sign\":\"Happying\",\"id\":1,\"sid\":\"nbrj7qiau4o65ugkupjfep8ugq2hh1ts\",\"face\":\"1\",\"fanscount\":0,\"name\":\"james\",\"uptime\":\"2011-11-29 18:11:24\",\"blogcount\":0}}";
			jsonObject = new JSONObject(result);
			Iterator<String> it = jsonObject.keys();
			while (it.hasNext()) {
				// initialize
				String jsonKey = it.next();
				Log.d("jsonKey",jsonKey);
				String modelName = getModelName(jsonKey);
				Log.d("modelName",modelName);
				String modelClassName = "com.app.demos.model." + modelName;
				JSONArray modelJsonArray = jsonObject.optJSONArray(jsonKey);
				//JSONArray modelJsonArray = jsonObject.getJSONArray("Customer");
				//JSONArray modelJsonArray = jsonObject.optJSONArray("Customer");
			    //modelJsonArray="{\"id\":1,\"sign\":\"Happying\",\"face\":\"1\",\"sid\":\"nbrj7qiau4o65ugkupjfep8ugq2hh1ts\",\"fanscount\":0,\"name\":\"james\",\"uptime\":\"2011-11-29 18:11:24\",\"blogcount\":0}";
				// JSONObject
				if (modelJsonArray == null) {
					Log.d("运行这里1","有运行这里");
					JSONObject modelJsonObject = jsonObject.optJSONObject(jsonKey);
					if (modelJsonObject == null) {
						throw new Exception("Message result is invalid");
					}
					this.resultMap.put(modelName, json2model(modelClassName, modelJsonObject));
				// JSONArray
				} else {
					Log.d("运行这里2","有运行这里");
					ArrayList<BaseModel> modelList = new ArrayList<BaseModel>();
					for (int i = 0; i < modelJsonArray.length(); i++) {
						JSONObject modelJsonObject = modelJsonArray.optJSONObject(i);
						Log.d("modeClassNmae",modelClassName);
						modelList.add(json2model(modelClassName, modelJsonObject));
					}
					this.resultList.put(modelName, modelList);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private BaseModel json2model (String modelClassName, JSONObject modelJsonObject) throws Exception  {
		// auto-load model class
		BaseModel modelObj = (BaseModel) Class.forName(modelClassName).newInstance();
		Class<? extends BaseModel> modelClass = modelObj.getClass();
		// auto-setting model fields
		Iterator<String> it = modelJsonObject.keys();
		while (it.hasNext()) {
			String varField = it.next();
			String varValue = modelJsonObject.getString(varField);
			Field field = modelClass.getDeclaredField(varField);
			field.setAccessible(true); // have private to be accessable
			field.set(modelObj, varValue);
		}
		return modelObj;
	}
	
	private String getModelName (String str) {
		String[] strArr = str.split("\\W");
		if (strArr.length > 0) {
			str = strArr[0];
		}
		return AppUtil.ucfirst(str);
	}
	
}