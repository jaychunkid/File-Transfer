package server;

import common.Constant;
import common.StreamSocket;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//服务器逻辑类，需要刷新ui的操作使用SwingUtilities.invokeLater函数添加到EDT线程
public class FileTransferServer {
    private String directoryPath;      //提供下载的文件所在的目录
    private List<String> fileList;     //提供下载文件缓存

    public FileTransferServer(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    //缓存可供下载的文件名
    private void initDirectory() {
        File directory = new File(directoryPath);
        //目录不存在，则创建目录
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdir();
        }
        //互斥访问包装ArrayList对象
        fileList = Collections.synchronizedList(new ArrayList<>());
        String[] list = directory.list();
        if(list != null) {
            for (String name : list) {
                File file = new File(directoryPath + "\\" + name);
                //只缓存目录下文件，忽略所有子目录
                if (!file.isDirectory()) {
                    fileList.add(name);
                }
            }
        }
        //打印读取到的文件数目
        System.out.println("Inventory initiates successfully. Total " + fileList.size() + " files");
    }

    //绑定端口，监听连接
    public void startServer(int port) {
        initDirectory();
        System.out.println("Server started at " + port);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                StreamSocket socket = new StreamSocket(serverSocket.accept());
                System.out.println(socket.getIP() + ":" + socket.getPort() + "  " + "connected.");
                //启动一个子线程处理客户端的请求
                new Thread(new FileTransferRunnable(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Port has been occupy.");
        }
    }

    //负责处理客户端请求的线程
    private class FileTransferRunnable implements Runnable {
        private StreamSocket socket;     //与客户端连接的Socket

        FileTransferRunnable(StreamSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            handleConnect();
        }

        //分析客户端发送的请求，并进行相应处理
        private void handleConnect() {
            try {
                //循环读取请求信息
                while (true) {
                    String command = socket.receiveMessage();
                    boolean isDisconnect = false;
                    switch (command) {
                        case Constant.COMMAND_DISCONNECT:
                            isDisconnect = true;
                            break;
                        case Constant.COMMAND_GET_FILE_LIST:
                            sendFileDirectory();
                            break;
                        case Constant.COMMAND_TRANSFER:
                            upload();
                            break;
                        default:
                            break;
                    }
                    if (isDisconnect) {
                        break;
                    }
                }
                System.out.println(socket.getIP() + ":" + socket.getPort() + "  " + "disconnected.");
                socket.close();
            } catch (IOException e) {
                System.out.println(socket.getIP() + ":" + socket.getPort() + "  " + "disconnected.");
            }
        }

        //向客户端发送文件名缓存
        private void sendFileDirectory() {
            try {
                socket.sendMessage(Integer.toString(fileList.size()));
                //遍历ArrayList时需要做互斥访问处理
                synchronized (this) {
                    for (int i = 0; i < fileList.size(); ++i) {
                        socket.sendMessage(fileList.get(i));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //向客户端传输请求的文件
        private void upload() {
            FileInputStream reader = null;
            try{
                String fileName = socket.receiveMessage();
                File uploadFile = new File(directoryPath + "\\" + fileName);
                //检查请求文件是否存在
                if (uploadFile.exists() && uploadFile.isFile()) {
                    socket.sendMessage(Constant.MARK_FILE_EXISTENT);            //通知客户端文件存在
                    socket.sendMessage(Long.toString(uploadFile.length()));     //发送文件长度
                    //为了防止多个线程读取同一个文件，为代码块添加互斥限制
                    synchronized (this) {
                        reader = new FileInputStream(uploadFile);
                        byte[] buffer = new byte[10240];     //一次发送10KB的数据
                        int length = 0;
                        while ((length = reader.read(buffer)) != -1) {
                            socket.sendMessage(buffer, 0, length);
                        }
                    }
                    //打印更新请求成功信息
                    System.out.println(socket.getIP() + ":" + socket.getPort() + "  " +
                                    "request file <" + fileName + "> successfully.");
                } else {
                    socket.sendMessage(Constant.MARK_FILE_NONEXISTENT);     //通知客户端文件不存在
                    //打印更新请求失败信息
                    System.out.println(socket.getIP() + ":" + socket.getPort() + "  " +
                                    "request nonexistent file <" + fileName + ">.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        //解除对请求文件的占用
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

