import os
from typing import Optional, Awaitable

import tornado.httpserver
import tornado.ioloop
import tornado.websocket
import json
import logging
from rx.subject import Subject
from tornado.websocket import WebSocketHandler

from handler import AuthenticatedSingletonSocketHandler

logger = logging.getLogger(__name__)

PASSWORD = os.getenv('PASSWORD')
assert PASSWORD is not None, "Did not provide a password!"


class ControllerHandler(AuthenticatedSingletonSocketHandler):
    @property
    def instance(self) -> Optional[WebSocketHandler]:
        global controller
        return controller

    @instance.setter
    def instance(self, value: Optional[WebSocketHandler]):
        global controller
        controller = value

    def verify_password(self, password) -> bool:
        return password == 'test'

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        def message_observer(message):
            if not self.authenticated:
                logger.warning("Unauthenticated user")
                return
            global device
            if device is None:
                return

            action = message.get('type')
            if action == 'set_power':
                value = float(message.get('value'))
                logger.debug("Received set power command value=%s", value)
                device.power.on_next(value)
        self.messages.subscribe(message_observer)

    def check_origin(self, origin: str) -> bool:
        return True


class DeviceHandler(AuthenticatedSingletonSocketHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.power = Subject()

        def power_observable(power):
            self.send_obj({'type': 'power', 'value': power})
        self.power.subscribe(power_observable)

    def verify_password(self, password) -> bool:
        return password == 'test'

    @property
    def instance(self) -> Optional[WebSocketHandler]:
        global device
        return device

    @instance.setter
    def instance(self, value: Optional[WebSocketHandler]):
        global device
        device = value

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
    loggers = [logging.getLogger(name) for name in logging.root.manager.loggerDict]
    for logger in loggers:
        logger.setLevel(logging.INFO)

    logger.info("Starting server")
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(6969)
    tornado.ioloop.IOLoop.instance().start()
