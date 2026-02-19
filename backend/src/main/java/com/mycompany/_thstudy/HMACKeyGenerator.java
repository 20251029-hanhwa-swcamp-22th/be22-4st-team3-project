package com.mycompany._thstudy;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class HMACKeyGenerator {

  /* HS512 알고리즘을 사용하기 위해 키 사이즈가 512비트 이상이어야 함.
   * 이 클래스는 안전한 난수 키를 생성하여 콘솔에 출력해줌.
   * 출력된 키를 application.yml의 jwt.secret에 설정하면 됨.
   */
  public static void main(String[] args) {
    try {
      /* HS512를 위한 KeyGenerator 생성 */
      KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA512");
      keyGen.init(512); // 512비트 키 사이즈 설정

      /* 비밀 키 생성 */
      SecretKey secretKey = keyGen.generateKey();

      /* 키를 Base64로 인코딩하여 문자열로 변환 */
      String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

      System.out.println("HS512 Key: " + encodedKey);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}