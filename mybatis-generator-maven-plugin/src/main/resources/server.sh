#!/bin/sh
#
#   ~ Copyright (c) 2017. 秒差距科技
#

#chkconfig: 345 86 14
#description: Startup and shutdown script for server.jar
 
PIDFILE=./app.pid
#取最新的jar包
SERVER=`ls -t *.jar |head -1`
#SERVER=./server.jar
ARGS="-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Xms1024m
-Xmx1024m
-Xmn384m
-Xss256k
-XX:PermSize=512M
-XX:MaxPermSize=512m
-XX:+DisableExplicitGC
-XX:+UseConcMarkSweepGC
-XX:+CMSParallelRemarkEnabled
-XX:+UseCMSCompactAtFullCollection
-XX:LargePageSizeInBytes=128m
-XX:+UseFastAccessorMethods
-XX:+UseCMSInitiatingOccupancyOnly
-XX:CMSInitiatingOccupancyFraction=50
"

# 有些服务器找不到java命令,加载一下
. /etc/profile


start()
{
	if test  $SERVER
	then
		echo  "启动 $SERVER"
#		nohup java -jar gemini-1.0-SNAPSHOT.jar > /dev/null 2>&1 & echo $! > run.pid
	#               if java $ARGS -jar $SERVER> /dev/null 2>&1 &
			if nohup java $ARGS -jar $SERVER &
		then
			echo $! > $PIDFILE
			echo  "server start OK"
		else
			echo  "server start failed"
		fi
	else
		echo  "Couldn't find server.jar"
	fi
}
 
stop()
{
         if test $PIDFILE
         then
                   echo "停止 $SERVER"
                   if kill -9 `cat $PIDFILE`
                   then
                            echo "server stop OK"
                   else
	                    echo "server stop faild"
                   fi
         else
                   echo "No server running"
         fi
}
 
restart()
{
    echo "Restarting server"
    stop
    start
}

case $1 in
         start)
                start
        ;;
         stop)
        	stop
        ;;
         restart)
        	restart
        ;;
         *)
        	echo "Usage: $SCRIPTNAME {start|stop|restart|list}" >&2
        exit 1
        ;;
esac
exit 0

