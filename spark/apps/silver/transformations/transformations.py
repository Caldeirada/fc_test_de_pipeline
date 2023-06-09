from pyspark.sql.functions import col, upper, udf
import pycountry


def uppercase_column(df, column_name: str):
    aux = df.withColumn(column_name, upper(col(column_name)))
    return aux

def get_country_name_from_country_code2(df, country_col):
    get_country_name_from_2code_udf = udf(lambda code: pycountry.countries.get(alpha_3=code).name)
    df = df.withColumn("country_name", get_country_name_from_2code_udf(col(country_col)))
    df = df.drop(country_col)
    return df

