FROM ibm-semeru-runtimes:open-17.0.7_7-jdk
# RUN mkdir /opt/shareclasses
RUN mkdir -p /opt/openslice/lib/
COPY target/org.etsi.osl.tmf.api-1.3.0-exec.jar /opt/openslice/lib/
CMD ["java", "-Xshareclasses:cacheDir=/opt/shareclasses", "-jar", "/opt/openslice/lib/org.etsi.osl.tmf.api-1.3.0-exec.jar"]
EXPOSE 13082