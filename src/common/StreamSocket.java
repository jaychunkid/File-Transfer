package common;

import java.io.*;
import java.net.Socket;

//封装流式Socket
public class StreamSocket {
    private Socket socket;
    //为了能够按字节读写，使用DataOutputStream和DataInputStream包装输入、输出流
    private DataOutputStream os;     //向流中写入信息
    private DataInputStream is;      //从流中读取信息
    private String ip;               //记录连接方ip地址
    private int port;                //记录连接方端口号

    public StreamSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        ip = host;
        this.port = port;
        setStreams();
    }

    public StreamSocket(Socket socket) throws IOException {
        this.socket = socket;
        ip = socket.getInetAddress().toString();
        port = socket.getPort();
        setStreams();
    }

    private void setStreams() throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    public void sendMessage(String message) throws IOException {
        os.writeUTF(message);
        os.flush();
    }

    public void sendMessage(byte[] buffer, int offset, int length) throws IOException {
        os.write(buffer, offset, length);
        os.flush();
    }

    public String receiveMessage() throws IOException {
        return is.readUTF();
    }

    public int receiveMessage(byte[] buffer) throws IOException {
        return is.read(buffer);
    }

    public String getIP(){
        return ip;
    }

    public int getPort() { return port; }

    public void close() throws IOException {
        is.close();
        os.close();
        socket.close();
    }
}
