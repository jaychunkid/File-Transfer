package client;

import common.Constant;
import common.StreamSocket;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

//客户端逻辑类，需要刷新ui的操作使用SwingUtilities.invokeLater函数添加到EDT线程
public class FileTransferClient {
    private ClientUI ui;
    private StreamSocket socket;

    public void setUI(ClientUI ui){
        this.ui = ui;
    }

    //连接服务器，切换为文件传输界面
    void connect(String host, int port){
        try {
            socket = new StreamSocket(host, port);
            SwingUtilities.invokeLater(() -> {
                    ui.resetCursor();
                    ui.initTransferUI();
                });
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                    ui.resetCursor();
                    ui.showErrorDialog("Wrong host or port.");
                });
        }
    }

    //与服务器断开连接，切换为连接界面
    void disconnect() {
        try {
            socket.sendMessage(Constant.COMMAND_DISCONNECT);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //从服务器获取文件列表
    List<String> getFileList(){
        List<String> fileList = new ArrayList<>();
        try {
        socket.sendMessage(Constant.COMMAND_GET_FILE_LIST);

            int size = Integer.parseInt(socket.receiveMessage());
            for(int i = 0; i < size; ++i){
                fileList.add(socket.receiveMessage());
            }
        } catch (IOException e) {
            //发生通信错误，断开连接
            ui.showErrorDialog("Connect failed.");
            disconnect();
        }
        return fileList;
    }

    //从服务器下载指定文件，并保存到对应路径
    void download(File downloadFile, String fileName) {
        try {
            socket.sendMessage(Constant.COMMAND_TRANSFER);     //发送下载文件请求
            socket.sendMessage(fileName);     //发送请求的文件名
            //如果本地存在同名文件，先删除本地文件
            if (downloadFile.exists() && downloadFile.isFile()) {
                downloadFile.delete();
            }
            try {
                downloadFile.createNewFile();
            } catch (IOException e) {
                //新建文件失败，提示错误信息并返回
                SwingUtilities.invokeLater(() -> {
                        ui.resetCursor();
                        ui.showErrorDialog("Create file error.");
                    });
                return;
            }
            FileOutputStream writer = null;
            writer = new FileOutputStream(downloadFile);
            String str = socket.receiveMessage();     //读取服务器发送的信息
            //检查收到的信息类型
            if (str.equals(Constant.MARK_FILE_NONEXISTENT)) {
                //请求文件不存在与服务器，弹出错误信息并返回
                SwingUtilities.invokeLater(() -> {
                        ui.resetCursor();
                        ui.showErrorDialog("File doesn't exist.");
                    });
                return;
            } else if (str.equals(Constant.MARK_FILE_EXISTENT)){
                //请求的文件存在，开始从服务器下载文件
                final long fileSize = Long.parseLong(socket.receiveMessage());     //获取文件长度
                long receiveSize = 0;
                byte[] buffer = new byte[10240];
                //由于服务器与客户端的连接在文件传输完成后并不会关闭，
                //所以在文件内容下载完成后，receiveMessage函数会阻塞，而不是返回-1，
                //故这里利用接收到的内容长度与文件长度进行比较，确定文件是否下载完成。
                while (receiveSize < fileSize) {
                    int length = socket.receiveMessage(buffer);
                    if(length == -1){
                        //文件未下载完成是流出现中断，连接错误
                        throw new IOException("Connect failed.");
                    } else {
                        receiveSize += length;
                        writer.write(buffer, 0, length);
                    }
                }
            }
            try {
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                    ui.resetCursor();
                    ui.showInformationDialog("Download success.");
                });
        } catch (IOException e) {
            //发送给通信错误，恢复鼠标，断开连接
            SwingUtilities.invokeLater(() -> {
                    ui.resetCursor();
                    ui.showErrorDialog("Connect failed.");
                    ui.initConnectUI();
                });
            disconnect();
        }
    }
}
