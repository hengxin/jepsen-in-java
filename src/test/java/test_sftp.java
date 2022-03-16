import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import util.Constant;

import java.io.IOException;
import java.io.InputStream;

public class test_sftp {


    public static void main(String[] args) {
        try {
            String host="192.168.62.6";
            int port=22;
            String userName="root";
            String password="root";

            JSch jsch = new JSch();
            Session session = jsch.getSession(userName, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking","no");
            session.setTimeout(6000);
            session.connect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");

            sftp.connect();
            sftp.put("src/main/resources/mini-distributed-example.yaml", "/root", new SftpProgressMonitor() {
                private long transfered;
                @Override
                public void init(int op, String src, String dest, long max) {
                    System.out.println("Transferring begin.");
                }

                @Override
                public boolean count(long count) {
                    transfered = transfered + count;
                    System.out.println("Currently transferred total size: " + transfered + " bytes");
                    return true;
                }

                @Override
                public void end() {
                    System.out.println("Transferring done.");
                }
            }, ChannelSftp.OVERWRITE);
//            sftp.put("src/main/resources/mysql-community.repo", "/etc/yum.repos.d", new SftpProgressMonitor() {
//                private long transfered;
//                @Override
//                public void init(int op, String src, String dest, long max) {
//                    System.out.println("Transferring begin.");
//                }
//
//                @Override
//                public boolean count(long count) {
//                    transfered = transfered + count;
//                    System.out.println("Currently transferred total size: " + transfered + " bytes");
//                    return true;
//                }
//
//                @Override
//                public void end() {
//                    System.out.println("Transferring done.");
//                }
//            }, ChannelSftp.OVERWRITE);


            sftp.disconnect();

            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}