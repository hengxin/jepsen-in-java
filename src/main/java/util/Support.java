package util;

import com.jcraft.jsch.*;
import core.db.Zone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Slf4j
public class Support {

    private static JSch jsch = new JSch();

    public static String TxtToString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);
            StringBuilder result  = new StringBuilder();
            for(String line: lines) {
                if(!line.equals(""))
                    result.append(line).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return "";
        }
    }

    public static Exception ExecuteCommand(Zone zone, String command) {
        try {
            Session session = jsch.getSession(zone.getUsername(), zone.getIP(), Constant.SSH_PORT);
            session.setPassword(zone.getPassword());
            session.setConfig("StrictHostKeyChecking","no");
            session.setTimeout(6000);
            session.connect();

            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            InputStream in = exec.getInputStream();
            exec.setCommand(command);       // 默认位置是/$username
            exec.connect();

            String s = IOUtils.toString(in, "UTF-8");
            log.info("执行命令：" + command + " 结果："+s);

            in.close();
            exec.disconnect();
            session.disconnect();
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return e;
        }
    }

    public static Exception SendFile(Zone zone, String srcPath, String destPath) {
        try {
            Session session = jsch.getSession(zone.getUsername(), zone.getIP(), Constant.SSH_PORT);
            session.setPassword(zone.getPassword());
            session.setConfig("StrictHostKeyChecking","no");
            session.setTimeout(6000);
            session.connect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");

            sftp.connect();
            sftp.put(srcPath, destPath, new MySftpProgressMonitor(), ChannelSftp.OVERWRITE);

            sftp.disconnect();
            session.disconnect();
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return e;
        }
    }

    private static class MySftpProgressMonitor implements SftpProgressMonitor {
        private long transferred;
        @Override
        public void init(int op, String src, String dest, long max) {
            log.info("Transferring begin.");
        }

        @Override
        public boolean count(long count) {
            transferred = transferred + count;
            log.info("Currently transferred total size: " + transferred + " bytes");
            return true;
        }

        @Override
        public void end() {
            log.info("Transferring done.");
        }
    }
}
