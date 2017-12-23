import java.util.Scanner;

public class JavaFunctions {

    public static void print(String str) {
        System.out.print(str);
    }

    public static void print(int str) {
        System.out.print(str);
    }

    public static void print(double str) {
        System.out.print(str);
    }

    public static void print(boolean str) {
        System.out.print(str);
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static void println(int val) {
        System.out.println(val);
    }

    public static void println(boolean val) {
        System.out.println(val);
    }

    public static void println(double val) {
        System.out.println(val);
    }

    public static String toString(int val) {return String.valueOf(val);}

    public static String toString(double val) {return Double.toString((double) val);}

    public static String toString(boolean val) {return String.valueOf(val);}

    public static String concat(String base, String addition) {return base.concat(addition);}

    public static int size(String[] arr) {return arr.length;}

    public static int size(int[] arr) {return arr.length;}

    public static int size(boolean[] arr) {return arr.length;}

    public static int size(double[] arr) {return arr.length;}

    public static String readLine() {
        return new Scanner(System.in).nextLine();
    }

    public static int readInt() {
        return new Scanner(System.in).nextInt();
    }

    public static double readDouble() {
        return new Scanner(System.in).nextDouble();
    }

}
