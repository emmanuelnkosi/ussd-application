package com.afrocast.endresponse;

public class EndResponse {
	
	private String responseExitCode;
	private String responseMessage;


	public EndResponse(String responseExitCode, String responseMessage) {
		this.responseExitCode = responseExitCode;
		this.responseMessage = responseMessage;
	}

	public String getResponseExitCode() {
		return responseExitCode;
	}

	public void setResponseExitCode(String responseExitCode) {
		this.responseExitCode = responseExitCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
}
