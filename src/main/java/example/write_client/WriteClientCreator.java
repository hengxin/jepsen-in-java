package example.write_client;

import core.client.Client;
import core.client.ClientCreator;
import core.db.Zone;

public class WriteClientCreator implements ClientCreator {

    @Override
    public Client Create(Zone zone) {
        return new WriteClient(Integer.parseInt(zone.getIp().split("\\.")[3]),2000);
    }
}