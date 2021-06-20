import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable{


    private final Socket socket;  //сокет подключенного клиента
    private final Path pathBase = Paths.get(".").toAbsolutePath().normalize();

    public ClientHandler(Socket socket){
        this.socket = socket;
    }


    @Override
    public void run() {
        try(
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
        ){
            while(true){
                String command = in.readUTF();
                //System.out.println(command);

                if("#getRootPath".equals(command)){
                    System.out.println("command is coming from client: #getRootPath");
                    getUserDir(out, in);
                }
                if("#getFileList".equals(command)){
                    System.out.println("command is coming from client: #getFileList");
                    getUserFileList(out, in);
                }
                if("#isDirectory".equals(command)){
                    System.out.println("command is coming from client: #isDirectory");
                    isDirectory(out, in);
                }

                if("#upload".equals(command)){
                    System.out.println("command is coming from client: #upload");
                    upload(out, in);
                }
                if("#message".equals(command)){
                    System.out.println("command is coming from client: #message");
                    message(out, in);
                }
                if("#download".equals(command)){
                    System.out.println("command is coming from client: #download");
                    download(out, in);
                }
                if("#delete".equals(command)){
                    System.out.println("command is coming from client: #delete");
                    delete(out, in);
                }
                if("#createDir".equals(command)){
                    System.out.println("command is coming from client: #createDir");
                    createDir(out, in);
                }


                if("#closeConnection".equals(command)){
                    System.out.println("command is coming from client: #closeConnection");
                    out.writeUTF("GoodBye");
                    disconnected();
                    System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
                    System.out.println();
                    break;
                }

            }
        }catch(SocketException socketException){
            System.out.printf("Client %s disconnected\n", socket.getInetAddress());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createDir(DataOutputStream out, DataInputStream in) {
        try {
            String dirPath = in.readUTF();
            Path absolutePath = Paths.get(pathBase.toString(), dirPath);
            if(Files.exists(absolutePath)) {
                out.writeUTF("WRONG");
            }
            else{
                Files.createDirectory(absolutePath);
                out.writeUTF(dirPath + " is created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void delete(DataOutputStream out, DataInputStream in) {
        try {
            String filePath = in.readUTF();
            Path absolutePath = Paths.get(pathBase.toString(), filePath);
            Files.deleteIfExists(absolutePath);
            out.writeUTF(filePath + " is deleted");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void isDirectory(DataOutputStream out, DataInputStream in) {
        try {
            String newPath = in.readUTF();
            Path absolutePath = Paths.get(pathBase.toString(), newPath);
            out.writeBoolean(Files.isDirectory(absolutePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserFileList(DataOutputStream out, DataInputStream in) {
        try {
            Path pathRelative =  Paths.get(in.readUTF());
            Path pathAbsolute = Paths.get(pathBase.toString()).resolve(pathRelative);
            if(!Files.exists(pathAbsolute)){
                Files.createDirectory(pathAbsolute);
            }
            List<FileInfo> fileList = Files.list(pathAbsolute).map(FileInfo::new).collect(Collectors.toList());
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(fileList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getUserDir(DataOutputStream out, DataInputStream in) {
        try {
            String user = in.readUTF();
            if("anemchenko".equals(user)){
                String pathRelative = "anemchenko/";
                out.writeUTF(pathRelative);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void download(DataOutputStream out, DataInputStream in) {
        try {
            String filePath = in.readUTF(); //Принимаем от клиента имя файла для скачивания
            System.out.println("Request of downloading the file: " + filePath);

            File file = new File(Paths.get(pathBase.toString(),filePath).toUri());
            if(!file.exists()){
                out.writeLong(-1);
                throw new FileNotFoundException();
            }
            long fileLength = file.length();

            out.writeLong(fileLength); //Отправляем на клиент размер файла

            System.out.println("Sending the file...");
            FileInputStream fis = new FileInputStream(file); //Отправялем сам файл
            int read;
            byte[] buffer = new byte[8 * 1024];
            while((read = fis.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            out.flush();
            fis.close();
            String clientResponse = in.readUTF();
            System.out.println(clientResponse);
            out.writeUTF("Server: File has been sent");

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private void message(DataOutputStream out, DataInputStream in) {
        try {
            System.out.println("Incoming message...");
            String message = in.readUTF();
            System.out.println("The message is: " + message);
            System.out.println();
            out.writeUTF("Message: '" + message + "' is delivered to the server");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void upload(DataOutputStream out, DataInputStream in) throws IOException {
        try{
            String filename = in.readUTF();
            long size = in.readLong();
            String uploadToDir = in.readUTF();
            File file = new File(Paths.get(pathBase.toString(), uploadToDir, filename).toUri()); // read file name
            System.out.println("File is being uploaded from client: " + filename);
            if(!file.exists()){
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            for (int i = 0; i < (size + (8 * 1024-1))/buffer.length ; i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();

            System.out.println("File '"+filename+"' uploaded from client to server");
            System.out.println();

            out.writeUTF("File '"+filename +"' with size = " + size + " bytes has been uploaded");
        }catch(Exception e){
            out.writeUTF("WRONG");
        }
    }

    private void disconnected() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
