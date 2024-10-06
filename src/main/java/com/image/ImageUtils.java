package com.image;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

public class ImageUtils {

    // 裁剪图像
    public static Image cropImage(ImageView imageView, double startX, double startY, double endX, double endY) {
        if (imageView.getImage() == null) {
            showAlert("No image loaded!");
            return null;
        }

        // 计算裁剪区域的宽和高
        double cropWidth = Math.abs(endX - startX);
        double cropHeight = Math.abs(endY - startY);

        // 避免裁剪区域过小
        if (cropWidth < 5 || cropHeight < 5) {
            showAlert("The crop area is too small!");
            return null;
        }

        // 计算图像和视图的比例
        double imageWidth = imageView.getImage().getWidth();
        double imageHeight = imageView.getImage().getHeight();
        double viewWidth = imageView.getFitWidth();
        double viewHeight = imageView.getFitHeight();

        double scaleX = imageWidth / viewWidth;
        double scaleY = imageHeight / viewHeight;

        // 计算裁剪区域在原始图像上的位置
        double cropStartX = (Math.min(startX, endX) - imageView.getLayoutX()) * scaleX;
        double cropStartY = (Math.min(startY, endY) - imageView.getLayoutY()) * scaleY;
        cropWidth = cropWidth * scaleX;
        cropHeight = cropHeight * scaleY;

        // 确保裁剪区域在图像范围内
        if (cropStartX + cropWidth > imageWidth) {
            cropWidth = imageWidth - cropStartX;
        }
        if (cropStartY + cropHeight > imageHeight) {
            cropHeight = imageHeight - cropStartY;
        }

        // 确保裁剪区域有效
        if (cropStartX < 0 || cropStartY < 0 || cropWidth <= 0 || cropHeight <= 0) {
            showAlert("Invalid crop area!");
            return null;
        }

        try {
            // 执行裁剪
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            BufferedImage croppedImage = bufferedImage.getSubimage(
                    (int) cropStartX, (int) cropStartY, (int) cropWidth, (int) cropHeight);

            return SwingFXUtils.toFXImage(croppedImage, null);
        } catch (RasterFormatException ex) {
            showAlert("Cropping failed: " + ex.getMessage());
            return null;
        }
    }

    // 显示提示信息
    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Image Processing");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
