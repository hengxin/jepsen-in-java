package example.oceanbase.read_write_client;

import core.client.Client;
import core.client.ClientCreator;
import core.db.Node;

public class ReadAndWriteClientCreator implements ClientCreator {

    @Override
    public Client Create(Node node) {
        return new ReadAndWriteClient(2000);
    }
}