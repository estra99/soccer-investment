### Arranca hadoopserver
'''
./start.sh
start-all.sh
'''
### Carga de los csv a hadoop fs
'''
cd mapr/
hadoop fs -copyFromLocal Player.csv /data/input
hadoop fs -copyFromLocal Player_Attributes.csv /data/input
hadoop fs -copyFromLocal European_Rosters.tsv /data/input
'''
### Pipeline en Hive
'''
// 1. crea la base de datos jugadores en hive.
create schema jugadores;

// 2. crea una tabla temporal para el archivo de jugadores.
create table tmp_jugadores(id int, player_api_id int, player_name string) row format delimited fields terminated by ',';

// 3. carga los datos a la tabla tmp de jugadores.
load data inpath '/data/input/Player.csv' into table tmp_jugadores;

// 4. crear una tabla temporal para el archivo de detalles de jugadores.
create table tmp_jugadores_detalles (id int, player_fifa_api_id int, player_api_id int, date_update string, overall_rating int, potential int, preferred_foot string, attacking_work_rate string, defensive_work_rate string, crossing int,	finishing int, heading_accuracy int,	short_passing int, volleys int, dribbling int, curve int, free_kick_accuracy int,	long_passing int,	ball_control int,	acceleration int,	sprint_speed int,	agility int,	reactions int,	balance int,	shot_power int,	jumping int,	stamina int,	strength int,	long_shots int,	aggression int,	interceptions int,	positioning int,	vision int,	penalties int,	marking int,	standing_tackle int,	sliding_tackle int,	gk_diving int,	gk_handling int,	gk_kicking int,	gk_positioning int,	gk_reflexes int ) row format delimited fields terminated by ',';  

// 5. carga los datos a la tabla tmp de detalles de los jugadores.
load data inpath '/data/input/Player_Attributes.csv' into table tmp_jugadores_detalles;

// 6. crear una tabla para pasar los datos limpios y necesarios de la tabla tmp de jugadores.
CREATE TABLE IF NOT EXISTS Jugador ( id_jugador int, nombre string)
COMMENT 'Nombre de jugadores'
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
STORED AS TEXTFILE;

// 7. insertar los datos desde la tabla tmp a la final de jugadores.
INSERT INTO Jugador (id_jugador, nombre)
SELECT player_api_id, player_name
FROM tmp_jugadores;

// 8. crear una tabla para pasar los datos limpios y necesario de los detalles de jugadores
CREATE TABLE IF NOT EXISTS Detalles_Jugador (id_jugador int, ultima_actualizacion timestamp, rating_general int, potencial int, pie_dominante string)
COMMENT 'Detalle de jugadores'
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
STORED AS TEXTFILE;

// 9. insertar los datos desde la tabla tmp a la final de detalles de jugadores
INSERT INTO Detalles_Jugador (id_jugador, ultima_actualizacion, rating_general, potencial, pie_dominante)
SELECT tjd.player_api_id, tjd.date_update, tjd.overall_rating, tjd.potential, tjd.preferred_foot
FROM tmp_jugadores_detalles AS tjd
LEFT OUTER JOIN tmp_jugadores_detalles AS tjd2
  ON tjd.player_api_id = tjd2.player_api_id
        AND (tjd.date_update < tjd2.date_update
         OR (tjd.date_update = tjd2.date_update AND tjd.id < tjd2.id))
WHERE tjd2.player_api_id IS NULL;
'''
### Probando las tablas en Hive
'''
SELECT * FROM Jugador;
SELECT * FROM tmp_jugadores_detalles SORT BY id DESC LIMIT 2;
SELECT MONTH(date_update) from tmp_jugadores_detalles where id = 183978;
'''
## Pipeline en MapR
'''
// 1. procesar los datos, filtra por país elegido (Francia) y para dejarlos en un archivo separado por tabs (\t)
hadoop jar mapRMercado.jar main.program /data/input/European_Rosters.tsv /data/output

//Commandos para pruebas
hadoop fs -copyToLocal /data/output/part-r-00000 /home/hadoopuser/mapr //pasar el archivo de hfs a local

//Limpiar la carpeta de output para una nueva corrida
hadoop fs -rm /data/output/* 
hadoop fs -rmdir /data/output/

// 2. visualizar el archivo
hadoop fs -ls /data/output
hadoop fs -cat /data/output/part-r-00000

// 3. pasar el archivo a una tabla en Hive

create table tmp_jugadoresMercado(nombre_jugador string, club string, fecha_nacimiento string, posicion string, precio_actual integer, precio_maximo integer) row format delimited fields terminated by '\t';

load data inpath '/data/output/part-r-00000' into table tmp_jugadoresMercado;

CREATE TABLE IF NOT EXISTS Jugador_Mercado (nombre_jugador string, club string, fecha_nacimiento string, posicion string, precio_actual integer, precio_maximo integer)
COMMENT 'Valor en el mercado de los jugadores'
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
STORED AS TEXTFILE;

INSERT INTO Jugador_Mercado (nombre_jugador, club, fecha_nacimiento, posicion, precio_actual, precio_maximo)
SELECT nombre_jugador, club, fecha_nacimiento, posicion, precio_actual, precio_maximo
FROM tmp_jugadoresMercado;

// 4. Query para cruzar las tablas y combinarlas

SELECT jugador.id_jugador, jugador.nombre, jugador_mercado.fecha_nacimiento, detalles_jugador.rating_general, detalles_jugador.potencial, jugador_mercado.precio_actual, jugador_mercado.precio_maximo
FROM Jugador
JOIN Detalles_Jugador 
ON jugador.id_jugador = detalles_jugador.id_jugador
JOIN Jugador_Mercado
ON jugador.nombre = jugador_mercado.nombre_jugador
WHERE jugador_mercado.precio_maximo is not null and jugador_mercado.precio_actual is not null;

'''
### FIN DEL DATAMART

### Análisis en PySpark


