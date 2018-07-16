#!/bin/sh

libs=".:../libs/antlr-runtime-3.5.2.jar:../libs/backport-util-concurrent-3.1.jar:../libs/commons-codec-1.5.jar:../libs/commons-lang-2.6.jar:../libs/commons-logging-1.1.1.jar:../libs/groovy-all-1.6.9.jar:../libs/htmlunit-2.27-OSGi.jar:../libs/ical4j-1.0.2.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar:../libs/natty-0.13.jar:../libs/quartz-1.8.4.jar:../libs/slf4j-api-1.7.10.jar:../libs/slf4j-nop-1.7.10.jar"

# echo java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Main ../config1.properties ../questions/test.txt
# java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Main ../config1.properties ../questions/test.txt

echo java -cp $libs Main ../config1.properties ../questions/test-TagMe.txt
java -cp $libs Main ../config1.properties ../questions/test-TagMe.txt