package lost.calc.api;

public interface Fn {

  String name();

  int paramCount();

  double call(double... params);
}
