#include <Arduino.h>
#include <SoftwareSerial.h>

#include "hc05.hpp"
#include "pins.hpp"

SoftwareSerial bt(PIN_BT_TX, PIN_BT_RX);

bool is_connected() { return digitalRead(PIN_BT_STATE); }

void set_motor(int level) { analogWrite(PIN_MOTOR, level); }

void setup() {
    Serial.begin(9600);

    Serial.println("Initializing...");

    bt.begin(38400);
    pinMode(PIN_BT_EN, OUTPUT);
    pinMode(PIN_BT_STATE, INPUT);
    pinMode(PIN_LED, OUTPUT);
    pinMode(PIN_MOTOR, OUTPUT);

    set_motor(0);
    digitalWrite(PIN_BT_EN, 0);
    digitalWrite(PIN_LED, 0);

    {
        HC05_ATMode at(&bt, PIN_BT_EN, PIN_BT_STATE);
        delay(10);
        at.set_name("Vibe Check");
        at.set_pin("8763");
        at.set_role(HC05_Role_Slave);
    }

    Serial.println("Ready to accept connections");
}

void loop() {
    while (!is_connected()) {
        delay(500);
    }
    Serial.println("Successfully connected");
    bt.println("READY");

    while (is_connected()) {
        digitalWrite(PIN_LED, 1);
        auto s = bt.readStringUntil('\n');
        digitalWrite(PIN_LED, 0);
        if (s.length() > 0) {
            Serial.print("Received command: ");
            Serial.println(s);
            auto power = int(1023 * constrain(s.toFloat(), 0.0, 1.0));
            set_motor(power);
            bt.println("OK");
        }
    }

    digitalWrite(PIN_LED, 0);
    Serial.println("Disconnected");
}