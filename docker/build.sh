#!/bin/bash

current_path=`pwd`
case "`uname`" in
    Darwin)
        bin_abs_path=`cd $(dirname $0); pwd`
        ;;
    Linux)
        bin_abs_path=$(readlink -f $(dirname $0))
        ;;
    *)
        bin_abs_path=`cd $(dirname $0); pwd`
        ;;
esac
BASE=${bin_abs_path}

if [ "$1" == "base" ] ; then
    docker build --no-cache -t canal/otter-osbase $BASE/base
else 
    rm -rf $BASE/node.*.tar.gz ; 
    rm -rf $BASE/manager.*.tar.gz ; 
    cd $BASE/../ && mvn clean package -Dmaven.test.skip -Denv=release && cd $current_path ;
    cp $BASE/../target/node.deployer-*.tar.gz $BASE/
    cp $BASE/../target/manager.deployer-*.tar.gz $BASE/
    docker build --no-cache -t canal/otter-all $BASE/
fi

