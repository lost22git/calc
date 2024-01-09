package lost.calc.error;

import lost.calc.api.Token;
import lost.calc.api.Tree;

public class ParserError extends RuntimeException {
  public ParserError(String message) {
    super(message);
  }

  public ParserError(Token token) {
    this(STR."\{token} should not be here 😡");
  }

  public ParserError(Tree tree) {
    this(STR."\{tree} is not completed 😡");
  }

}
