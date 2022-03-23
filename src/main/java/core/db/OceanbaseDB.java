package core.db;

import lombok.extern.slf4j.Slf4j;



@Slf4j
public class OceanbaseDB implements DB {
    @Override
    public Exception SetUp(Zone zone) {
        log.info("Set up OceanbaseDB in" + zone.getIP());
//        String downloadDBCommand = Support.TxtToString("src/main/resources/configure_obcontrol.txt");
//        // TODO 怎么把输出搞成实时的会提高用户体验 不然会执行很久没反应
//        // TODO 换成shell脚本
        // TODO add
//        return Constant.ExecuteCommand(zone, downloadDBCommand);
        return null;
    }

    @Override
    public Exception TearDown(Zone zone) {
        return null;
    }

    @Override
    public String Name() {
        return "Oceanbase";
    }
}
