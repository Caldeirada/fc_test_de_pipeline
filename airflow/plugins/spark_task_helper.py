from airflow.operators.bash import BashOperator

def get_spark_tasks(pipeline_config: dict):
    """
        creates spark submit command
        Args:
            pipeline_config (dict): arg with parameters to use in spark submit
    """    
    cmd_args = ["spark-submit"]
    for arg, value in pipeline_config.items():
        if arg == "script":
            cmd_args.append(f"{value}")
        else:
            cmd_args.append(f"--{arg} {value}")
    
    return " ".join(cmd_args)
