import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigInteger;
import java.security.MessageDigest;

/** Holds some static utility methods. */
class Util {
  static <K> Set<K> keysWithMinCount(Map<K, Integer> counts, int min) {
    return counts.keySet().stream()
        .filter(k -> counts.get(k) >= min)
        .collect(Collectors.toSet());
  }

  /**
   * SHA256散列函数
   * @param str
   * @return
   */
  public static String SHA256(String str) {
    String toReturn = null;
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.reset();
      digest.update(str.getBytes("utf8"));
      toReturn = String.format("%064x", new BigInteger(1, digest.digest()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return toReturn;
  }
}
