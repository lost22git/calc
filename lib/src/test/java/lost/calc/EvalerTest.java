package lost.calc;

import lost.calc.api.Fn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvalerTest {
  @Test
  void eval() {
    var evaler = Evaler.create();
    var env = Evaler.createEnv();
    env
            .putVar("a", 1)
            .putVar("b", 2)
            .installFn(new Fn() {
              @Override
              public String name() {
                return "log10";
              }

              @Override
              public int paramCount() {
                return 1;
              }

              @Override
              public double call(double... params) {
                return Math.log10(params[0]);
              }
            });


    assertEquals(1, evaler.eval("a+b == b+a", env));
    assertEquals(1, evaler.eval("a+b >= b+a", env));
    assertEquals(0, evaler.eval("a+b > b+a", env));
    assertEquals(1, evaler.eval("(a+b)**2 == 9", env));
    assertEquals(1, evaler.eval("a/b == 0.5", env));
    assertEquals(1, evaler.eval("a%b == 1", env));
    assertEquals(1, evaler.eval("log10(100) == 2", env));
    assertEquals(1, evaler.eval("log10(a) * b  == 0", env));
  }
}
