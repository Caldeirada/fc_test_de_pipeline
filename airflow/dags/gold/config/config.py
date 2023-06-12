import os
import pkgutil

import yaml


ENV = os.getenv("ENV")

data = pkgutil.get_data(__package__, f"config-{ENV}.yaml")

config_dict = yaml.safe_load(data)
