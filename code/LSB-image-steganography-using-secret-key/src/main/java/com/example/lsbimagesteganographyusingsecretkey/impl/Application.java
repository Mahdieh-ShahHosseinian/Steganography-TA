package com.example.lsbimagesteganographyusingsecretkey.impl;

import java.io.IOException;

public class Application {
    private static final String COVER_IMAGE_FILE_PATH = "E:\\New folder\\Uni\\Semester 8\\stegano-cryptography\\project\\git\\Steganography-TA\\code\\LSB-image-steganography-using-secret-key\\src\\main\\resources\\cover-image.jpg";
    private static final String STEGO_IMAGE_FILE_PATH = "E:\\New folder\\Uni\\Semester 8\\stegano-cryptography\\project\\git\\Steganography-TA\\code\\LSB-image-steganography-using-secret-key\\src\\main\\resources\\stego-image.jpg";
    private static final String message = "sut";
    private static final String secretKey = "sohel";

    public static void main(String[] args) throws IOException {
        LSBEncoder lsbEncoder = new LSBEncoder(
                COVER_IMAGE_FILE_PATH,
                STEGO_IMAGE_FILE_PATH,
                message,
                secretKey
        );
        lsbEncoder.hidingProcess();

        LSBDecoder lsbDecoder = new LSBDecoder(
                STEGO_IMAGE_FILE_PATH,
                message.length(),
                secretKey
        );
        sendNecessaryData(lsbEncoder, lsbDecoder);

        System.out.println("message recovered= " + lsbDecoder.recoveryProcess());
    }

    private static void sendNecessaryData(LSBEncoder lsbEncoder, LSBDecoder lsbDecoder) {
        lsbDecoder.setRedMatrix2DDecimalArray(lsbEncoder.getRedMatrix2DDecimalArray());
        lsbDecoder.setGreenMatrix2DDecimalArray(lsbEncoder.getGreenMatrix2DDecimalArray());
        lsbDecoder.setBlueMatrix2DDecimalArray(lsbEncoder.getBlueMatrix2DDecimalArray());
    }
}
