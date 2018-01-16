最近设计一个数据统计系统,系统中上百种数据统计维度,而且这些数据统计的指标可能随时会调整.
如果基于java编码的方式逐个实现数据统计的API设计,工作量大而且维护起来成本较高;
最终确定为将"数据统计"的计算部分单独分离成脚本文件(javascript,或者Groovy),非常便捷了实现了"数据统计Task" 与 "数据统计规则(计算)"解耦,
且可以动态的加载和运行的能力.顺便对JAVA嵌入运行Groovy脚本做个备忘.

Java中运行Groovy,有三种比较常用的类支持:GroovyShell,GroovyClassLoader以及Java-Script引擎(JSR-223).

1) GroovyShell: 通常用来运行"script片段"或者一些零散的表达式(Expression)

2) GroovyClassLoader: 如果脚本是一个完整的文件,特别是有API类型的时候,比如有类似于JAVA的接口,面向对象设计时,通常使用GroovyClassLoader.

3) ScriptEngine: JSR-223应该是推荐的一种使用策略.规范化,而且简便.


关于groovy更多调用 参考http://shift-alt-ctrl.iteye.com/blog/1896690
