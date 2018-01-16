package cn.zjc.script;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

public class GroovyJSR223 {
	private static final ScriptEngine engine;

	static {
		ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName("groovy");
	}

	public static void main(String args[]) {
		try {
			String HelloLanguage = "1==1";
			Object result = engine.eval(HelloLanguage);
			System.err.println(result);
			
			HelloLanguage = "order.busiStatus == 10 && order.orderFlow == 20 && order.status == 20";
			CompiledScript compile = ((Compilable)engine).compile(HelloLanguage);
			
			Order order = new Order();
			//order.setBusiStatus(10);
			order.setOrderFlow(20);
			order.setRemark("haha");
			
			Bindings bindings = new SimpleBindings();
			bindings.put("order", order);
			result = compile.eval(bindings);
			System.err.println(result);
			
			System.out.println();
//			result = compile.eval("order.getRemark()");
//			System.err.println(result.equals("haha"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void demo1() {
		try {
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("groovy");
			String HelloLanguage = "def hello(language) {return \"Hello $language\"}";
			engine.eval(HelloLanguage);
			Invocable inv = (Invocable) engine;
			Object[] params = { new String("Groovy") };
			Object result = inv.invokeFunction("hello", params);
			assert result.equals("Hello Groovy");
			System.err.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
