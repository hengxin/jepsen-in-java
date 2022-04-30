package example.cassandra.read_write_client;

import core.client.Client;
import core.client.ClientCreator;
import core.db.Node;

public class ReadAndWriteDoubleClientCreator implements ClientCreator {
    @Override
    public Client Create(Node node) {
        return new ReadAndWriteDoubleClient(1000);
    }
}
