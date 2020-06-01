FROM openjdk:8u222-jdk-stretch
MAINTAINER CarpenterLee

#RUN apt-get update && apt-get --yes install sudo less net-tools lsof wget unzip vim

RUN useradd -ms /bin/bash -G sudo admin
RUN echo 'admin:aliesearch' | chpasswd
SHELL ["/bin/bash", "-c"]


