package lost.calc.impl;

import lost.calc.api.Env;
import lost.calc.api.Fn;

import java.util.HashMap;
import java.util.Map;

public class DEnv implements Env {
  private final Map<String, Double> vars = new HashMap<>();
  private final Map<String, Fn> fns = new HashMap<>();

  @Override
  public Env putVar(String name,
                     double value) {
    this.vars.put(name, value);
    return this;
  }

  @Override
  public boolean hasVar(String name) {
    return this.vars.containsKey(name);
  }

  @Override
  public double getVar(String name) {
    return this.vars.get(name);
  }

  @Override
  public Env installFn(Fn fn) {
    fns.put(fn.name(), fn);
    return this;
  }

  @Override
  public Env uninstallFn(Fn fn) {
    fns.remove(fn.name());
    return this;
  }

  @Override
  public Fn findFn(String name) {
    return fns.get(name);
  }
}
