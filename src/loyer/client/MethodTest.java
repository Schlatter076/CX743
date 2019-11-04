package loyer.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import loyer.serial.SerialPortTools;

public class MethodTest {

  public static void main(String[] args) throws Exception {
    
    List<Integer> list = new ArrayList<>();
    list.add(5);
    list.add(7);
    list.add(2);
    list.add(3);
    list.add(10);
    list.add(15);
    list.add(6);
    //Collections.sort(list);
    System.out.println(list.toString());
    System.out.println(Collections.max(list));
    list.clear();
    System.out.println(list.toString());
    list.add(896);
    System.out.println(list.toString());
    System.out.println(Collections.max(list));
    
    int a = 0;
    a++;
    System.out.println(a);
    byte[] comB = new byte[16];
    byte[] data = {0x00, 0x23, (byte) 0xf3, (byte) 0xf4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x78, 0x0a, 0x00};
    for(int i = 0; i < data.length; i++) {
      if (isEquals(data[i], "f3") && isEquals(data[i + 1], "f4") && isEquals(data[i + 15], "0a")) {
        System.arraycopy(data, i, comB, 0, 16);
        break;
      }
    }
    System.out.println(SerialPortTools.bytesToHex(comB));
  }
  public static boolean isEquals(byte hex, String data) {
    return String.format("%02x", hex).equals(data);
  }
  public static void fillPosi(byte[] pos, int x, int y, int z) {
    pos[2] = (byte) ((x >> 24) & 0xff);
    pos[3] = (byte) ((x >> 16) & 0xff);
    pos[4] = (byte) ((x >> 8) & 0xff);
    pos[5] = (byte) (x & 0xff);
    pos[6] = (byte) ((y >> 24) & 0xff);
    pos[7] = (byte) ((y >> 16) & 0xff);
    pos[8] = (byte) ((y >> 8) & 0xff);
    pos[9] = (byte) (y & 0xff);
    pos[10] = (byte) ((z >> 24) & 0xff);
    pos[11] = (byte) ((z >> 16) & 0xff);
    pos[12] = (byte) ((z >> 8) & 0xff);
    pos[13] = (byte) (z & 0xff);
  }

}
