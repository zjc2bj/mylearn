package cn.zjc.script.sample;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class ScriptParse {
	private static Logger log = LoggerFactory.getLogger(ScriptParse.class);

	/**
	 * 将字符串转为脚本
	 * @param script
	 * @return
	 */
	public static Script parse2Script(String script) {
		if(StringUtils.isBlank(script)) {
			return null;
		}
		GroovyShell shell = new GroovyShell();
		Script parse = shell.parse(script);
		parse.setMetaClass(new ServiceMetaClass(parse.getClass()));
		return parse;

	}

	/**
	 * 执行脚本
	 * @param script 脚本
	 * @param params 脚本中的参数
	 * @return
	 */
	public static Object invoke(Script script, Map<String, Object> params) {
		if(script == null) {
			return null;
		}
		try {
			if (params != null && params.size() > 0) {
				Set<Entry<String, Object>> entrySet = params.entrySet();
				for (Entry<String, Object> entry : entrySet) {
					script.setProperty(entry.getKey(), entry.getValue());
				}
			}else {
				script.setBinding(new Binding());//没有参数 则清空 防止上次执行的参数 带入下次执行
			}
			Object run = script.run();
			return run;
		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String[] args) {
		
		Script parse3 = parse2Script("num.intdiv(60)");
		System.out.println(invoke(parse3, ImmutableMap.of("num", 301)));
		parse3 = parse2Script("(num * 100.0).toBigDecimal().setScale(1, BigDecimal.ROUND_HALF_UP)");
		System.out.println(invoke(parse3, ImmutableMap.of("num", 0.85)));
//		Script parse3 = parse2Script("queryCount(111)");
//		System.out.println(parse3.run());
		
//		Script parse3 = parse2Script("您已产生 ${count} 条差评");
//		parse3.setProperty("count", "3");
//		System.out.println(parse3.evaluate("'您已产生'+ count +'条差评'"));
//		
//		parse3 = parse2Script("'您已差评'");
//		System.out.println(parse3.run());
		
//		Script parse2 = parse2Script("if(80>=10) '良好' else '及格'");
//		System.out.println(parse2.run());
//		
//		Script parse = parse2Script("order.busiStatus == 10");
//
//		Map<String, Object> params = ImmutableMap.of("orderStatus", 10, "orderFlow", 20, "busiStatus", "10");
//		invoke(parse, ImmutableMap.of("order", params));
//
//		params = ImmutableMap.of("orderStatus", 30, "orderFlow", 20);
//		invoke(parse, ImmutableMap.of("order", params));
//
//		String paramsStr = "com.google.common.collect.ImmutableMap.of('userId', order.userId, 'nickname', order.name)";
//		Script paramsScript = parse2Script(paramsStr);
//		Object invoke = invoke(paramsScript,
//				ImmutableMap.of("order", ImmutableMap.of("userId", 83494234523l, "name", "zxj")));
//		System.out.println(invoke);
//
//		paramsStr = "buttonParams";
//		paramsScript = parse2Script(paramsStr);
//		invoke = invoke(paramsScript, ImmutableMap.of("buttonParams", ImmutableMap.of("userId", 83494234523l, "name", "zxj", "appcode", "101")));
//		System.out.println(invoke);
//		
//		String openIdStr = "order.wxOpenId != null && !order.wxOpenId.equals('')";
//		Script openIdScript = parse2Script(openIdStr);
//		Object value = invoke(openIdScript,
//				ImmutableMap.of("order", ImmutableMap.of("wxOpenId", null, "name", "zxj")));
//		System.out.println("======="+value);
	}
}
