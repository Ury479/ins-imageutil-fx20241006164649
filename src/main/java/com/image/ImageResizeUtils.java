package com.image;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ImageResizeUtils {

    // 调整图像大小并居中
    public static void centerImage(double size, ImageView imageView, AnchorPane imagePane, double iWidth, double iHeight, double pWidth, double pHeight, double pX, double pY) {
        double newWidth = pWidth * size;
        double newHeight = pHeight * size;
        Image img = imageView.getImage();
        if (img != null) {
            double w;
            double h;

            double reducCoeff = getReducCoeff(newWidth, newHeight, iWidth, iHeight);

            w = iWidth * reducCoeff;
            h = iHeight * reducCoeff;

            imagePane.setLayoutX(pX + (pWidth - w) / 2);
            imagePane.setLayoutY(pY + (pHeight - h) / 2);

            imagePane.setPrefWidth(w);
            imagePane.setMaxWidth(w);
            imagePane.setMinWidth(w);

            imagePane.setPrefHeight(h);
            imagePane.setMaxHeight(h);
            imagePane.setMinHeight(h);

            imageView.setFitWidth(w);
            imageView.setFitHeight(h);
        }
    }

    // 计算缩放系数
    public static double getReducCoeff(double newWidth, double newHeight, double iWidth, double iHeight) {
        double ratioW = newWidth / iWidth;
        double ratioH = newHeight / iHeight;
        return Math.min(ratioW, ratioH);
    }

    // 重置图像容器大小
    public static void resetImagePane(AnchorPane imagePane, double pWidth, double pHeight) {
        if (pWidth > 0) {
            imagePane.setPrefWidth(pWidth);
        }
        if (pHeight > 0) {
            imagePane.setPrefHeight(pHeight);
        }
        imagePane.setLayoutX(150);
        imagePane.setLayoutY(60);
    }
}

