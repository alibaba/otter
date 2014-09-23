/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.model.config.pipeline;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.ReflectionUtils;

import com.alibaba.otter.shared.common.model.config.Transient;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.RemedyAlgorithm;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncConsistency;
import com.alibaba.otter.shared.common.model.config.channel.ChannelParameter.SyncMode;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter.RetrieverType;
import com.alibaba.otter.shared.common.utils.OtterToStringStyle;

/**
 * pipeline相关的参数类
 * 
 * @author jianghang 2011-9-2 上午10:42:27
 */
public class PipelineParameter implements Serializable {

    private static final long     serialVersionUID           = 8112362911827913152L;
    private Long                  pipelineId;
    private Long                  parallelism                = 3L;                          // 并行度
    private LoadBanlanceAlgorithm lbAlgorithm                = LoadBanlanceAlgorithm.Random; // 负载均衡算法
    private Boolean               home                       = false;                       // 是否为主站点
    private SelectorMode          selectorMode               = SelectorMode.Canal;          // 数据获取方式
    private String                destinationName;
    private Short                 mainstemClientId;                                         // mainstem订阅id
    private Integer               mainstemBatchsize          = 10000 * 10;                  // mainstem订阅批次大小
    private Integer               extractPoolSize            = 5;                           // extract模块载入线程数，针对单个载入通道
    private Integer               loadPoolSize               = 5;                           // load模块载入线程数，针对单个载入通道
    private Integer               fileLoadPoolSize           = 5;                           // 文件同步线程数

    private Boolean               dumpEvent                  = true;                        // 是否需要dumpevent对象
    private Boolean               dumpSelector               = true;                        // 是否需要dumpSelector信息
    private Boolean               dumpSelectorDetail         = true;                        // 是否需要dumpSelector的详细信息
    private PipeChooseMode        pipeChooseType             = PipeChooseMode.AUTOMATIC;    // pipe传输模式
    private Boolean               useBatch                   = true;                        // 是否使用batch模式
    private Boolean               skipSelectException        = false;                       // 是否跳过select时的执行异常
    private Boolean               skipLoadException          = false;                       // 是否跳过load时的执行异常
    private ArbitrateMode         arbitrateMode              = ArbitrateMode.ZOOKEEPER;     // 调度模式，默认进行自动选择
    private Long                  batchTimeout               = -1L;                         // 获取批量数据的超时时间,-1代表不进行超时控制，0代表永久，>0则表示按照指定的时间进行控制(单位毫秒)
    private Boolean               fileDetect                 = false;                       // 是否开启文件同步检测
    private Boolean               skipFreedom                = false;                       // 是否跳过自由门数据
    private Boolean               useLocalFileMutliThread    = false;                       // 是否启用对local
                                                                                             // file同步启用多线程
    private Boolean               useFileEncrypt             = false;                       // 是否针对文件进行加密处理
    private Boolean               useExternalIp              = false;                       // 是否起用外部Ip
    private Boolean               useTableTransform          = true;                        // 是否启用转化机制，比如类型不同，默认为true，兼容老逻辑
    private Boolean               enableCompatibleMissColumn = true;                        // 是否启用兼容字段不匹配处理
    private Boolean               skipNoRow                  = false;                       // 跳过反查没记录的情况
    private String                channelInfo;                                              // 同步标记，设置该标记后会在retl_mark中记录，在messageParse时进行check，相同则忽略
    private Boolean               dryRun                     = false;                       // 是否启用dry
                                                                                             // run模型，只记录load日志，不同步数据
    private Boolean               ddlSync                    = true;                        // 是否支持ddl同步
    private Boolean               skipDdlException           = false;                       // 是否跳过ddl执行异常

    // ================================= channel parameter
    // ================================

    @Transient
    private Boolean               enableRemedy;                                             // 是否启用冲突补救算法
    @Transient
    private RemedyAlgorithm       remedyAlgorithm;                                          // 冲突补救算法
    @Transient
    private Integer               remedyDelayThresoldForMedia;                              // 针对回环补救，如果反查速度过快，容易查到旧版本的数据记录，导致中美不一致，所以设置一个阀值，低于这个阀值的延迟不进行反查
    @Transient
    private SyncMode              syncMode;                                                 // 同步模式：字段/整条记录
    @Transient
    private SyncConsistency       syncConsistency;                                          // 同步一致性要求

    // ================================= system parameter
    // ================================
    @Transient
    private String                systemSchema;                                             // 默认为retl，不允许为空
    @Transient
    private String                systemMarkTable;                                          // 双向同步标记表
    @Transient
    private String                systemMarkTableColumn;                                    // 双向同步标记的列名
    @Transient
    private String                systemMarkTableInfo;                                      // 双向同步标记的info信息，比如类似BI_SYNC
    @Transient
    private String                systemBufferTable;                                        // otter同步buffer表
    @Transient
    private String                systemDualTable;                                          // otter同步心跳表
    @Transient
    private RetrieverType         retriever;                                                // 下载方式

    /**
     * 合并pipeline参数设置
     */
    public void merge(PipelineParameter pipelineParameter) {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                // Skip static and final fields.
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                ReflectionUtils.makeAccessible(field);
                Object srcValue = field.get(pipelineParameter);
                if (srcValue != null) { // 忽略null值
                    field.set(this, srcValue);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 合并system参数设置
     */
    public void merge(SystemParameter globalParmeter) {
        try {
            BeanUtils.copyProperties(this, globalParmeter);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 合并channel参数设置
     */
    public void merge(ChannelParameter channelParameter) {
        try {
            BeanUtils.copyProperties(this, channelParameter);
        } catch (Exception e) {
            // ignore
        }
    }

    public static enum LoadBanlanceAlgorithm {
        /** 轮询 */
        RoundRbin,
        /** 随机 */
        Random,
        /** Stick */
        Stick;

        public boolean isRoundRbin() {
            return this.equals(LoadBanlanceAlgorithm.RoundRbin);
        }

        public boolean isRandom() {
            return this.equals(LoadBanlanceAlgorithm.Random);
        }

        public boolean isStick() {
            return this.equals(LoadBanlanceAlgorithm.Stick);
        }
    }

    public static enum ArbitrateMode {
        /** 内存调度 */
        MEMORY,
        /** rpc调度 */
        RPC,
        /** zk watcher调度 */
        ZOOKEEPER,
        /** 自动选择 */
        AUTOMATIC;

        public boolean isMemory() {
            return this.equals(ArbitrateMode.MEMORY);
        }

        public boolean isRpc() {
            return this.equals(ArbitrateMode.RPC);
        }

        public boolean isZookeeper() {
            return this.equals(ArbitrateMode.ZOOKEEPER);
        }

        public boolean isAutomatic() {
            return this.equals(ArbitrateMode.AUTOMATIC);
        }
    }

    public static enum PipeChooseMode {
        /** 自动选择 */
        AUTOMATIC,
        /** RPC */
        RPC,
        /** HTTP */
        HTTP;

        public boolean isAutomatic() {
            return this.equals(PipeChooseMode.AUTOMATIC);
        }

        public boolean isRpc() {
            return this.equals(PipeChooseMode.RPC);
        }

        public boolean isHttp() {
            return this.equals(PipeChooseMode.HTTP);
        }
    }

    public static enum SelectorMode {

        Eromanga, Canal;

        public boolean isEromanga() {
            return this.equals(SelectorMode.Eromanga);
        }

        public boolean isCanal() {
            return this.equals(SelectorMode.Canal);
        }

    }

    // ======================== setter / getter ===========================

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getParallelism() {
        return parallelism;
    }

    public void setParallelism(Long parallelism) {
        this.parallelism = parallelism;
    }

    public LoadBanlanceAlgorithm getLbAlgorithm() {
        return lbAlgorithm;
    }

    public void setLbAlgorithm(LoadBanlanceAlgorithm lbAlgorithm) {
        this.lbAlgorithm = lbAlgorithm;
    }

    public Short getMainstemClientId() {
        return mainstemClientId;
    }

    public void setMainstemClientId(short mainstemClientId) {
        this.mainstemClientId = mainstemClientId;
    }

    public Integer getMainstemBatchsize() {
        return mainstemBatchsize;
    }

    public Boolean isHome() {
        return home;
    }

    public void setHome(Boolean home) {
        this.home = home;
    }

    public Integer getLoadPoolSize() {
        return loadPoolSize;
    }

    public void setLoadPoolSize(Integer loadPoolSize) {
        this.loadPoolSize = loadPoolSize;
    }

    public Integer getExtractPoolSize() {
        return extractPoolSize;
    }

    public void setExtractPoolSize(int extractPoolSize) {
        this.extractPoolSize = extractPoolSize;
    }

    public Boolean isDumpEvent() {
        return dumpEvent;
    }

    public void setDumpEvent(Boolean dumpEvent) {
        this.dumpEvent = dumpEvent;
    }

    public PipeChooseMode getPipeChooseType() {
        return pipeChooseType;
    }

    public void setPipeChooseType(PipeChooseMode pipeChooseType) {
        this.pipeChooseType = pipeChooseType;
    }

    public Boolean isUseBatch() {
        return useBatch;
    }

    public Boolean getSkipLoadException() {
        // 兼容性处理
        return skipLoadException == null ? false : skipLoadException;
    }

    public void setSkipLoadException(Boolean skipLoadException) {
        this.skipLoadException = skipLoadException;
    }

    public void setSelectorMode(SelectorMode selectorMode) {
        this.selectorMode = selectorMode;
    }

    public Boolean getHome() {
        return home;
    }

    public Boolean getDumpEvent() {
        return dumpEvent;
    }

    public void setExtractPoolSize(Integer extractPoolSize) {
        this.extractPoolSize = extractPoolSize;
    }

    public Boolean getDumpSelector() {
        // 兼容性处理
        return dumpSelector == null ? true : dumpSelector;
    }

    public void setDumpSelector(Boolean dumpSelector) {
        this.dumpSelector = dumpSelector;
    }

    public Boolean getDumpSelectorDetail() {
        // 兼容性处理
        return dumpSelectorDetail == null ? true : dumpSelectorDetail;
    }

    public void setDumpSelectorDetail(Boolean dumpSelectorDetail) {
        this.dumpSelectorDetail = dumpSelectorDetail;
    }

    public SelectorMode getSelectorMode() {
        return selectorMode;
    }

    public ArbitrateMode getArbitrateMode() {
        return arbitrateMode == null ? ArbitrateMode.ZOOKEEPER : arbitrateMode;
    }

    public void setArbitrateMode(ArbitrateMode arbitrateMode) {
        this.arbitrateMode = arbitrateMode;
    }

    public Long getBatchTimeout() {
        return batchTimeout == null ? -1 : batchTimeout;
    }

    public void setBatchTimeout(Long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    public Boolean getFileDetect() {
        return fileDetect == null ? false : fileDetect;
    }

    public void setFileDetect(Boolean fileDetect) {
        this.fileDetect = fileDetect;
    }

    public Integer getFileLoadPoolSize() {
        return fileLoadPoolSize == null ? 5 : fileLoadPoolSize;
    }

    public void setFileLoadPoolSize(Integer fileLoadPoolSize) {
        this.fileLoadPoolSize = fileLoadPoolSize;
    }

    public Boolean getSkipFreedom() {
        return skipFreedom == null ? false : skipFreedom;
    }

    public void setSkipFreedom(Boolean skipFreedom) {
        this.skipFreedom = skipFreedom;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public Boolean getUseLocalFileMutliThread() {
        return useLocalFileMutliThread == null ? false : useLocalFileMutliThread;
    }

    public void setUseLocalFileMutliThread(Boolean useLocalFileMutliThread) {
        this.useLocalFileMutliThread = useLocalFileMutliThread;
    }

    public Boolean getUseExternalIp() {
        return useExternalIp == null ? false : useExternalIp;
    }

    public void setUseExternalIp(Boolean useExternalIp) {
        this.useExternalIp = useExternalIp;
    }

    public Boolean getUseFileEncrypt() {
        return useFileEncrypt == null ? false : useFileEncrypt;
    }

    public void setUseFileEncrypt(Boolean useFileEncrypt) {
        this.useFileEncrypt = useFileEncrypt;
    }

    public Boolean getUseTableTransform() {
        return useTableTransform == null ? true : useTableTransform;
    }

    public void setUseTableTransform(Boolean useTableTransform) {
        this.useTableTransform = useTableTransform;
    }

    public Boolean getSkipNoRow() {
        return skipNoRow == null ? false : skipNoRow;
    }

    public void setSkipNoRow(Boolean skipNoRow) {
        this.skipNoRow = skipNoRow;
    }

    public Boolean getEnableCompatibleMissColumn() {
        return enableCompatibleMissColumn == null ? true : enableCompatibleMissColumn;
    }

    public void setEnableCompatibleMissColumn(Boolean enableCompatibleMissColumn) {
        this.enableCompatibleMissColumn = enableCompatibleMissColumn;
    }

    public String getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(String channelInfo) {
        this.channelInfo = channelInfo;
    }

    public Boolean getDryRun() {
        return dryRun == null ? false : dryRun;
    }

    public Boolean isDryRun() {
        return dryRun == null ? false : dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public Boolean getDdlSync() {
        return ddlSync == null ? true : ddlSync;
    }

    public void setDdlSync(Boolean ddlSync) {
        this.ddlSync = ddlSync;
    }

    public Boolean getSkipDdlException() {
        return skipDdlException == null ? false : skipDdlException;
    }

    public void setSkipDdlException(Boolean skipDdlException) {
        this.skipDdlException = skipDdlException;
    }

    // =============================channel parameter ==========================

    public Boolean getEnableRemedy() {
        return enableRemedy;
    }

    public Boolean isEnableRemedy() {
        return enableRemedy;
    }

    public void setEnableRemedy(Boolean enableRemedy) {
        this.enableRemedy = enableRemedy;
    }

    public SyncMode getSyncMode() {
        return syncMode;
    }

    public void setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
    }

    public SyncConsistency getSyncConsistency() {
        return syncConsistency;
    }

    public void setSyncConsistency(SyncConsistency syncConsistency) {
        this.syncConsistency = syncConsistency;
    }

    public RemedyAlgorithm getRemedyAlgorithm() {
        return remedyAlgorithm;
    }

    public void setRemedyAlgorithm(RemedyAlgorithm remedyAlgorithm) {
        this.remedyAlgorithm = remedyAlgorithm;
    }

    // ============================= system parameter
    // ================================

    public Boolean getUseBatch() {
        return useBatch;
    }

    public void setUseBatch(Boolean useBatch) {
        this.useBatch = useBatch;
    }

    public void setMainstemClientId(Short mainstemClientId) {
        this.mainstemClientId = mainstemClientId;
    }

    public void setMainstemBatchsize(Integer mainstemBatchsize) {
        this.mainstemBatchsize = mainstemBatchsize;
    }

    public String getSystemSchema() {
        return systemSchema;
    }

    public void setSystemSchema(String systemSchema) {
        this.systemSchema = systemSchema;
    }

    public String getSystemMarkTable() {
        return systemMarkTable;
    }

    public void setSystemMarkTable(String systemMarkTable) {
        this.systemMarkTable = systemMarkTable;
    }

    public String getSystemBufferTable() {
        return systemBufferTable;
    }

    public void setSystemBufferTable(String systemBufferTable) {
        this.systemBufferTable = systemBufferTable;
    }

    public RetrieverType getRetriever() {
        return retriever;
    }

    public void setRetriever(RetrieverType retriever) {
        this.retriever = retriever;
    }

    public String getSystemMarkTableColumn() {
        return systemMarkTableColumn;
    }

    public void setSystemMarkTableColumn(String systemMarkTableColumn) {
        this.systemMarkTableColumn = systemMarkTableColumn;
    }

    public String getSystemDualTable() {
        return systemDualTable;
    }

    public void setSystemDualTable(String systemDualTable) {
        this.systemDualTable = systemDualTable;
    }

    public String getSystemMarkTableInfo() {
        return systemMarkTableInfo;
    }

    public void setSystemMarkTableInfo(String systemMarkTableInfo) {
        this.systemMarkTableInfo = systemMarkTableInfo;
    }

    public Integer getRemedyDelayThresoldForMedia() {
        return remedyDelayThresoldForMedia;
    }

    public void setRemedyDelayThresoldForMedia(Integer remedyDelayThresoldForMedia) {
        this.remedyDelayThresoldForMedia = remedyDelayThresoldForMedia;
    }

    public Boolean getSkipSelectException() {
        return skipSelectException == null ? false : skipSelectException;
    }

    public void setSkipSelectException(Boolean skipSelectException) {
        this.skipSelectException = skipSelectException;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, OtterToStringStyle.DEFAULT_STYLE);
    }

}
