package client;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

//客户端界面类
public class ClientUI {
    private FileTransferClient client;
    private JFrame frame;

    public ClientUI(FileTransferClient client){
        this.client = client;
        initConnectUI();
    }

    //连接界面的初始化
    void initConnectUI(){
        //当前界面不为空，则首先释放当前界面
        if(frame != null){
            frame.dispose();
        }
        frame = new JFrame("Connect");
        frame.setSize(230, 150);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //主面板
        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);

        //客户端host输入框与标签
        JLabel labelHost = new JLabel("host:");
        JTextField textFieldHost = new JTextField();
        labelHost.setBounds(10,10, 50, 20);
        textFieldHost.setBounds(55, 10, 150, 20);
        panel.add(labelHost);
        panel.add(textFieldHost);

        //客户端port输入框与标签
        JLabel labelPort = new JLabel("port:");
        JTextField textFieldPort = new JTextField();
        labelPort.setBounds(10, 35, 50, 20);
        textFieldPort.setBounds(55, 35, 150, 20);
        panel.add(labelPort);
        panel.add(textFieldPort);

        //启动连接的按钮
        JButton button = new JButton("Connect");
        button.setBounds(60, 70, 90, 20);
        button.addActionListener((e) -> {
                //获取用户输入的host、port
                String host = textFieldHost.getText();
                String port = textFieldPort.getText();
                //检查host、port是否为空
                if(!host.equals("") && !port.equals("")){
                    setBusyCursor();
                    new Thread(() -> client.connect(host.trim(), Integer.parseInt(port.trim()))).start();
                } else {
                    showErrorDialog("Host and port shouldn't be empty.");
                }
            });
        panel.add(button);

        frame.setVisible(true);
    }

    //文件下载界面初始化
    void initTransferUI(){
        //当前界面不为空，则首先释放当前界面
        if(frame != null){
            frame.dispose();
        }
        frame = new JFrame("File Transfer");
        frame.setSize(300, 400);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //主面板
        JPanel panel = new JPanel();
        panel.setLayout(null);     //控件使用setBounds函数设置大小和位置，主面板必须清除布局
        frame.add(panel);

        //下载和断开连接的按钮，下载按钮点击事件需要声明文件表后定义
        JButton confirm = new JButton("Download");
        JButton disconnect = new JButton("Disconnect");
        confirm.setEnabled(false);     //下载按钮在没有文件被选中的情况下，为不可用状态
        confirm.setBounds(170, 40, 100, 30);
        disconnect.setBounds(170, 80, 100, 30);
        disconnect.setEnabled(false);     //断开连接按钮在文件表加载完成前，为不可用状态
        disconnect.addActionListener((e) -> {
                new Thread(() -> client.disconnect()).start();
                initConnectUI();
            });
        panel.add(confirm);
        panel.add(disconnect);

        //文件表标签
        JLabel label = new JLabel("Files");
        label.setBounds(10, 5, 50, 20);
        panel.add(label);

        //文件表的滚动面板
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 30, 150, 300);
        panel.add(scrollPane);

        //文件表，文件名的获取操作通过SwingWorker类开启工作线程执行
        JList<String> fileNameList = new JList<>();
        new SwingWorker<List<String>, Void>(){
            //在工作线程中执行的操作
            @Override
            protected List<String> doInBackground() throws Exception {
                return client.getFileList();
            }
            //工作线程完成后被调用，在EDT中执行
            @Override
            protected void done() {
                try {
                    List<String> fileList = get();
                    String[] fileArray = fileList.toArray(new String[0]);
                    fileNameList.setListData(fileArray);
                    frame.repaint();     //更新ui显示内容
                } catch (Exception e) {
                    e.printStackTrace();
                }
                resetCursor();
                disconnect.setEnabled(true);
            }
        }.execute();
        fileNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);     //文件表设置为单选
        fileNameList.addListSelectionListener((e) -> confirm.setEnabled(true));
        scrollPane.setViewportView(fileNameList);

        confirm.addActionListener((e) -> {
                //文件选择框，选择下载文件保存路径
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showDialog(frame, null);
                File f = fileChooser.getSelectedFile();     //获取选择的目录
                //检查用户选择路径是否为空（按下“取消”键或直接关闭了文件选择框）
                if(f != null) {
                    File downloadFile = new File(f.toPath() + "\\" + fileNameList.getSelectedValue());
                    //检查保存路径是否存在同名文件
                    if(downloadFile.exists() && downloadFile.isFile()){
                        if(!showConfirmDialog("The file with this name already exist. Would you want to cover it?")){
                            return;
                        }
                    }
                    setBusyCursor();     //鼠标设置为忙碌状态
                    client.download(downloadFile, fileNameList.getSelectedValue());
                }
        });

        frame.setVisible(true);
        setBusyCursor();
    }

    //弹出错误信息的对话框
    void showErrorDialog(String errorMessage){
        JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //弹出普通信息的对话框
    void showInformationDialog(String message){
        JOptionPane.showMessageDialog(frame, message, null, JOptionPane.INFORMATION_MESSAGE);
    }

    //弹出确认信息的对话框
    boolean showConfirmDialog(String message){
        if(JOptionPane.showConfirmDialog(frame, message, "Confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
            return true;
        } else {
            return false;
        }
    }

    //鼠标图标设置为忙等状态
    void setBusyCursor(){
        frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    //鼠标图标恢复
    void resetCursor(){
        frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
