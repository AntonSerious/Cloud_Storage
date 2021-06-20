import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    public VBox mainPanel;
    public Button btnDeleteFile;
    Connection connection;

    private String userName = "anemchenko";
    @FXML
    VBox clientPanel, serverPanel;

    @FXML
    Button btnUploadFile;
    @FXML
    Button btnDownloadFile;


    ClientPanelController cpc;
    ServerPanelController spc;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            connection = new Connection("localhost", 6789);

            cpc = (ClientPanelController) clientPanel.getProperties().get("ctrl");
            spc = (ServerPanelController) serverPanel.getProperties().get("ctrl");

            spc.setConnection(connection);
            spc.updateFileTable(Paths.get(userName), connection.getFileList(Paths.get(userName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
        connection.sendCloseConnection();
    }


    public void btnUploadFile(ActionEvent actionEvent) {
        if(cpc.getSelectedFilename() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected!", ButtonType.OK);
            alert.showAndWait();
            System.out.println("no file is selected");
            return;
        }
        else{
            System.out.println("uploading file: "+ cpc.getSelectedFilePath());
            connection.uploadFile(cpc.getSelectedFilePath(), spc.getCurrentPath());
            spc.updateFileTable(spc.getCurrentPath(), connection.getFileList(spc.getCurrentPath()));
        }
    }

    public void btnDownloadFile(ActionEvent actionEvent) {
        if(spc.getSelectedFilename() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected!", ButtonType.OK);
            alert.showAndWait();
            System.out.println("no file is selected");
            return;
        }
        else{
            System.out.println("downloading file: "+ spc.getSelectedFilePath());
            connection.downloadFile(cpc.getCurrentPath(), spc.getSelectedFilePath());
            cpc.updateFileTable(cpc.getCurrentPath());
        }
    }

    public void btnDeleteFile(ActionEvent actionEvent) throws IOException {
        if(cpc.getSelectedFilename() == null && spc.getSelectedFilename() == null){
            return;
        }else if(cpc.getSelectedFilename() != null){
            Files.deleteIfExists(cpc.getSelectedFilePath().toAbsolutePath());
            cpc.updateFileTable(cpc.getCurrentPath());
        }else if(spc.getSelectedFilename() != null){
            connection.deleteFile(spc.getSelectedFilePath());
            spc.updateFileTable(spc.getCurrentPath(), connection.getFileList(spc.getCurrentPath()));
        }

    }

    public void btnCreateDirClient(ActionEvent actionEvent) throws IOException {
        if(cpc.getCurrentPath() != null){
            TextInputDialog dialog = new TextInputDialog("new folder");
            dialog.setTitle("Создание директории");
            dialog.setHeaderText("Введите имя директории:");
            dialog.setContentText("Имя директории:");
            Optional<String> dirName = dialog.showAndWait();
            if(!dirName.isPresent()){
                return;
            }
            Path dirPath = cpc.getCurrentPath().resolve(dirName.get()).toAbsolutePath();
            if(Files.exists(dirPath)){
                Alert alert = new Alert(Alert.AlertType.WARNING, "Каталог с таким именем уже существует", ButtonType.OK);
                alert.showAndWait();
            }else{
                Files.createDirectory(cpc.getCurrentPath().resolve(dirName.get()).toAbsolutePath());
                cpc.updateFileTable(cpc.getCurrentPath());
            }
        }
    }
    public void btnCreateDirServer(ActionEvent actionEvent) throws IOException {
        if(spc.getCurrentPath() == null){
            return;
        }else if(spc.getCurrentPath() != null){
            TextInputDialog dialog = new TextInputDialog("new folder");

            dialog.setTitle("Создание директории");
            dialog.setHeaderText("Введите имя директории:");
            dialog.setContentText("Имя директории:");
            Optional<String> dirName = dialog.showAndWait();
            if(!dirName.isPresent()){
                return;
            }
            Path newDir = Paths.get(spc.getCurrentPath().toString(), dirName.get());
            connection.createDir(newDir);

            spc.updateFileTable(spc.getCurrentPath(), connection.getFileList(spc.getCurrentPath()));
        }
    }

}
