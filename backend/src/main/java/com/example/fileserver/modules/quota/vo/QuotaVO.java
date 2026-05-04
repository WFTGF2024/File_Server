package com.example.fileserver.modules.quota.vo;

import lombok.Data;

@Data
public class QuotaVO {

    private Long userId;
    private Long totalStorageLimitBytes;
    private Long usedStorageBytes;
    private Long maxSingleFileSizeBytes;
    private Integer maxBatchUploadCount;
    private String policySource;
    private Double usagePercent;
}
