package cn.zjc.learn.nio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 容量（capacity）
 *	capacity指的是缓冲区能够容纳元素的最大数量，这个值在缓冲区创建时被设定，而且不能够改变，如下，我们创建了一个最大容量为10的字节缓冲区；
 *	ByteBuffer bf = ByteBuffer.allocate(10);
 * 上界（limit）
 *	limit指的是缓冲区中第一个不能读写的元素的数组下标索引，也可以认为是缓冲区中实际元素的数量；
 * 位置（position）
 *	position指的是下一个要被读写的元素的数组下标索引，该值会随get()和put()的调用自动更新；
 * 标记（mark）
 *	一个备忘位置，调用mark()方法的话，mark值将存储当前position的值，等下次调用reset()方法时，会设定position的值为之前的标记值；
 * </pre>
 * 
 * @see http://www.cnblogs.com/chenpi/p/6475510.html
 */
public class BufferFieldMethodDemo {

	public static void main(String[] args) throws Exception {
		ByteBuffer bf = (ByteBuffer)ByteBuffer.allocate(10);
		// 此时：mark = -1; position = 0; limit = 10; capacity = 10;
		System.out.println("1.  "+BufferUtils.toString(bf));

		bf.put((byte) 'H').put((byte) 'e').put((byte) 'l').put((byte) 'l').put((byte) '0');
		// 此时：mark = -1; position = 5; limit = 10; capacity = 10;
		System.out.println("2.  "+BufferUtils.toString(bf));
		
		// 调用flip()方法，切换为读就绪状态
		bf.flip();
		// 此时：mark = -1; position = 0; limit = 5; capacity = 10;
		System.out.println("3.  "+BufferUtils.toString(bf));
		
		// 读取两个元素
		System.out.println("" + (char) bf.get() + (char) bf.get());
		// 此时：mark = -1; position = 2; limit = 5; capacity = 10;
		System.out.println("4.  "+BufferUtils.toString(bf));
		
		// 标记此时的position位置
		bf.mark();
		// 此时：mark = 2; position = 2; limit = 5; capacity = 10;
		System.out.println("5.  "+BufferUtils.toString(bf));
		
		// 读取两个元素后，恢复到之前mark的位置处
		System.out.println("" + (char) bf.get() + (char) bf.get());
		// 执行完第一行代码：mark = 2; position = 4; limit = 5; capacity = 10;
		System.out.println("6.  "+BufferUtils.toString(bf));

		bf.reset();
		System.out.println("7.  "+BufferUtils.toString(bf));
		// 属性变化情况：
		// 执行完第二行代码：mark = 2; position = 2; limit = 5; capacity = 10;

		// 调用compact()方法，释放已读数据的空间，准备重新填充缓存区
		bf.compact();
		// 此时：mark = 2; position = 3; limit = 10; capacity = 10;
		System.out.println("8.  "+BufferUtils.toString(bf));
		// 注意观察数组中元素的变化，实际上进行了数组拷贝，抛弃了已读字节元素，保留了未读字节元素；
		//compact()方法只会清除已经读过的数据。任何未读的数据都被移到缓冲区的起始处，新写入的数据将放到缓冲区未读数据的后面
		
		bf.clear();
		// 此时：mark:-1,position:0,limit:10,capacity:10,remain:10
		System.out.println("9.  "+BufferUtils.toString(bf));
		//Buffer 被清空了。Buffer中的数据并未清除，只是这些标记告诉我们可以从哪里开始往Buffer里写数据。
	}
}
