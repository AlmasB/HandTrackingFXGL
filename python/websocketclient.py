#!/usr/bin/env python


import asyncio

import websockets

import APICalling


async def hello():

    uri = "ws://localhost:8750"

    async with websockets.connect(uri) as websocket:

        greeting = await websocket.recv()
        print(f"<<< {greeting}")

        while True:

            # message = input("Send a Message ")

            # await websocket.send(message)

            # print(f">>> {message}")

            response = await websocket.recv()

            # response must be sent to pi

            APICalling.send_req(response)

            # receive response from pi

            print(f"<<< {response}")


if __name__ == "__main__":

    asyncio.run(hello())
