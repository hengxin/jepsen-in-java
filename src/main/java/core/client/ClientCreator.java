package core.client;

import core.db.Node;

public interface ClientCreator {

    // 可根据creatorConfig来生成client
    Client Create(Node node);
}