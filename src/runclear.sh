#!/bin/sh

echo java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Clear ../questions/test-TagMe.txt
java -cp ".:../libs/htmlunit-2.27-OSGi.jar:../libs/json-simple-1.1.1.jar:../libs/mysql-connector-java-5.1.42-bin.jar" Clear ../questions/test-TagMe.txt
