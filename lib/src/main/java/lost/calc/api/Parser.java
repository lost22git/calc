package lost.calc.api;

public interface Parser {

  Tree parse(Iterable<Token> tokens);

}
