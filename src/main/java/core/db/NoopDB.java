package core.db;

public class NoopDB implements DB {
    @Override
    public Exception SetUp(Zone zone) {
        return null;
    }

    @Override
    public Exception TearDown(Zone zone) {
        return null;
    }

    @Override
    public String Name() {
        return "";
    }
}
