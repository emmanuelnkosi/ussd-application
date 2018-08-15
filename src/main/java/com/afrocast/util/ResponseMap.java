package com.afrocast.util;

import java.util.HashMap;
import java.util.Map;

import com.afrocast.response.Response;

public class ResponseMap {
	
	private Map<String,Response> mapAdd;
	private Map<String,Response> mapReplace;
	
	public ResponseMap() {
		mapAdd = new HashMap<>();
		mapReplace = new HashMap<>();
		
	}

}
