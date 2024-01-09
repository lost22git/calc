package lost.calc;

import lost.calc.api.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SliceTest {

  @Test
  void mustBeStartLeEnd() {
    assertThrows(IllegalArgumentException.class, () -> new Token.Slice(-1, 0));
    assertThrows(IllegalArgumentException.class, () -> new Token.Slice(1, 0));
  }

}
