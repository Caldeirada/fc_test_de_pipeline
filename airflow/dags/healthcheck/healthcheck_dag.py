from airflow import DAG
from datetime import datetime

from airflow.operators.dummy_operator import DummyOperator
from airflow.operators.python_operator import BranchPythonOperator
from airflow.operators.dagrun_operator import TriggerDagRunOperator
from airflow.utils.state import State


from external_dag_sensor import ExternalDagrunSensor

from healthcheck.config.config import config_dict

"""
performs an healthcheck on the configured dags
if a dag is not running it will trigger the dag to start processing
"""
with DAG(
    "healthcheck_dag",
    schedule_interval="*/5 * * * *",
    start_date=datetime(2023, 6, 2),
    tags=["healthcheck", "streaming"],
    catchup=False
) as dag:

    def decide_branch(trigger, success, **context):
        #get upstream task and read status, if failed we need to restart the stream process
        dag_run = context["dag_run"]
        upstream_task_id = context['ti'].task.upstream_list[0].task_id
        upstream_task = list(filter(lambda t: t.task_id == upstream_task_id, dag_run.get_task_instances()))[0]

        print(f"state: {upstream_task.state}")
        print(upstream_task.state == State.SUCCESS)
        if upstream_task.state == State.SUCCESS:
            return success
        else:
            return trigger

    for dag_name in config_dict["dags"]:
        wait_for_task = ExternalDagrunSensor(
            task_id=f"healthcheck_{dag_name}", external_dag_id=dag_name
        )
        branch_task = BranchPythonOperator(
            task_id=f"healthcheck_{dag_name}_branch",
            python_callable=decide_branch,
            op_args=[
                f"healthcheck_{dag_name}_trigger",
                f"healthcheck_{dag_name}_successful",
            ],
            trigger_rule="all_done",
            provide_context=True,
            dag=dag,
        )
        trigger_task = TriggerDagRunOperator(
            task_id=f"healthcheck_{dag_name}_trigger", trigger_dag_id=dag_name
        )

        failure_task = DummyOperator(task_id=f"healthcheck_{dag_name}_successful")

        wait_for_task >> branch_task >> [trigger_task, failure_task]
