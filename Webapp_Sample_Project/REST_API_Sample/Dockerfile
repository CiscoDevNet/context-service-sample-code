FROM java:8-jre
#https://github.com/docker-library/tomcat

# boilerplate tomcat8
ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH

RUN mkdir -p "$CATALINA_HOME"
WORKDIR $CATALINA_HOME

ENV TOMCAT_MAJOR 8
ENV TOMCAT_VERSION 8.0.46
ENV TOMCAT_TGZ_URL https://www.apache.org/dist/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz

# extract tomcat from tar.gz
RUN set -x \
    && curl -fSL "$TOMCAT_TGZ_URL" -o tomcat.tar.gz \
    && tar -xvf tomcat.tar.gz --strip-components=1 \
    && rm bin/*.bat \
    && rm tomcat.tar.gz*

# add the Context Service REST app WAR file to tomcat webapps
ADD ./target/rest.war $CATALINA_HOME/webapps/

# update setenv.sh, adding the Context Service connection data file path to tomcat environment vars
RUN echo "CATALINA_OPTS=\"\$CATALINA_OPTS -Dconnection.info.file=$CATALINA_HOME/connection.properties\"" | \
    tee -a $CATALINA_HOME/bin/setenv.sh \
    && chmod 755 $CATALINA_HOME/bin/setenv.sh

# expose port 8080 for tomcat
EXPOSE 8080
# save Context Service connection data string from environment var to connection.properties file
# and then start tomcat
ENTRYPOINT echo "connection.data=$CONNECTION_DATA" > $CATALINA_HOME/connection.properties ; catalina.sh run
