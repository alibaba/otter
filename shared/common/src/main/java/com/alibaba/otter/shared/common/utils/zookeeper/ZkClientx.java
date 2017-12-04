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

package com.alibaba.otter.shared.common.utils.zookeeper;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkConnection;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkLock;
import org.I0Itec.zkclient.exception.ZkBadVersionException;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.shared.common.utils.zookeeper.ZkEventThread.ZkEvent;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;

/**
 * 使用自定义的ZooKeeperx for zk connection
 * 
 * @author jianghang 2012-7-10 下午02:31:15
 * @version 4.1.0
 */
public class ZkClientx implements Watcher {

    // 对于zkclient进行一次缓存，避免一个jvm内部使用多个zk connection
    private static Map<String, ZkClientx> clients = OtterMigrateMap.makeComputingMap(new Function<String, ZkClientx>() {

                                                      public ZkClientx apply(String servers) {
                                                          return new ZkClientx(servers);
                                                      }
                                                  });

    public static ZkClientx getZkClient(String servers) {
        return clients.get(servers);
    }

    public ZkClientx(String serverstring){
        this(serverstring, Integer.MAX_VALUE);
    }

    public ZkClientx(String zkServers, int connectionTimeout){
        this(new ZooKeeperx(zkServers), connectionTimeout);
    }

    public ZkClientx(String zkServers, int sessionTimeout, int connectionTimeout){
        this(new ZooKeeperx(zkServers, sessionTimeout), connectionTimeout);
    }

    public ZkClientx(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer zkSerializer){
        this(new ZooKeeperx(zkServers, sessionTimeout), connectionTimeout, zkSerializer);
    }

    public ZkClientx(IZkConnection connection, int connectionTimeout){
        this(connection, connectionTimeout, new ByteSerializer());
    }

    public ZkClientx(IZkConnection zkConnection, int connectionTimeout, ZkSerializer zkSerializer){
        _connection = zkConnection;
        _zkSerializer = zkSerializer;
        connect(connectionTimeout, this);
    }

    /**
     * 获取链接
     */
    public IZkConnection getConnection() {
        return _connection;
    }

    /**
     * Create a persistent Sequential node.
     * 
     * @param path
     * @param createParents if true all parent dirs are created as well and no {@link ZkNodeExistsException} is thrown
     * in case the path already exists
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public String createPersistentSequential(String path, boolean createParents) throws ZkInterruptedException,
                                                                                IllegalArgumentException, ZkException,
                                                                                RuntimeException {
        try {
            return create(path, null, CreateMode.PERSISTENT_SEQUENTIAL);
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents);
            return createPersistentSequential(path, createParents);
        }
    }

    /**
     * Create a persistent Sequential node.
     * 
     * @param path
     * @param data
     * @param createParents if true all parent dirs are created as well and no {@link ZkNodeExistsException} is thrown
     * in case the path already exists
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public String createPersistentSequential(String path, Object data, boolean createParents)
                                                                                             throws ZkInterruptedException,
                                                                                             IllegalArgumentException,
                                                                                             ZkException,
                                                                                             RuntimeException {
        try {
            return create(path, data, CreateMode.PERSISTENT_SEQUENTIAL);
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents);
            return createPersistentSequential(path, data, createParents);
        }
    }

    /**
     * Create a persistent Sequential node.
     * 
     * @param path
     * @param data
     * @param createParents if true all parent dirs are created as well and no {@link ZkNodeExistsException} is thrown
     * in case the path already exists
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public void createPersistent(String path, Object data, boolean createParents) throws ZkInterruptedException,
                                                                                 IllegalArgumentException, ZkException,
                                                                                 RuntimeException {
        try {
            create(path, data, CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            if (!createParents) {
                throw e;
            }
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents);
            createPersistent(path, data, createParents);
        }
    }

    /**
     * copy from zkclient包中的代码，主要为解决ZkEventThread并发执行
     */
    private static final Logger                                   LOG            = LoggerFactory.getLogger(ZkClientx.class);

    protected IZkConnection                                       _connection;
    private final Map<String, Set<IZkChildListener>>              _childListener = new ConcurrentHashMap<String, Set<IZkChildListener>>();
    private final ConcurrentHashMap<String, Set<IZkDataListener>> _dataListener  = new ConcurrentHashMap<String, Set<IZkDataListener>>();
    private final Set<IZkStateListener>                           _stateListener = new CopyOnWriteArraySet<IZkStateListener>();
    private KeeperState                                           _currentState;
    private final ZkLock                                          _zkEventLock   = new ZkLock();
    private boolean                                               _shutdownTriggered;
    private ZkEventThread                                         _eventThread;
    // TODO PVo remove this later
    private Thread                                                _zookeeperEventThread;
    private ZkSerializer                                          _zkSerializer;

    public void setZkSerializer(ZkSerializer zkSerializer) {
        _zkSerializer = zkSerializer;
    }

    public List<String> subscribeChildChanges(String path, IZkChildListener listener) {
        synchronized (_childListener) {
            Set<IZkChildListener> listeners = _childListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkChildListener>();
                _childListener.put(path, listeners);
            }
            listeners.add(listener);
        }
        return watchForChilds(path);
    }

    public void unsubscribeChildChanges(String path, IZkChildListener childListener) {
        synchronized (_childListener) {
            final Set<IZkChildListener> listeners = _childListener.get(path);
            if (listeners != null) {
                listeners.remove(childListener);
            }
        }
    }

    public void subscribeDataChanges(String path, IZkDataListener listener) {
        Set<IZkDataListener> listeners;
        synchronized (_dataListener) {
            listeners = _dataListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkDataListener>();
                _dataListener.put(path, listeners);
            }
            listeners.add(listener);
        }
        watchForData(path);
        LOG.debug("Subscribed data changes for " + path);
    }

    public void unsubscribeDataChanges(String path, IZkDataListener dataListener) {
        synchronized (_dataListener) {
            final Set<IZkDataListener> listeners = _dataListener.get(path);
            if (listeners != null) {
                listeners.remove(dataListener);
            }
            if (listeners == null || listeners.isEmpty()) {
                _dataListener.remove(path);
            }
        }
    }

    public void subscribeStateChanges(final IZkStateListener listener) {
        synchronized (_stateListener) {
            _stateListener.add(listener);
        }
    }

    public void unsubscribeStateChanges(IZkStateListener stateListener) {
        synchronized (_stateListener) {
            _stateListener.remove(stateListener);
        }
    }

    public void unsubscribeAll() {
        synchronized (_childListener) {
            _childListener.clear();
        }
        synchronized (_dataListener) {
            _dataListener.clear();
        }
        synchronized (_stateListener) {
            _stateListener.clear();
        }
    }

    // </listeners>

    /**
     * Create a persistent node.
     * 
     * @param path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public void createPersistent(String path) throws ZkInterruptedException, IllegalArgumentException, ZkException,
                                             RuntimeException {
        createPersistent(path, false);
    }

    /**
     * Create a persistent node.
     * 
     * @param path
     * @param createParents if true all parent dirs are created as well and no {@link ZkNodeExistsException} is thrown
     * in case the path already exists
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public void createPersistent(String path, boolean createParents) throws ZkInterruptedException,
                                                                    IllegalArgumentException, ZkException,
                                                                    RuntimeException {
        try {
            create(path, null, CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            if (!createParents) {
                throw e;
            }
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents);
            createPersistent(path, createParents);
        }
    }

    /**
     * Create a persistent node.
     * 
     * @param path
     * @param data
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public void createPersistent(String path, Object data) throws ZkInterruptedException, IllegalArgumentException,
                                                          ZkException, RuntimeException {
        create(path, data, CreateMode.PERSISTENT);
    }

    /**
     * Create a persistent, sequental node.
     * 
     * @param path
     * @param data
     * @return create node's path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public String createPersistentSequential(String path, Object data) throws ZkInterruptedException,
                                                                      IllegalArgumentException, ZkException,
                                                                      RuntimeException {
        return create(path, data, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * Create an ephemeral node.
     * 
     * @param path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public void createEphemeral(final String path) throws ZkInterruptedException, IllegalArgumentException,
                                                  ZkException, RuntimeException {
        create(path, null, CreateMode.EPHEMERAL);
    }

    /**
     * Create a node.
     * 
     * @param path
     * @param data
     * @param mode
     * @return create node's path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public String create(final String path, Object data, final CreateMode mode) throws ZkInterruptedException,
                                                                               IllegalArgumentException, ZkException,
                                                                               RuntimeException {
        if (path == null) {
            throw new NullPointerException("path must not be null.");
        }
        final byte[] bytes = data == null ? null : serialize(data);

        return retryUntilConnected(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return _connection.create(path, bytes, mode);
            }
        });
    }

    /**
     * Create an ephemeral node.
     * 
     * @param path
     * @param data
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public void createEphemeral(final String path, final Object data) throws ZkInterruptedException,
                                                                     IllegalArgumentException, ZkException,
                                                                     RuntimeException {
        create(path, data, CreateMode.EPHEMERAL);
    }

    /**
     * Create an ephemeral, sequential node.
     * 
     * @param path
     * @param data
     * @return created path
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs
     */
    public String createEphemeralSequential(final String path, final Object data) throws ZkInterruptedException,
                                                                                 IllegalArgumentException, ZkException,
                                                                                 RuntimeException {
        return create(path, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void process(WatchedEvent event) {
        LOG.debug("Received event: " + event);
        _zookeeperEventThread = Thread.currentThread();

        boolean stateChanged = event.getPath() == null;
        boolean znodeChanged = event.getPath() != null;
        boolean dataChanged = event.getType() == EventType.NodeDataChanged || event.getType() == EventType.NodeDeleted
                              || event.getType() == EventType.NodeCreated
                              || event.getType() == EventType.NodeChildrenChanged;

        getEventLock().lock();
        try {

            // We might have to install child change event listener if a new node was created
            if (getShutdownTrigger()) {
                LOG.debug("ignoring event '{" + event.getType() + " | " + event.getPath()
                          + "}' since shutdown triggered");
                return;
            }
            if (stateChanged) {
                processStateChanged(event);
            }
            if (dataChanged) {
                processDataOrChildChange(event);
            }
        } finally {
            if (stateChanged) {
                getEventLock().getStateChangedCondition().signalAll();

                // If the session expired we have to signal all conditions, because watches might have been removed and
                // there is no guarantee that those
                // conditions will be signaled at all after an Expired event
                // TODO PVo write a test for this
                if (event.getState() == KeeperState.Expired) {
                    getEventLock().getZNodeEventCondition().signalAll();
                    getEventLock().getDataChangedCondition().signalAll();
                    // We also have to notify all listeners that something might have changed
                    fireAllEvents();
                }
            }
            if (znodeChanged) {
                getEventLock().getZNodeEventCondition().signalAll();
            }
            if (dataChanged) {
                getEventLock().getDataChangedCondition().signalAll();
            }
            getEventLock().unlock();
            LOG.debug("Leaving process event");
        }
    }

    private void fireAllEvents() {
        for (Entry<String, Set<IZkChildListener>> entry : _childListener.entrySet()) {
            fireChildChangedEvents(entry.getKey(), entry.getValue());
        }
        for (Entry<String, Set<IZkDataListener>> entry : _dataListener.entrySet()) {
            fireDataChangedEvents(entry.getKey(), entry.getValue());
        }
    }

    public List<String> getChildren(String path) {
        return getChildren(path, hasListeners(path));
    }

    protected List<String> getChildren(final String path, final boolean watch) {
        return retryUntilConnected(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                return _connection.getChildren(path, watch);
            }
        });
    }

    /**
     * Counts number of children for the given path.
     * 
     * @param path
     * @return number of children or 0 if path does not exist.
     */
    public int countChildren(String path) {
        try {
            return getChildren(path).size();
        } catch (ZkNoNodeException e) {
            return 0;
        }
    }

    protected boolean exists(final String path, final boolean watch) {
        return retryUntilConnected(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return _connection.exists(path, watch);
            }
        });
    }

    public boolean exists(final String path) {
        return exists(path, hasListeners(path));
    }

    private void processStateChanged(WatchedEvent event) {
        LOG.info("zookeeper state changed (" + event.getState() + ")");
        setCurrentState(event.getState());
        if (getShutdownTrigger()) {
            return;
        }
        try {
            fireStateChangedEvent(event.getState());

            if (event.getState() == KeeperState.Expired) {
                reconnect();
                fireNewSessionEvents();
            }
        } catch (final Exception e) {
            throw new RuntimeException("Exception while restarting zk client", e);
        }
    }

    private void fireNewSessionEvents() {
        for (final IZkStateListener stateListener : _stateListener) {
            _eventThread.send(new ZkEvent("New session event sent to " + stateListener) {

                @Override
                public void run() throws Exception {
                    stateListener.handleNewSession();
                }
            });
        }
    }

    private void fireStateChangedEvent(final KeeperState state) {
        for (final IZkStateListener stateListener : _stateListener) {
            _eventThread.send(new ZkEvent("State changed to " + state + " sent to " + stateListener) {

                @Override
                public void run() throws Exception {
                    stateListener.handleStateChanged(state);
                }
            });
        }
    }

    private boolean hasListeners(String path) {
        Set<IZkDataListener> dataListeners = _dataListener.get(path);
        if (dataListeners != null && dataListeners.size() > 0) {
            return true;
        }
        Set<IZkChildListener> childListeners = _childListener.get(path);
        if (childListeners != null && childListeners.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean deleteRecursive(String path) {
        List<String> children;
        try {
            children = getChildren(path, false);
        } catch (ZkNoNodeException e) {
            return true;
        }

        for (String subPath : children) {
            if (!deleteRecursive(path + "/" + subPath)) {
                return false;
            }
        }

        return delete(path);
    }

    private void processDataOrChildChange(WatchedEvent event) {
        final String path = event.getPath();

        if (event.getType() == EventType.NodeChildrenChanged || event.getType() == EventType.NodeCreated
            || event.getType() == EventType.NodeDeleted) {
            Set<IZkChildListener> childListeners = _childListener.get(path);
            if (childListeners != null && !childListeners.isEmpty()) {
                fireChildChangedEvents(path, childListeners);
            }
        }

        if (event.getType() == EventType.NodeDataChanged || event.getType() == EventType.NodeDeleted
            || event.getType() == EventType.NodeCreated) {
            Set<IZkDataListener> listeners = _dataListener.get(path);
            if (listeners != null && !listeners.isEmpty()) {
                fireDataChangedEvents(event.getPath(), listeners);
            }
        }
    }

    private void fireDataChangedEvents(final String path, Set<IZkDataListener> listeners) {
        for (final IZkDataListener listener : listeners) {
            _eventThread.send(new ZkEvent("Data of " + path + " changed sent to " + listener) {

                @Override
                public void run() throws Exception {
                    // reinstall watch
                    exists(path, true);
                    try {
                        Object data = readData(path, null, true);
                        listener.handleDataChange(path, data);
                    } catch (ZkNoNodeException e) {
                        listener.handleDataDeleted(path);
                    }
                }
            });
        }
    }

    private void fireChildChangedEvents(final String path, Set<IZkChildListener> childListeners) {
        try {
            // reinstall the watch
            for (final IZkChildListener listener : childListeners) {
                _eventThread.send(new ZkEvent("Children of " + path + " changed sent to " + listener) {

                    @Override
                    public void run() throws Exception {
                        try {
                            // if the node doesn't exist we should listen for the root node to reappear
                            exists(path);
                            List<String> children = getChildren(path);
                            listener.handleChildChange(path, children);
                        } catch (ZkNoNodeException e) {
                            listener.handleChildChange(path, null);
                        }
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Failed to fire child changed event. Unable to getChildren.  ", e);
        }
    }

    public boolean waitUntilExists(String path, TimeUnit timeUnit, long time) throws ZkInterruptedException {
        Date timeout = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));
        LOG.debug("Waiting until znode '" + path + "' becomes available.");
        if (exists(path)) {
            return true;
        }
        acquireEventLock();
        try {
            while (!exists(path, true)) {
                boolean gotSignal = getEventLock().getZNodeEventCondition().awaitUntil(timeout);
                if (!gotSignal) {
                    return false;
                }
            }
            return true;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    protected Set<IZkDataListener> getDataListener(String path) {
        return _dataListener.get(path);
    }

    public void showFolders(OutputStream output) {
        throw new UnsupportedOperationException();
        // try {
        // output.write(ZkPathUtil.toString(this).getBytes());
        // } catch (final IOException e) {
        // e.printStackTrace();
        // }
    }

    public void waitUntilConnected() throws ZkInterruptedException {
        waitUntilConnected(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitUntilConnected(long time, TimeUnit timeUnit) throws ZkInterruptedException {
        return waitForKeeperState(KeeperState.SyncConnected, time, timeUnit);
    }

    public boolean waitForKeeperState(KeeperState keeperState, long time, TimeUnit timeUnit)
                                                                                            throws ZkInterruptedException {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        Date timeout = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));

        LOG.debug("Waiting for keeper state " + keeperState);
        acquireEventLock();
        try {
            boolean stillWaiting = true;
            while (_currentState != keeperState) {
                if (!stillWaiting) {
                    return false;
                }
                stillWaiting = getEventLock().getStateChangedCondition().awaitUntil(timeout);
            }
            LOG.debug("State is " + _currentState);
            return true;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    private void acquireEventLock() {
        try {
            getEventLock().lockInterruptibly();
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    /**
     * @param <T>
     * @param callable
     * @return result of Callable
     * @throws ZkInterruptedException if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException if called from anything except the ZooKeeper event thread
     * @throws ZkException if any ZooKeeper exception occurred
     * @throws RuntimeException if any other exception occurs from invoking the Callable
     */
    public <T> T retryUntilConnected(Callable<T> callable) throws ZkInterruptedException, IllegalArgumentException,
                                                          ZkException, RuntimeException {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        while (true) {
            try {
                return callable.call();
            } catch (ConnectionLossException e) {
                // we give the event thread some time to update the status to 'Disconnected'
                Thread.yield();
                waitUntilConnected();
            } catch (SessionExpiredException e) {
                // we give the event thread some time to update the status to 'Expired'
                Thread.yield();
                waitUntilConnected();
            } catch (KeeperException e) {
                throw ZkException.create(e);
            } catch (InterruptedException e) {
                throw new ZkInterruptedException(e);
            } catch (Exception e) {
                throw ExceptionUtil.convertToRuntimeException(e);
            }
        }
    }

    public void setCurrentState(KeeperState currentState) {
        getEventLock().lock();
        try {
            _currentState = currentState;
        } finally {
            getEventLock().unlock();
        }
    }

    /**
     * Returns a mutex all zookeeper events are synchronized aginst. So in case you need to do something without getting
     * any zookeeper event interruption synchronize against this mutex. Also all threads waiting on this mutex object
     * will be notified on an event.
     * 
     * @return the mutex.
     */
    public ZkLock getEventLock() {
        return _zkEventLock;
    }

    public boolean delete(final String path) {
        try {
            retryUntilConnected(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    _connection.delete(path);
                    return null;
                }
            });

            return true;
        } catch (ZkNoNodeException e) {
            return false;
        }
    }

    private byte[] serialize(Object data) {
        return _zkSerializer.serialize(data);
    }

    @SuppressWarnings("unchecked")
    private <T extends Object> T derializable(byte[] data) {
        if (data == null) {
            return null;
        }
        return (T) _zkSerializer.deserialize(data);
    }

    public <T extends Object> T readData(String path) {
        return (T) readData(path, false);
    }

    public <T extends Object> T readData(String path, boolean returnNullIfPathNotExists) {
        T data = null;
        try {
            data = (T) readData(path, null);
        } catch (ZkNoNodeException e) {
            if (!returnNullIfPathNotExists) {
                throw e;
            }
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T readData(String path, Stat stat) {
        return (T) readData(path, stat, hasListeners(path));
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> T readData(final String path, final Stat stat, final boolean watch) {
        byte[] data = retryUntilConnected(new Callable<byte[]>() {

            @Override
            public byte[] call() throws Exception {
                return _connection.readData(path, stat, watch);
            }
        });
        return (T) derializable(data);
    }

    public void writeData(String path, Object object) {
        writeData(path, object, -1);
    }

    /**
     * Updates data of an existing znode. The current content of the znode is passed to the {@link DataUpdater} that is
     * passed into this method, which returns the new content. The new content is only written back to ZooKeeper if
     * nobody has modified the given znode in between. If a concurrent change has been detected the new data of the
     * znode is passed to the updater once again until the new contents can be successfully written back to ZooKeeper.
     * 
     * @param <T>
     * @param path The path of the znode.
     * @param updater Updater that creates the new contents.
     */
    public <T extends Object> void updateDataSerialized(String path, DataUpdater<T> updater) {
        Stat stat = new Stat();
        boolean retry;
        do {
            retry = false;
            try {
                T oldData = (T) readData(path, stat);
                T newData = updater.update(oldData);
                writeData(path, newData, stat.getVersion());
            } catch (ZkBadVersionException e) {
                retry = true;
            }
        } while (retry);
    }

    public void writeData(final String path, Object datat, final int expectedVersion) {
        final byte[] data = serialize(datat);
        retryUntilConnected(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                _connection.writeData(path, data, expectedVersion);
                return null;
            }
        });
    }

    public void watchForData(final String path) {
        retryUntilConnected(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                _connection.exists(path, true);
                return null;
            }
        });
    }

    /**
     * Installs a child watch for the given path.
     * 
     * @param path
     * @return the current children of the path or null if the zk node with the given path doesn't exist.
     */
    public List<String> watchForChilds(final String path) {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        return retryUntilConnected(new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                exists(path, true);
                try {
                    return getChildren(path, true);
                } catch (ZkNoNodeException e) {
                    // ignore, the "exists" watch will listen for the parent node to appear
                }
                return null;
            }
        });
    }

    /**
     * Connect to ZooKeeper.
     * 
     * @param maxMsToWaitUntilConnected
     * @param watcher
     * @throws ZkInterruptedException if the connection timed out due to thread interruption
     * @throws ZkTimeoutException if the connection timed out
     * @throws IllegalStateException if the connection timed out due to thread interruption
     */
    public void connect(final long maxMsToWaitUntilConnected, Watcher watcher) throws ZkInterruptedException,
                                                                              ZkTimeoutException, IllegalStateException {
        boolean started = false;
        try {
            getEventLock().lockInterruptibly();
            setShutdownTrigger(false);
            _eventThread = new ZkEventThread(_connection.getServers());
            _eventThread.start();
            _connection.connect(watcher);

            LOG.debug("Awaiting connection to Zookeeper server");
            if (!waitUntilConnected(maxMsToWaitUntilConnected, TimeUnit.MILLISECONDS)) {
                throw new ZkTimeoutException("Unable to connect to zookeeper server within timeout: "
                                             + maxMsToWaitUntilConnected);
            }
            started = true;
        } catch (InterruptedException e) {
            States state = _connection.getZookeeperState();
            throw new IllegalStateException("Not connected with zookeeper server yet. Current state is " + state);
        } finally {
            getEventLock().unlock();

            // we should close the zookeeper instance, otherwise it would keep
            // on trying to connect
            if (!started) {
                close();
            }
        }
    }

    public long getCreationTime(String path) {
        try {
            getEventLock().lockInterruptibly();
            return _connection.getCreateTime(path);
        } catch (KeeperException e) {
            throw ZkException.create(e);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    /**
     * Close the client.
     * 
     * @throws ZkInterruptedException
     */
    public void close() throws ZkInterruptedException {
        if (_connection == null) {
            return;
        }
        LOG.debug("Closing ZkClient...");
        getEventLock().lock();
        try {
            setShutdownTrigger(true);
            _eventThread.interrupt();
            _eventThread.join(2000);
            _connection.close();
            _connection = null;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
        LOG.debug("Closing ZkClient...done");
    }

    private void reconnect() {
        getEventLock().lock();
        try {
            _connection.close();
            _connection.connect(this);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    public void setShutdownTrigger(boolean triggerState) {
        _shutdownTriggered = triggerState;
    }

    public boolean getShutdownTrigger() {
        return _shutdownTriggered;
    }

    public int numberOfListeners() {
        int listeners = 0;
        for (Set<IZkChildListener> childListeners : _childListener.values()) {
            listeners += childListeners.size();
        }
        for (Set<IZkDataListener> dataListeners : _dataListener.values()) {
            listeners += dataListeners.size();
        }
        listeners += _stateListener.size();

        return listeners;
    }
}
