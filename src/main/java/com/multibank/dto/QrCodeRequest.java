package com.multibank.dto;

import jakarta.validation.constraints.NotBlank;

public class QrCodeRequest {

    @NotBlank
    private String payload;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
