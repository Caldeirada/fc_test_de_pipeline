from datetime import datetime

from airflow import DAG

from airflow.operators.bash import BashOperator
from spark_task_helper import get_spark_tasks
from silver.config.config import config_dict


"""
launches streaming consumer 
"""


def create_dag(name, config):
    with DAG(
        f"silver_transformation_dag_{name}",
        schedule=None,
        start_date=datetime(2023, 6, 2),
        tags=["silver", "s3", "transformation"],
    ) as dag:
        BashOperator(task_id=name, bash_command=get_spark_tasks(config["spark"]))


for dag in config_dict["dags"]:
    create_dag(dag, config_dict["dags"][dag])
