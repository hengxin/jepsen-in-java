package core.client;

import core.db.Zone;

public interface ClientCreator {

    // 可根据creatorConfig来生成client
    Client Create(Zone zone);
}