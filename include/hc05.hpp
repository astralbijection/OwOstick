#include <Arduino.h>

enum HC05_Role { HC05_Role_Slave = 0, HC05_Role_Master = 1 };

/**
 * https://www.teachmemicro.com/hc-05-bluetooth-command-list/
 */
class HC05_ATMode {
    Stream *stream;
    int pin_en;
    int pin_state;

   public:
    HC05_ATMode(Stream *stream, int pin_en, int pin_state);

    void reset();
    void set_role(HC05_Role mode);
    String get_name();
    void set_name(String name);
    String get_pin();
    void set_pin(String pin);

    ~HC05_ATMode();
};
