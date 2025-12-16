package com.gateway.client.domain;

import java.util.ArrayList;
import java.util.List;

public class FeiShuLocalExcelResult {
    private String targetSheetName;
    private List<List<String>> dataList; // 字符串类型的二维列表
    private int maxRowIndex = -1;
    private int maxColumnIndex = -1;
    private List<FeiShuLocalMergeCell> mergeRegions = new ArrayList<>();

    public String getTargetSheetName() {
        return targetSheetName;
    }

    public void setTargetSheetName(String targetSheetName) {
        this.targetSheetName = targetSheetName;
    }

    public List<List<String>> getDataList() {
        return dataList;
    }

    public void setDataList(List<List<String>> dataList) {
        this.dataList = dataList;
    }

    public int getMaxRowIndex() {
        return maxRowIndex;
    }

    public void setMaxRowIndex(int maxRowIndex) {
        this.maxRowIndex = maxRowIndex;
    }

    public int getMaxColumnIndex() {
        return maxColumnIndex;
    }

    public void setMaxColumnIndex(int maxColumnIndex) {
        this.maxColumnIndex = maxColumnIndex;
    }

    public List<FeiShuLocalMergeCell> getMergeRegions() {
        return mergeRegions;
    }

    public void setMergeRegions(List<FeiShuLocalMergeCell> mergeRegions) {
        this.mergeRegions = mergeRegions;
    }
}
