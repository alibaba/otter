package com.alibaba.otter.node.etl.common.pipe.impl.http;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.io.download.DataRetrieverFactory;
import com.alibaba.otter.node.etl.common.jetty.JettyEmbedServer;
import com.alibaba.otter.node.etl.common.pipe.Pipe;
import com.alibaba.otter.shared.common.utils.NioUtils;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.google.common.collect.Sets;

/**
 * 基于http下载的pipe实现
 * 
 * @author jianghang 2011-10-13 下午06:31:13
 * @version 4.0.0
 */
public abstract class AbstractHttpPipe<T, KEY extends HttpPipeKey> implements Pipe<T, KEY>, InitializingBean {

    protected static final Long               DEFAULT_PERIOD = 60 * 1000L;
    protected static final String             DATE_FORMAT    = "yyyy-MM-dd-HH-mm-ss";
    protected static ScheduledExecutorService schedulor      = Executors.newScheduledThreadPool(
                                                                                                1,
                                                                                                new NamedThreadFactory(
                                                                                                                       "HttpPipe-Cleaner")); ;
    protected Logger                          logger         = LoggerFactory.getLogger(this.getClass());
    protected JettyEmbedServer                jettyEmbedServer;                                                                             // 注入对象，确保server已经启动
    protected Long                            period         = DEFAULT_PERIOD;
    protected ConfigClientService             configClientService;
    protected Long                            timeout        = 24 * 60 * 60 * 1000L;                                                        // 对应的超时时间,24小时
    protected String                          htdocsDir;                                                                                    // http服务下载路径
    protected String                          downloadDir;                                                                                  // 下载完成后目标路径
    protected RemoteUrlBuilder                remoteUrlBuilder;
    protected DataRetrieverFactory            dataRetrieverFactory;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(remoteUrlBuilder);
        Assert.notNull(htdocsDir);
        NioUtils.create(new File(htdocsDir), false, 3);
        if (StringUtils.isEmpty(downloadDir)) {
            downloadDir = htdocsDir;
        } else {
            NioUtils.create(new File(downloadDir), false, 3);
        }
        // 启动一下清理任务
        schedulor.scheduleAtFixedRate(new Runnable() {

            public void run() {
                try {
                    long threshold = System.currentTimeMillis() - timeout;
                    File htdocsDirFile = new File(htdocsDir);
                    File[] htdocsFiles = htdocsDirFile.listFiles();
                    Set<File> files = Sets.newHashSet();
                    for (File htdocsFile : htdocsFiles) {
                        files.add(htdocsFile);
                    }
                    if (downloadDir.equals(htdocsDir) == false) {
                        File downloadDirFile = new File(downloadDir);
                        File[] downloadFiles = downloadDirFile.listFiles();
                        for (File downloadFile : downloadFiles) {
                            files.add(downloadFile);
                        }
                    }

                    for (File file : files) {
                        boolean isOld = FileUtils.isFileOlder(file, threshold);
                        if (isOld) {
                            NioUtils.delete(file, 3);
                        }
                    }
                } catch (Exception e) {
                    logger.error("old_file_clean_error", e);
                }
            }
        }, DEFAULT_PERIOD, DEFAULT_PERIOD, TimeUnit.MILLISECONDS);
    }

    // ==================== setter / getter ==================

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setJettyEmbedServer(JettyEmbedServer jettyEmbedServer) {
        this.jettyEmbedServer = jettyEmbedServer;
    }

    public void setRemoteUrlBuilder(RemoteUrlBuilder remoteUrlBuilder) {
        this.remoteUrlBuilder = remoteUrlBuilder;
    }

    public void setHtdocsDir(String htdocsDir) {
        this.htdocsDir = htdocsDir;
    }

    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    public void setDataRetrieverFactory(DataRetrieverFactory dataRetrieverFactory) {
        this.dataRetrieverFactory = dataRetrieverFactory;
    }

}
