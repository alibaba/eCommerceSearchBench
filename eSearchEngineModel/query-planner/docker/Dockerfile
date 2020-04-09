FROM aliesearch-base-image:latest
MAINTAINER CarpenterLee

#RUN java -version
RUN chmod -R a+w /tmp/
RUN chown -R admin:admin /home/admin/
USER admin
COPY --chown=admin:admin file/model_all.* /home/admin/
COPY --chown=admin:admin file/neo4j-community-3.5.6-unix.tar.* /home/admin/
COPY --chown=admin:admin file/neo4j.conf /home/admin/
COPY --chown=admin:admin file/entrypoint.sh /home/admin/
COPY --chown=admin:admin file/query-planner.jar /home/admin/

WORKDIR /home/admin
CMD ["/bin/bash", "/home/admin/entrypoint.sh"]
