import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class Test {
    public static void main(String[] args) {
        try {

            String host="192.168.62.4";
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
            InputStream in = exec.getInputStream();
            exec.setCommand("yum -y install wget");       // 注意此时进来是root用户 默认位置是/root
            exec.connect();
            String s = IOUtils.toString(in, "UTF-8");
            System.out.println("结果："+s);
            in.close();
            exec.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}