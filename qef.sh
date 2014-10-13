#!/bin/bash
mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="ch.epfl.codimsd.qeef.QueryManagerImpl" -Dexec.args="$1"
