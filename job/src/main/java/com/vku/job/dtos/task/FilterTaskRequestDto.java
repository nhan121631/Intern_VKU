package com.vku.job.dtos.task;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class FilterTaskRequestDto {
    private String status;
    private Long userId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private String createAtFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private String createAtTo;
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String order = "asc";
}
