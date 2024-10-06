package com.image;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.concurrent.Task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

public class MainController {

    // 图像处理相关属性
    private String suffix;
    private Stack<Image> imageHistory = new Stack<>(); // 用于撤销
    private Task<?> currentTask; // 当前正在运行的任务

    // FXML 绑定的组件
    @FXML
    private AnchorPane imagePane;
    @FXML
    private AnchorPane selectionRect; // 用于显示裁剪区域的矩形
    @FXML
    private ImageView imageView;
    @FXML
    private Slider strengthSlider;
    @FXML
    private Label imageLabel;

    // 裁剪区域的坐标
    private double startX, startY, endX, endY;

    // 初始化方法
    @FXML
    public void initialize() {
        System.out.println("MainController initialized");
        selectionRect.setVisible(false); // 初始化时隐藏裁剪矩形
    }

    // 图像拖拽进入检测
    public void imageDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles() || event.getDragboard().hasUrl()) {
            event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            imagePane.setStyle("-fx-background-color: #aaaaaa; -fx-border-color: red;");
        }
        event.consume();
    }

    // 处理图像拖拽放下操作
    public void imageDragDropped(DragEvent event) throws IOException {
        // 检查拖拽文件并加载图像
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().get(0);
            suffix = getFileSuffix(file.getName());
            loadImageFromFile(file);
        }
        event.setDropCompleted(true);
        event.consume();
    }

    /**
     * 当鼠标在 imageView 上按下时触发，开始选择裁剪区域
     */
    @FXML
    private void onMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        selectionRect.setVisible(true); // 显示裁剪矩形
        selectionRect.setLayoutX(startX); // 设置矩形起始 X 坐标
        selectionRect.setLayoutY(startY); // 设置矩形起始 Y 坐标
        selectionRect.setPrefWidth(0); // 重置矩形宽度
        selectionRect.setPrefHeight(0); // 重置矩形高度
    }

    /**
     * 当鼠标在 imageView 上拖拽时触发，调整裁剪区域大小
     */
    @FXML
    private void onMouseDragged(MouseEvent event) {
        endX = event.getX();
        endY = event.getY();

        // 计算矩形的宽和高
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        // 设置矩形的宽高及位置
        selectionRect.setPrefWidth(width);
        selectionRect.setPrefHeight(height);
        selectionRect.setLayoutX(Math.min(startX, endX));
        selectionRect.setLayoutY(Math.min(startY, endY));
    }

    /**
     * 当鼠标在 imageView 上释放时触发，结束选择裁剪区域
     */
    @FXML
    private void onMouseReleased(MouseEvent event) {
        endX = event.getX();
        endY = event.getY();
        // 当鼠标松开时，记录最终的坐标，矩形显示结束
    }

    /**
     * 当裁剪按钮被点击时触发
     */
    @FXML
    private void cropImage(ActionEvent event) {
        if (imageView.getImage() == null) {
            showAlert("No image loaded!");
            return;
        }

        // 使用 ImageUtils 进行裁剪
        Image croppedImage = ImageUtils.cropImage(imageView, startX, startY, endX, endY);

        if (croppedImage != null) {
            // 更新 ImageView 显示裁剪后的图像
            imageView.setImage(croppedImage);
            selectionRect.setVisible(false); // 隐藏裁剪框
            showAlert("Image cropped successfully!");
        } else {
            showAlert("Cropping failed.");
        }
    }

    // 导出图像
    @FXML
    private void exportNewImage(ActionEvent event) {
        if (imageView.getImage() == null) {
            showAlert("No image to export!");
            return;
        }
        ImageExportUtils.exportImage(imagePane, suffix); // 使用工具类进行导出
    }

    // 应用边缘检测算法
    @FXML
    public void applyRobertsCross(ActionEvent event) { applyEdgeDetection("roberts"); }
    @FXML
    public void applyLaplacian(ActionEvent event) { applyEdgeDetection("laplacian"); }
    @FXML
    public void applySobel(ActionEvent event) { applyEdgeDetection("sobel"); }

    // 应用边缘检测任务
    private void applyEdgeDetection(String method) {
        if (imageView.getImage() == null) {
            showAlert("No image loaded!");
            return;
        }
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }
        saveHistory(imageView.getImage()); // 存储历史图像状态

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
        int strength = (int) strengthSlider.getValue();
        EdgeDetectionTask task = new EdgeDetectionTask(bufferedImage, method, strength);
        currentTask = task;

        task.setOnSucceeded(workerStateEvent -> imageView.setImage(task.getValue()));
        task.setOnFailed(workerStateEvent -> showAlert("Edge detection failed!"));
        new Thread(task).start();
    }

    // 保存历史记录
    private void saveHistory(Image image) {
        imageHistory.push(image);
    }

    // 撤销操作
    @FXML
    private void undoAction(ActionEvent event) {
        if (!imageHistory.isEmpty()) {
            Image previousImage = imageHistory.pop();
            imageView.setImage(previousImage);
        }
    }

    // 显示提示信息
    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Image Processing");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // 工具方法：获取文件后缀
    private String getFileSuffix(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    // 加载图像文件
    private void loadImageFromFile(File file) {
        List<Image> images = new ArrayList<>();
        try {
            if (suffix.equals("zip")) {
                ZipFile zipFile = new ZipFile(file);
                zipFile.stream().forEach(entry -> {
                    try {
                        Image image = new Image(zipFile.getInputStream(entry));
                        if (image.getWidth() > 0 && image.getHeight() > 0) {
                            images.add(image);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                Image image = new Image("file:" + file.getAbsolutePath());
                images.add(image);
            }
        } catch (IOException e) {
            showAlert("Error loading image file: " + e.getMessage());
        }
        imageView.setImage(images.get(0));
        saveHistory(imageView.getImage()); // 存储当前图像状态
    }
}
