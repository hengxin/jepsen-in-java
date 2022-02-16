package core.client;

import core.db.Zone;

public class NoopClientCreator implements ClientCreator {
    private CreatorConfig creatorConfig;

    @Override
    public Client Create(Zone zone) {
        if(this.creatorConfig.getDbName().equals("OceanBase"))
            return new NoopClient();
        return null;
    }
}

// Below for example
class CreatorConfig {
    private String dbName = "OceanBase";

    public String getDbName() {
        return dbName;
    }
}