import client.ClientUI;
import client.FileTransferClient;

import javax.swing.*;

//运行客户端，ui在EDT线程中初始化
public class startClient {
    public static void main(String[] args) {
        FileTransferClient client = new FileTransferClient();
        SwingUtilities.invokeLater(() -> {
            client.setUI(new ClientUI(client));
        });
    }
}
