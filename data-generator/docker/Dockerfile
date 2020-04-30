FROM aliesearch-base-image:latest
MAINTAINER CarpenterLee

RUN java -version

RUN chmod -R a+w /tmp/
COPY file/data/excellent_item_segment.txt /home/admin/
COPY file/data/good_item_segment.txt /home/admin/
COPY file/data/bad_item_segment.txt /home/admin/
COPY file/*.sh /home/admin/
COPY file/benchmark-cli.zip /home/admin/
RUN chown -R admin:admin /home/admin/
USER admin
#COPY --chown=admin:admin file/data/excellent_item_segment.txt /home/admin/
#COPY --chown=admin:admin file/data/good_item_segment.txt /home/admin/
#COPY --chown=admin:admin file/data/bad_item_segment.txt /home/admin/
#COPY --chown=admin:admin file/*.sh /home/admin/
#COPY --chown=admin:admin file/benchmark-cli.zip /home/admin/

RUN ls -al /home/admin/

WORKDIR /home/admin
CMD ["/bin/bash", "/home/admin/entrypoint.sh"]
