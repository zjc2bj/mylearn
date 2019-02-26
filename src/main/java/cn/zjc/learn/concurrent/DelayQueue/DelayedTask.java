package cn.zjc.learn.concurrent.DelayQueue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedTask implements Delayed{
	public String name;
    public Long delayTime;
    public TimeUnit delayTimeUnit;
    public Long executeTime;//ms

    public DelayedTask(String name, long delayTime, TimeUnit delayTimeUnit) {
        this.name = name;
        this.delayTime = delayTime;
        this.delayTimeUnit = delayTimeUnit;
        this.executeTime = System.currentTimeMillis() + delayTimeUnit.toMillis(delayTime);
    }

	@Override
	public int compareTo(Delayed o) {
		if(this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
	        return 1;
	    }else if(this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
	        return -1;
	    }
	    return 0;
	}

	@Override
	public long getDelay(TimeUnit unit) {
	    return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

}
