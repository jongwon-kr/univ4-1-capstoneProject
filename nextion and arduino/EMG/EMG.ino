// Klien EMG 보드용 아두이노 코드

// 아두이노를 초기화합니다.
void setup() {
  // 센서를 초기화합니다.
  // 센서를 아두이노의 A0 핀에 연결합니다.

  // 시리얼 통신을 시작합니다.
  Serial.begin(9600);
}

// 아두이노가 반복적으로 실행하는 함수
void loop() {
  // 센서로부터 데이터를 읽습니다.
  delay(100);
  int data = analogRead(A0);
  // 데이터를 시리얼 모니터에 출력합니다.
  Serial.println(data);
}
