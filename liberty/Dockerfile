FROM websphere-liberty

MAINTAINER Jamie Coleman <jlcoleman@uk.ibm.com>

COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/
COPY 9.0.0.0-IBM-MQ-Java-InstallRA.jar / 
COPY MQ_Test.war /opt/ibm/wlp/usr/servers/defaultServer/dropins/
	
RUN installUtility install --acceptLicense defaultServer
RUN java -jar 9.0.0.0-IBM-MQ-Java-InstallRA.jar --acceptLicense /
	
ENV LICENSE accept
