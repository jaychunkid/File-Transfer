## 文件传输
分布式计算课程实验，通过socket方式实现一个基于C/S模式的文件传输程序。
其中，服务器端读取document目录下的文件，之后监听客户请求，读取文件并向客户端发送文件。
客户端首先需要输入服务器的IP地址和端口号以连接服务器，之后从服务器获取可下载文件列表，选择文件名，指定下载目录即可下载文件。
* `StreamSocket`：封装了流式socket的一些基本操作
* `FileTransferServer`：服务器功能实现
* `FileTransferClient`：客户端功能实现
* `ClientUI`：客户端界面实现，基于swing
* `Constant`：服务器与客户端的通信格式
### 程序使用
* `服务器`：运行startServer下的main方法启动服务器，可以通过执行文件时传入参数，指定服务器启动的端口号，否则默认在1907端口上启动。
启动时读取document目录下文件，并输出文件数目和启动的端口号。
服务器仅在启动时读取文件目录，不支持实时更新文件列表。
* `客户端`：运行startClient下的main方法启动客户端，在登录界面中输入服务器的IP地址和端口号，连接成功即可进入文件传输界面。
