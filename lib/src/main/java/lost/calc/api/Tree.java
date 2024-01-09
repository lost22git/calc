package lost.calc.api;

import lost.calc.error.EvalerError;

import java.util.ArrayDeque;

public sealed interface Tree permits Tree.TreeBase {
  Token.Slice slice();

  ArrayDeque<Tree> kids();

  boolean isCompleted();

  void markCompleted();

  double eval(Env env);

  default String showCompleted() {
    return isCompleted() ? "😊" : "😡";
  }

  default String dump(int indent) {
    var result = new StringBuilder();
    dump(this, result, 0, indent);
    return result.toString();
  }

  private static void dump(Tree tree,
                           StringBuilder sb,
                           int level,
                           int indent) {
    sb.append(STR."\{" ".repeat(indent * level)}\{tree} {\n");
    for (Tree kid : tree.kids()) {
      dump(kid, sb, level + 1, indent);
    }
    sb.append(STR."\{" ".repeat(indent * level)}}\n");
  }

  abstract sealed class TreeBase implements Tree permits NumberTree, VarTree, CallTree, ParTree, PrefixTree, InfixTree, PostfixTree {
    private final ArrayDeque<Tree> kids = new ArrayDeque<>();
    private boolean completed;
    private final Token.Slice slice;

    public TreeBase(Token.Slice slice) {
      this.slice = slice;
    }

    @Override
    public Token.Slice slice() {
      return this.slice;
    }

    @Override
    public ArrayDeque<Tree> kids() {
      return this.kids;
    }

    @Override
    public boolean isCompleted() {
      return this.completed;
    }

    @Override
    public void markCompleted() {
      this.completed = true;
    }
  }

  /**
   * 数字
   */
  final class NumberTree extends TreeBase {
    public final double value;

    public NumberTree(double value,
                      Token.Slice slice) {
      super(slice);
      super.markCompleted();
      this.value = value;
    }

    @Override
    public String toString() {
      return STR."NumberTree(\{this.value}) \{this.slice()} \{this.showCompleted()}";
    }

    @Override
    public double eval(Env env) {
      return this.value;
    }
  }

  /**
   * 变量
   */
  final class VarTree extends TreeBase {
    public final String value;

    public VarTree(String value,
                   Token.Slice slice) {
      super(slice);
      super.markCompleted();
      this.value = value;
    }

    @Override
    public String toString() {
      return STR."VarTree(\{this.value}) \{this.slice()} \{this.showCompleted()}";
    }

    @Override
    public double eval(Env env) {
      if (env.hasVar(this.value))
        return env.getVar(this.value);
      throw new EvalerError(STR."\{this} not found from env 😡");
    }
  }

  /**
   * 函数调用
   */
  final class CallTree extends TreeBase {
    public final String value;

    public CallTree(String value,
                    Token.Slice slice) {
      super(slice);
      this.value = value;
    }

    @Override
    public String toString() {
      return STR."CallTree(\{this.value}) \{this.slice()} \{this.showCompleted()}";
    }

    @Override
    public double eval(Env env) {
      var fn = env.findFn(this.value);
      if (fn == null)
        throw new EvalerError(STR."\{this} not found from env 😡");
      var pCount = fn.paramCount();
      if (this.kids().size() != pCount)
        throw new EvalerError(STR."\{this} params count not match 😡");
      var params = new double[pCount];
      int i = 0;
      for (Tree kid : this.kids()) {
        params[i++] = kid.eval(env);
      }
      return fn.call(params);
    }
  }

  /**
   * 前缀表达式
   */
  final class PrefixTree extends TreeBase {
    public final Operator value;

    public PrefixTree(Operator value,
                      Token.Slice slice) {
      super(slice);
      this.value = value;
    }

    @Override
    public boolean isCompleted() {
      boolean result = super.isCompleted();
      if (!result && kids().size() == 1) {
        result = true;
        super.markCompleted();
      }
      return result;
    }

    @Override
    public double eval(Env env) {
      if (this.kids().size() != 1)
        throw new EvalerError(STR."\{this} params count not match 😡");
      return switch (this.value) {
        case Not -> this.kids().getFirst().eval(env) == 0 ? 1 : 0;
        default -> throw new EvalerError(STR."\{this} is not a prefix-operator 😡");
      };
    }

    @Override
    public String toString() {
      return STR."PrefixTree(\{this.value}) \{this.slice()} \{this.showCompleted()}";
    }
  }

  /**
   * 中缀表达式
   */
  final class InfixTree extends TreeBase {
    public final Operator value;

    public InfixTree(Operator value,
                     Token.Slice slice) {
      super(slice);
      this.value = value;
    }

    @Override
    public boolean isCompleted() {
      boolean result = super.isCompleted();
      if (!result && kids().size() == 2) {
        result = true;
        super.markCompleted();
      }
      return result;
    }

    private static Double toBool(double v) {
      return (double) (v == 0 ? 0 : 1);
    }

    @Override
    public double eval(Env env) {
      if (this.kids().size() != 2)
        throw new EvalerError(STR."\{this} params count not match 😡");
      var iterator = this.kids().iterator();
      var lhs = iterator.next().eval(env);
      var rhs = iterator.next().eval(env);
      return switch (this.value) {
        case Pow -> Math.pow(lhs, rhs);
        case Multi -> lhs * rhs;
        case Div -> lhs / rhs;
        case Plus -> lhs + rhs;
        case Minus -> lhs - rhs;
        case Mod -> lhs % rhs;
        case And -> toBool(lhs).intValue() & toBool(rhs).intValue();
        case Or -> toBool(lhs).intValue() | toBool(rhs).intValue();
        case Lt -> lhs < rhs ? 1 : 0;
        case Le -> lhs <= rhs ? 1 : 0;
        case Gt -> lhs > rhs ? 1 : 0;
        case Ge -> lhs >= rhs ? 1 : 0;
        case Eq -> lhs == rhs ? 1 : 0;
        case Ne -> lhs != rhs ? 1 : 0;
        default -> throw new EvalerError(STR."\{this} is not a infix-operator 😡");
      };
    }

    @Override
    public String toString() {
      return STR."InfixTree(\{this.value}) \{this.slice()} \{this.showCompleted()}";
    }
  }

  /**
   * 后缀表达式
   */
  final class PostfixTree extends TreeBase {
    public final Operator value;

    public PostfixTree(Operator value,
                       Token.Slice slice) {
      super(slice);
      this.value = value;
    }

    @Override
    public boolean isCompleted() {
      boolean result = super.isCompleted();
      if (!result && kids().size() == 1) {
        result = true;
        super.markCompleted();
      }
      return result;
    }

    @Override
    public double eval(Env env) {
      throw new EvalerError(STR."\{this} is not a post-operator 😡");
    }

    @Override
    public String toString() {
      return STR."PostfixTree(\{this.value}) \{this.slice()} \{this.showCompleted()}";
    }
  }

  /**
   * 括号
   */
  final class ParTree extends TreeBase {

    public ParTree(Token.Slice slice) {
      super(slice);
    }

    @Override
    public String toString() {
      return STR."ParTree() \{this.slice()} \{this.showCompleted()}";
    }

    @Override
    public double eval(Env env) {
      if (this.kids().size() != 1)
        throw new EvalerError(STR."\{this} params count not match 😡");
      return this.kids().getFirst().eval(env);
    }
  }
}
