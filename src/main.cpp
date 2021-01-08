#include <Arduino.h>
#include <SoftwareSerial.h>

#include "hc05.hpp"
#include "pins.hpp"

SoftwareSerial bt(PIN_BT_TX, PIN_BT_RX);

bool is_connected() { return digitalRead(PIN_BT_STATE); }

void set_motor(int level) { analogWrite(PIN_MOTOR, level); }

void setup() {
    Serial.begin(9600);
    bt.begin(38400);

    Serial.println("Starting");

    pinMode(PIN_BT_EN, OUTPUT);
    pinMode(PIN_BT_STATE, INPUT);
    pinMode(PIN_LED, OUTPUT);
    pinMode(PIN_MOTOR, OUTPUT);

    set_motor(0);
    digitalWrite(PIN_BT_EN, 0);
    digitalWrite(PIN_LED, 1);

    {
        HC05_ATMode at(&bt, PIN_BT_EN, PIN_BT_STATE);
        delay(10);
        at.set_name("Vibe Check");
        at.set_pin("8763");
        at.set_role(HC05_Role_Slave);
    }

    set_motor(1023);
}

void loop() {
    while (!is_connected()) {
        delay(500);
    }
    bt.println("OK");
    while (is_connected()) {
    }
}