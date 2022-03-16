package example;

import core.client.Client;

import javax.xml.transform.sax.SAXSource;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteClient extends Client {

    int sequence;

    public WriteClient(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public Exception Start() {
        Statement statement = null;
        try {
            statement = this.getConnection().createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS t1 (c1 VARCHAR(50) primary key);";
            statement.execute(createTableSQL);
            for(int i = 0; i < 300; i++){
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String value = "IP." + sequence + " " + dateFormat.format(date);
                String writeSQL = String.format("INSERT INTO t1 VALUES(\"%s\");", value);
                statement.execute(writeSQL);
                System.out.println("Successfully add " + value);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        } finally {
            try {
                this.getConnection().close();
                if(statement != null)
                    statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
