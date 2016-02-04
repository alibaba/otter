package com.alibaba.otter.node.etl.select.selector.canal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.alibaba.otter.node.etl.select.exceptions.SelectException;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

/**
 * Created with Intellij IDEA. Author: yinxiu Date: 2016-01-11 Time: 16:12
 */
public class CanalFilterSupport {

    /**
     * 构建filter 表达式
     */
    public static String makeFilterExpression(Pipeline pipeline) {
        List<DataMediaPair> dataMediaPairs = pipeline.getPairs();
        if (dataMediaPairs.isEmpty()) {
            throw new SelectException("ERROR ## the pair is empty,the pipeline id = " + pipeline.getId());
        }

        Set<String> mediaNames = new HashSet<String>();
        for (DataMediaPair dataMediaPair : dataMediaPairs) {
            DataMedia.ModeValue namespaceMode = dataMediaPair.getSource().getNamespaceMode();
            DataMedia.ModeValue nameMode = dataMediaPair.getSource().getNameMode();

            if (namespaceMode.getMode().isSingle()) {
                buildFilter(mediaNames, namespaceMode.getSingleValue(), nameMode, false);
            } else if (namespaceMode.getMode().isMulti()) {
                for (String namespace : namespaceMode.getMultiValue()) {
                    buildFilter(mediaNames, namespace, nameMode, false);
                }
            } else if (namespaceMode.getMode().isWildCard()) {
                buildFilter(mediaNames, namespaceMode.getSingleValue(), nameMode, true);
            }
        }

        StringBuilder result = new StringBuilder();
        Iterator<String> iter = mediaNames.iterator();
        int i = -1;
        while (iter.hasNext()) {
            i++;
            if (i == 0) {
                result.append(iter.next());
            } else {
                result.append(",").append(iter.next());
            }
        }

        String markTable = pipeline.getParameters().getSystemSchema() + "."
                           + pipeline.getParameters().getSystemMarkTable();
        String bufferTable = pipeline.getParameters().getSystemSchema() + "."
                             + pipeline.getParameters().getSystemBufferTable();
        String dualTable = pipeline.getParameters().getSystemSchema() + "."
                           + pipeline.getParameters().getSystemDualTable();

        if (!mediaNames.contains(markTable)) {
            result.append(",").append(markTable);
        }

        if (!mediaNames.contains(bufferTable)) {
            result.append(",").append(bufferTable);
        }

        if (!mediaNames.contains(dualTable)) {
            result.append(",").append(dualTable);
        }

        // String otterTable = pipeline.getParameters().getSystemSchema() +
        // "\\..*";
        // if (!mediaNames.contains(otterTable)) {
        // result.append(",").append(otterTable);
        // }

        return result.toString();
    }

    private static void buildFilter(Set<String> mediaNames, String namespace, DataMedia.ModeValue nameMode,
                                    boolean wildcard) {
        String splitChar = ".";
        if (wildcard) {
            splitChar = "\\.";
        }

        if (nameMode.getMode().isSingle()) {
            mediaNames.add(namespace + splitChar + nameMode.getSingleValue());
        } else if (nameMode.getMode().isMulti()) {
            for (String name : nameMode.getMultiValue()) {
                mediaNames.add(namespace + splitChar + name);
            }
        } else if (nameMode.getMode().isWildCard()) {
            mediaNames.add(namespace + "\\." + nameMode.getSingleValue());
        }
    }
}
