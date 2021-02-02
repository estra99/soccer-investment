from pyspark.sql import SparkSession
from pyspark.sql.functions import col, date_format, to_date, year, collect_set
from pyspark.sql.types import (DateType, IntegerType, FloatType, StructField, StructType, TimestampType, StringType)
import os
import json

spark = SparkSession.builder.appName("Soccer Papi").getOrCreate()

players_schema = StructType([
    StructField('Id', IntegerType()),
    StructField('Nombre', StringType()),
    StructField('Club', StringType()),
    StructField('Fecha Nacimiento', StringType()),
    StructField('Rating General', IntegerType()),
    StructField('Potencial', IntegerType()),
    StructField('Valor Actual Mercado (Euros)', IntegerType()),
    StructField('Maximo Valor Mercado (Euros)', IntegerType())])


dataframe = spark.read.format("csv").option("header", "false").option("delimiter", "\t").schema(players_schema).load("playersDataMart.tsv")

dataframePerfilInversion = dataframe.withColumn('Perfil de Inversion', (35*(col("Rating General")/100)+ 
                                                                        35*(col("Potencial")/100)+
                                                                        30*(col("Valor Actual Mercado (Euros)")/col("Maximo Valor Mercado (Euros)"))).cast(FloatType()))

dataFrameInversionistas = dataframePerfilInversion.withColumn('Precio proyectado', (col("Valor Actual Mercado (Euros)")/100*(col("Perfil de Inversion"))).cast(IntegerType()))

#dataFrameInversionistas.show()

dataFrameInversionistas.groupBy('Club').agg(collect_set('Nombre'), collect_set('Precio proyectado')).coalesce(1).write.mode('overwrite').format('json').save("output")

os.system("cat output/*.json > clubes.json")
os.system("rm -rf output")

file = open('clubes.json', 'r')
lines = file.readlines()
clubs = []
for line in lines:
    club = json.loads(line)
    players = []
    for i in range(len(club['collect_set(Nombre)'])):
        players.append({"name": club['collect_set(Nombre)'][i], "value": club['collect_set(Precio proyectado)'][i]})
    club = {"name": club['Club'], "children": players}
    clubs.append(club)
flare = { "name": "flare", "children": clubs}
file.close()
with open('ready.json', 'w') as f:
    json.dump(flare, f)