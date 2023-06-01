# 8ballpool kafka

Pipeline to process real-time data from 8ballpool game server.
Ingest data from a kakfa topic and parses and aggregates the data to be ready for consumuption by the reporting level.
A datafaker is made available to test the pipeline features.

## Tech 

- Docker
- Spark(PySpark)
- Python
- Airflow
- Java
- MinIO (local storage)

MinIO is used as a bucket storage service, it provides direct connectivity with AWS S3 API and is a good candidate for local development
