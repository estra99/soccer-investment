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
hadoop fs -copyFromLocal European_Rosters.csv /data/input
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
// 1. procesar los datos, filtra por país elegido y los limpia para dejarlos en un archivo
hadoop jar procesa_mercado.jar main.program /data/input/European_Rosters.csv /data/output

// 2. visualizar el archivo
hadoop fs -ls /data/output
hadoop fs -cat /data/output/part-r-00000

// 3. pasar el archivo a una tabla en Hive

// 4. cruzar las tablas y combinarlas

'''
### FIN DEL DATAMART

### Análisis en PySpark
