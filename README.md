///////////////// uso de la imagen /////////////////////
docker run -it -p 9000:9000 -p 22:22 -v C:\Dev\docker\hadoop\mapr:/home/hadoopuser/mapr --name hadoopserver hadoop
docker exec -it hadoopserver bash
////////////////////////////////////////////////

//////////// esto solo se hace una vez por container ////////////
su - hadoopuser
cd /home/hadoopuser
ssh-keygen -t rsa -P '' -f /home/hadoopuser/.ssh/id_rsa
ssh-copy-id hadoopuser@localhost
exit
///////////////////////////////////////////

////////////Iniciar/parar el servicio de hadoop//opt/hadoop/hadoop-3.3.0/sbin /////////////
start-all.sh
stop-all.sh
////////////////////////////////////////////

// copiar files from host to container
docker cp maprexample.jar hadoopserver:/home/hadoopuser
docker cp datadates.csv  hadoopserver:/home/hadoopuser

hadoop fs -mkdir /data
hadoop fs -mkdir /data/input
hadoop fs -copyFromLocal datadates.csv /data/input
hadoop jar maprexample.jar main.program /data/input/datadates.csv /data/output

