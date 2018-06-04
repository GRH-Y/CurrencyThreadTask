package task.utils;


import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * IO读写操作
 * Created by prolog on 4/17/2017.
 *
 * @author yyz
 * @date 4/17/2017.
 */
public class IoUtils {
    /*** 成功标志*/
    public static final int SUCCESS = 0;
    /*** 失败标志*/
    public static final int FAIL = -1;
    /*** 超时标志*/
    public static final int TIMEOUT = -2;

    private static final int SIZE = 4096;

    private static final int MAX_SEND_SIZE = 1460;


    /**
     * 跳过流数据
     *
     * @param is         输入流
     * @param skipLength 跳过数据数量
     */
    public static long skip(InputStream is, int skipLength) {
        try {
            return is.skip(skipLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FAIL;
    }

    /**
     * 跳过流数据
     *
     * @param is         输入流
     * @param skipLength 跳过数据数量
     */
    public static void skip(RandomAccessFile is, int skipLength) {
        try {
            is.seek(skipLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳过流数据
     *
     * @param channel    输入流
     * @param skipLength 跳过数据数量
     */
    public static void skip(FileChannel channel, int skipLength) {
        try {
            long position = channel.position();
            channel.position(position + skipLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*=====================================================================================*/
    /*                     InputStream 读取                                                */
    /*=====================================================================================*/

    /**
     * 堵塞式管道流，InputStream输入流不停往OutputStream输出流写数据
     *
     * @param is 输入流
     * @param os 输出流
     */
    public static boolean pipReadWrite(InputStream is, OutputStream os, boolean isCanTimeOut) {
        boolean state = true;
        int missCount = 0;
        while (state) {
            try {
                int available = is.available();
                available = available > 0 ? available : SIZE;
                byte[] buffer = new byte[available];
                int len = is.read(buffer, 0, available);
                if (len > 0) {
                    os.write(buffer, 0, len);
                    os.flush();
                } else {
                    state = missCount >= 8 ? false : state;
                    state = isCanTimeOut ? state && isCanTimeOut : false;
                    missCount++;
                }
            } catch (Throwable e) {
                if (e instanceof SocketTimeoutException == false) {
                    state = false;
                    e.printStackTrace();
                } else {
                    state = missCount >= 8 ? false : state;
                    missCount++;
                }
            }
        }
        return state;
    }

    /**
     * 尝试读取流中的数据，适用不确定数据的大小情况下
     *
     * @param inputStream 输入流
     * @param buffer      输出结果在buffer[0]里面
     * @return 读取成功返回 SUCCESS,超时返回 TIMEOUT，流异常返回 FAIL（返回FAIL一般可以认定该流通道已不能使用）
     */
    public static int tryRead(InputStream inputStream, byte[][] buffer) {
        int result = FAIL;
        if (inputStream == null) {
            return result;
        }

        boolean isExit = false;
        boolean isFist = true;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        while (isExit == false) {
            try {
                int available = inputStream.available();
                available = isFist && available == 0 ? SIZE : available;
                if (available > 0) {
                    byte[] tmp = new byte[available];
                    int len = inputStream.read(tmp);
                    stream.write(tmp, 0, len);
                    isFist = false;
                } else {
                    isExit = true;
                    result = SUCCESS;
                }
            } catch (Throwable e) {
                result = e instanceof SocketTimeoutException ? TIMEOUT : FAIL;
                stream = null;
                isExit = true;
            }
        }
        if (stream != null) {
            buffer[0] = stream.toByteArray();
        }
        return result;
    }

    /**
     * 尝试读取流中的数据，适用不确定数据的大小情况下
     *
     * @param inputStream 输入流
     * @return 读取成功返回不为空
     */
    public static byte[] tryRead(InputStream inputStream) {
        byte[] result = null;
        if (inputStream == null) {
            return result;
        }

        boolean isExit = false;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            while (isExit == false) {
                int available = inputStream.available();
                available = available == 0 ? SIZE : available;
                if (available > 0) {
                    byte[] tmp = new byte[available];
                    int len = inputStream.read(tmp);
                    if (len > 0) {
                        stream.write(tmp, 0, len);
                    } else {
                        isExit = true;
                    }
                } else {
                    isExit = true;
                }
            }
        } catch (Throwable e) {
            if (e instanceof SocketTimeoutException == false) {
                e.printStackTrace();
            }
        } finally {
            result = stream.toByteArray();
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static byte[] tryRead(SocketChannel channel) {
        byte[] data = null;
        if (channel == null) {
            return data;
        }
        int ret;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        int sum = buffer.array().length;
        do {
            try {
                ret = channel.read(buffer);
                stream.write(buffer.array(), 0, ret);
                if (ret == sum) {
                    buffer.clear();
                } else {
                    sum = FAIL;
                }
            } catch (IOException e) {
                if (e instanceof SocketTimeoutException == false) {
                    e.printStackTrace();
                }
                sum = FAIL;
            }
        } while (sum >= 0 && channel.isConnected());
        data = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /*=====================================================================================*/
    /*                     try 读取                                                         */
    /*=====================================================================================*/

    /**
     * 读取socket数据
     *
     * @param inputStream  输入流
     * @param buffer       缓冲区（要确定流中有足够多的数据来填满缓冲区，如果不能填满会因为超时而返回）
     * @param offset       从指定的位置开始读取
     * @param length       读取的长度
     * @param timeoutCount 大于0则socket超时重试次数,0则不重试，-1则永远重试
     * @return 读取成功返回 SUCCESS,超时返回 TIMEOUT
     */
    public static int readToFull(InputStream inputStream, byte[] buffer, int offset, int length, int timeoutCount) {
        int result = FAIL;
        boolean ret = buffer == null || inputStream == null || offset < 0 || length < 1 || length > buffer.length - offset;
        if (ret) {
            Logcat.e("readToFull Parameter exception !");
            return result;
        }
        int sum = offset;
        int len;
        int missCount = 0;
        while (length > 0) {
            try {
                int available = inputStream.available();
                if (available > 0 && available < length) {
                    len = inputStream.read(buffer, sum, available);
                } else {
                    len = inputStream.read(buffer, sum, length);
                }
                if (len > 0) {
                    sum += len;
                    length -= len;
                    missCount = 0;
                    result = SUCCESS;
                } else {
                    length = 0;
                    result = FAIL;
                }
            } catch (Throwable e) {
                if (e instanceof SocketTimeoutException) {
                    missCount++;
                    boolean existed = timeoutCount == 0 || timeoutCount > 0 && missCount >= timeoutCount;
                    if (existed) {
                        length = 0;
                        result = TIMEOUT;
                    }
                    Logcat.w("tryRead ==>missCount =" + missCount);
                } else {
                    length = 0;
                    result = FAIL;
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 读取socket数据
     *
     * @param inputStream  输入流
     * @param buffer       缓冲区（要确定流中有足够多的数据来填满缓冲区，如果不能填满会因为超时而返回）
     * @param timeoutCount 大于0则socket超时重试次数,0则不重试，-1则永远重试
     * @return 读取成功返回 SUCCESS,超时返回 TIMEOUT
     */
    public static int readToFull(InputStream inputStream, byte[] buffer, int timeoutCount) {
        return readToFull(inputStream, buffer, 0, buffer.length, timeoutCount);
    }

    /**
     * 读取输入流的数据 （没有重试，一但读取异常就返回，不能保证填满buffer）
     *
     * @param inputStream 输入流
     * @param buffer      缓冲区（要确定流中有足够多的数据来填满缓冲区，如果不能填满会因为超时而返回）
     * @return
     */
    public static int readToFull(InputStream inputStream, byte[] buffer) {
        return readToFull(inputStream, buffer, 0, buffer.length, 0);
    }

    public static int readToFull(InputStream inputStream, byte[] buffer, int offset, int length) {
        return readToFull(inputStream, buffer, offset, length, 0);
    }


    /*=====================================================================================*/
    /*                         RandomAccessFile 方式读取流                               */
    /*=====================================================================================*/

    /**
     * 随机读取文件
     *
     * @param inputStream
     * @param buffer
     * @return
     */
    public static int readToFull(RandomAccessFile inputStream, byte[] buffer) {
        return readToFull(inputStream, buffer, 0, buffer.length);
    }

    public static int readToFull(RandomAccessFile inputStream, byte[] buffer, int offset) {
        return readToFull(inputStream, buffer, offset, buffer.length);
    }

    /**
     * 读取流数据
     *
     * @param inputStream 数据流
     * @param buffer      读取数据到该缓存区
     * @param offset      读取到该缓存区的指定的位置
     * @param length      读取数据的长度
     * @return 成功返回  0
     */
    public static int readToFull(RandomAccessFile inputStream, byte[] buffer, int offset, int length) {
        int sum = 0, len, ret = FAIL;
        if (buffer == null || inputStream == null || offset < 0 || length < 0) {
            return ret;
        }
        while (sum < length) {
            try {
                len = inputStream.read(buffer, offset + sum, length - sum);
                if (len < 0) {
                    length = 0;
                } else {
                    sum += len;
                }
                ret = SUCCESS;
            } catch (Exception e) {
                length = 0;
                ret = FAIL;
            }
        }
        return ret;
    }


    /*=====================================================================================*/
    /*                         FileChannel 方式读取文件                                    */
    /*=====================================================================================*/

    /**
     * 读取数据
     *
     * @param channel 数据流
     * @param buffer  读取数据到该缓存区
     * @return 成功返回 0
     */
    public static int readToFull(FileChannel channel, ByteBuffer buffer) {
        return readToFull(channel, buffer, 0);
    }

    /**
     * 读取数据
     *
     * @param channel 数据流
     * @param buffer  读取数据到该缓存区
     * @param offset  读取到该缓存区的指定的位置
     * @return 成功返回 0
     */
    public static int readToFull(FileChannel channel, ByteBuffer buffer, int offset) {
        int len = FAIL;
        if (buffer == null || channel == null || offset < 0) {
            return len;
        }
        try {
            len = channel.read(buffer, offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return len;
    }

    /*=====================================================================================*/
    /*                         SocketChannel 方式读取文件                                    */
    /*=====================================================================================*/

    /**
     * 读取数据
     *
     * @param channel 数据流
     * @param buffer  读取数据到该缓存区
     * @return 成功返回 0
     */
    public static int readToFull(SocketChannel channel, ByteBuffer buffer) {
        return readToFull(channel, buffer, 0);
    }

    /**
     * 读取数据
     *
     * @param channel      数据流
     * @param buffer       读取数据到该缓存区
     * @param timeoutCount 大于0则socket超时重试次数,0则不重试，-1则永远重试
     * @return 成功返回 0
     */
    public static int readToFull(SocketChannel channel, ByteBuffer buffer, int timeoutCount) {
        int ret = FAIL;
        if (buffer == null || channel == null) {
            return ret;
        }
        int sum = buffer.array().length;
        do {
            try {
                ret = channel.read(buffer);
                if (ret < 1 && timeoutCount > 0) {
                    if (timeoutCount == 0) {
                        sum = 0;
                        ret = FAIL;
                    }
                    timeoutCount--;
                }
                sum -= ret;
                ret = SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
                sum = 0;
                ret = FAIL;
            }
        } while (sum > 0);
        return ret;
    }

    /*=====================================================================================*/
    /*                         writeToFull 往 OutputStream 流写数据                          */
    /*=====================================================================================*/

    /**
     * 写入数据到socket
     *
     * @param data     数据
     * @param isUnpack 是否拆包，为true如果数据包大于1460则拆分若干个1460大小的包
     */
    public static boolean writeToFull(OutputStream outputStream, byte[] data, boolean isUnpack) {
        boolean result = false;
        if (data == null && outputStream == null) {
            return result;
        }
        try {
            if (isUnpack) {
                int length = data.length;
                int len = length > MAX_SEND_SIZE ? MAX_SEND_SIZE : length;
                if (len == length) {
                    outputStream.write(data, 0, len);
                    outputStream.flush();
                } else {
                    int off = 0;
                    while (off < length) {
                        if (off + len > length) {
                            len = length - off;
                        }
                        outputStream.write(data, off, len);
                        outputStream.flush();
                        off += len;
                    }
                }
            } else {
                outputStream.write(data, 0, data.length);
                outputStream.flush();
            }
            result = true;
        } catch (Throwable e) {
        }
        return result;
    }

    /*=====================================================================================*/
    /*                         writeToFull 往 OutputStream 流写数据                          */
    /*=====================================================================================*/

    public static boolean writeToFull(SocketChannel channel, byte[] data, boolean isUnpack) {
        return writeToFull(channel, ByteBuffer.wrap(data), isUnpack);
    }

    public static boolean writeToFull(SocketChannel channel, ByteBuffer data, boolean isUnpack) {
        boolean result = false;
        if (channel == null || data == null) {
            return result;
        }
        try {
            if (isUnpack) {
                int length = data.capacity();
                int len = length > MAX_SEND_SIZE ? MAX_SEND_SIZE : length;
                if (len == length) {
                    channel.write(data);
                } else {
                    int off = 0;
                    while (off < length) {
                        if (off + len > length) {
                            len = length - off;
                        }
                        data.position(off);
                        data.limit(len);
                        channel.write(data);
                        off += len;
                    }
                }

            } else {
                channel.write(data);
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
