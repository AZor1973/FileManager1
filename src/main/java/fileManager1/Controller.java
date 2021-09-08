package fileManager1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class Controller implements Initializable {

    @FXML
    ListView<FileInfo> fileList;
    @FXML
    TextField pathField;
    Path root;
    Path selectedCopyFile;
    Path selectedMoveFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(FileInfo item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            String formattedFileName = String.format("%-30s", item.getFilename());
                            String formattedFileLength = String.format("%,d bytes", item.getLength());
                            if (item.getLength() == -1L) {
                                formattedFileLength = String.format("%s", "[ DIR ]");
                            }
                            if (item.getLength() == -2L) {
                                formattedFileLength = "";
                            }
                            String text = String.format("%s %-20s", formattedFileName, formattedFileLength);
                            setText(text);
                        }
                    }
                };
            }
        });
        goToPath(Paths.get("1"));
    }

    public void menuItemFileExitAction() {
        Platform.exit();
    }

    public void goToPath(Path path){
        root = path;
        pathField.setText(root.toAbsolutePath().toString());
        fileList.getItems().clear();
        fileList.getItems().addAll(new FileInfo(FileInfo.UP_TOKEN, -2L));
        fileList.getItems().addAll(scanFiles(path));
        fileList.getItems().sort((o1, o2) -> {
            if (o1.getFilename().equals(FileInfo.UP_TOKEN)){
                return -1;
            }
            if ((int) Math.signum(o1.getLength()) == (int) Math.signum(o2.getLength())) {
                return o1.getFilename().compareTo(o2.getFilename());
            }
            return (int) (o1.getLength() - o2.getLength());
        });

    }

    public List<FileInfo> scanFiles(Path root) {
        try {
//            List<FileInfo> out = new ArrayList<>();
//            List<Path> pathsInRoot = null;
//            pathsInRoot = Files.list(root).collect(Collectors.toList());
//            for (Path p : pathsInRoot) {
//                out.add(new FileInfo(p));
//            }
//            return out;
            return Files.list(root).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Files scan exception: " + root);
        }
    }

    public void filesListClicked(MouseEvent mouseEvent) {
       if(mouseEvent.getClickCount() == 2){
          FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
          if (fileInfo != null){
            if (fileInfo.isDirectory()){
               Path pathTo = root.resolve(fileInfo.getFilename());
               goToPath(pathTo);
            }
            if (fileInfo.isUpElement()){
                Path pathTo = root.toAbsolutePath().getParent();
                goToPath(pathTo);
            }
          }
       }
    }

    public void refresh(){
        goToPath(root);
    }

    public void copyAction(ActionEvent actionEvent) {
        FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
        if (selectedCopyFile == null && (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement())){
            return;
        }
        if (selectedCopyFile == null){
            selectedCopyFile = root.resolve(fileInfo.getFilename());
            ((Button) actionEvent.getSource()).setText("Coping: " + fileInfo.getFilename());
            return;
        }
        try{
            Files.copy(selectedCopyFile, root.resolve(selectedCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            selectedCopyFile = null;
            ((Button) actionEvent.getSource()).setText("Copy");
            refresh();
        }catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Copying impossible");
            alert.showAndWait();
        }
    }

    public void moveAction(ActionEvent actionEvent) {
        FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
        if (selectedMoveFile == null && (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement())){
            return;
        }
        if (selectedMoveFile == null){
            selectedMoveFile = root.resolve(fileInfo.getFilename());
            ((Button) actionEvent.getSource()).setText("Cutting: " + fileInfo.getFilename());
            return;
        }
        try{
            Files.move(selectedMoveFile, root.resolve(selectedMoveFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            selectedMoveFile = null;
            ((Button) actionEvent.getSource()).setText("Cut");
            refresh();
        }catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Cutting impossible");
            alert.showAndWait();
        }
    }

    public void deleteAction() {
        FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
        if (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement()){
            return;
        }
        try {
            Files.delete(root.resolve(fileInfo.getFilename()));
            refresh();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Delete fail");
            alert.showAndWait();
        }
    }

    public void viewAction() {
        FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
        if (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement()){
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION,String.format("%s : %,d bytes", fileInfo.getFilename(), fileInfo.getLength()) );
        alert.setTitle("File info");
        alert.setHeaderText(fileInfo.getFilename());
        alert.showAndWait();
    }
}
