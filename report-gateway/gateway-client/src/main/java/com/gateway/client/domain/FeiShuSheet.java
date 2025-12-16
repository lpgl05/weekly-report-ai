package com.gateway.client.domain;

/**
 * 飞书sheet配置
 */
public class FeiShuSheet {
    private String sheetId;
    private String title;
    private Integer frozenRowCount;
    private Integer frozenColumnCount;
    private Integer rowCount;
    private Integer columnCount;

    public String getSheetId() {
        return sheetId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getFrozenRowCount() {
        return frozenRowCount;
    }

    public void setFrozenRowCount(Integer frozenRowCount) {
        this.frozenRowCount = frozenRowCount;
    }

    public Integer getFrozenColumnCount() {
        return frozenColumnCount;
    }

    public void setFrozenColumnCount(Integer frozenColumnCount) {
        this.frozenColumnCount = frozenColumnCount;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }
}
