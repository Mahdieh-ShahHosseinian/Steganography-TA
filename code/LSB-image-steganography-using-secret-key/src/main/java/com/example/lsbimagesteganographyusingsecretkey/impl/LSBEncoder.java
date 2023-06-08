package com.example.lsbimagesteganographyusingsecretkey.impl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LSBEncoder {
    private static String COVER_IMAGE_FILE_PATH;
    private static String STEGO_IMAGE_FILE_PATH;
    private static String MESSAGE;
    private static String SECRET_KEY;

    private int[][] redMatrix2DDecimalArray;
    private int[][] greenMatrix2DDecimalArray;
    private int[][] blueMatrix2DDecimalArray;

    private int[] redMatrix1DBitStreamArray;
    private int[] greenMatrix1DBitStreamArray;
    private int[] blueMatrix1DBitStreamArray;

    private int[] message1DBitStreamArray;
    private int[] secretKey1DBitStreamArray;

    public LSBEncoder(String coverImageFilePath, String stegoImageFilePath, String message, String secretKey) {
        LSBEncoder.COVER_IMAGE_FILE_PATH = coverImageFilePath;
        LSBEncoder.STEGO_IMAGE_FILE_PATH = stegoImageFilePath;
        LSBEncoder.MESSAGE = message;
        LSBEncoder.SECRET_KEY = secretKey;
    }

    public void hidingProcess() throws IOException {
        /* take cover-image */
        File coverImageFile = new File(COVER_IMAGE_FILE_PATH);
        BufferedImage coverImage = ImageIO.read(coverImageFile);

        /* divide cover-image into three matrix (RGB) */
        int height = coverImage.getHeight(), width = coverImage.getWidth();
        this.redMatrix2DDecimalArray = new int[height][width];
        this.greenMatrix2DDecimalArray = new int[height][width];
        this.blueMatrix2DDecimalArray = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = coverImage.getRGB(x, y);
                this.redMatrix2DDecimalArray[y][x] = (rgb >> 16) & 0xFF;
                this.greenMatrix2DDecimalArray[y][x] = (rgb >> 8) & 0xFF;
                this.blueMatrix2DDecimalArray[y][x] = rgb & 0xFF;
            }
        }

        /* convert RGB matrix to 1-D bit stream array */
        this.redMatrix1DBitStreamArray = new int[this.redMatrix2DDecimalArray.length * this.redMatrix2DDecimalArray[0].length * 8];
        this.greenMatrix1DBitStreamArray = new int[this.greenMatrix2DDecimalArray.length * this.greenMatrix2DDecimalArray[0].length * 8];
        this.blueMatrix1DBitStreamArray = new int[this.blueMatrix2DDecimalArray.length * this.blueMatrix2DDecimalArray[0].length * 8];
        this.convert2DDecimalArrayTo1DBitStreamArray(this.redMatrix2DDecimalArray, this.redMatrix1DBitStreamArray);
        this.convert2DDecimalArrayTo1DBitStreamArray(this.greenMatrix2DDecimalArray, this.greenMatrix1DBitStreamArray);
        this.convert2DDecimalArrayTo1DBitStreamArray(this.blueMatrix2DDecimalArray, this.blueMatrix1DBitStreamArray);

        /* convert message to 1-D bit stream array */
        this.message1DBitStreamArray = new int[MESSAGE.length() * 8];
        this.convertStringTo1DBitStreamArray(MESSAGE, this.message1DBitStreamArray);

        /* convert secret key to 1-D bit stream array */
        this.secretKey1DBitStreamArray = new int[SECRET_KEY.length() * 8];
        this.convertStringTo1DBitStreamArray(SECRET_KEY, this.secretKey1DBitStreamArray);

        /* for each bit of message: decision-making using XOR to choose where to hide */
        this.hideMessage();

        /* convert 1-D bit stream array to 2-D decimal array */
        this.update2DDecimalArrayUsing1DBitStreamArray(this.greenMatrix1DBitStreamArray, this.greenMatrix2DDecimalArray);
        this.update2DDecimalArrayUsing1DBitStreamArray(this.blueMatrix1DBitStreamArray, this.blueMatrix2DDecimalArray);

        /* create stego-image */
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = this.redMatrix2DDecimalArray[y][x];
                rgb = (rgb << 8) + this.greenMatrix2DDecimalArray[y][x];
                rgb = (rgb << 8) + this.blueMatrix2DDecimalArray[y][x];
                image.setRGB(x, y, rgb);
            }
        }

        File stegoImageFile = new File(STEGO_IMAGE_FILE_PATH);
        ImageIO.write(image, "jpg", stegoImageFile);
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

    private void hideMessage() {
        int i = 0, secretKeyLength = this.secretKey1DBitStreamArray.length;
        int j = 1;
        for (int message1Bit : this.message1DBitStreamArray) {
            if (i + 1 == secretKeyLength) {
                i = 0;
            }
            int secretKeyBit = this.secretKey1DBitStreamArray[i++];
            int redLSB = this.redMatrix1DBitStreamArray[8 * j - 1];
            int xor = secretKeyBit ^ redLSB;
            switch (xor) {
                case 1 -> this.greenMatrix1DBitStreamArray[8 * j - 1] = message1Bit;
                case 0 -> this.blueMatrix1DBitStreamArray[8 * j - 1] = message1Bit;
            }
            j++;
        }
    }

    private void update2DDecimalArrayUsing1DBitStreamArray(int[] oneDBitStreamArray, int[][] twoDDecimalArray) {
        StringBuilder eightBits = new StringBuilder();
        int j = 0, k = 0, twoDDecimalArrayHeight = twoDDecimalArray.length, twoDDecimalArrayWidth = twoDDecimalArray[0].length;
        for (int i = 0; i < oneDBitStreamArray.length; i++) {
            eightBits.append(oneDBitStreamArray[i]);
            if ((i + 1) % 8 == 0) {
                if (k + 1 == twoDDecimalArrayWidth) {
                    j++;
                }
                if (k + 1 == twoDDecimalArrayHeight) {
                    k = 0;
                } else {
                    k++;
                }
                twoDDecimalArray[j][k] = Integer.parseInt(String.valueOf(eightBits), 2);
                eightBits = new StringBuilder();
            }
        }
    }
}
