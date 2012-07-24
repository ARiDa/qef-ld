#! /bin/bash
# add the libraries to the CLASSPATH.
ORIGINAL_DIR=$PWD
APP_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $APP_DIR
MAIN_CLASS=ch.epfl.codimsd.qeef.QueryManagerImpl
APP_CLASSPATH=CoDIMS-tcp/build/classes
DIRLIBS=CoDIMS-tcp/lib/*.jar

for i in ${DIRLIBS}
do
  APP_CLASSPATH=$APP_CLASSPATH:"$i"
done

java -cp "$APP_CLASSPATH:$CLASSPATH" $MAIN_CLASS "$@"
cd "$ORIGINAL_DIR"

