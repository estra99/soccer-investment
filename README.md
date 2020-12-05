///////////////// uso de la imagen /////////////////////
docker run -it -p 9000:9000 -p 22:22 -v C:\Dev\docker\hadoop\mapr:/home/hadoopuser/mapr --name hadoopserver hadoop
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
hadoop fs -copyFromLocal datasales.dat /data/input
hadoop jar maprexample.jar main.program /data/input/datadates.csv /data/output

/// For Hive
CREATE SCHEMA <name>; // para crear una db

create table tmp_sales(fecha string, monto decimal(10,2)) row format delimited fields terminated by ',';

load data inpath '/data/input/datasales.dat' into table tmp_sales;

CREATE TABLE IF NOT EXISTS sales ( fecha timestamp, monto decimal(10,2))
COMMENT 'Ventas por mes por anyo'
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
STORED AS TEXTFILE;

insert into table sales select from_unixtime(unix_timestamp(fecha, 'MM/dd/yyyy')), monto from tmp_sales;

SELECT MONTH(fecha), YEAR(fecha), SUM(monto) from sales group by YEAR(fecha), MONTH(fecha);

SELECT anyo, MAX(monto) from (
    SELECT MONTH(fecha) mes, YEAR(fecha) anyo, SUM(monto) monto from sales group by YEAR(fecha), MONTH(fecha)
) as tabla 
group by anyo;
