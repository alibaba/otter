package com.alibaba.otter.node.etl.common.io.download;

/**
 * Enum description
 */
public enum DownloadStatus {

    RUNNING("Downloading"), EXCEPTION("Error"), COMPLETE("Done"), ABORT("Abort"), CONNECTING("Connecting"),
    IDLE("Idle"), PAUSED("Paused"), RETRYING("Retrying"), REDIRECTING("Redirecting"), CONNECTED("Connected"),
    PAUSING("Pausing");

    private String title;

    DownloadStatus(String title){
        this.title = title;
    }

    public String title() {
        return title;
    }
}
