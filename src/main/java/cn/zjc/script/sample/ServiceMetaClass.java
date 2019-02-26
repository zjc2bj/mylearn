package cn.zjc.script.sample;

import java.util.Date;


import groovy.lang.DelegatingMetaClass;

public class ServiceMetaClass extends DelegatingMetaClass {
	
	public ServiceMetaClass(Class theClass) {
		super(theClass);
		initialize();
	}
	
	public Object invokeMethod(Class sender, Object a_object,
			String a_methodName, Object[] a_arguments, boolean isCallToSuper,
			boolean fromInsideClass) {
		if (!isCallToSuper)
			return invokeMethod(a_object, a_methodName, a_arguments);
		else
			return delegate.invokeMethod(sender, a_object, a_methodName,
					a_arguments, isCallToSuper, fromInsideClass);
	}

	
	public Object invokeMethod(Object a_object, String a_methodName,
			Object[] a_arguments) {
		Object o = internalInvoke(a_object, a_methodName, a_arguments);
		if (o == null) {
			return super.invokeMethod(a_object, a_methodName, a_arguments);
		} else
			return o;
	}

	public Object invokeStaticMethod(Object object, String methodName,
			Object[] arguments) {
		Object o = internalInvoke(null, methodName, arguments);
		if (o == null) {
			return delegate.invokeStaticMethod(object, methodName, arguments);
		} else
			return o;
	}
	
	protected Object internalInvoke(Object a_object, String a_methodName,
			Object[] a_arguments) {
		if (a_methodName.equals("toInt")) {
//			return MathUtils.toInt(a_arguments[0]);
		}
		if (a_methodName.equals("toDefRate")) {
//			return MathUtils.toDefRate((Double) a_arguments[0]);
		}
		if (a_methodName.equals("toDubbo")) {
//			return MathUtils.toDubbo(a_arguments[0]);
		}
		if (a_methodName.equals("removePoint0")) {
//			return MathUtils.removePoint0((String) a_arguments[0]);
		}

		return null;
	}
}
