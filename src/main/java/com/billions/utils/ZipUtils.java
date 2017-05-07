package com.billions.utils;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by OneOfBillions on 2016/9/28 0028.
 *
 * @version V1.0
 * @date 2016.09.28
 */
public class ZipUtils {

    /**
     * Tag.
     */
    private static final String TAG = ZipUtils.class.getSimpleName();
    /**
     * BUFFER_SIZE.
     */
    public static int BUFFER_SIZE = 4096;

    /**
     * Instantiates a new Zip utils.
     */
    private ZipUtils() {
    }

    /**
     * 打包文件
     * 文件或文件夹的集合
     *
     * @param fileDir 需要打包的文件目录
     * @param out     输出的zip文件
     * @return the boolean
     */
    public static boolean zipDir(String fileDir, String out) {

        Log.d(TAG, "打包目录" + fileDir);
        Log.d(TAG, "产出文件" + out);
        boolean result = false;
        if (!TextUtils.isEmpty(fileDir) && !TextUtils.isEmpty(out)) {

            File dir = new File(fileDir);
            if (dir.exists()) {
                //被打包的文件目录存在

                File outFile = new File(out);
                File outParentFile = outFile.getParentFile();
                if (!outParentFile.exists()) {
                    Log.d(TAG, "创建产出文件目录");
                    //创建ZIP文件目录
                    outFile.mkdirs();
                }
                Map<String, File> map = new HashMap<>();
                collectFilefromDir(dir, null, map);
                ZipArchiveOutputStream zaos = null;
                try {
                    zaos = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
                    for (Map.Entry<String, File> entry : map.entrySet()) {
                        ZipArchiveEntry zae = new ZipArchiveEntry(entry.getValue(), entry.getKey());
                        zaos.putArchiveEntry(zae);
                        InputStream is = null;
                        if (!zae.isDirectory()) {
                            is = new BufferedInputStream(new FileInputStream(entry.getValue()));
                            IOUtils.copy(is, zaos);
                        }
                        zaos.closeArchiveEntry();
                        IOUtils.closeQuietly(is);

                    }
                    result = true;
                } catch (Exception ex) {
                    Log.d(TAG, ex.toString());

                } finally {
                    try {
                        if (zaos != null) {
                            zaos.finish();
                            zaos.close();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }

                }


            }
        }
        return result;

    }


    /**
     * 递归相对路径下的所有文件，并收集到的信息以<文件路径，文件>存放在map中。
     *
     * @param childFile  the child file
     * @param parentPath the parent path
     * @param filesMap   the files map
     */
    private static void collectFilefromDir(File childFile, String parentPath, Map<String, File>
            filesMap) {
        String childPath = childFile.getName();
        if (!TextUtils.isEmpty(parentPath)) {
            childPath = parentPath + File.separator + childPath;// 设置在zip包里的相对路径
        }
        if (childFile.isFile()) {
            filesMap.put(childPath, childFile);
        } else if (childFile.isDirectory()) {
            File[] files = childFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : childFile.listFiles()) {
                    collectFilefromDir(file, childPath, filesMap);
                }
            } else {
                filesMap.put(childPath, childFile);
            }

        }


    }


    /**
     * Un zip list.
     *
     * @param zipfile the zipfile
     * @param destDir the dest dir
     * @return the list
     * @throws Exception the exception
     */
    public static List<String> unZip(File zipfile, String destDir) throws Exception {
        if (destDir == null) {
            destDir = zipfile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        ZipArchiveInputStream is = null;
        List<String> fileNames = new ArrayList<>();

        try {
            is = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipfile)));
            ZipArchiveEntry entry;
            while ((entry = is.getNextZipEntry()) != null) {
                fileNames.add(entry.getName());
                if (entry.isDirectory()) {
                    File directory = new File(destDir, entry.getName());
                    directory.mkdirs();
                } else {
                    File file = new File(destDir, entry.getName());
                    File path = file.getParentFile();
                    if (!path.exists()) {
                        path.mkdirs();
                    }

                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName())),
                                BUFFER_SIZE);
                        IOUtils.copy(is, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            IOUtils.closeQuietly(is);
        }

        return fileNames;
    }
}
