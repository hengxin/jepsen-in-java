import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;
import util.Constant;

import java.io.IOException;
import java.io.InputStream;

public class test_ssh {

    static public void SetCommands(ChannelExec exec){
//        exec.setCommand("grep \"password\" /var/log/mysqld.log");       // irVH6p4%g%kz
//
        String downloadDBCommand = Constant.TxtToString("src/main/resources/obd_mysql.txt");
        exec.setCommand(downloadDBCommand);
//
        exec.setCommand("echo 'skip_grant_tables' >> /etc/my.cnf\n" +
                "service mysqld restart\n" +
                "mysql -u root -e \"update mysql.user set authentication_string = password('root') where user = 'root'\"\n" +
                "mysql -u root -e \"update mysql.user set host = '%' where user = 'root'\"\n" +
                "mysql -u root -e \"update mysql.user set password_expired='N'\"\n" +
                "mysql -u root -e \"flush privileges\"\n" +
                "sed -i '$d' /etc/my.cnf\n" +
                "service mysqld restart\n" +
                "firewall-cmd --zone=public --add-port=3306/tcp --permanent\n" +
                "firewall-cmd --zone=public --add-port=2881/tcp --permanent\n" +
                "firewall-cmd --reload\n");
    }

    public static void main(String[] args) {
        try {

            String host="192.168.62.7";
//            String host="192.168.62.6";
//            String host="192.168.62.5";
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


            test_ssh.SetCommands(exec);
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