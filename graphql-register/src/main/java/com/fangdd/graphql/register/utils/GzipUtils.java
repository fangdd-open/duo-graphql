package com.fangdd.graphql.register.utils;

import com.fangdd.graphql.exceptions.GraphqlRegistryException;
import com.google.common.base.Charsets;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP压缩
 *
 * @author xuwenzhen
 * @date 18/1/2
 */
public class GzipUtils {
    private GzipUtils() {
    }

    /**
     * 压缩
     *
     * @param str
     * @return
     * @throws IOException
     */
    public static byte[] compress(final String str) {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(obj);
            gzip.write(str.getBytes(Charsets.UTF_8));
            gzip.close();
        } catch (IOException e) {
            throw new GraphqlRegistryException("gzip压缩失败", e);
        }
        return obj.toByteArray();
    }

    /**
     * 解压
     *
     * @param compressed
     * @return
     * @throws IOException
     */
    public static String decompress(final byte[] compressed) {
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        if (!isCompressed(compressed)) {
            return new String(compressed, Charsets.UTF_8);
        }
        GZIPInputStream gis;
        try {
            gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
        } catch (IOException e) {
            throw new GraphqlRegistryException("gzip解压缩失败", e);
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, Charsets.UTF_8));

        String line;
        StringBuilder outStr = new StringBuilder();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }
        } catch (IOException e) {
            throw new GraphqlRegistryException("gzip解压缩失败", e);
        }

        return outStr.toString();
    }

    /**
     * 判断是否gzip
     *
     * @param compressed 数据
     * @return
     */
    private static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
