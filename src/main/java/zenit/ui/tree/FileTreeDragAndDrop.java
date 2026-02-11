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