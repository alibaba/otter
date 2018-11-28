FROM canal/otter-osbase:v1

MAINTAINER agapple (jianghang115@gmail.com)

# install otter
COPY image/ /tmp/docker/
COPY manager.deployer-*.tar.gz /home/admin/
COPY node.deployer-*.tar.gz /home/admin/

RUN \
    cp -R /tmp/docker/alidata /alidata && \
    chmod +x /alidata/bin/* && \
    mkdir -p /home/admin && \
    cp -R /tmp/docker/admin/* /home/admin/  && \
    /bin/cp -f alidata/bin/lark-wait /usr/bin/lark-wait && \
    mkdir -p /home/admin/manager && \
    tar -xzvf /home/admin/manager.deployer-*.tar.gz -C /home/admin/manager && \
    /bin/rm -f /home/admin/manager.deployer-*.tar.gz && \
    mkdir -p /home/admin/node && \
    tar -xzvf /home/admin/node.deployer-*.tar.gz -C /home/admin/node && \
    /bin/rm -f /home/admin/node.deployer-*.tar.gz && \
    mkdir -p home/admin/manager/logs  && \
    mkdir -p home/admin/node/logs  && \
    mkdir -p home/admin/zkData  && \
    chmod +x /home/admin/*.sh  && \
    chmod +x /home/admin/bin/*.sh  && \
    chown admin: -R /home/admin && \
    yum clean all && \
    true

ENV DOCKER_DEPLOY_TYPE=VM PATH=$PATH:/usr/local/mysql/bin:/usr/local/mysql/scripts

WORKDIR /home/admin

ENTRYPOINT [ "/alidata/bin/main.sh" ]
CMD [ "/home/admin/app.sh" ]
