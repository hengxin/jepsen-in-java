package example.bank;

import core.client.Client;
import core.client.ClientCreator;
import core.db.Zone;

import java.util.ArrayList;

public class BankClientCreator implements ClientCreator {
    @Override
    public Client Create(Zone zone) {
        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(new Account(0, 200));
        accounts.add(new Account(1, 300));
        return new BankClient(3, accounts);     // TODO modify
    }
}
