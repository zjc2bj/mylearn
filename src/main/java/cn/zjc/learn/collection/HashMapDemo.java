package cn.zjc.learn.collection;

/**
 * 
 * 参考文档： http://blog.csdn.net/a_long_/article/details/51594159
 * <pre>
 * public V put(K key, V value) {  
    // 处理key为null，HashMap允许key和value为null  
    if (key == null)  
        return putForNullKey(value);  
    // 得到key的哈希码  
    int hash = hash(key);  
    // 通过哈希码计算出bucketIndex  
    int i = indexFor(hash, table.length);  
    // 取出bucketIndex位置上的元素，并循环单链表，判断key是否已存在  
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {  
        Object k;  
        // 哈希码相同并且对象相同时  
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {  
            // 新值替换旧值，并返回旧值  
            V oldValue = e.value;  
            e.value = value;  
            e.recordAccess(this);  
            return oldValue;  
        }  
    }  
  
    // key不存在时，加入新元素  
    modCount++;  
    addEntry(hash, key, value, i);  
    return null;  
}  
 * </pre>
 * 
 * <pre>
 * HashMap有两个参数影响其性能：初始容量和加载因子。默认初始容量是16，加载因子是0.75。
 * 容量是哈希表中桶(Entry数组)的数量，初始容量只是哈希表在创建时的容量。
 * 加载因子是哈希表在其容量自动增加之前可以达到多满的一种尺度。
 * 当哈希表中的条目数超出了加载因子与当前容量的乘积时，通过调用 rehash 方法将容量翻倍。
 * </pre>
 * <pre>
 * 如果很多映射关系要存储在 HashMap 实例中，则相对于按需执行自动的 rehash 操作以增大表的容量来说，使用足够大的初始容量创建它将使得映射关系能更有效地存储。 
 * 当HashMap存放的元素越来越多，到达临界值(阀值)threshold时，就要对Entry数组扩容，这是Java集合类框架最大的魅力，HashMap在扩容时，新数组的容量将是原来的2倍，
 * 由于容量发生变化，原有的每个元素需要重新计算bucketIndex，再存放到新数组中去，也就是所谓的rehash。
 * HashMap默认初始容量16，加载因子0.75，也就是说最多能放16*0.75=12个元素，当put第13个时，HashMap将发生rehash，rehash的一系列处理比较影响性能，所以当我们需要向HashMap存放较多元素时，最好指定合适的初始容量和加载因子，否则HashMap默认只能存12个元素，将会发生多次rehash操作。
 * </pre>
 */
class HashMapDemo {

}
