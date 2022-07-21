#encoding: utf-8

import requests

# Input from Java must be named "input"


def get_status(ip, port, colour):
    parameter_1 = colour
    request_line = "http://{ip}:{port}/{p1}".format(
        ip=ip, port=port, p1=parameter_1)
    return requests.get(request_line).content


def set_status(ip, port, colour):
    parameter_1 = colour
    request_line = "http://{ip}:{port}/colour/{p1}".format(
        ip=ip, port=port, p1=parameter_1)
    return requests.get(request_line).content


def send_req(ip, port, input):
    if (input == "PEACE"):
        print(set_status(ip, port, "green"))
    elif (input == "OK"):
        print(set_status(ip, port, "red"))
