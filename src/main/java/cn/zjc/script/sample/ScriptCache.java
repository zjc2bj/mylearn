package cn.zjc.script.sample;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import groovy.lang.Script;

/**
 * 脚本缓存 和 执行
 * @author zhujunchao
 */
public class ScriptCache {
	private static ThreadLocal<Map<String,Object>> paramsLocal = new ThreadLocal<Map<String,Object>>();
	private static final Map<String, Script> countCache = Maps.newConcurrentMap();
	
	/**
	 * 字符串转脚本 并缓存
	 * @param scriptExpress
	 */
	public static void setScript(String scriptExpress) {
		if(StringUtils.isBlank(scriptExpress)) {
			return;
		}
		if(countCache.get(scriptExpress) == null) {
			Script script = ScriptParse.parse2Script(scriptExpress);
			countCache.put(scriptExpress, script);
		}
	}
	
	/**
	 * 根据字符串 获取对应脚本的执行结果
	 * <p>
	 * <b>调用前 需要调用{@link #setThreadLocalParams(Map)}设置参数</b>
	 * </p>
	 * @param scriptExpress
	 */
	public static Object getScriptValue(String scriptExpress) {
		if(StringUtils.isBlank(scriptExpress)) {
			return null;
		}
		Script script = countCache.get(scriptExpress);
		Map<String, Object> params = paramsLocal.get();
		return ScriptParse.invoke(script, params);
	}
	
	public static void setThreadLocalParams(Map<String,Object> params) {
		paramsLocal.set(params);
	}
	
	public static void removeThreadLocalParams() {
		paramsLocal.remove();;
	}
}
