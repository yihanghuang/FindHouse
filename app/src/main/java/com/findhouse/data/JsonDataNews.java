package com.findhouse.data;

/**
 * - @Description:
 * - @Author:  huangyihang
 * - @Time:  2019-08-15 18:23
 */
public class JsonDataNews<T> {

    private Result<T> result;
    private int error_code;
    private String reason;

    public Result<T> getResult() {
        return result;
    }

    public void setResult(Result<T> result) {
        this.result = result;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
