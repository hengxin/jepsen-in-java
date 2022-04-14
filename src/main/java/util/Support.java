package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.*;
import core.client.Client;
import core.client.ClientInvokeResponse;
import core.db.Zone;
import core.record.ActionEnum;
import core.record.Operation;
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


    // 用户自定义的Operation的request类型传进来
    public static ArrayList<Operation> TxtToOperations(String filePath, Class requestClass) {
        ArrayList<Operation> operations = new ArrayList<>();
        String content = TxtToString(filePath);
        if(content.equals(""))
            return operations;
        String[] strings = content.split("\n");
        for(String str: strings) {
            Operation operation = JSONObject.parseObject(str, Operation.class);     // 在这里data会被反序列化成JsonObject类型
            if(operation.getAction() == ActionEnum.InvokeOperation)
                operation.setData(JSONObject.parseObject(JSON.toJSONString(operation.getData()), requestClass));
            else
                operation.setData(JSONObject.parseObject(JSON.toJSONString(operation.getData()), ClientInvokeResponse.class));
            operations.add(operation);
        }
        return operations;
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
    public static <T> T JDBCQueryWithZone(Zone zone, String sql, Function<ResultSet, T> handle) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(zone.getOceanBaseURL(), zone.getUsername(), zone.getPassword());
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            return handle.apply(rs);
        } catch(Exception e) {
            log.error(e.getMessage());
            return null;
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

    // TODO 合并一下
    // executeQuery用于select
    // Attention: maybe return is null!!!
    public static <T> T JDBCQueryWithClient(Client client, String selectSQL, Function<ResultSet, T> handle) {
        if(client.getConnection() == null)
            return null;
        Connection connection = client.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(selectSQL);
            return handle.apply(rs);
        } catch(Exception e) {
            log.error(e.getMessage());
            return null;
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    // executeUpdate用于create, insert, delete, update
    public static Exception JDBCUpdate(Client client, String allSQL) {
        if(client.getConnection() == null)
            return new Exception("Client's connection is null!");
        Connection connection = client.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for(String sql: allSQL.split(";"))
                statement.executeUpdate(sql + ";");
            return null;
        } catch(Exception e) {
            log.error(e.getMessage());
            return e;
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public static String ShellCommand(String shell_path, String args) {
        return "chmod u+x " + shell_path + "\n" + shell_path + " " + args;
    }

    public static Exception ExecuteCommand(Zone zone, String command) {
        try {
            Session session = jsch.getSession(zone.getUsername(), zone.getIp(), SSH_PORT);
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
            Session session = jsch.getSession(zone.getUsername(), zone.getIp(), SSH_PORT);
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
