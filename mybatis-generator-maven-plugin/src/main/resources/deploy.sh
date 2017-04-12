#!/bin/sh
#
#   ~ Copyright (c) 2017. 秒差距科技
#

#服务器 root@255.255.255.255
HOST=$1
#远程目录
SERVER_PATH=$2
if ssh ${HOST} test -e "$SERVER_PATH"
then echo "SERVER_PATH ${SERVER_PATH}"
else ssh ${HOST} "mkdir -p ${SERVER_PATH}"
echo "ssh ${HOST} mkdir -p ${SERVER_PATH}"
fi

#项目base目录
BASEDIR=$3
#cd $4
#echo "cd ${BASEDIR}"

JAR_NAME=`ls -t ${BASEDIR}/target/*.jar|head -1`
#该命令的作用是去掉变量var从左边算起的最后一个'/'字符及其左边的内容，返回从左边算起的最后一个'/'（不含该字符）的右边的内容
JAR_NAME=${JAR_NAME##*/}

logfile=$5
tail()
{
#		nohup java -jar gemini-1.0-SNAPSHOT.jar > /dev/null 2>&1 & echo $! > run.pid
	#               if java $ARGS -jar $SERVER> /dev/null 2>&1 &
	echo "ssh $HOST \"tail -f $SERVER_PATH/${logfile:-nohup.out}\""
    ssh ${HOST} "tail -f $SERVER_PATH/${logfile:-nohup.out}"
}
scp()
{


    echo "发现jar文件：${JAR_NAME}"

    if ssh ${HOST} test -e "$SERVER_PATH/server.sh"
    then
        echo "[1/3] server.sh 已经存在"
    else
         echo "[1/3] 上传 server.sh 到 $HOST:$SERVER_PATH/server.sh"
#         scp命令用不起，会死循环
         echo `rsync -avz -e ssh /tmp/com.parsec/server.sh ${HOST}:${SERVER_PATH}/`
         ssh ${HOST} "chmod +x ${SERVER_PATH}/server.sh"
    fi

#    if ssh ${HOST} test -e "$SERVER_PATH/application.properties"
#    then
#        echo application.properties exist
#    else
#         echo `rsync -avz -e ssh ../src/main/resources/application.properties ${HOST}:${SERVER_PATH}/`
#    fi

    if ssh ${HOST} test -e "$SERVER_PATH/logback.xml"
    then
        echo "[2/3]logback.xml 已经存在"
    else
         echo "[2/3] 上传 logback.xml 到 $HOST:$SERVER_PATH/logback.xml"
         echo `rsync -avz -e ssh ${BASEDIR}/src/main/resources/logback.xml ${HOST}:${SERVER_PATH}/`
    fi

    echo "[3/3] 上传 $JAR_NAME 到 $HOST:$SERVER_PATH"
    echo `rsync -avz -e ssh ${BASEDIR}/target/${JAR_NAME} ${HOST}:${SERVER_PATH}/`
}

server_start()
{
    ssh -f ${HOST} " cd $SERVER_PATH; pwd;  ./server.sh start >nohup.out 2>&1 &   "
}
server_restart()
{
    ssh ${HOST} "cd $SERVER_PATH && ./server.sh restart >nohup.out 2>&1 & "
}

server_stop()
{
    ssh ${HOST} "cd $SERVER_PATH && ./server.sh stop"
}

case $4 in
         deploy)
                server_stop
                scp
                server_start
                tail
        ;;
         stop)
        	server_stop
        ;;
         start)
        	server_start
        	tail
        ;;
         restart)
        	server_restart
        	tail
        ;;
         tail)
        	tail
        ;;
         *)
        	echo "Usage: $SCRIPTNAME {deploy|start|stop|restart|tail}" >&2
        	read Char
        	while [ true ]
        	do
        	case $Char in
                 deploy)
                        server_stop
                        scp
                        server_start
                        tail
                        exit 1
                ;;
                 stop)
                    server_stop
                        exit 1
                ;;
                 start)
                    server_start
                    tail
                        exit 1
                ;;
                 restart)
                    server_restart
                    tail
                        exit 1
                ;;
                 tail)
                    tail
                        exit 1
                ;;
                 *)
                echo "Usage: $SCRIPTNAME {deploy|start|stop|restart|tail}" >&2
                read Char
                 ;;
            esac

            done


        exit 1
        ;;
esac
exit 0


