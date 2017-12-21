
public class JavaFunctions {

    public static void print(String str) {
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

}
