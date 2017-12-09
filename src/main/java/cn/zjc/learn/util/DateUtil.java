package cn.zjc.learn.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	private static SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat hmsFormat = new SimpleDateFormat("HH:mm:ss");

	public static String getDateStr() {
		return defaultFormat.format(new Date());
	}
	public static String getHMSStr() {
		return hmsFormat.format(new Date());
	}
}
