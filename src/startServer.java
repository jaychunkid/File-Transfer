import server.FileTransferServer;

//运行服务器，ui在EDT线程中初始化，服务器主线程的ServerSocket阻塞在主线程
public class startServer {
    public static void main(String[] args) {
        FileTransferServer server = new FileTransferServer("document");
        if(args.length > 0) {
            //参数指定端口号
            server.startServer(Integer.parseInt(args[0]));
        } else {
            //默认端口号1907
            server.startServer(1907);
        }
    }
}
