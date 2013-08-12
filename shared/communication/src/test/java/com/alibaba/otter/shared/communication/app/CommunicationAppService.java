package com.alibaba.otter.shared.communication.app;

import com.alibaba.otter.shared.communication.app.event.AppCreateEvent;
import com.alibaba.otter.shared.communication.app.event.AppDeleteEvent;
import com.alibaba.otter.shared.communication.app.event.AppUpdateEvent;
import com.alibaba.otter.shared.communication.core.model.Event;

/**
 * @author jianghang 2011-9-13 下午08:30:48
 */
public interface CommunicationAppService {

    public boolean onCreate(AppCreateEvent event);

    public boolean onUpdate(AppUpdateEvent event);

    public boolean onDelete(AppDeleteEvent event);

    public Event handleEvent(Event event);
}
