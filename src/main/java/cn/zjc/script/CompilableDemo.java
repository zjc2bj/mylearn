package cn.zjc.script;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class CompilableDemo {
    public static void main(String[] args) throws Exception {
        test1();
        test2();
    }

    private static void test1() throws ScriptException {
        String script = " println (greeting); greeting= 'Good Afternoon!' ";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        engine.put("greeting", "Good Morning!");

        if (engine instanceof Compilable) {
            Compilable compilable = (Compilable) engine;
            CompiledScript compiledScript = compilable.compile(script);
            compiledScript.eval();
            compiledScript.eval();
        }
    }

    public static void test2() throws ScriptException, NoSuchMethodException {
        String script = " function greeting(message){println (message);}";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        engine.eval(script);

        if (engine instanceof Invocable) {
            Invocable invocable = (Invocable) engine;
            invocable.invokeFunction("greeting", "hi");
            // It may through NoSuchMethodException
            try {
                invocable.invokeFunction("nogreeing");
            } catch (NoSuchMethodException e) {
                // expected
            }
        }

    }
}
