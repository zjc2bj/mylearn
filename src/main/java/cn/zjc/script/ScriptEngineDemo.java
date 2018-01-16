package cn.zjc.script;

import java.util.Date;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

// 需要提醒的是,在groovy中,${expression} 将会被认为一个变量,如果需要输出"$"符号,需要转义为"\$".   

public class ScriptEngineDemo {
    public static void evalScript() throws Exception {
        ScriptEngineManager factory = new ScriptEngineManager();
        // 每次生成一个engine实例
        ScriptEngine engine = factory.getEngineByName("groovy");
        System.out.println(engine.toString());
        assert engine != null;
        // javax.script.Bindings
        Bindings binding = engine.createBindings();
        binding.put("date", new Date());
        // 如果script文本来自文件,请首先获取文件内容
        engine.eval("def getTime(){return date.getTime();}", binding);
        engine.eval("def sayHello(name,age){return 'Hello,I am ' + name + ',age' + age;}");
        Long time = (Long) ((Invocable) engine).invokeFunction("getTime", null);
        System.out.println(time);
        String message = (String) ((Invocable) engine).invokeFunction("sayHello", "zhangsan", new Integer(12));
        System.out.println(message);
    }

}
