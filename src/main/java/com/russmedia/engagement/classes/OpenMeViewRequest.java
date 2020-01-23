package com.russmedia.engagement.classes;

public class OpenMeViewRequest {

    private String entryPoint;
    private String additionalParams;

    public OpenMeViewRequest(String entryPoint, String additionalParams) {
        this.entryPoint = entryPoint;
        this.additionalParams = additionalParams;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public String getAdditionalParams() {
        return additionalParams;
    }
}
