from datetime import datetime

from airflow import DAG

from airflow.operators.bash import BashOperator
from spark_task_helper import get_spark_tasks
from bronze.config.config import config_dict


"""
launches streaming consumer 
"""
with DAG(
    "bronze_extraction_dag",
    schedule=None,
    start_date=datetime(2023, 6, 2),
    tags=["bronze", "kafka", "consumer"],
) as dag:
    for pipeline in config_dict["spark"]:
        BashOperator(
            task_id=pipeline,
            bash_command=get_spark_tasks(config_dict["spark"][pipeline]),
        )
