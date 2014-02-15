

#include <G35String.h>
#include <Math.h>

// Total # of lights on string (usually 50, 48, or 36). Maximum is 63, because
// the protocol uses 6-bit addressing and bulb #63 is reserved for broadcast
// messages.
#define LIGHT_COUNT (25)  // 2-25 works great.  1 thinks it's position 32?

// Arduino pin number. Pin 13 will blink the on-board LED.
#define G35_PIN (2)

G35String lights(G35_PIN, LIGHT_COUNT);

int test_count = 0;
int completed_tests = 0;
int failed_tests = 0;

const int START = -1;
const int FINISH = -2;
const int FAILURE = -3;
const int SUCCESS = -4;

const int ACK = 6;

void setup()
{
  Serial.begin(115200);
  
  lights.enumerate();
  lights.fill_color(0, 25, G35::MAX_INTENSITY, COLOR_BLACK);
}

void loop()
{
  // float percentage = 2.0 / 4.0;
  float percentage = (float)completed_tests / (float)test_count;
  int last_bulb = ceil(percentage * (float)LIGHT_COUNT);
  
  color_t color = failed_tests > 0 ? COLOR_RED : COLOR_GREEN;
  
  lights.fill_color(0, last_bulb, G35::MAX_INTENSITY, color);
  
  delay(10); // 
}

void serialEvent() // To check if there is any data on the Serial line
{
  while (Serial.available())
  {
    int val = Serial.parseInt();
    if(val == START)   
    {
      test_count = 0;
      completed_tests = 0;
      failed_tests = 0;
      lights.fill_color(0, 25, G35::MAX_INTENSITY, COLOR_BLACK);
    }
    else if(val == FINISH)
    {
    }
    else if(val == FAILURE)
    {
      failed_tests++;
      completed_tests++;
    }
    else if(val == SUCCESS) 
    {
      completed_tests++;
    }
    else
    {
      test_count = val;
    }
    
    Serial.println(ACK);
  }
}




