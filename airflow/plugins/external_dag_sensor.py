from airflow.exceptions import AirflowFailException
from airflow.models import DagRun
from airflow.sensors.base_sensor_operator import BaseSensorOperator
from airflow.models.baseoperator import BaseOperator
from airflow.utils.db import provide_session
from airflow.utils.decorators import apply_defaults
from airflow.utils.state import State

class ExternalDagrunSensor(BaseOperator):
    """
    Waits for a different DAG to complete; if the dagrun has failed, this
    task fails itself as well.

    :param external_dag_id: The dag_id that contains the task you want to
        wait for
    :type external_dag_id: str
    """

    template_fields = ["external_dag_id"]
    ui_color = "#19647e"

    @apply_defaults
    def __init__(self, external_dag_id, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.external_dag_id = external_dag_id

    @provide_session
    def poke(self, context, session=None):
        print(context)
        dag_id = self.external_dag_id
        self.log.info(f"Poking for {dag_id} on ... ")

        state = (
            session.query(DagRun.state)
            .filter(
                DagRun.dag_id == dag_id,
                DagRun.state.in_(['running']),
            )
            .scalar()
        )
        return state

    def execute(self, context):
        state = self.poke(context)
        print(f"AQUI AQUI \n\n\n {state}")
        if state != State.RUNNING:
            raise AirflowFailException(
                f"The external DAG run {self.external_dag_id} has failed"
            )
        # the return value of '.execute()' will be pushed to XCom by default
        return state
