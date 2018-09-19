package task.cache;


import util.Logcat;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class SerializableUtils {

    private SerializableUtils() {
    }

    public static Serializable readFile(String path, String fileName) {
        return readFile(new File(path, fileName));
    }

    public static Serializable readFile(String fileName) {
        return readFile(new File(fileName));
    }

    /**
     * 读取文件（通过内存映射方式，但不适合读取大文件）
     *
     * @param file
     * @return
     */
    public static <T extends Serializable> T readFile(File file) {
        Serializable ret = null;
        if (!file.exists()) {
            return (T) ret;
        }
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            Logcat.d("==>  readFile  RandomAccessFile ====");
            FileChannel channel = accessFile.getChannel();
            FileLock fileLock = channel.tryLock();
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            if (byteBuffer.limit() > 0) {
                byte[] data = new byte[byteBuffer.limit()];
                byteBuffer.get(data);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                ret = (Serializable) ois.readObject();
            }
            fileLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) ret;
    }

    public static boolean writeFile(String fileName, Serializable data) {
        return writeFile(new File(fileName), data);
    }


    public static boolean writeFile(String path, String fileName, Serializable data) {
        return writeFile(new File(path, fileName), data);
    }


    public static boolean writeFile(File file, Serializable data) {
        boolean ret = false;
        if (file == null || data == null) {
            return ret;
        }
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream ops = new ObjectOutputStream(outputStream)) {

            ops.writeObject(data);
            FileChannel channel = accessFile.getChannel();
            FileLock fileLock = channel.tryLock();
            ByteBuffer byteBuffer = ByteBuffer.wrap(outputStream.toByteArray());
            channel.write(byteBuffer);
            fileLock.release();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
