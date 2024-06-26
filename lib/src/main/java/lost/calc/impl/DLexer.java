package lost.calc.impl;

import lost.calc.api.Lexer;
import lost.calc.api.Operator;
import lost.calc.api.Token;
import lost.calc.api.Token.*;
import lost.calc.error.LexerError;

import java.util.Arrays;
import java.util.Iterator;

import static lost.calc.impl.DLexer.State.*;

public class DLexer implements Lexer {
  enum State {
    stInit,
    stInteger, stDouble,
    stIdent,
    stOperator
  }

  record StateData(State state, int start) {
    static final StateData INIT = new StateData(stInit, -1);
  }

  @Override
  public Iterable<Token> lex(String text) {
    return () -> new Iter(text);
  }

  static class Iter implements Iterator<Token> {
    final char[] chars;
    int pos = 0;
    StateData stateData = StateData.INIT;
    Token nextToken = null;

    Iter(String text) {
      chars = text.toCharArray();
    }

    @Override
    public boolean hasNext() {
      var token = parseNext();
      if (token == null) return false;
      this.nextToken = token;
      return true;
    }

    @Override
    public Token next() {
      return this.nextToken;
    }

    private Token parseNext() {
      Token result = null;
      int len = chars.length;
      while (result == null && pos <= len) {
        char c = (pos == len) ? ' ' : chars[pos]; // EOF

        // 空白字符
        if (Character.isWhitespace(c))
          result = tryUpdateStateAndGenerateToken(stInit, pos++);

          // 数字
        else if (Character.isDigit(c))
          result = switch (this.stateData.state) {
            case stDouble -> tryUpdateStateAndGenerateToken(stDouble, pos++);
            case stIdent -> tryUpdateStateAndGenerateToken(stIdent, pos++);
            default -> tryUpdateStateAndGenerateToken(stInteger, pos++);
          };

          // `.`
        else if (c == '.') {
          if (this.stateData.state == stInteger) {
            result = tryUpdateStateAndGenerateToken(stDouble, pos++);
          } else {
            throw new LexerError(STR."[\{pos}]: `.` must only be as decimal point of number 😡");
          }
        }

        // 字母
        else if (Character.isLetter(c))
          result = tryUpdateStateAndGenerateToken(stIdent, pos++);

          // 运算符
        else if (Arrays.binarySearch(Operator.CHARS, c) >= 0)
          result = tryUpdateStateAndGenerateToken(stOperator, pos++);

          // `(`
        else if (c == '(') {
          var t = tryUpdateStateAndGenerateToken(stInit, pos);
          result = (t != null) ? t : new OpenToken(Slice.both(pos++));
        }

        // `)`
        else if (c == ')') {
          var t = tryUpdateStateAndGenerateToken(stInit, pos);
          result = (t != null) ? t : new CloseToken(Slice.both(pos++));
        }

        // `,`
        else if (c == ',') {
          var t = tryUpdateStateAndGenerateToken(stInit, pos);
          result = (t != null) ? t : new CommaToken(Slice.both(pos++));
        }

        // 非法字符
        else throw new LexerError(STR."[\{pos}]: `\{c}` is not a valid token 😡");
      }

      return result;
    }

    private Token tryUpdateStateAndGenerateToken(State newState,
                                                 int pCurrent) {

      var stateDataToGenerateToken = updateState(newState, pCurrent);

      if (stateDataToGenerateToken == null) return null;

      return generateToken(pCurrent, stateDataToGenerateToken);
    }

    /**
     * update newState and return oldState to generate token
     *
     * @param newState the new state
     * @param pCurrent current pos
     * @return oldState
     */
    private StateData updateState(State newState,
                                  int pCurrent) {
      var currentStateData = this.stateData;

      if (currentStateData.state == newState) {
        return null;
      }

      // stInteger -> stDouble
      if (currentStateData.state == stInteger && newState == stDouble) {
        this.stateData = new StateData(stDouble, currentStateData.start);
        return null;
      }

      this.stateData = newState == stInit ? StateData.INIT : new StateData(newState, pCurrent);

      if (currentStateData.state == stInit) return null;

      return currentStateData;
    }

    private Token generateToken(int pEnd,
                                StateData stateData) {
      var tokenSlice = new Slice(stateData.start, pEnd - 1);
      var tokenString = new String(this.chars, tokenSlice.start(), tokenSlice.length());

      return switch (stateData.state) {
        case stInit -> null;
        case stInteger, stDouble -> {
          try {
            yield new NumberToken(Double.parseDouble(tokenString), tokenSlice);
          } catch (Exception e) {
            throw new LexerError(STR."\{tokenSlice}: `\{tokenString}` is not a valid number 😡");
          }
        }
        case stIdent -> new IdentToken(tokenString, tokenSlice);
        case stOperator -> {
          var operator = Operator.find(tokenString);
          if (operator == null)
            throw new LexerError(STR."\{tokenSlice}: `\{tokenString}` is not a valid operator 😡");
          yield new OperatorToken(operator, tokenSlice);
        }
      };
    }

  }
}
