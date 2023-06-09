package com.example.lsbimagesteganographyusingsecretkey.impl;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class LSBEncoder {
    private final String coverImageFilePath;
    private final String stegoImageFilePath;
    private final String message;
    private final String secretKey;

    private int[][] redMatrix2DDecimalArray;
    private int[][] greenMatrix2DDecimalArray;
    private int[][] blueMatrix2DDecimalArray;

    private int[] redMatrix1DBitStreamArray;
    private int[] greenMatrix1DBitStreamArray;
    private int[] blueMatrix1DBitStreamArray;

    private int[] message1DBitStreamArray;
    private int[] secretKey1DBitStreamArray;

    public LSBEncoder(String coverImageFilePath, String stegoImageFilePath, String message, String secretKey) {
        this.coverImageFilePath = coverImageFilePath;
        this.stegoImageFilePath = stegoImageFilePath;
        this.message = message;
        this.secretKey = secretKey;
    }

    public void hidingProcess() {
        try {
            /* take cover-image */
            File coverImageFile = new File(coverImageFilePath);
            BufferedImage coverImage = ImageIO.read(coverImageFile);

            /* divide cover-image into three matrix (RGB) */
            int height = coverImage.getHeight(), width = coverImage.getWidth();
            this.redMatrix2DDecimalArray = new int[height][width];
            this.greenMatrix2DDecimalArray = new int[height][width];
            this.blueMatrix2DDecimalArray = new int[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = new Color(coverImage.getRGB(x, y));
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    this.redMatrix2DDecimalArray[y][x] = red;
                    this.greenMatrix2DDecimalArray[y][x] = green;
                    this.blueMatrix2DDecimalArray[y][x] = blue;
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
        this.message1DBitStreamArray = new int[message.length() * 8];
        this.convertStringTo1DBitStreamArray(message, this.message1DBitStreamArray);

        /* convert secret key to 1-D bit stream array */
        this.secretKey1DBitStreamArray = new int[secretKey.length() * 8];
        this.convertStringTo1DBitStreamArray(secretKey, this.secretKey1DBitStreamArray);

        /* print */
        System.out.println("BEFORE:");
        this.print();

        /* for each bit of message: decision-making using XOR to choose where to hide */
        this.hideMessage();

        /* convert 1-D bit stream array to 2-D decimal array */
        this.update2DDecimalArrayUsing1DBitStreamArray(this.greenMatrix1DBitStreamArray, this.greenMatrix2DDecimalArray);
        this.update2DDecimalArrayUsing1DBitStreamArray(this.blueMatrix1DBitStreamArray, this.blueMatrix2DDecimalArray);

        /* print */
        System.out.println("AFTER:");
        this.print();

            /* create stego-image */
            BufferedImage stegoImage = new BufferedImage(width, height, coverImage.getType());

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red = this.redMatrix2DDecimalArray[y][x];
                    int green = this.greenMatrix2DDecimalArray[y][x];
                    int blue = this.blueMatrix2DDecimalArray[y][x];
                    Color color = new Color(red, green, blue);
                    stegoImage.setRGB(x, y, color.getRGB());
                }
            }

            File stegoImageFile = new File(stegoImageFilePath);
            ImageIO.write(stegoImage, "jpg", stegoImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        int j = 7;
        for (int message1Bit : this.message1DBitStreamArray) {
            if (i == secretKeyLength) {
                i = 0;
            }
            int secretKeyBit = this.secretKey1DBitStreamArray[i++];
            int redLSB = this.redMatrix1DBitStreamArray[j];
            int xor = secretKeyBit ^ redLSB;
            switch (xor) {
                case 1 -> this.greenMatrix1DBitStreamArray[j] = message1Bit;
                case 0 -> this.blueMatrix1DBitStreamArray[j] = message1Bit;
            }
            j += 8;
        }
    }

    private void update2DDecimalArrayUsing1DBitStreamArray(int[] oneDBitStreamArray, int[][] twoDDecimalArray) {
        StringBuilder eightBits = new StringBuilder();
        int j = 0, k = 0, twoDDecimalArrayHeight = twoDDecimalArray.length, twoDDecimalArrayWidth = twoDDecimalArray[0].length;
        for (int i = 0; i < oneDBitStreamArray.length; i++) {
            eightBits.append(oneDBitStreamArray[i]);
            if ((i + 1) % 8 == 0) {
                if (k == twoDDecimalArrayWidth) {
                    j++;
                    k = 0;
                }
                twoDDecimalArray[j][k++] = Integer.parseInt(String.valueOf(eightBits), 2);
                eightBits = new StringBuilder();
            }
        }
    }

    public void print() {
        System.out.println("Red Matrix 2-D Decimal Array: [" + redMatrix2DDecimalArray.length + "][" + redMatrix2DDecimalArray[0].length + "]");
        System.out.println(Arrays.deepToString(redMatrix2DDecimalArray));
        System.out.println();
        System.out.println("Green Matrix 2-D Decimal Array: [" + greenMatrix2DDecimalArray.length + "][" + greenMatrix2DDecimalArray[0].length + "]");
        System.out.println(Arrays.deepToString(greenMatrix2DDecimalArray));
        System.out.println();
        System.out.println("Blue Matrix 2-D Decimal Array: [" + blueMatrix2DDecimalArray.length + "][" + blueMatrix2DDecimalArray[0].length + "]");
        System.out.println(Arrays.deepToString(blueMatrix2DDecimalArray));
        System.out.println();

        System.out.println("Secret Key 1-D Bit Stream Array: length=" + secretKey1DBitStreamArray.length);
        System.out.println(Arrays.toString(secretKey1DBitStreamArray));
        System.out.println();

        System.out.println("Message 1-D Bit Stream Array: length=" + message1DBitStreamArray.length);
        System.out.println(Arrays.toString(message1DBitStreamArray));
        System.out.println();

        System.out.println("Red Matrix 1-D Bit Stream Array: length=" + redMatrix1DBitStreamArray.length);
        System.out.println(Arrays.toString(redMatrix1DBitStreamArray));
        System.out.println();

        System.out.println("Green Matrix 1-D Bit Stream Array: length=" + greenMatrix1DBitStreamArray.length);
        System.out.println(Arrays.toString(greenMatrix1DBitStreamArray));
        System.out.println();

        System.out.println("Blue Matrix 1-D Bit Stream Array: length=" + blueMatrix1DBitStreamArray.length);
        System.out.println(Arrays.toString(blueMatrix1DBitStreamArray));
        System.out.println();
    }

    //========== getters ==========

    public int[][] getRedMatrix2DDecimalArray() {
        return redMatrix2DDecimalArray;
    }

    public int[][] getGreenMatrix2DDecimalArray() {
        return greenMatrix2DDecimalArray;
    }

    public int[][] getBlueMatrix2DDecimalArray() {
        return blueMatrix2DDecimalArray;
    }
}
