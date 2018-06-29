package common;

//用于双方通信的信息
//为了不和文件数目信息重叠，不使用枚举类
//为了不和文件名重叠，使用只有后缀名的类文件名格式
public class Constant {
    //客户端请求信息
    public static final String COMMAND_GET_FILE_LIST = ".GET_FILE_LIST";
    public static final String COMMAND_DISCONNECT = ".DISCONNECT";
    public static final String COMMAND_TRANSFER = ".TRANSFER";
    //服务器应答信息
    public static final String MARK_FILE_NONEXISTENT = ".FILE_NONEXISTENT";
    public static final String MARK_FILE_EXISTENT = ".FILE_EXISTENT";
}
