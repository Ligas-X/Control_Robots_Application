#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

//2, 3 for Arduino Micro
#define CE_PIN 7
#define CSN_PIN 8

#define UP 1
#define UPRIGHT 2
#define RIGHT 3
#define DOWNRIGHT 4
#define DOWN 5
#define DOWNLEFT 6
#define LEFT 7
#define UPLEFT 8
#define NONE 9

#define B1_A 5
#define B1_B 6
#define A1_A 9
#define A1_B 10

const uint64_t pipe = 0xF0F0F0F0E1LL; // Труба для первого приемника
//const uint64_t pipe = 0xF0F0F0F0D2LL; // Труба для второго приемника

RF24 radio(CE_PIN, CSN_PIN); // CE, CSN

void moveForward() // движение прямо
{
  digitalWrite(B1_A, LOW);
  analogWrite(B1_B, 160); // левое колесо - вперед
  digitalWrite(A1_A, LOW);
  analogWrite(A1_B, 160); // правое колесо - вперед
}

void moveForwardRight() // вперед и направо
{
  digitalWrite(B1_A, LOW);
  analogWrite(B1_B, 100); // левое колесо - вперед
  digitalWrite(A1_A, LOW);
  analogWrite(A1_B, 200); // правое колесо - вперед
}

void rotationRight() // вращение на месте в правую сторону
{
  analogWrite(B1_A, 100); // левое колесо - назад
  digitalWrite(B1_B, LOW);
  digitalWrite(A1_A, LOW);
  analogWrite(A1_B, 100); // правое колесо - вперед
}

void moveBackwardRight() // назад и направо
{
  analogWrite(B1_A, 100); // левое колесо - назад
  digitalWrite(B1_B, LOW);
  analogWrite(A1_A, 200); // правое колесо - назад
  digitalWrite(A1_B, LOW);
}

void moveBackward() // движение назад
{
  analogWrite(B1_A, 160);
  digitalWrite(B1_B, LOW);
  analogWrite(A1_A, 160);
  digitalWrite(A1_B, LOW);
}

void moveBackwardLeft() // назад и налево
{
  analogWrite(B1_A, 200); // левое колесо - назад
  digitalWrite(B1_B, LOW);
  analogWrite(A1_A, 100); // правое колесо - назад
  digitalWrite(A1_B, LOW);
}

void rotationLeft() // вращение на месте в левую сторону
{
  digitalWrite(B1_A, LOW);
  analogWrite(B1_B, 100); // левое колесо - вперед
  analogWrite(A1_A, 100); // правое колесо - назад
  digitalWrite(A1_B, LOW);
}

void moveForwardLeft() // вперед и налево
{
  digitalWrite(B1_A, LOW);
  analogWrite(B1_B, 200); // левое колесо - вперед
  digitalWrite(A1_A, LOW);
  analogWrite(A1_B, 100); // правое колесо - вперед
}

void stopMoving() // остановка
{
  digitalWrite(B1_A, LOW);
  digitalWrite(B1_B, LOW);
  digitalWrite(A1_A, LOW);
  digitalWrite(A1_B, LOW);
}

void setup() {
  Serial.begin(9600);
//  while (!Serial) {
//    ; // wait for serial port to connect. Needed for Leonardo only
//  }
  Serial.print("START RECEIVER ONE!!!");
  //Serial.print("START RECEIVER TWO!!!");
  
  pinMode(B1_A, OUTPUT);
  pinMode(B1_B, OUTPUT);
  pinMode(A1_A, OUTPUT);
  pinMode(A1_B, OUTPUT);
  delay(1000);

  radio.begin(); // включение модуля
  radio.setChannel(0); // установка канала вещания
  //radio.setRetries(15, 15); // установка интервала и количества попыток "дозвона" до приемника
  radio.setRetries(0, 0);
  radio.setAutoAck(1); // установка режима подтверждения приема
  //radio.setDataRate(RF24_250KBPS); // установка минимальной скорости RF24_250KBPS for 250kbs, RF24_1MBPS for 1Mbps, or RF24_2MBPS for 2Mbps
  radio.setDataRate(RF24_1MBPS);
  //radio.setPALevel(RF24_PA_MAX); // установка максимальной мощности  RF24_PA_MIN=-18dBm, RF24_PA_LOW=-12dBm, RF24_PA_MED=-6dBM, and RF24_PA_HIGH=0dBm.
  radio.setPALevel(RF24_PA_MIN);
  radio.openReadingPipe(1, pipe);
  radio.setPALevel(RF24_PA_MIN);
  radio.startListening();
}

void loop() {
  if (radio.available()) {

    int messageArray[4];
    //int arrSize = sizeof(messageArray) / sizeof(messageArray[0]);

    radio.read(messageArray, sizeof(messageArray)); // здесь массив наполняется данными

    //    for (int i = 0; i < arrSize; i++) {
    //      Serial.print(messageArray[i]);
    //    }
    //    Serial.println();

    /*
      if (messageArray[1] == 1) // если второй робот выбран
      {
      digitalWrite(13, HIGH); // включаем светодиод
      }
      else // иначе
      {
      digitalWrite(13, LOW); // выключаем светодиод
      }
    */

    switch (messageArray[3]) // выбираем направление движения
    {
      case UP: // вперед
        //Serial.println("Forward");
        moveForward();
        break;
      case UPRIGHT: // вперед и направо (левое колесо едет вперед быстрее правого)
        //Serial.println("Forward Right");
        moveForwardRight();
        break;
      case RIGHT: // поворот направо
        //Serial.println("Right");
        rotationRight();
        break;
      case DOWNRIGHT: // назад и направо (левое колесо едет назад быстрее правого)
        //Serial.println("Backward Right");
        moveBackwardRight();
        break;
      case DOWN: // назад
        //Serial.println("Backward");
        moveBackward();
        break;
      case DOWNLEFT: // назад и налево (правое колесо едет назад быстрее левого)
        //Serial.println("Backward Left");
        moveBackwardLeft();
        break;
      case LEFT: // поворот налево
        //Serial.println("Rotation Left");
        rotationLeft();
        break;
      case UPLEFT: // вперед и налево (правое колесо едет вперед быстрее левого)
        //Serial.println("Forward Left");
        moveForwardLeft();
        break;
      default: // стоп, если ни один из вышеперечисленных вариантов не подошел
        //Serial.println("Stop");
        stopMoving();
    }

  }
}
