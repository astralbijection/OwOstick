#include <Arduino.h>
#include <SoftwareSerial.h>

#include "hc05.hpp"
#include "pins.hpp"

SoftwareSerial bt(PIN_BT_TX, PIN_BT_RX);

void setup() {
    Serial.begin(9600);
    bt.begin(38400);

    Serial.println("Starting");

    pinMode(PIN_BT_EN, OUTPUT);
    pinMode(PIN_BT_STATE, INPUT);
    digitalWrite(PIN_BT_EN, 0);

    pinMode(PIN_LED, OUTPUT);
    digitalWrite(PIN_LED, 1);
    {
        HC05_ATMode at(&bt, PIN_BT_EN, PIN_BT_STATE);
        delay(10);
        at.set_name("Vibe Check");
        at.set_pin("5432");
        at.set_role(HC05_Role_Slave);

        delay(1000);

        Serial.println(at.get_name());
    }
}

void loop() {}