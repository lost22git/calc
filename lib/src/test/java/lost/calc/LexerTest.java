package lost.calc;

import lost.calc.error.LexerError;
import lost.calc.impl.DLexer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class LexerTest {

  @Test
  void lex() {
    var lexer = new DLexer();

    System.out.println("-".repeat(33));
    var expr = "a**2 + 2*a*B + B**2 == (a+B)**2";
    System.out.println(STR."expr = \{expr}");
    lexer.lex(expr).forEach(System.out::println);
    System.out.println("-".repeat(33));

    expr = "log(2) + log(3) > log(5)";
    System.out.println(STR."expr = \{expr}");
    lexer.lex(expr).forEach(System.out::println);
    System.out.println("-".repeat(33));

    expr = "1.1 * 100 == 110";
    System.out.println(STR."expr = \{expr}");
    lexer.lex(expr).forEach(System.out::println);
    System.out.println("-".repeat(33));

    expr = "10 % 3 == 1";
    System.out.println(STR."expr = \{expr}");
    lexer.lex(expr).forEach(System.out::println);
    System.out.println("-".repeat(33));
  }

  @Test
  void lexError() {
    var lexer = new DLexer();

    assertThrows(LexerError.class, () -> lexer.tokenStream("100_").toList());

    assertThrows(LexerError.class, () -> lexer.tokenStream(">>>").toList());

    assertThrows(LexerError.class, () -> lexer.tokenStream("2.2.2").toList());

    assertThrows(LexerError.class, () -> lexer.tokenStream(".2").toList());

  }

}
