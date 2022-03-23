package util;

import com.jcraft.jsch.*;
import core.db.Zone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static util.Constant.SSH_PORT;


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

    public static ArrayList<Integer> ShuffleByCount(int length) {
        // shuffle indices
        ArrayList<Integer> indices = new ArrayList<>();
        for(int i = 0; i < length; i++)
            indices.add(i);
        Collections.shuffle(indices);       // 从后往前用一个随机数做index进行swap
        return indices;
    }

    // executeQuery用于select
    public static String JDBCQuery(Zone zone, String sql, Function<ResultSet, String> handle) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(zone.getOceanBaseURL(), zone.getUsername(), zone.getPassword());
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            return handle.apply(rs);
        } catch(Exception e) {
            log.error(e.getMessage());
            return "";
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if(connection != null)
                    connection.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    // executeUpdate用于create, insert, delete, update
    public static int JDBCUpdate(Zone zone, String sql) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(zone.getOceanBaseURL(), zone.getUsername(), zone.getPassword());
            statement = connection.createStatement();
            return statement.executeUpdate(sql);
        } catch(Exception e) {
            log.error(e.getMessage());
            return 0;
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if(connection != null)
                    connection.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public static Exception ExecuteCommand(Zone zone, String command) {
        try {
            Session session = jsch.getSession(zone.getUsername(), zone.getIP(), SSH_PORT);
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
            Session session = jsch.getSession(zone.getUsername(), zone.getIP(), SSH_PORT);
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
