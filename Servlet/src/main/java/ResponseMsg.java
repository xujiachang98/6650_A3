import lombok.Getter;
import lombok.Setter;
public class ResponseMsg {
    @Setter
    @Getter
    private String message;

    public ResponseMsg(String message) {
    }
}