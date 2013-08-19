/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.alibaba.otter.shared.etl.extend.processor;

import com.alibaba.otter.shared.etl.model.EventData;

/**
 * 业务自定义处理过程
 * 
 * @author jianghang 2012-6-25 下午02:26:36
 * @version 4.1.0
 */
public interface EventProcessor {

    /**
     * 自定义处理单条EventData对象
     * 
     * @return {@link EventData} 返回值=null，需要忽略该条数据
     */
    public EventData process(EventData eventData);
}
