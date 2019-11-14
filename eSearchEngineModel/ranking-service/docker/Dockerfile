FROM aliesearch-base-image:latest
MAINTAINER CarpenterLee

#RUN java -version
RUN chmod -R a+w /tmp/
RUN chown -R admin:admin /home/admin/
USER admin
COPY --chown=admin:admin file/ /home/admin/

WORKDIR /home/admin
CMD ["/bin/bash", "/home/admin/entrypoint.sh"]
