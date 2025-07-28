public class Main {
  public static void main(String[] args) {
    System.out.println("Banking Database server Starting...");

    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      System.out.println("Error: " + e);
    }
  }
}
