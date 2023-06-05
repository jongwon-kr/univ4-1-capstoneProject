#include <SoftwareSerial.h>    // 소프트웨어시리얼을 사용하기위한 헤더추가
#include <Nextion.h>
#include <Adafruit_MLX90614.h>
SoftwareSerial SerialForNex(10, 11);  // RX:10 ,TX: 11
SoftwareSerial hc06(2, 3);  // RX : 2, TX : 2

#include <Wire.h>
#include "MAX30105.h"
#include "heartRate.h"
int maxValue, avgValue, minValue, cValue, lastCheck;
unsigned int sumValue;

String cMax, cAvg, cMin, inTemp, outTemp;
NexButton btnTest1 = NexButton(2, 4, "b0");
NexButton btnTest2 = NexButton(3, 4, "b0");
NexButton btnTest3 = NexButton(4, 3, "b0");
NexButton btnTest4 = NexButton(5, 2, "b0");
NexButton btnTest5 = NexButton(9, 10, "b2");

// 결과전송(bluetooth)버튼
NexButton hBT = NexButton(6, 2, "b0");
NexButton gBT = NexButton(7, 3, "b0");
NexButton bBT = NexButton(8, 3, "b0");
NexButton TBT = NexButton(9, 3, "b0");

NexTouch *nex_listen_list[] = {
  &btnTest1, &btnTest2, &btnTest3, &btnTest4, &btnTest5, &hBT, &gBT, &bBT, &TBT, NULL
};


void btnTest1Callback(void *ptr) {
  NexText hMax = NexText(6, 8, "maxT");
  NexText hAvg = NexText(6, 9, "avgT");
  NexText hMin = NexText(6, 10, "minT");
  NexWaveform hS = NexWaveform(6, 4, "s0");
  maxValue = 0;
  avgValue = 0;
  minValue = 0;
  cValue = 0;
  sumValue = 0;
  pinMode(8, INPUT); // Setup for leads off detection LO +
  pinMode(9, INPUT); // Setup for leads off detection LO -
  for (int i = 0; i < 200; i++) {
    if ((digitalRead(8) == 1) || (digitalRead(9) == 1)) {
      Serial.println('!');
    }
    else {
      // send the value of analog input 0:
      cValue = analogRead(A0);
      sumValue += cValue;
      avgValue = sumValue / (i + 1);
      if (cValue > 0  && cValue > maxValue) {
        maxValue = cValue;
      }
      if (cValue  > 20) {
        if (minValue == 0)
          minValue = cValue;
        if (cValue < minValue) {
          minValue = cValue;
        }
      }
      cMax = String(maxValue);
      hMax.setText(cMax.c_str());
      cMin = String(minValue);
      hMin.setText(cMin.c_str());
      cAvg = String(avgValue);
      hAvg.setText(cAvg.c_str());
      hS.addValue(0, cValue);
    }
    //Wait for a bit to keep serial data from saturating
  }
  lastCheck = 1;
}

void btnTest2Callback(void *ptr) {
  NexText gMax = NexText(7, 8, "maxT");
  NexText gAvg = NexText(7, 9, "avgT");
  NexText gMin = NexText(7, 10, "minT");
  NexWaveform gS = NexWaveform(7, 4, "s0");
  maxValue = 0;
  avgValue = 0;
  minValue = 0;
  cValue = 0;
  sumValue = 0;
  // 센서로부터 데이터를 읽습니다.
  for (int i = 0; i < 200; i++) {
    cValue = analogRead(A0);
    gS.addValue(0, cValue);
    sumValue += cValue;
    avgValue = sumValue / (i + 1);
    if (cValue > 0  && cValue > maxValue) {
      maxValue = cValue;
    }
    if (cValue  > 0) {
      if (minValue == 0)
        minValue = cValue;
      if (cValue < minValue) {
        minValue = cValue;
      }
    }

    cMax = String(maxValue);
    gMax.setText(cMax.c_str());
    cAvg = String(avgValue);
    gAvg.setText(cAvg.c_str());
    cMin = String(minValue);
    gMin.setText(cMin.c_str());
  }
  lastCheck = 2;
}

void btnTest3Callback(void *ptr) {
  Serial.begin(115200);
  NexText bMax = NexText(8, 8, "maxT");
  NexText bAvg = NexText(8, 9, "avgT");
  NexText bMin = NexText(8, 10, "minT");
  NexWaveform bS = NexWaveform(8, 4, "s0");
  cValue = 0;
  maxValue = 0;
  avgValue = 0;
  minValue = 0;
  sumValue = 0;
  MAX30105 particleSensor;
  const byte RATE_SIZE = 4; //Increase this for more averaging. 4 is good.
  byte rates[RATE_SIZE]; //Array of heart rates

  byte rateSpot = 0;
  long lastBeat = 0; //Time at which the last beat occurred
  float beatsPerMinute;

  // Initialize sensor
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) //Use default I2C port, 400kHz speed
  {
    while (1);
  }

  particleSensor.setup(); //Configure sensor with default settings
  particleSensor.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0); //Turn off Green LED

  for (int i = 0; i < 1000; i++) {
    long irValue = particleSensor.getIR();

    if (checkForBeat(irValue) == true)
    {
      //We sensed a beat!
      long delta = millis() - lastBeat;
      lastBeat = millis();

      beatsPerMinute = 60 / (delta / 1000.0);
      cValue = beatsPerMinute;
      if (beatsPerMinute < 255 && beatsPerMinute > 20)
      {
        rates[rateSpot++] = (byte)beatsPerMinute; //Store this reading in the array
        rateSpot %= RATE_SIZE; //Wrap variable

        //Take average of readings
        avgValue = 0;
        for (byte x = 0 ; x < RATE_SIZE ; x++)
          avgValue += rates[x];
        avgValue /= RATE_SIZE;
      }
    }
    /*
      Serial.print("IR=");
      Serial.print(irValue);
      Serial.print(", BPM=");
      Serial.print(beatsPerMinute);
    */
    bS.addValue(0, cValue);
    if (cValue > 20  && cValue > maxValue) {
      maxValue = cValue;
    }
    if (cValue  > 20) {
      if (minValue == 0)
        minValue = cValue;
      if (cValue < minValue) {
        minValue = cValue;
      }
    }
    /*
      Serial.print(", Avg BPM=");
      Serial.print(avgValue);
      Serial.print(", Max BPM=");
      Serial.print(maxValue);
      Serial.print(", Min BPM=");
      Serial.print(minValue);
      Serial.print(", i=");
      Serial.println(i);
    */
    if (maxValue == 0) {
      i = 0;
    }
  }
  cMax = String(maxValue);
  bMax.setText(cMax.c_str());
  cAvg = String(avgValue);
  bAvg.setText(cAvg.c_str());
  cMin = String(minValue);
  bMin.setText(cMin.c_str());
  lastCheck = 3;
}

void btnTest4Callback(void *ptr) {
  // 온도 측정 테스트
  NexText outTempT = NexText(9, 6, "outTemp");
  NexText inTempT = NexText(9, 7, "inTemp");
  Adafruit_MLX90614 mlx = Adafruit_MLX90614();
  mlx.begin();
  float outTempf = mlx.readAmbientTempC();
  float inTempf = mlx.readObjectTempC();

  outTemp = String(outTempf);
  outTempT.setText(outTemp.c_str());
  inTemp = String(inTempf);
  inTempT.setText(inTemp.c_str());
  delay(100);
  lastCheck = 4;
}

// 블루투스를 통한 결과 전송
void BTbtnCallback(void *ptr) {
  String sendMsg;
  if (lastCheck == 1) {
    sendMsg = "#h#" + cMax + "#" + cAvg + "#" + cMin;
    byte buff[sendMsg.length()];
    for(int i = 0;i<sendMsg.length();i++){
      buff[i] = sendMsg[i];
    }
    hc06.write(buff, sendMsg.length());
  } else if (lastCheck == 2) {
    sendMsg = "#g#" + cMax + "#" + cAvg + "#" + cMin;
    byte buff[sendMsg.length()];
    for(int i = 0;i<sendMsg.length();i++){
      buff[i] = sendMsg[i];
    }
    hc06.write(buff, sendMsg.length());
  } else if (lastCheck == 3) {
    sendMsg = "#b#" + cMax + "#" + cAvg + "#" + cMin;
    byte buff[sendMsg.length()];
    for(int i = 0;i<sendMsg.length();i++){
      buff[i] = sendMsg[i];
    }
    hc06.write(buff, sendMsg.length());
  } else if (lastCheck == 4) {
    sendMsg = "#b#" + outTemp + "#" + inTemp;
        byte buff[sendMsg.length()];
    for(int i = 0;i<sendMsg.length();i++){
      buff[i] = sendMsg[i];
    }
    hc06.write(buff, sendMsg.length());
  }
}

void setup(void) {
  Serial.begin(9600);
  hc06.begin(9600);
  nexInit();
  btnTest1.attachPop(btnTest1Callback, &btnTest1);
  btnTest2.attachPop(btnTest2Callback, &btnTest2);
  btnTest3.attachPop(btnTest3Callback, &btnTest3);
  btnTest4.attachPop(btnTest4Callback, &btnTest4);
  btnTest5.attachPop(btnTest4Callback, &btnTest5);
  hBT.attachPop(BTbtnCallback, &hBT);
  gBT.attachPop(BTbtnCallback, &gBT);
  bBT.attachPop(BTbtnCallback, &bBT);
  TBT.attachPop(BTbtnCallback, &TBT);
}

void loop(void) {
  nexLoop(nex_listen_list);
}
