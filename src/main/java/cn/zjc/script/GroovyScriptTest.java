package cn.zjc.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyScriptTest {
	private static Object runScript(String script, String[] split) {
		Binding binding = new Binding();
		binding.setVariable("r", split);
		GroovyShell shell = new GroovyShell(binding);
		return shell.evaluate(script);
	}
	
	private static void test1() {
		String line = "tom,2,2015-02-05,120,true,2.5";
		String script = "import cn.zjc.bean.Cat;Cat e = new Cat();e.name=r[0];e.age=r[1];e.birthday=cn.zjc.util.DateUtils.str2Date(r[2],\"yyyy-MM-dd\");e.price=Long.parseLong(r[3]);e.isalive=r[4];e.weight=Float.valueOf(r[5]);return e;";
		String[] split = line.split(",");

		Object value = runScript(script, split);
		System.out.println(value);
	}
	
	public static void main(String[] args) {
		Binding binding = new Binding();
		binding.setVariable("count", 3);
		GroovyShell shell = new GroovyShell(binding);
		System.out.println(shell.evaluate("'您已产生'+count+'条差评'"));
	}
}
