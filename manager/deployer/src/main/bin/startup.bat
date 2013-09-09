@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set ENV_PATH=.\
if "%OS%" == "Windows_NT" set ENV_PATH=%~dp0%

set conf_dir=%ENV_PATH%\..\conf
set webapp_dir=%ENV_PATH%\..\
set otter_conf=%conf_dir%\otter.properties
set logback_configurationFile=%conf_dir%\logback.xml

set CLASSPATH=%webapp_dir%;%conf_dir%;%conf_dir%\..\lib\*;%CLASSPATH%

set JAVA_MEM_OPTS= -Xms128m -Xmx512m -XX:PermSize=128m
set JAVA_OPTS_EXT= -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dapplication.codeset=UTF-8 -Dfile.encoding=UTF-8
set JAVA_DEBUG_OPT= -server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=7099,server=y,suspend=n
set CANAL_OPTS= -DappName=otter-manager -Ddubbo.application.logger=slf4j -Dlogback.configurationFile="%logback_configurationFile%" -Dotter.conf="%otter_conf%"

set JAVA_OPTS= %JAVA_MEM_OPTS% %JAVA_OPTS_EXT% %JAVA_DEBUG_OPT% %OTTER_OPTS%

set CMD_STR= java %JAVA_OPTS% -classpath "%CLASSPATH%" com.alibaba.otter.manager.deployer.OtterManagerLauncher
echo start cmd : %CMD_STR%

java %JAVA_OPTS% -classpath "%CLASSPATH%" com.alibaba.otter.manager.deployer.OtterManagerLauncher