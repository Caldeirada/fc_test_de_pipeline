import os
import pkgutil

import yaml


ENV = os.getenv("ENV")

data = pkgutil.get_data(__package__, f"config-{ENV}.yaml")
config_dict = yaml.safe_load(data)


def load_event_config(event_name):
    event_data = pkgutil.get_data(__package__, f"events/{event_name}.yaml")
    return yaml.safe_load(event_data)

