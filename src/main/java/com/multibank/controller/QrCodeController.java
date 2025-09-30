package com.multibank.controller;

import com.multibank.dto.QrCodeRequest;
import com.multibank.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> generateQr(@Valid @RequestBody QrCodeRequest request) {
        String base64 = qrCodeService.generateQrCodeBase64(request.getPayload());
        return ResponseEntity.ok(Map.of("base64", base64));
    }
}
