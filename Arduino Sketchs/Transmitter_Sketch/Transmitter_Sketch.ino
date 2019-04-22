#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

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

const uint64_t pipes[2] = {0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL};
int countPipes = 2;

RF24 radio(CE_PIN, CSN_PIN); // CE, CSN

char recievedData[2]; // полученный массив символов
//int sendingData[2]; // целочисленный массив, который будет отправлен

int controlMethods, directionValue;

int val; // переменная, в которую будем считывать значения из последовательного порта
int LED = 13; // пин, к которому подключен встроенный светодиод

int remakeArray[] = {9, 9, 9, 9};

char incomingByte;
String temp;

int firstRobot, secondRobot, thirdRobot;

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

void sendingCommand(int *command, size_t sizeArr, int pipeNumber) // отправка команды модулям
{
  radio.openWritingPipe(pipes[pipeNumber]); // открываем соответствующую трубу
  radio.flush_tx(); // сливаем буфер передачи
  radio.write(command, sizeArr); // отправляем команду
}

void setup()
{
  Serial.begin(9600); // инициализация последовательного порта
  Serial.print("START TRANSMITTER!!!");

  pinMode (B1_A, OUTPUT);
  pinMode (B1_B, OUTPUT);
  pinMode (A1_A, OUTPUT);
  pinMode (A1_B, OUTPUT);
  delay(1000);

  radio.begin(); // включение модуля
  radio.setChannel(0); // установка канала вещания
  //radio.setRetries(15, 15); // установка интервала и количества попыток "дозвона" до приемника
  radio.setRetries(0, 0); // установка интервала и количества попыток "дозвона" до приемника
  radio.setAutoAck(1); // установка режима подтверждения приема
  //radio.setDataRate(RF24_250KBPS); // установка минимальной скорости RF24_250KBPS for 250kbs, RF24_1MBPS for 1Mbps, or RF24_2MBPS for 2Mbps
  radio.setDataRate(RF24_1MBPS);
  //radio.setPALevel(RF24_PA_MAX); // установка максимальной мощности  RF24_PA_MIN=-18dBm, RF24_PA_LOW=-12dBm, RF24_PA_MED=-6dBM, and RF24_PA_HIGH=0dBm.
  radio.setPALevel(RF24_PA_MIN);
  radio.openWritingPipe(pipes[0]);
  radio.openWritingPipe(pipes[1]);
  radio.stopListening();
}

void loop()
{
  if (Serial.available()) {
    incomingByte = Serial.read();

    if (incomingByte == ',')
    {
      firstRobot = temp[0] - '0';
      secondRobot = temp[1] - '0';
      thirdRobot = temp[2] - '0';
      directionValue = temp[3] - '0';

      int sendingData[] = {firstRobot, secondRobot, thirdRobot, directionValue};

      //    if (remakeArray[0] == 1) // если первый робот выбран
      //    {
      //      digitalWrite(13, HIGH); // включаем светодиод
      //    }
      //    else // иначе
      //    {
      //      digitalWrite(13, LOW); // выключаем светодиод
      //    }


      switch (directionValue) // выбираем направление движения
      {
        case UP: // вперед
          //Serial.println("Forward");

          if (firstRobot == 1)
          {
            moveForward();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
            //            radio.openWritingPipe(pipes[0]); // открываем соответствующую трубу
            //            radio.flush_tx(); // сливаем буфер передачи
            //            radio.write(sendingData, sizeof(sendingData)); // отправляем команду
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
            //            radio.openWritingPipe(pipes[1]); // открываем соответствующую трубу
            //            radio.flush_tx(); // сливаем буфер передачи
            //            radio.write(sendingData, sizeof(sendingData)); // отправляем команду
          }
          break;
        case UPRIGHT: // вперед и направо (левое колесо едет вперед быстрее правого)
          //Serial.println("Forward Right");

          if (firstRobot == 1)
          {
            moveForwardRight();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
          break;
        case RIGHT: // поворот направо
          //Serial.println("Right");

          if (firstRobot == 1)
          {
            rotationRight();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
          break;
        case DOWNRIGHT: // назад и направо (левое колесо едет назад быстрее правого)
          //Serial.println("Backward Right");

          if (firstRobot == 1)
          {
            moveBackwardRight();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
          break;
        case DOWN: // назад
          //Serial.println("Backward");

          if (firstRobot == 1)
          {
            moveBackward();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
          break;
        case DOWNLEFT: // назад и налево (правое колесо едет назад быстрее левого)
          //Serial.println("Backward Left");

          if (firstRobot == 1)
          {
            moveBackwardLeft();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
          break;
        case LEFT: // поворот налево
          //Serial.println("Rotation Left");

          if (firstRobot == 1)
          {
            rotationLeft();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
          break;
        case UPLEFT: // вперед и налево (правое колесо едет вперед быстрее левого)
          //Serial.println("Forward Left");

          if (firstRobot == 1)
          {
            moveForwardLeft();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
          break;
        default: // стоп, если ни один из вышеперечисленных вариантов не подошел
          //Serial.println("Stop");

          if (firstRobot == 1)
          {
            stopMoving();
          }
          if (secondRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 0);
          }
          if (thirdRobot == 1)
          {
            sendingCommand(sendingData, sizeof(sendingData), 1);
          }
      }

      temp = "";
    }
    else
    {
      temp += incomingByte;
      delay(2);
    }
  }

}
