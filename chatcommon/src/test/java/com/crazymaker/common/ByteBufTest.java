package com.crazymaker.common;

import com.ljy.util.Print;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;


public class ByteBufTest {
    public static void test1() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);

        print("allocate ByteBuf(9, 100)", buffer);

        // write 方法改变写指针，写完之后写指针未到 capacity 的时候，buffer 仍然可写
        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print("writeBytes(1,2,3,4)", buffer);

        // read 方法改变读指针
        byte[] dst = new byte[buffer.readableBytes()];
        buffer.readBytes(dst);
        print("readBytes(" + dst.length + ")", buffer);


    }

    public static void testSlice() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);

        print("allocate ByteBuf(9, 100)", buffer);

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print("writeBytes(1,2,3,4)", buffer);

        ByteBuf buffer1 = buffer.slice();
        print("buffer slice", buffer1);
    }

    public static void test2(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);

        print("allocate ByteBuf(9, 100)", buffer);

        // write 方法改变写指针，写完之后写指针未到 capacity 的时候，buffer 仍然可写
        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        print("writeBytes(1,2,3,4)", buffer);

        // write 方法改变写指针，写完之后写指针未到 capacity 的时候，buffer 仍然可写, 写完 int 类型之后，写指针增加4
        buffer.writeInt(12);
        print("writeInt(12)", buffer);

        // write 方法改变写指针, 写完之后写指针等于 capacity 的时候，buffer 不可写
        buffer.writeBytes(new byte[]{5});
        print("writeBytes(5)", buffer);

        // write 方法改变写指针，写的时候发现 buffer 不可写则开始扩容，扩容之后 capacity 随即改变
        buffer.writeBytes(new byte[]{6});
        print("writeBytes(6)", buffer);

        // get 方法不改变读写指针
        Print.tcfo("getByte(3) return: " + buffer.getByte(3));
        Print.tcfo("getShort(3) return: " + buffer.getShort(3));
        Print.tcfo("getInt(3) return: " + buffer.getInt(3));
        print("getByte()", buffer);


        // set 方法不改变读写指针
        buffer.setByte(buffer.readableBytes() + 1, 0);
        print("setByte()", buffer);

        // read 方法改变读指针
        byte[] dst = new byte[buffer.readableBytes()];
        buffer.readBytes(dst);
        print("readBytes(" + dst.length + ")", buffer);

    }

    public static void main(String[] args) {
//       test1();
        testSlice();
    }

    private static void print(String action, ByteBuf buffer) {
        Print.tcfo("after ===========" + action + "============");
        Print.tcfo("capacity(): " + buffer.capacity());
        Print.tcfo("maxCapacity(): " + buffer.maxCapacity());
        Print.tcfo("readerIndex(): " + buffer.readerIndex());
        Print.tcfo("readableBytes(): " + buffer.readableBytes());
        Print.tcfo("isReadable(): " + buffer.isReadable());
        Print.tcfo("writerIndex(): " + buffer.writerIndex());
        Print.tcfo("writableBytes(): " + buffer.writableBytes());
        Print.tcfo("isWritable(): " + buffer.isWritable());
        Print.tcfo("maxWritableBytes(): " + buffer.maxWritableBytes());
    }
}