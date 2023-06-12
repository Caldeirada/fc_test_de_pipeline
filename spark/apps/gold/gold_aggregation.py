import sys

from pyspark.sql import SparkSession
from pyspark.sql.functions import count, coalesce, col, lit
 
from helper.spark_helper import set_s3_config, schema_event_parser
from config.config import load_event_config, config_dict

# extract json from kafka read value {"value": <json string value>}

EVENT = sys.argv[1]
event_config = load_event_config(EVENT)

def get_in_stream_bucket(spark):
    in_schema = schema_event_parser(event_config["schema"])
    return (
        spark.readStream.format(event_config["s3"]["in_format"])
        .option("path", f"s3a://{event_config['s3']['in_path']}")
        .schema(in_schema)
        .load()
    )


spark = SparkSession.builder.appName(event_config["spark_name"]).getOrCreate()

set_s3_config(spark, config_dict["s3"])

in_buckets_df = get_in_stream_bucket(spark)

# Select platform, country, and date columns
selected_df = in_buckets_df.select("time", "platform", "country_name", "ref_date")

out_df = selected_df

database_url = "jdbc:mysql://mysql:3306/local_env"
table_name = "country_2"
username = "root"
password = "secret"

def func(batch_df, _):
    agg_df = spark.read \
    .format("jdbc") \
    .option("url", event_config["mysql"]["database_url"]) \
    .option("dbtable", event_config["mysql"]["table_name"]) \
    .option("user", event_config["mysql"]["username"]) \
    .option("password", event_config["mysql"]["password"]) \
    .option("driver", "com.mysql.jdbc.Driver") \
    .load()

    batch_df.persist()
    aux_df = batch_df.groupBy("platform", "country_name", "ref_date") \
    .agg(count("*").alias("record_count"))

    sum_df = agg_df.join(aux_df, ["country_name", "ref_date", "platform"], "fullouter") \
    .select(
        col("country_name"),
        col("ref_date"),
        col("platform"),
        (coalesce(agg_df["record_count"], lit(0)) + coalesce(aux_df["record_count"], lit(0))).alias("record_count")
    )
    
    # insert new data into temp table to them overwrite current existing one
    sum_df.write \
    .mode("overwrite") \
    .format("jdbc") \
    .option("url", event_config["mysql"]["database_url"]) \
    .option("dbtable", f"{event_config['mysql']['table_name']}_temp") \
    .option("user", event_config["mysql"]["username"]) \
    .option("password", event_config["mysql"]["password"]) \
    .option("driver", "com.mysql.jdbc.Driver") \
    .save()

    agg_df_2 = spark.read \
    .format("jdbc") \
    .option("url", event_config["mysql"]["database_url"]) \
    .option("dbtable", f"{event_config['mysql']['table_name']}_temp") \
    .option("user", event_config["mysql"]["username"]) \
    .option("password", event_config["mysql"]["password"]) \
    .option("driver", "com.mysql.jdbc.Driver") \
    .load()

    agg_df_2.write \
    .mode("overwrite") \
    .format("jdbc") \
    .option("url", event_config["mysql"]["database_url"]) \
    .option("dbtable", event_config["mysql"]["table_name"]) \
    .option("user", event_config["mysql"]["username"]) \
    .option("password", event_config["mysql"]["password"]) \
    .option("driver", "com.mysql.jdbc.Driver") \
    .save()
    batch_df.unpersist()

out_df.writeStream \
    .trigger(processingTime="30 seconds") \
    .foreachBatch(func) \
    .start() \
    .awaitTermination()
