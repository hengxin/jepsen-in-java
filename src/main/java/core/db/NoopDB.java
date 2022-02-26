package core.db;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;
import util.Constant;

import java.io.InputStream;

public class NoopDB implements DB {
    @Override
    public Exception SetUp(Zone zone) {
        System.out.println("Set up NoopDB in" + zone.getIP());
        String downloadDBCommand = "";      // TODO &&连接 maybe提前下wget,mysql等库
        return ExecuteCommand(zone, downloadDBCommand);
    }

    @Override
    public Exception TearDown(Zone zone) {
        return null;
    }

    @Override
    public String Name() {
        return "";
    }

    private Exception ExecuteCommand(Zone zone, String command) {
        try {
            JSch jsch = new JSch();
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
            System.out.println("结果："+s);

            in.close();
            exec.disconnect();
            session.disconnect();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }
}
