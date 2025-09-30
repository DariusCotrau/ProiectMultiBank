package com.multibank.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QrCodeService {

    private static final int DEFAULT_SIZE = 300;

    public String generateQrCodeBase64(String payload) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, DEFAULT_SIZE, DEFAULT_SIZE);
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream);
                return Base64.getEncoder().encodeToString(stream.toByteArray());
            }
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Nu s-a putut genera codul QR", e);
        }
    }
}
