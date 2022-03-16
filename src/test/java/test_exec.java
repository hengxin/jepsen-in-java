import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;
import util.Constant;

import java.io.InputStream;

public class test_exec {

    public static void main(String[] args) {
        String[] hosts = {"192.168.62.6", "192.168.62.7", "192.168.62.8", "192.168.62.9"};
        String[] obcontrol = {"192.168.62.6"};
        String[] observers = {"192.168.62.7", "192.168.62.8", "192.168.62.9"};
        String[] test_server = {"192.168.62.9"};
        for(String host: test_server) {
//        for(String host: hosts) {
//        for(String host: observers) {
            try {
                int port=22;
                String userName="root";
                String password="root";

                JSch jsch = new JSch();
                Session session = jsch.getSession(userName, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking","no");
                session.setTimeout(6000);
                session.connect();
                //建立连接结束
                //发送指令
                ChannelExec exec = (ChannelExec) session.openChannel("exec");
                InputStream rightStream = exec.getInputStream();
                InputStream wrongStream = exec.getErrStream();


//                String command = Constant.TxtToString("src/main/resources/centos8_mysql.txt");

                String command = "kill -CONT 5700";
//                String command = "systemctl restart chronyd.service && chronyc tracking";
//                String command = "timedatectl set-ntp true\n" +
//                        "chronyc tracking";
                exec.setCommand(command);
                exec.connect();

                String s1 = IOUtils.toString(rightStream, "UTF-8");
                System.out.println("正确结果："+s1);
                String s2 = IOUtils.toString(wrongStream, "UTF-8");
                System.out.println("错误结果："+s2);
                rightStream.close();
                wrongStream.close();
                exec.disconnect();


                session.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}