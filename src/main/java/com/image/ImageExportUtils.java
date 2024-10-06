package com.image;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;

public class ImageExportUtils {
    private String suffix;

    private double iWidth;
    private double iHeight;
    private double pWidth;
    private double pHeight;

    private double oldPWidth;
    private double oldPHeight;
    private double pX;
    private double pY;

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

    // 导出图像到用户选择的目录
    public static void exportImage(AnchorPane imagePane, String fileSuffix) {
        if (fileSuffix == null || fileSuffix.isEmpty()) {
            showAlert("Invalid file extension!");
            return;
        }

        // 创建 SnapshotParameters 对象，保持缩放比例
        SnapshotParameters sp = new SnapshotParameters();
        sp.setTransform(Transform.scale(5, 5)); // 将图像放大 5 倍，导出高分辨率图像

        // 获取当前图像的快照 (包括裁剪后的部分)
        WritableImage imgReturn = imagePane.snapshot(sp, null);

        // 弹出选择导出目录对话框
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(new Stage());

        if (directory != null) {
            // 设置导出的文件名
            String fileName = directory + "/exportedImage." + fileSuffix;
            File file = new File(fileName);

            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imgReturn, null);

            try {
                // 写入文件
                boolean write = ImageIO.write(bufferedImage, fileSuffix, file);
                if (write) {
                    showAlert("Image exported successfully to: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                showAlert("File write error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("No directory selected.");
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
