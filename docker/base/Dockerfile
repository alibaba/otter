FROM canal/osbase:v1

MAINTAINER agapple (jianghang115@gmail.com)

RUN \
    groupadd -r mysql && useradd -r -g mysql mysql && \
    yum -y install wget mysql-server --nogpgcheck && \
    yum clean all && \
    wget -q https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz -O /home/admin/zookeeper-3.4.13.tar.gz && \
    tar -xzvf /home/admin/zookeeper-*.tar.gz -C /home/admin && \
    /bin/cp -rf /home/admin/zookeeper-3.4.13/conf/zoo_sample.cfg /home/admin/zookeeper-3.4.13/conf/zoo.cfg && \
    sed -i -e 's/^dataDir=\/tmp\/zookeeper$/dataDir=\/home\/admin\/zkData/' /home/admin/zookeeper-3.4.13/conf/zoo.cfg && \
    sed -i -e 's/^#autopurge/autopurge/' /home/admin/zookeeper-3.4.13/conf/zoo.cfg && \
    /bin/rm -f /home/admin/zookeeper-3.4.13.tar.gz && \
    true

CMD ["/bin/bash"]
