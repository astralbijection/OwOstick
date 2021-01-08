#include "hc05.hpp"

HC05_ATMode::HC05_ATMode(Stream *stream, int pin_en, int pin_state)
    : stream(stream), pin_en(pin_en), pin_state(pin_state) {
    digitalWrite(pin_en, 1);
}

void HC05_ATMode::reset() {}

String HC05_ATMode::get_name() {
    stream->println("AT+NAME?");
    return stream->readStringUntil('\n');
}

void HC05_ATMode::set_name(String name) {
    stream->print("AT+NAME=");
    stream->println(name);
    stream->readStringUntil('\n');
}

String HC05_ATMode::get_pin() {
    stream->println("AT+PSWD?");
    stream->readBytes((char *)nullptr, 6);
    return stream->readStringUntil('\n');
}

void HC05_ATMode::set_pin(String pin) {
    stream->print("AT+PSWD=");
    stream->println(pin);
    stream->readStringUntil('\n');
}

void HC05_ATMode::set_role(HC05_Role role) {
    stream->print("AT+ROLE=");
    stream->println(role);
    stream->readStringUntil('\n');
}

HC05_ATMode::~HC05_ATMode() { digitalWrite(pin_en, 0); }