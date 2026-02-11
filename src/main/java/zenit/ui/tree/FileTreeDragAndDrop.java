package zenit.ui.tree;

import java.io.File;

import javafx.scene.control.*;
import javafx.scene.input.*;

public class FileTreeDragAndDrop {

    public static void attach(TreeView<String> treeView) {
        treeView.setCellFactory(tv -> {
            TreeCell<String> cell = new TreeCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };

            // Börja dra
            cell.setOnDragDetected(e -> {
                if (cell.getTreeItem() instanceof FileTreeItem) {
                    File file = ((FileTreeItem<String>) cell.getTreeItem()).getFile();
                    if (file != null) {
                        Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(file.getAbsolutePath());
                        db.setContent(content);
                    }
                }
                e.consume();
            });