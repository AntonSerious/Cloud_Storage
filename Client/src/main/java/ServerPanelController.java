import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ServerPanelController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;


    @FXML
    TextField pathField;

    private String userName;

    private Connection connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userName = "anemchenko";
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Type");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>(){
                @Override
                protected void updateItem(Long item, boolean empty){
                    super.updateItem(item, empty);
                    if(item == null || empty){
                        setText("");
                        setStyle("");
                    }else{
                        String text = String.format("%,d bytes", item);
                        if(item == -1L){
                            text = "DIR";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileNameColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Update date");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);
        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);

        filesTable.getSortOrder().add(fileTypeColumn);

//        disksBox.getItems().clear();
//        disksBox.getItems().add(userName);
//        disksBox.getSelectionModel().select(0);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2){
                    System.out.println("double click");
                    Path newPath = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFilename());
                    if(connection.isDirectory(newPath)){
                        updateFileTable(newPath, connection.getFileList(newPath));
                    }
                }
            }
        });

    }


    public void updateFileTable(Path path, List<FileInfo> fileList){

        pathField.setText(path.normalize().toString());
        filesTable.getItems().clear();
        //filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
        filesTable.getItems().addAll(fileList);

        filesTable.sort();

    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if(upperPath != null){

            updateFileTable(upperPath, connection.getFileList(upperPath));
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        //updateFileTable(Paths.get(element.getSelectionModel().getSelectedItem()));
    }
    public String getSelectedFilename(){
        if(!filesTable.isFocused()){
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }
    public Path getCurrentPath(){
        return Paths.get(pathField.getText());
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Path getSelectedFilePath() {
        if(!filesTable.isFocused()){
            return null;
        }
        return Paths.get(pathField.getText(), filesTable.getSelectionModel().getSelectedItem().getFilename());
    }
}
