import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;


/**
 * Swing client - File Storage
 * Client command: upload filename | download filename
 */
public class Client extends JFrame {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public Client() throws IOException {
        //initialization
        this.socket = new Socket("localhost", 6789);

        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        //create form

        setSize(300, 300);
        JPanel panel = new JPanel(new GridLayout(4,1));
        JButton btnSendMsg = new JButton("SEND Message");
        JButton btnUploadFile = new JButton("UPLOAD File");
        JButton btnDownloadFile = new JButton("DOWNLOAD File");

        JTextField textField = new JTextField();

        btnSendMsg.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();

                sendMessage(message);
            }
        });

        btnUploadFile.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = textField.getText();

                sendFile(filename);

            }
        });

        btnDownloadFile.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = textField.getText();

                getFile(filename);

            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                sendCloseConnection();
            }
        });

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel.add(textField);
        panel.add(btnSendMsg);
        panel.add(btnUploadFile);
        panel.add(btnDownloadFile);

        add(panel);
        setVisible(true);
    }

    private void sendCloseConnection() {
        try {
            out.writeUTF("#closeConnection");
            String response = in.readUTF();
            System.out.println(response);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
      При вводе имени файла и нажатии на кнопку "DOWNLOAD file", будет произведена попытка скачать файл с сервера.
       */
    private void getFile(String filename) {
        try{
            this.out.writeUTF("#download");
            this.out.writeUTF(filename);


            long size = in.readLong(); //принимаем размер файла от сервера
            if(size == -1){
                System.out.println("There is no such file on a Server: " + filename);
                return;
            }
            System.out.println("Size of file requested: "+ size +" bytes");


            File file = new File("Client/client_files/" + filename); // read file name
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            for (int i = 0; i < (size + (8 * 1024-1))/buffer.length ; i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
            System.out.println("File '"+filename+"' downloaded from server to client");
            out.writeUTF("Client: File has been recieved");
            System.out.println();

            String response = in.readUTF();
            System.out.println(response);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendFile(String filename) {
        try{
            File file = new File("Client/client_files/"+ filename);
            if(!file.exists()){
                throw new FileNotFoundException();
            }

            long fileLength = file.length();
            FileInputStream fis = new FileInputStream(file);

            this.out.writeUTF("#upload");
            this.out.writeUTF(filename);
            this.out.writeLong(fileLength);

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
            System.err.println("File not found: " + filename);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * message sending
     * @param message
     */
    private void sendMessage(String message) {
        try {
            this.out.writeUTF("#message");
            this.out.writeUTF(message);

            String response = in.readUTF();
            System.out.println(response);
        }catch(EOFException eofException){
            System.err.println("Reading command error from " + socket.getInetAddress());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}
