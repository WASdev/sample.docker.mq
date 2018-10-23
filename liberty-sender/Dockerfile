FROM websphere-liberty

MAINTAINER Jamie Coleman <jlcoleman@uk.ibm.com>

COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/
COPY 9.1.0.0-IBM-MQ-Java-InstallRA.jar / 
COPY sender.war /opt/ibm/wlp/usr/servers/defaultServer/dropins/
	
RUN installUtility install --acceptLicense defaultServer

USER root
RUN mkdir -p /wmq && chown -R 1001:0 /wmq
USER 1001
RUN java -jar 9.1.0.0-IBM-MQ-Java-InstallRA.jar --acceptLicense /
