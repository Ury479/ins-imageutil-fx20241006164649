package com.image;

import javafx.scene.image.Image;

import java.util.Stack;

public class ImageHistoryManager {

    private final Stack<Image> history = new Stack<>();

    // 保存当前图像的快照
    public void saveState(Image image) {
        if (image != null) {
            history.push(image);
        }
    }

    // 撤销至上一状态
    public Image undo() {
        if (!history.isEmpty()) {
            return history.pop();
        }
        return null; // 如果没有可撤销的状态，返回 null
    }

    // 检查是否可以撤销
    public boolean canUndo() {
        return !history.isEmpty();
    }
}
