package lost.calc;

import lost.calc.api.Env;
import lost.calc.api.Lexer;
import lost.calc.api.Parser;
import lost.calc.impl.DEnv;
import lost.calc.impl.DLexer;
import lost.calc.impl.DParser;

public class Evaler {
  private final Lexer lexer;
  private final Parser parser;

  public Evaler(Lexer lexer,
                Parser parser) {
    this.lexer = lexer;
    this.parser = parser;
  }

  public static Evaler create() {
    return new Evaler(new DLexer(), new DParser());
  }

  public static Env createEnv() {
    return new DEnv();
  }

  public double eval(String text,
                     Env env) {
    return parser.parse(lexer.lex(text)).eval(env);
  }

}
