from ubuntu

RUN apt update && apt upgrade -y
RUN apt-get install -y default-jre
RUN apt-get install -y openjdk-8-jdk
RUN apt install -y wget
RUN apt install -y iproute2
RUN apt install -y iputils-ping
RUN apt-get install -y openssh-server openssh-client
RUN apt-get install -y rsync
RUN apt-get -y install nano
RUN apt-get -y install yarn
RUN apt-get install -y nodejs
RUN apt install git-all -y
RUN apt-get install -y libtidy-dev

RUN mkdir /opt/hadoop
WORKDIR /opt/hadoop

RUN wget https://downloads.apache.org/hadoop/common/hadoop-3.3.0/hadoop-3.3.0.tar.gz
RUN tar -xzf hadoop-3.3.0.tar.gz

RUN adduser --disabled-password --gecos "" hadoopuser
RUN echo "hadoopuser:hadoop" | chpasswd

RUN mkdir /home/hadoopuser/tmpdata
RUN mkdir /home/hadoopuser/tmpdata/dfs
RUN mkdir /home/hadoopuser/tmpdata/dfs/name
RUN mkdir /home/hadoopuser/tmpdata/dfs/name/current
RUN mkdir /home/hadoopuser/dfsdata
RUN mkdir /home/hadoopuser/dfsdata/namenode
RUN mkdir /home/hadoopuser/dfsdata/datanode
RUN chmod a+rw -R /home/hadoopuser/tmpdata
RUN chmod a+rw -R /home/hadoopuser/dfsdata
RUN mkdir /home/hadoopuser/mapr

COPY ./etc/bashrc /etc
COPY ./etc/bashrc /home/hadoopuser/.bashrc

WORKDIR /etc
RUN cat bashrc > .bashrc
RUN chmod +x .bashrc

WORKDIR /home/hadoopuser
RUN chmod +x .bashrc

COPY /hadoop/etc/hadoop-env.sh /opt/hadoop/hadoop-3.3.0/etc/hadoop
COPY /hadoop/etc/core-site.xml /opt/hadoop/hadoop-3.3.0/etc/hadoop
COPY /hadoop/etc/hdfs-site.xml /opt/hadoop/hadoop-3.3.0/etc/hadoop
COPY /hadoop/etc/mapred-site.xml /opt/hadoop/hadoop-3.3.0/etc/hadoop
COPY /hadoop/etc/yarn-site.xml /opt/hadoop/hadoop-3.3.0/etc/hadoop

WORKDIR /home/hadoopuser
COPY /start.sh /home/hadoopuser
RUN chmod +x start.sh

RUN mkdir /opt/hadoop/hadoop-3.3.0/logs
RUN chmod ugo+rwx /opt/hadoop/hadoop-3.3.0/logs

WORKDIR /opt/hadoop/hadoop-3.3.0/bin
RUN ./hdfs namenode -format -force -nonInteractive

RUN chown -R hadoopuser /home/hadoopuser/tmpdata
RUN chown -R hadoopuser /home/hadoopuser/dfsdata

WORKDIR /home/hadoopuser
RUN cd /home/hadoopuser

CMD ["bash", "-C", "/home/hadoopuser/start.sh"]
