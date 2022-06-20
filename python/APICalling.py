#encoding: utf-8

import requests
import json
import sys

# Input from Java must be named "input"


def get_status(colour):
    parameter_1 = colour
    ip = sys.argv[1]
    port = sys.argv[2]
    sql_string = "http://{ip}:{port}/{p1}".format(
        ip=ip, port=port, p1=parameter_1)
    return requests.get(sql_string).content


def set_status(colour):
    parameter_1 = colour
    ip = sys.argv[1]
    port = sys.argv[2]
    sql_string = "http://{ip}:{port}/{p1}".format(
        ip=ip, port=port, p1=parameter_1)
    return requests.get(sql_string).content


def send_req(input):
    if (input == "THUMB_INDEX_PINCH"):
        print(set_status("green"))
    elif (input == "THUMB_MIDDLE_FINGER_PINCH"):
        print(set_status("red"))
