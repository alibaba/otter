#!/bin/bash
set -e

source /etc/profile
export JAVA_HOME=/usr/java/latest
export PATH=$JAVA_HOME/bin:$PATH
touch /tmp/start.log
chown admin: /tmp/start.log
chown admin: /home/admin/manager
chown admin: /home/admin/node
chown admin: /home/admin/zkData
host=`hostname -i`

# default config
if [ -z "${RUN_MODE}" ]; then
    RUN_MODE="ALL"
fi

if [ -z "${MYSQL_USER_PASSWORD}" ]; then
    MYSQL_USER_PASSWORD="otter"
fi
if [ -z "${OTTER_MANAGER_MYSQL}" ]; then
    OTTER_MANAGER_MYSQL="127.0.0.1:3306"
fi
# waitterm
#   wait TERM/INT signal.
#   see: http://veithen.github.io/2014/11/16/sigterm-propagation.html
waitterm() {
        local PID
        # any process to block
        tail -f /dev/null &
        PID="$!"
        # setup trap, could do nothing, or just kill the blocker
        trap "kill -TERM ${PID}" TERM INT
        # wait for signal, ignore wait exit code
        wait "${PID}" || true
        # clear trap
        trap - TERM INT
        # wait blocker, ignore blocker exit code
        wait "${PID}" 2>/dev/null || true
}

# waittermpid "${PIDFILE}".
#   monitor process by pidfile && wait TERM/INT signal.
#   if the process disappeared, return 1, means exit with ERROR.
#   if TERM or INT signal received, return 0, means OK to exit.
waittermpid() {
        local PIDFILE PID do_run error
        PIDFILE="${1?}"
        do_run=true
        error=0
        trap "do_run=false" TERM INT
        while "${do_run}" ; do
                PID="$(cat "${PIDFILE}")"
                if ! ps -p "${PID}" >/dev/null 2>&1 ; then
                        do_run=false
                        error=1
                else
                        sleep 1
                fi
        done
        trap - TERM INT
        return "${error}"
}


function checkStart() {
    local name=$1
    local cmd=$2
    local timeout=$3
    cost=5
    while [ $timeout -gt 0 ]; do
        ST=`eval $cmd`
        if [ "$ST" == "0" ]; then
            sleep 1
            let timeout=timeout-1
            let cost=cost+1
        elif [ "$ST" == "" ]; then
            sleep 1
            let timeout=timeout-1
            let cost=cost+1
        else
            break
        fi
    done
    echo "$name start successful"
}

function start_zookeeper() {
    echo "start zookeeper ..."
    # start zookeeper
    rm -f /home/admin/zkData/myid
    sed -i '/^server\..*/d' /home/admin/zookeeper-3.4.13/conf/zoo.cfg
    su admin -c "mkdir -p /home/admin/zkData; cd /home/admin/zkData; /home/admin/zookeeper-3.4.13/bin/zkServer.sh start >> /home/admin/zkData/zookeeper.log 2>&1"
    sleep 5
    #check start
    checkStart "zookeeper" "echo stat | nc 127.0.0.1 2181 | grep -c Outstanding" 30
}

function stop_zookeeper() {
    # stop zookeeper
    echo "stop zookeeper"
    su admin -c 'mkdir -p /home/admin/zkData; cd /home/admin/zkData; /home/admin/zookeeper-3.4.13/bin/zkServer.sh stop >> /home/admin/zkData/zookeeper.log 2>&1'
    echo "stop zookeeper successful ..."
}

function start_manager() {
    echo "start manager ..."
    # start manager
    if [ -n "${OTTER_MANAGER_MYSQL}" ] ; then
        cmd="sed -i -e 's/^otter.database.driver.url.*$/otter.database.driver.url = jdbc:mysql:\/\/${OTTER_MANAGER_MYSQL}\/otter/' /home/admin/manager/conf/otter.properties"
        eval $cmd
        cmd="sed -i -e 's/^otter.database.driver.username.*$/otter.database.driver.username = ${MYSQL_USER}/' /home/admin/manager/conf/otter.properties"
        eval $cmd
        cmd="sed -i -e 's/^otter.database.driver.password.*$/otter.database.driver.password = ${MYSQL_USER_PASSWORD}/' /home/admin/manager/conf/otter.properties"
        eval $cmd
        cmd="sed -i -e 's/^otter.communication.manager.port.*$/otter.communication.manager.port = 8081/' /home/admin/manager/conf/otter.properties"
        eval $cmd
        cmd="sed -i -e 's/^otter.domainName.*$/otter.domainName = ${host}/' /home/admin/manager/conf/otter.properties"
        eval $cmd
    fi
    su admin -c "cd /home/admin/manager/bin ; sh startup.sh 1>>/tmp/start.log 2>&1"
    #check start
    sleep 5
    checkStart "manager" "nc 127.0.0.1 8080 -w 1 -z | wc -l" 60
}

function stop_manager() {
    # stop manager
    echo "stop manager"
    su admin -c 'cd /home/admin/manager/bin; sh stop.sh 1>>/tmp/start.log 2>&1'
    echo "stop manager successful ..."
}

function start_node() {
    echo "start node ..."
    # start node
    cmd="sed -i -e 's/^otter.manager.address.*$/otter.manager.address = 127.0.0.1:8081/' /home/admin/node/conf/otter.properties"
    eval $cmd
    
    su admin -c 'cd /home/admin/node/bin/ && echo 1 > /home/admin/node/conf/nid && sh startup.sh 1>>/tmp/start.log 2>&1'
    sleep 5
    #check start
    checkStart "node" "nc 127.0.0.1 2088 -w 1 -z | wc -l" 30
}

function stop_node() {
    # stop node
    echo "stop node"
    su admin -c 'cd /home/admin/node/bin/ && sh stop.sh'
    echo "stop node successful ..."
}

function start_mysql() {
    echo "start mysql ..."
    # start mysql
    MYSQL_ROOT_PASSWORD=Hello1234
    MYSQL_USER=otter
    MYSQL_DATABASE=otter
    if [ -z "$(ls -A /var/lib/mysql)" ]; then
        mysql_install_db --user=mysql --datadir=/var/lib/mysql 1>>/tmp/start.log 2>&1
        # These statements _must_ be on individual lines, and _must_ end with
        # semicolons (no line breaks or comments are permitted).
        # TODO proper SQL escaping on ALL the things D:
        TEMP_FILE='/tmp/init.sql'
        echo "update mysql.user set password=password('${MYSQL_ROOT_PASSWORD}') where user='root';" >> $TEMP_FILE
        echo "grant all privileges on *.* to 'root'@'%' WITH GRANT OPTION ;" >> $TEMP_FILE
        echo "create database if not exists $MYSQL_DATABASE ;" >> $TEMP_FILE
        echo "create user $MYSQL_USER identified by '$MYSQL_USER_PASSWORD' ;" >> $TEMP_FILE
        echo "grant all privileges on $MYSQL_DATABASE.* to '$MYSQL_USER'@'%' identified by '$MYSQL_USER_PASSWORD' ;" >> $TEMP_FILE
        echo "grant all privileges on $MYSQL_DATABASE.* to '$MYSQL_USER'@'localhost' identified by '$MYSQL_USER_PASSWORD' ;" >> $TEMP_FILE
        echo "flush privileges;" >> $TEMP_FILE
        service mysqld start
        checkStart "mysql" "echo 'show status' | mysql -s -h127.0.0.1 -P3306 -uroot | grep -c Uptime" 30
        mysql -h127.0.0.1 -uroot -e "source $TEMP_FILE" 1>>/tmp/start.log 2>&1

        cmd="sed -i -e 's/#OTTER_MY_ZK#/127.0.0.1:2181/' /home/admin/bin/ddl.sql"
        eval $cmd
        cmd="sed -i -e 's/#OTTER_NODE_HOST#/127.0.0.1/' /home/admin/bin/ddl.sql"
        eval $cmd
        cmd="mysql -h127.0.0.1 -u$MYSQL_USER -p$MYSQL_USER_PASSWORD $MYSQL_DATABASE -e 'source /home/admin/bin/ddl.sql' 1>>/tmp/start.log 2>&1"
        eval $cmd
        /bin/rm -f /home/admin/bin/ddl.sql
    else
        chown -R mysql:mysql /var/lib/mysql
        service mysqld start
        #check start
        checkStart "mysql" "echo 'show status' | mysql -b -s  -h127.0.0.1 -P3306 -uroot -p$MYSQL_ROOT_PASSWORD | grep -c Uptime" 30
    fi
}

function stop_mysql() {
    echo "stop mysql ..."
    # stop mysql
    service mysqld stop
    echo "stop mysql successful ..."
}

echo "==> START ..."
start_mysql
start_zookeeper
start_manager
start_node
echo "you can visit manager link : http://$host:8080/ , just have fun !"

echo "==> START SUCCESSFUL ..."

tail -f /dev/null &
# wait TERM signal
waitterm

echo "==> STOP"

stop_node
stop_manager
stop_zookeeper
stop_zookeeper
stop_mysql

echo "==> STOP SUCCESSFUL ..."