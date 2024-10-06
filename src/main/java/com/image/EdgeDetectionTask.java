package com.image;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class EdgeDetectionTask extends Task<Image> {

    private BufferedImage bufferedImage;
    private String method;
    private int strength;

    public EdgeDetectionTask(BufferedImage bufferedImage, String method, int strength) {
        this.bufferedImage = bufferedImage;
        this.method = method;
        this.strength = strength;
    }

    @Override
    protected Image call() throws Exception {
        BufferedImage resultImage = null;

        switch (method) {
            case "roberts":
                resultImage = robertsCross(bufferedImage, strength);
                break;
            case "laplacian":
                resultImage = laplacian(bufferedImage, strength);
                break;
            case "sobel":
                resultImage = sobel(bufferedImage, strength);
                break;
        }

        return SwingFXUtils.toFXImage(resultImage, null);
    }

    private BufferedImage robertsCross(BufferedImage image, int strength) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int p1 = image.getRGB(x, y) & 0xff;
                int p2 = image.getRGB(x + 1, y + 1) & 0xff;
                int p3 = image.getRGB(x + 1, y) & 0xff;
                int p4 = image.getRGB(x, y + 1) & 0xff;

                int edge = Math.abs(p1 - p2) + Math.abs(p3 - p4);
                edge = Math.min(255, edge * strength / 50);
                edgeImage.setRGB(x, y, new java.awt.Color(edge, edge, edge).getRGB());
            }
        }
        return edgeImage;
    }

    private BufferedImage sobel(BufferedImage image, int strength) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = 0, gy = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = image.getRGB(x + i, y + j) & 0xff;
                        gx += pixel * sobelX[i + 1][j + 1];
                        gy += pixel * sobelY[i + 1][j + 1];
                    }
                }

                int edge = Math.min(255, (int) Math.sqrt(gx * gx + gy * gy) * strength / 50);
                edgeImage.setRGB(x, y, new java.awt.Color(edge, edge, edge).getRGB());
            }
        }
        return edgeImage;
    }

    private BufferedImage laplacian(BufferedImage image, int strength) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[][] laplacianKernel = {{0, 1, 0}, {1, -4, 1}, {0, 1, 0}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sum = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = image.getRGB(x + i, y + j) & 0xff;
                        sum += pixel * laplacianKernel[i + 1][j + 1];
                    }
                }

                int edge = Math.min(255, Math.abs(sum) * strength / 50);
                edgeImage.setRGB(x, y, new java.awt.Color(edge, edge, edge).getRGB());
            }
        }
        return edgeImage;
    }
}

