package example.write_client;

import core.client.Client;
import core.client.ClientCreator;
import core.db.Zone;

public class ReadAndWriteClientCreator implements ClientCreator {

    @Override
    public Client Create(Zone zone) {
        return new ReadAndWriteClient(2000);
    }
}