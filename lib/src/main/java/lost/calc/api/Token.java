package lost.calc.api;

public sealed interface Token permits Token.NumberToken, Token.IdentToken, Token.OperatorToken, Token.OpenToken, Token.CloseToken, Token.CommaToken {

  record Slice(int start, int end) {
    public static Slice both(int n) {
      return new Slice(n, n);
    }

    public Slice {
      if (start < 0) {
        throw new IllegalArgumentException(STR."Slice: must be start >= 0, [start:\{start}]");
      }
      if (start > end) {
        throw new IllegalArgumentException(STR."Slice: must be start <= end, [start:\{start}, end:\{end}]");
      }
    }

    public int length() {
      return end - start + 1;
    }

    @Override
    public String toString() {
      return STR."[\{this.start},\{this.end}]";
    }
  }

  /**
   * 数字
   *
   * @param value
   */
  record NumberToken(double value, Slice slice) implements Token {
  }

  /**
   * 运算符
   *
   * @param value
   */
  record OperatorToken(Operator value, Slice slice) implements Token {
  }

  /**
   * 变量 or 函数名
   *
   * @param value
   */
  record IdentToken(String value, Slice slice) implements Token {
  }

  /**
   * `(` 左括号
   */
  record OpenToken(Slice slice) implements Token {

  }

  /**
   * `)` 右括号
   */
  record CloseToken(Slice slice) implements Token {

  }

  /**
   * `,` 逗号 函数参数分隔符
   */
  record CommaToken(Slice slice) implements Token {

  }

}


