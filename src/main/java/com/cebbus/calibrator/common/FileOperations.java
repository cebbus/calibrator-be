package com.cebbus.calibrator.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
public class FileOperations {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private FileOperations() {
    }

    public static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return name.substring(dot + 1);
    }

    public static String createFileName(String name) {
        String randomName = Integer.toString(SECURE_RANDOM.nextInt(1000));
        return randomName + System.currentTimeMillis() + "." + getExtension(name);
    }

    public static String uploadFile(String pathForUpload, String originalFileName, String fileData) {
        String createdFileName = createFileName(originalFileName);
        byte[] data = Base64.decodeBase64(fileData.contains(",") ? fileData.split(",")[1] : fileData);

        uploadFile(pathForUpload, createdFileName, data);
        return createdFileName;
    }

    public static void uploadFile(String pathForUpload, String fileName, byte[] data) {
        File file = new File(pathForUpload, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (Exception e) {
            throw new RuntimeException("Error writing data to " + file.getPath(), e);
        }
    }

    public static void uploadFile(String pathForUpload, String fileName, Object data) {
        File path = new File(pathForUpload);
        if (!path.exists() && !path.mkdir()) {
            throw new RuntimeException("Path creation error! Path: " + path.getPath());
        }

        File file = new File(path, fileName);
        try (
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T readFile(File file) {
        try (
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void downloadFile(HttpServletResponse response, String fullPath, String downloadedFileName) {
        downloadFile(response, new File(fullPath), downloadedFileName);
    }

    public static void downloadFile(HttpServletResponse response, File file, String downloadedFileName) {
        downloadFile(response, file, downloadedFileName, false);
    }

    public static void downloadFile(
            HttpServletResponse response,
            File file,
            String downloadedFileName,
            boolean deleteAfterDownload) {

        addFileNameToResponse(response, downloadedFileName);
        response.setContentType(getContentType(file.getName()));

        response.setContentLength((int) file.length());

        try (
                FileInputStream fis = new FileInputStream(file);
                InputStream is = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream()
        ) {
            FileCopyUtils.copy(is, os);
            response.flushBuffer();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (deleteAfterDownload) {
                deleteFile(file);
            }
        }
    }

    public static void copy(File source, File destination) {
        try {
            FileCopyUtils.copy(source, destination);
        } catch (IOException e) {
            throw new RuntimeException("Copy operation failed.", e);
        }
    }

    public static void deleteFiles(String[] fileNames, String directory) {
        if (fileNames == null || directory == null) {
            return;
        }
        for (String fileName : fileNames) {
            deleteFile(fileName, directory);
        }
    }

    public static void deleteFile(String name, String directory) {
        if (name == null || directory == null) {
            return;
        }

        deleteFile(new File(directory, name));
    }

    public static void deleteFile(File file) {
        if (file == null) {
            return;
        }

        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void deleteFile(List<File> files) {
        for (File file : files) {
            deleteFile(file);
        }
    }

    public static void deleteDir(File dir) {
        FileSystemUtils.deleteRecursively(dir);
    }

    public static URL getResource(String dir, String file) {
        String path = dir + "/" + file;

        ClassLoader loader = FileOperations.class.getClassLoader();
        URL resource = loader.getResource(path);

        if (resource == null) {
            throw new RuntimeException("Template could not be found! Template : " + path);
        }

        return resource;
    }

    public static void setResponseHeaderToFileDownload(HttpServletResponse response, String fileName) {
        addFileNameToResponse(response, fileName);
        response.setContentType(getContentType(fileName));
    }

    public static String fileToString(File file) {
        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(file);
            byte[] encoded = Base64.encodeBase64(bytes);
            return new String(encoded);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void writeFile(String content, File target) {
        try {
            Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void addFileNameToResponse(HttpServletResponse response, String name) {
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
    }

    private static String getContentType(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        return mimeType;
    }
}
