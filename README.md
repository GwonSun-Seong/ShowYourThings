# ShowYourThings
## English ##
## The application using this source is designed to help blind people identify objects. ##
The code for the main author can be found in the master branch.
The thesis can be found at the link below (Announced December 2, UCWIT 2022)
https://docs.google.com/document/d/1PrxLd7xtIwGm6G2fyHp54b1h77unuHvXjdIaMYEDV7A/edit?usp=sharing

## Project Timeline ##
second semester of 2022: Conception of the idea and initiation of application design
November 13, 2022: Completion of paper writing, submission to UCWIT 2022
November 25, 2022: Awarded the Best Presentation at the Co-Ed Industrial Mentoring Capstone Design final presentation in the university (among 17 teams)
December 2, 2022: Presentation at UCWIT 2022

June 1, 2023: Official launch on the Google Play Store
October 20, 2023: Final update for bug fixes and maintenance (version 4)
December 18, 2023: Service termination due to personal reasons and changes in the working environment

## Working Principle ##
1. Barcode recognition through camera using Google ML Kit library (EAN-13, ISBN, QR)
   
2-1. (Jsoup) Using the barcode value to search on the KoreanNet barcode lookup site, fetching product name information / Medium processing speed, moderate amount of barcode product name data
2-2. (Selenium) Sending the barcode value to a self-built server (Tomcat), following the next step based on the presence of data in the DB / Slow processing speed, large amount of barcode product name data
  If the product name exists in the DB: Display the stored product name result from the MySQL DB on the server's site
  If the product name does not exist in the DB: The server searches the barcode value on Consumer 24, saves the crawling result in the MySQL DB, and displays the product name crawling result on the server's site
2-3. (API) Using the barcode value to fetch product name information using the public data portal API (using Java to Python library, implemented in a separate Python file) / Fast processing speed, limited amount of barcode product name data

3. Providing the fetched product name information or the output information from the site to the user through a dialog and TTS, including price information crawled from shopping sites (Danawa, Naver) based on the product name

Extra. Implementation of user settings in the app for selecting between Jsoup, Selenium, API as the lookup method and adjusting TTS speed, etc., using RoomDB to store app settings

## 한국어 (Korean) ##
## 시각장애인들의 물품 식별을 위해 고안된 애플리케이션의 소스 코드입니다. ##
"master" 분기에서 주 작성자의 코드를 확인할 수 있습니다.
2022년 12월 2일에 소스 코드와 관련한 논문이 공개되었습니다. 아래의 링크에서 확인 가능합니다.
https://docs.google.com/document/d/1PrxLd7xtIwGm6G2fyHp54b1h77unuHvXjdIaMYEDV7A/edit?usp=sharing

## 프로젝트 타임라인 ##
22년 2학기 초 - 아이디어 고안 및 애플리케이션 설계 시작 
22년 11월 13일 - 논문 작성 완료, UCWIT 2022 투고
22년 11월 25일 - 교내 Co-Ed 산업체 멘토링 캡스톤 디자인 최종발표회 최우수상 수상 (17팀)
22년 12월 2일 - UCWIT 2022 발표회

23년 6월 1일 - 구글 플레이 스토어 정식 출시
23년 10월 20일 - 버그 수정 및 유지보수 마지막 업데이트 (version 4)
23년 12월 18일 - 개인 사정 및 작업 환경의 변화로 인한 서비스 종료

## 작동 원리 ##
1. Google ML Kit 라이브러리를 활용해 카메라를 통한 바코드 인식 (EAN-13, ISBN, QR)

2-1. (Jsoup) 바코드 인식 값을 바탕으로 코리안넷 바코드 조회 사이트에 검색, 상품명 정보를 받아 옴 / 처리 속도 중간, 바코드 상품명 데이터 양 중간  
2-2 (Selenium) 바코드 인식 값을 자체 구축 서버(톰캣)에 전달, DB의 데이터 존재 여부에 따라 다음 순서 실행 / 처리 속도 느림, 바코드 상품명 데이터 양 많음
  DB에 상품명이 있는 경우 - Mysql DB에 저장되어있는 상품명 결과를 구축한 서버의 사이트에 출력
  DB에 상품명이 없는 경우 - 자체 구축 서버에서 바코드값을 소비자 24에 검색, 크롤링 결과를 Mysql DB에 저장, 상품명 크롤링 결과를 구축한 서버의 사이트에 출력
2-3. (API) 바코드 인식 값을 바탕으로 공공데이터 포탈의 API를 활용해 상품명 정보를 받아 옴 (Java to Python 라이브러리를 활용, 별도의 python 파일로 해당 기능 구현) / 처리 속도 빠름, 바코드 상품명 데이터 양 적음

3. 받아온 상품명 정보 값 or 사이트의 출력 정보 값을 바탕으로 상품명, 해당 상품명을 바탕으로 쇼핑 사이트(다나와, 네이버)의 가격정보 크롤링하여 사용자에게 다이얼로그 및 TTS로 정보 제공

번외. 설정에서 Jsoup, Selenium, API 세 가지 조회 방식 중 선택 기능, TTS 속도 등의 사용자 설정값을 RoomDB를 구현해 앱 자체적으로 설정정보를 저장

