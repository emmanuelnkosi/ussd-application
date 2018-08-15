package com.afrocast.endrequest;

public class EndRequest {

    private String reason;
    private String exitCode;
    private String networkName;
    private String countryName;

    public EndRequest(){}

    public EndRequest(String reason, String exitCode, String networkName, String countryName) {
        this.reason = reason;
        this.exitCode = exitCode;
        this.networkName = networkName;
        this.countryName = countryName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}
