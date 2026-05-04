package com.example.fileserver.common.constant;

public class FileConstants {

    private FileConstants() {
    }

    public static final String ROOT_FOLDER_ID = "0";
    public static final long ROOT_FOLDER_ID_LONG = 0L;
    public static final int MAX_FOLDER_DEPTH = 10;
    public static final String STORAGE_PATH_DATE_FORMAT = "yyyy-MM";
    public static final String FILE_NAME_SEPARATOR = ".";
    public static final String PATH_SEPARATOR = "/";

    // Preview status
    public static final int PREVIEW_NOT_AVAILABLE = 0;
    public static final int PREVIEW_AVAILABLE = 1;
    public static final int PREVIEW_GENERATING = 2;
    public static final int PREVIEW_FAILED = 3;

    // Resource types
    public static final String RESOURCE_TYPE_FILE = "file";
    public static final String RESOURCE_TYPE_FOLDER = "folder";

    // Audit result
    public static final int AUDIT_RESULT_SUCCESS = 1;
    public static final int AUDIT_RESULT_FAILURE = 0;
}
