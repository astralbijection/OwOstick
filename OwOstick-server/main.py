from typing import Optional

import tornado
import tornado.websocket
import json
import logging


logger = logging.getLogger(__name__)


class ControllerHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        global controller
        logger.info("Controller connected: %s", self)
        if controller is not None:
            logger.warn("Controller already exists! Closing.")
            self.close()
            return
        controller = self

    def check_origin(self, origin: str) -> bool:
        return True

    def on_message(self, message):
        global device
        if device is None:
            return
        logger.info("Got message: %s", message)
        obj = json.loads(message)
        logger.debug("Decoded message to object: %s", obj)
        action = obj.get('action')
        if action == 'set_power':
            value = float(obj.get('value'))
            logger.debug("Received set power command value=%s", value)
            device.set_power(value)

    def on_close(self):
        global controller
        if controller == self:
            controller = None


class DeviceHandler(tornado.websocket.WebSocketHandler):
    def set_power(self, value):
        self.write_message(json.dumps({'action': 'set_power', 'value': value}))

    def open(self):
        global device
        if device is not None:
            self.close()
        device = self
        self.write_message("Hello World")

    def on_message(self, message):
        global controller
        if controller is None:
            return

    def on_close(self):
        global device
        if device == self:
            device = None


controller: Optional[ControllerHandler] = None
device: Optional[DeviceHandler] = None


application = tornado.web.Application([
    (r'/api/controller', ControllerHandler),
    (r'/api/device', DeviceHandler),
])


if __name__ == "__main__":
    logger.setLevel(logging.DEBUG)
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()
