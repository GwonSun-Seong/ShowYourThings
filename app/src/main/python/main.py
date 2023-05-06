import requests
import xml.etree.ElementTree as ET
import myapikey

def Useapi(barcode):
    keyId = myapikey.keyId
    serviceId = "C005"

    # API 요청 URL을 생성합니다.
    url = f"http://openapi.foodsafetykorea.go.kr/api/{keyId}/{serviceId}/xml/1/1/BAR_CD={barcode}"

    # API 요청을 보내고 응답을 받습니다.
    response = requests.get(url)

    # XML 응답을 파싱합니다.
    root = ET.fromstring(response.content)

    # 파싱한 XML에서 원하는 데이터를 추출합니다.
    for item in root.iter("row"):
        prdlst_nm = item.find("PRDLST_NM").text
        return prdlst_nm
