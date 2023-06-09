package com.example.lsbimagesteganographyusingsecretkey.impl;

import java.io.IOException;

public class LSBDecoder {
    private final String stegoImageFilePath;
    private final int messageLength;
    private final String secretKey;

    private int[][] redMatrix2DDecimalArray;
    private int[][] greenMatrix2DDecimalArray;
    private int[][] blueMatrix2DDecimalArray;

    private int[] redMatrix1DBitStreamArray;
    private int[] greenMatrix1DBitStreamArray;
    private int[] blueMatrix1DBitStreamArray;

    private int[] secretKey1DBitStreamArray;

    public LSBDecoder(String stegoImageFilePath, int messageLength, String secretKey) {
        this.stegoImageFilePath = stegoImageFilePath;
        this.messageLength = messageLength;
        this.secretKey = secretKey;
    }

    public String recoveryProcess() throws IOException {
        /* convert RGB matrix to 1-D bit stream array */
        this.redMatrix1DBitStreamArray = new int[this.redMatrix2DDecimalArray.length * this.redMatrix2DDecimalArray[0].length * 8];
        this.greenMatrix1DBitStreamArray = new int[this.greenMatrix2DDecimalArray.length * this.greenMatrix2DDecimalArray[0].length * 8];
        this.blueMatrix1DBitStreamArray = new int[this.blueMatrix2DDecimalArray.length * this.blueMatrix2DDecimalArray[0].length * 8];
        this.convert2DDecimalArrayTo1DBitStreamArray(this.redMatrix2DDecimalArray, this.redMatrix1DBitStreamArray);
        this.convert2DDecimalArrayTo1DBitStreamArray(this.greenMatrix2DDecimalArray, this.greenMatrix1DBitStreamArray);
        this.convert2DDecimalArrayTo1DBitStreamArray(this.blueMatrix2DDecimalArray, this.blueMatrix1DBitStreamArray);

        /* convert secret key to 1-D bit stegoImage = {BufferedImage@1133} "BufferedImage@7b1d7fff: type = 5 ColorModel: #pixelBits = 24 numComponents = 3 color space = java.awt.color.ICC_ColorSpace@2aae9190 transparency = 1 has alpha = false isAlphaPre = false ByteInterleavedRaster: width = 57 height = 42 #numDataElements 3 dataOff[0] = 2"… Show imageam array */
        this.secretKey1DBitStreamArray = new int[secretKey.length() * 8];
        this.convertStringTo1DBitStreamArray(secretKey, this.secretKey1DBitStreamArray);

        /* for each bit of message: decision-making using XOR to choose where to hide */
        return this.recoverMessage();
    }

    private void convert2DDecimalArrayTo1DBitStreamArray(int[][] twoDDecimalArray, int[] oneDBitStreamArray) {
        int j = 0;
        for (int[] arr : twoDDecimalArray) {
            for (int element : arr) {
                String binaryString = Integer.toBinaryString(element);
                binaryString = this.fillTo8Bit(binaryString);
                for (int i = 0; i < binaryString.length(); i++) {
                    oneDBitStreamArray[j++] = Integer.parseInt(String.valueOf(binaryString.charAt(i)));
                }
            }
        }
    }

    private void convertStringTo1DBitStreamArray(String word, int[] oneDBitStreamArray) {
        int k = 0;
        for (int i = 0; i < word.length(); i++) {
            String binaryString = Integer.toBinaryString(word.charAt(i));
            binaryString = this.fillTo8Bit(binaryString);
            for (int j = 0; j < binaryString.length(); j++) {
                oneDBitStreamArray[k++] = Integer.parseInt(String.valueOf(binaryString.charAt(j)));
            }
        }
    }

    private String fillTo8Bit(String binaryString) {
        StringBuilder binaryStringBuilder = new StringBuilder(binaryString);
        while (binaryStringBuilder.length() != 8) {
            binaryStringBuilder.insert(0, '0');
        }
        binaryString = binaryStringBuilder.toString();
        return binaryString;
    }

    private String recoverMessage() {
        StringBuilder message = new StringBuilder();
        StringBuilder messageBitStream = new StringBuilder();
        int i = 0, secretKeyLength = this.secretKey1DBitStreamArray.length;
        int j = 7;
        for (int k = 0; k < this.messageLength * 8; k++) {
            if (i == secretKeyLength) {
                i = 0;
            }
            int secretKeyBit = this.secretKey1DBitStreamArray[i++];
            int redLSB = this.redMatrix1DBitStreamArray[j];
            int xor = secretKeyBit ^ redLSB;
            switch (xor) {
                case 1 -> messageBitStream.append(this.greenMatrix1DBitStreamArray[j]);
                case 0 -> messageBitStream.append(this.blueMatrix1DBitStreamArray[j]);
            }
            j += 8;
        }

        StringBuilder eightBits = new StringBuilder();
        for (int k = 0; k < messageBitStream.length(); k++) {
            eightBits.append(messageBitStream.charAt(k));
            if ((k + 1) % 8 == 0) {
                message.append((char) Integer.parseInt(eightBits.toString(), 2));
                eightBits = new StringBuilder();
            }
        }

        return message.toString();
    }

    // ========== setters ==========

    public void setRedMatrix2DDecimalArray(int[][] redMatrix2DDecimalArray) {
        this.redMatrix2DDecimalArray = redMatrix2DDecimalArray;
    }

    public void setGreenMatrix2DDecimalArray(int[][] greenMatrix2DDecimalArray) {
        this.greenMatrix2DDecimalArray = greenMatrix2DDecimalArray;
    }

    public void setBlueMatrix2DDecimalArray(int[][] blueMatrix2DDecimalArray) {
        this.blueMatrix2DDecimalArray = blueMatrix2DDecimalArray;
    }
}
