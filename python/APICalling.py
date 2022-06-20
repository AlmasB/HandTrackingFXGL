#encoding: utf-8

import requests
import json

# Input from Java must be named "input"


def get_status(colour):
    parameter_1 = colour
    sql_string = "http://192.168.43.65:5000/{p1}".format(p1=parameter_1)
    return requests.get(sql_string).content


def set_status(colour):
    parameter_1 = colour
    sql_string = "http://192.168.43.65:5000/colour/{p1}".format(p1=parameter_1)
    return requests.get(sql_string).content


def send_req(input):
    if (input == "THUMB_INDEX_PINCH"):
        print(set_status("green"))
    elif (input == "THUMB_MIDDLE_FINGER_PINCH"):
        print(set_status("red"))
