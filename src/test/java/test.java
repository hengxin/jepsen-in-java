import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class test {
    public static void main(String[] args) {
        try {
            Path path = Paths.get("src/main/resources/obd_mysql.txt");
            List<String> lines = Files.readAllLines(path);
            String result  = "";
            for(String line: lines) {
                if(!line.equals(""))
                    result += (line + "\n");
            }
            System.out.println(result);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
