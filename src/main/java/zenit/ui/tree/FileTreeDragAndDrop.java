package zenit.ui.tree;

import java.io.File;
import java.nio.file.*;

import javafx.scene.control.*;
import javafx.scene.input.*;

public class FileTreeDragAndDrop {


    public static void attach(TreeView<String> treeView) {
        treeView.setCellFactory(tv -> {
            // Skapar en cell som visar filnamn och kan dras
            TreeCell<String> cell = new TreeCell<>() {

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };

          // Hämtar filen som är kopplad til lcellen
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

         // Kolla om målet är en mapp och acceptera draget
            cell.setOnDragOver(e -> {
                if (e.getDragboard().hasString() && cell.getTreeItem() instanceof FileTreeItem) {
                    File target = ((FileTreeItem<String>) cell.getTreeItem()).getFile();
                    if (target != null && target.isDirectory()) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                e.consume();
            });


            cell.setOnDragDropped(e -> {
                boolean success = false;
                if (e.getDragboard().hasString() && cell.getTreeItem() instanceof FileTreeItem) {
                    File source = new File(e.getDragboard().getString());
                    FileTreeItem<String> targetItem = (FileTreeItem<String>) cell.getTreeItem();
                    File targetDir = targetItem.getFile();

                    if (targetDir != null && targetDir.isDirectory()) {
                        try {
                            // Flytta filen på disk
                            Path target = targetDir.toPath().resolve(source.getName());
                            Files.move(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                            // Uppdatera trädstrukturen
                            FileTreeItem<String> root = (FileTreeItem<String>) treeView.getRoot();
                            FileTreeItem<String> sourceItem = FileTree.getTreeItemFromFile(root, source);
                            if (sourceItem != null && sourceItem.getParent() != null) {
                                sourceItem.getParent().getChildren().remove(sourceItem);
                            }
                            FileTree.createParentNode(targetItem, target.toFile());

                            success = true;
                        } catch (Exception ignored) {}
                    }
                }
                e.setDropCompleted(success);
                e.consume();
            });

            return cell;
        });
    }
}