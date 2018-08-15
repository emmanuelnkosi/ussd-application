package com.afrocast.response;

public class Response {
	
	private Boolean shouldClose;
	private String ussdMenu;
	private int responseExitCode;
	private String responseMessage;
	
	public Response() {}

	public Response(Boolean shouldClose, String ussdMenu, int responseExitCode, String responseMessage) {
		super();
		this.shouldClose = shouldClose;
		this.ussdMenu = ussdMenu;
		this.responseExitCode = responseExitCode;
		this.responseMessage = responseMessage;
	}

	public Boolean getShouldClose() {
		return shouldClose;
	}

	public void setShouldClose(Boolean shouldClose) {
		this.shouldClose = shouldClose;
	}

	public String getUssdMenu() {
		return ussdMenu;
	}

	public void setUssdMenu(String ussdMenu) {
		this.ussdMenu = ussdMenu;
	}

	public int getResponseExitCode() {
		return responseExitCode;
	}

	public void setResponseExitCode(int responseExitCode) {
		this.responseExitCode = responseExitCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	
	
	
	

}
