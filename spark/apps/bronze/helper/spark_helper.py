from pyspark.sql import SparkSession

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
