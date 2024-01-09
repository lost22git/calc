package lost.calc.api;

public interface Env {

  Env putVar(String name,
              double value);

  boolean hasVar(String name);

  double getVar(String name);

  Env installFn(Fn fn);

  Env uninstallFn(Fn fn);

  Fn findFn(String name);

}

