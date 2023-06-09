import importlib

def get_transformation_function(func_name, name= "transformations.transformations"):
    module = importlib.import_module(name)
    return getattr(module, func_name)

def transformation(df, transformation, args):
    func = get_transformation_function(transformation)
    return func(df, **args)