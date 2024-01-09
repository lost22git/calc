package lost.calc;

import lost.calc.error.ParserError;
import lost.calc.impl.DLexer;
import lost.calc.impl.DParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserTest {

  void doParse(String expr) {
    var parser = new DParser();
    var lexer = new DLexer();

    System.out.println(STR."expr = \{expr}");
    var tree = parser.parse(lexer.lex(expr));
    var s = tree.dump(2);
    System.out.println(s);
  }

  @Test
  void parse() {
    String[] expr = {
            "a**2 + 2*a*B + B**2 == (a+B)**2",
            "log(aa+bb)*cc+(dd)"
    };
    for (String s : expr) {
      doParse(s);
    }
  }

  @Test
  void parseError() {
    String[] expr = {
            "(log(a+b)",
            "a+b+()",
            "a+b(",
            "a,b",
            "log(a,,)"
    };
    for (String s : expr) {
      assertThrows(ParserError.class, () -> doParse(s));
    }
  }
}
