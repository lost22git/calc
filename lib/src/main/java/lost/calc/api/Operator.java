package lost.calc.api;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public enum Operator {
  // 算术运算符
  Pow("**", 8),
  Multi("*", 7),
  Div("/", 7),
  Plus("+", 6),
  Minus("-", 6),
  Mod("%", 5),

  // 逻辑运算符

  Not("!", 4),
  And("&&", 3),
  Or("||", 2),

  // 比较运算符
  Lt("<", 1),
  Le("<=", 1),
  Gt(">", 1),
  Ge(">=", 1),
  Eq("==", 1),
  Ne("!=", 1),

  ;


  public final String value;
  public final int order;

  private final static Map<String, Operator> map;
  public final static char[] CHARS;

  static {
    var all = Operator.values();
    map = new HashMap<>(all.length);
    for (var v : all) {
      map.put(v.value, v);
    }

    // get CHARS
    var set = new TreeSet<Character>();
    for (String k : map.keySet()) {
      for (char c : k.toCharArray()) {
        set.add(c);
      }
    }
    CHARS = new char[set.size()];
    int i = 0;
    for (Character c : set) {
      CHARS[i] = c;
      i++;
    }

  }

  public static Operator find(String value) {
    if (value == null || value.isBlank()) return null;
    return map.get(value);
  }


  Operator(String value,
           int order) {
    this.value = value;
    this.order = order;
  }

  @Override
  public String toString() {
    return STR."`\{this.value}`";
  }
}
