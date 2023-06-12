import sys

from pyspark.sql import SparkSession
from pyspark.sql.streaming import DataStreamWriter
from pyspark.sql.functions import col, from_json
from pyspark.sql.types import StringType, StructType
 
from transformations.transformation_handler import transformation
from helper.spark_helper import set_s3_config, schema_event_parser
from config.config import load_event_config, config_dict

# extract json from kafka read value {"value": <json string value>}

EVENT = sys.argv[1]
event_config = load_event_config(EVENT)

# using this function to abstract the streaming dataframe connection
# easily changed to adaptad need requirments
# for example reading json format requires schema, here we use kafka write schema we defined in bronze task
# {key: <str>, value: <json str>}
def get_in_stream_bucket(spark):
    in_schema = (
        StructType().add("key", StringType(), True).add("value", StringType(), True)
    )
    return (
        spark.readStream.format(event_config["s3"]["in_format"])
        .option("path", f"s3a://{event_config['s3']['in_path']}")
        .schema(in_schema)
        .load()
    )


spark = SparkSession.builder.appName(event_config["spark_name"]).getOrCreate()

set_s3_config(spark, config_dict["s3"])

in_buckets_df = get_in_stream_bucket(spark)

# assuming every record in queue needs have the value column parsed
def parse_json(df):
    schema = schema_event_parser(event_config["schema"])
    return df.withColumn("jsonData", from_json(col("value"), schema)).select(
        "jsonData.*"
    )


in_process_df: DataStreamWriter = parse_json(in_buckets_df)

if event_config["transformations"]:
    for t in event_config["transformations"].keys():
        in_process_df = transformation(in_process_df, t, event_config["transformations"][t]["args"])
    

out_df = in_process_df

query = (
    out_df.writeStream.format(event_config["s3"]["out_format"])
    .outputMode("append")
    .trigger(processingTime="1 minute")
    .option("path", event_config["s3"]["out_path"])
    .option("checkpointLocation", event_config["s3"]["out_checkpoint"])
    .start()
)

query.awaitTermination()
