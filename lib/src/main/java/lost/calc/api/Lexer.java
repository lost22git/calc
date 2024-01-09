package lost.calc.api;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Lexer {


  Iterable<Token> lex(String text);

  default Stream<Token> tokenStream(String text) {
    return StreamSupport.stream(lex(text).spliterator(), false);
  }


}