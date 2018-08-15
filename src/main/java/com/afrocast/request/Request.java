package com.afrocast.request;

public class Request {
	
	private String msisdn;
	private String imsi;
	private String shortCode;
	private String optional;
	private String ussdNodeId;
	private String text;
	private String countryName;
	
	public Request() {}
	
	
	public Request(String msisdn, String imsi, String shortCode, String optional, String ussdNodeId, String text,
			String countryName) {
		super();
		this.msisdn = msisdn;
		this.imsi = imsi;
		this.shortCode = shortCode;
		this.optional = optional;
		this.ussdNodeId = ussdNodeId;
		this.text = text;
		this.countryName = countryName;
	}


	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getImsi() {
		return imsi;
	}
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}
	public String getOptional() {
		return optional;
	}
	public void setOptional(String optional) {
		this.optional = optional;
	}
	public String getUssdNodeId() {
		return ussdNodeId;
	}
	public void setUssdNodeId(String ussdNodeId) {
		this.ussdNodeId = ussdNodeId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	
	

}
