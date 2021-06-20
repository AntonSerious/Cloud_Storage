import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class Connection {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public Connection(String host, int port) throws IOException {
        try {
            socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCloseConnection() {
        try {
            out.writeUTF("#closeConnection");
            String response = in.readUTF();
            System.out.println(response);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void uploadFile(Path fileToUploadPath, Path uploadToDir ) {
        try{
            File file = new File(fileToUploadPath.toString());
            if(!file.exists()){
                throw new FileNotFoundException();
            }
            String filename = fileToUploadPath.getFileName().toString();
            System.out.println("method upload file. GetFileName: " + filename);
            long fileLength = file.length();
            FileInputStream fis = new FileInputStream(file);

            this.out.writeUTF("#upload");
            this.out.writeUTF(filename);
            this.out.writeLong(fileLength);
            this.out.writeUTF(uploadToDir.toString());
            int read = 0;
            byte[] buffer = new byte[8 * 1024];
            while((read = fis.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            out.flush();
            fis.close();
            String response = in.readUTF();
            System.out.println(response);
        }catch (FileNotFoundException e){
            System.err.println("File not found: " + fileToUploadPath);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void downloadFile(Path currentPath, Path filePath) {
        try{
            this.out.writeUTF("#download");
            this.out.writeUTF(filePath.toString());

            long size = in.readLong(); //принимаем размер файла от сервера
            if(size == -1){
                System.out.println("There is no such file on a Server: " + filePath);
                return;
            }
            System.out.println("Size of file requested: "+ size +" bytes");


            File file = new File(Paths.get(currentPath.toString(), filePath.getFileName().toString()).toUri()); // read file name
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            for (int i = 0; i < (size + (8 * 1024-1))/buffer.length ; i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
            System.out.println("File '"+filePath.getFileName().toString()+"' downloaded from server to client");
            out.writeUTF("Client: File has been recieved");
            System.out.println();

            String response = in.readUTF();
            System.out.println(response);


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public List<FileInfo> getFileList(Path rootPath){
        List<FileInfo> fileList;
        try {
            this.out.writeUTF("#getFileList");
            this.out.writeUTF(rootPath.toString());
            ObjectInputStream ois = new ObjectInputStream(in);

            fileList = (List<FileInfo>) ois.readObject();
            return fileList;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public boolean isDirectory(Path newPath) {
        try {
            this.out.writeUTF("#isDirectory");
            this.out.writeUTF(newPath.toString());
            return in.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteFile(Path selectedFilePath) {
        try {
            this.out.writeUTF("#delete");
            this.out.writeUTF(selectedFilePath.toString());
            String response = in.readUTF();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createDir(Path newDir) {
        try {
            this.out.writeUTF("#createDir");
            this.out.writeUTF(newDir.toString());
            String response = in.readUTF();
            System.out.println(response);
            if("WRONG".equals(response)){
                Alert alert = new Alert(Alert.AlertType.WARNING, "Каталог с таким именем уже существует", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
