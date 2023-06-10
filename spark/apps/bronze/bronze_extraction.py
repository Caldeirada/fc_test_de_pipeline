from pyspark.sql import SparkSession
from pyspark.sql.streaming import DataStreamWriter
from pyspark.sql.functions import col

from config.config import config_dict
from helper.spark_helper import set_s3_config, get_kafka_df

spark: SparkSession = SparkSession.builder.appName(config_dict["spark"]).getOrCreate()

# s3 configs for spark instance
set_s3_config(spark, config_dict["s3"])

# kafka configs for spark instance
kafka_df = get_kafka_df(spark, config_dict["kafka"])

def get_event_stream(evt) -> DataStreamWriter:
    """
        Creates stream processor for each key matching the prefixes in conf files    
    """
    return kafka_df.filter(col("key").startswith(config_dict['events'][evt]["prefix"]))\
    .select(col("key").cast("string"),col("value").cast("string"))\
    .writeStream \
    .format(config_dict["destination_format"]) \
    .trigger(processingTime="10 seconds") \
    .option("path", f"s3a://{config_dict['s3']['base_bucket']}/{config_dict['game']}/{config_dict['events'][evt]['bucket']}/events") \
    .option("checkpointLocation", f"s3a://{config_dict['s3']['base_bucket']}/{config_dict['game']}/checkpoint/{evt}")


events_stream_map = map(get_event_stream, config_dict["events"])
list(map(lambda evt_stream: evt_stream.start(), events_stream_map))

# Wait for termination
spark.streams.awaitAnyTermination()