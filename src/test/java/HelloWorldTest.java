import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldTest {

    @Test
    public void testMessage() {
        HelloWorld obj = new HelloWorld();
        assertEquals("Hello Jenkins SonarQube", obj.message());
    }
}
