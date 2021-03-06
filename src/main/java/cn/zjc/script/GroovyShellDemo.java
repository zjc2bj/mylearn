package cn.zjc.script;

import com.alibaba.fastjson.JSON;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyShellDemo {
    public static void main(String args[]) {
//        test1();
//        test2("['a':2,'b':4]");
//        test2("['a',2,'b',4]");
        test3("{\"total\":{\"gender\":[{\"label\":\"男\",\"percent\":0.579},{\"label\":\"女\",\"percent\":0.421}]}}");
    }

    private static void test3(String stringMap) {
    	Binding binding = new Binding();
        binding.setVariable("stringMap", JSON.parseObject(stringMap));

        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate("stringMap.total.gender[0].percent;");
        System.out.println(value);
		
	}

	private static void test1() {
        Binding binding = new Binding();
        binding.setVariable("x", 10);
        binding.setVariable("language", "Groovy");

        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate("println \"Welcome to $language\"; y = x * 2; z = x * 3; return x ");

        System.err.println(value + ", " + value.equals(10));
        System.err.println(binding.getVariable("y") + ", " + binding.getVariable("y").equals(20));
        System.err.println(binding.getVariable("z") + ", " + binding.getVariable("z").equals(30));
    }

    // "['a':2,'b':4]"
    public static void test2(String stringMap) {
        Binding binding = new Binding();
        binding.setVariable("stringMap", stringMap);

        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate("return evaluate(stringMap);");
        System.out.println(value);
    }
}
