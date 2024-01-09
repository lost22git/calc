package lost.calc.impl;

import lost.calc.api.Operator;
import lost.calc.api.Parser;
import lost.calc.api.Token;
import lost.calc.api.Tree;
import lost.calc.error.ParserError;

import java.util.ArrayDeque;
import java.util.EmptyStackException;
import java.util.function.Predicate;


public class DParser implements Parser {

  /**
   * cursor up to match fn
   *
   * @param stack stack
   * @param fn    fn
   */
  private void cursorUpTo(ArrayDeque<Tree> stack,
                          Predicate<Tree> fn) throws EmptyStackException {
    stack.pop();
    while (true) {
      var cursor = stack.peek();
      if (cursor == null) throw new EmptyStackException();
      if (fn.test(cursor)) return;
      stack.pop();
    }
  }

  @Override
  public Tree parse(Iterable<Token> tokens) {
    var result = new Tree.ParTree(Token.Slice.both(9999));
    var stack = new ArrayDeque<Tree>();
    stack.push(result);

    for (Token token : tokens) {
      var cursor = stack.peek();
      assert cursor != null;

      switch (token) {
        case null -> throw new AssertionError("token must be not null but a BUG 😡");
        case Token.NumberToken(double value, Token.Slice slice) -> {
          if (cursor.isCompleted()) throw new ParserError(token);
          var current = new Tree.NumberTree(value, slice);
          cursor.kids().addLast(current);
          stack.push(current);
        }
        case Token.IdentToken(String value, Token.Slice slice) -> {
          if (cursor.isCompleted()) throw new ParserError(token);
          var current = new Tree.VarTree(value, slice);
          cursor.kids().addLast(current);
          stack.push(current);
        }
        case Token.OperatorToken(Operator operator, Token.Slice slice) -> {
          var current = switch (operator) {
            case Not -> new Tree.PrefixTree(Operator.Not, slice);
            case Pow, Multi, Div, Plus, Minus, Mod, And, Or, Lt, Le, Gt, Ge, Eq, Ne ->
                    new Tree.InfixTree(operator, slice);
          };
          if (cursor.isCompleted()) {
            try {
              cursorUpTo(stack, c -> c instanceof Tree.ParTree
                                     || c instanceof Tree.CallTree
                                     || (c instanceof Tree.InfixTree i && i.value.order < operator.order)
                                     || (c instanceof Tree.PrefixTree p && p.value.order < operator.order));

            } catch (EmptyStackException e) {
              throw new ParserError(token);
            }
            cursor = stack.peek();
            assert cursor != null;
            current.kids().addLast(cursor.kids().removeLast());
            cursor.kids().addLast(current);
            stack.push(current);
          } else {
            if (current instanceof Tree.PrefixTree) {
              cursor.kids().addLast(current);
              stack.push(current);
            } else {
              throw new ParserError(token);
            }
          }
        }
        case Token.OpenToken(Token.Slice slice) -> {
          // VarTree + `(`  => CallTree
          if (cursor instanceof Tree.VarTree v) {
            var current = new Tree.CallTree(v.value, stack.pop().slice());
            cursor = stack.peek();
            assert cursor != null;
            cursor.kids().removeLast();
            cursor.kids().addLast(current);
            stack.push(current);
          } else {
            if (cursor.isCompleted()) throw new ParserError(token);
            var current = new Tree.ParTree(slice);
            cursor.kids().addLast(current);
            stack.push(current);
          }
        }
        case Token.CloseToken(Token.Slice _) -> {
          if (result == cursor)
            throw new ParserError(token);
          if (cursor.isCompleted()) {
            try {
              cursorUpTo(stack, c -> (c instanceof Tree.ParTree || c instanceof Tree.CallTree));
            } catch (EmptyStackException e) {
              throw new ParserError(token);
            }
            cursor = stack.peek();
            assert cursor != null;
            if (cursor == result)
              throw new ParserError(token);
            cursor.markCompleted();
          } else {
            if (cursor instanceof Tree.ParTree p && !p.kids().isEmpty()
                || cursor instanceof Tree.CallTree) {
              cursor.markCompleted();
            } else {
              throw new ParserError(token);
            }
          }
        }
        case Token.CommaToken(Token.Slice _) -> {
          if (cursor == result || !cursor.isCompleted())
            throw new ParserError(token);
          try {
            cursorUpTo(stack, c -> c instanceof Tree.CallTree);
          } catch (EmptyStackException e) {
            throw new ParserError(token);
          }
        }
      }
    }

    result.markCompleted();
    while (stack.peek() != null) {
      var cursor = stack.pop();
      if (!cursor.isCompleted()) {
        throw new ParserError(cursor);
      }
    }
    return result;
  }
}
