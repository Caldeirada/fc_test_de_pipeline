from enum import Enum

from pyspark.sql import SparkSession
from pyspark.sql.types import StringType, StructType, IntegerType, LongType, TimestampType

class TypeEnum(Enum):
    string = StringType()
    integer = IntegerType()
    long = LongType()
    timestamp = TimestampType()

def set_s3_config(spark: SparkSession, s3_dict: dict):
    spark.conf.set("fs.s3a.endpoint", s3_dict["url"])
    spark.conf.set("fs.s3a.access.key", s3_dict["access_key"])
    spark.conf.set("fs.s3a.secret.key", s3_dict["secret_key"])
    spark.conf.set("fs.s3a.path.style.access", "true")
    spark.conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")


def get_kafka_df(spark: SparkSession, kafka_dict: dict):
    kafka_df = (
        spark.readStream.format("kafka")
        .option("kafka.bootstrap.servers", kafka_dict["server"])
        .option("subscribe", kafka_dict["topic"])
        .option("startingOffsets", kafka_dict.get("starting_offests", "earliest"))
        .load()
    )
    return kafka_df

def schema_event_parser(schema_dict: dict) -> StructType:
    schema = StructType()

    spark_type = lambda type_str: getattr(TypeEnum, type_str).value

    for key, type_configs in schema_dict.items():
        if type_configs["type"] == "struct":   
            schema.add(
                key, schema_event_parser(type_configs["struct"])
            )
        else:
            schema.add(
                key, spark_type(type_configs["type"])
            )

    return schema
