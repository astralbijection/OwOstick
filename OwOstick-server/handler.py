import json
import logging
from abc import ABC
from typing import Optional

import rx
from rx.subject import Subject
from tornado.websocket import WebSocketHandler

logger = logging.getLogger(__name__)


class AuthenticatedSingletonSocketHandler(WebSocketHandler, ABC):
    def verify_password(self, password) -> bool:
        raise NotImplementedError

    @property
    def instance(self) -> Optional[WebSocketHandler]:
        raise NotImplementedError

    @instance.setter
    def instance(self, value: Optional[WebSocketHandler]):
        raise NotImplementedError

    @property
    def authenticated(self):
        return self.state == 'authenticated'

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.state = 'disconnected'
        self.messages = Subject()

    def open(self):
        logger.info("Socket connected: %s", self)
        if self.instance is not None:
            logger.warning("Another socket already exists! Closing.")
            self.close()
            return
        self.instance = self
        self.state = 'unauthenticated'

    def check_origin(self, origin: str) -> bool:
        return True

    def on_message(self, message):
        logger.info("Got message: %s", message)
        obj = json.loads(message)
        logger.debug("Decoded message to object: %s", obj)

        action = obj.get('type')
        if action == 'authenticate':
            auth_state = self.verify_password(obj.get('value'))
            logger.info("Authentication state=%s", auth_state)
            self.send_obj({'type': 'authentication', 'value': auth_state})
            if auth_state:
                self.state = 'authenticated'
            else:
                self.close()
            return

        self.messages.on_next(obj)

    def on_connection_close(self) -> None:
        logger.info("Cleaning up %s", self)
        if self.instance == self:
            self.instance = None

    def send_obj(self, data):
        self.write_message(json.dumps(data))
