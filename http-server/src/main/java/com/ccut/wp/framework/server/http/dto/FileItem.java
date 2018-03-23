package com.ccut.wp.framework.server.http.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public class FileItem implements java.io.Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(FileItem.class);
    private final byte[] bytes;
    private final String contentType;
    private final String fileName;

    public FileItem(byte[] bytes, String contentType, String fileName) {
        this.bytes = bytes;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "contentType='" + contentType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileLength='" + bytes.length + '\'' +
                '}';
    }
}
