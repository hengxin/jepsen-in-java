package example.write_client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RWRequest {
    String action;
    String name;
    int value;
}