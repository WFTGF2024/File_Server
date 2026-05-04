package com.example.fileserver.common.constant;

public class PolicyConstants {

    private PolicyConstants() {
    }

    // Cache key prefixes
    public static final String CACHE_POLICY_PREFIX = "fs:policy:";
    public static final String CACHE_QUOTA_PREFIX = "fs:quota:";
    public static final String CACHE_FOLDER_TREE_PREFIX = "fs:folder:tree:";

    // Policy source
    public static final String POLICY_SOURCE_CATEGORY = "category";
    public static final String POLICY_SOURCE_CUSTOM = "custom";

    // Status
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;

    // Default allowed file types (empty array means all types allowed)
    public static final String DEFAULT_ALLOWED_FILE_TYPES = "[]";
}
