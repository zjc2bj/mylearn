package cn.zjc.learn.stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class GroupBy {
	public static void main(String[] args) {
		test2();
	}

	private static void test1() {
		String json = "[{\"uid\":\"test11\",\"snapshotImageId\":\"fgw345243t4g45g4\",\"snapshotTime\":\"2019-02-12 12:50:00\"},{\"uid\":\"test22\",\"snapshotImageId\":\"fgw345243t4g45g4\",\"snapshotTime\":\"2019-02-12 12:45:00\"}]";
		List<HashMap> parseArray = JSONArray.parseArray(json, HashMap.class);
		Map<Object, List<HashMap>> collect = parseArray.stream().collect(Collectors.groupingBy(s -> s.get("uid")));
		System.out.println(collect);
	}
	
	private static void test2() {
		String json = "[{\"uid\":\"test11\",\"snapshotImageId\":\"fgw345243t4g45g4\",\"snapshotTime\":\"2019-02-12 12:50:00\"},{\"uid\":\"test22\",\"snapshotImageId\":\"fgw345243t4g45g4\",\"snapshotTime\":\"2019-02-12 12:45:00\"}]";
		List<FaceLogDO> list = JSONArray.parseArray(json, FaceLogDO.class);
        Map<Object, List<FaceLogDO>> collect = list.stream().collect(Collectors.groupingBy(FaceLogDO::getUid));
        System.out.println("result= " + JSON.toJSONString(collect));
        Map<String, FaceLogDO> collect2 = list.stream().collect(Collectors.toMap(FaceLogDO::getUid, a -> a, (k1, k2) -> k1));
        System.out.println("collect2= " + JSON.toJSONString(collect2));
	}
	
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class FaceLogDO{
		private String uid;
		private String snapshotImageId;
		private String snapshotTime;
	}
}
