package java.lang;

import java.util.Scanner;

/**
 * Predefined static imports
 */
public final class ImportStatic {

    private static Scanner scanner = null;

    /** Don't let anyone instantiate this class */
    private ImportStatic() {
    }

    /**
     * Prints an object to the "standard" output stream.
     *
     * @param      obj   The {@code Object} to be printed
     * @see java.lang.System#out
     * @see java.io.PrintStream#print(Object)
     * @see java.lang.Object#toString()
     * @see java.nio.charset.Charset#defaultCharset()
     */
    public static void print(Object obj) {
        System.out.print(obj);
    }

    /**
     * Prints an Object to the "standard" output stream and then terminates
     * the line.
     *
     * @param obj  The {@code Object} to be printed.
     * @see java.lang.System#out
     * @see java.io.PrintStream#println(Object)
     */
    public static void println(Object obj) {
        System.out.println(obj);
    }

    /**
     * Terminates the current line by writing the line separator string to
     * the "standard" output stream.
     *
     * @see java.lang.System#out
     * @see java.io.PrintStream#println()
     */
    public static void println() {
        System.out.println();
    }

   /**
    * Reads a single line of text from the console.
    * @throws java.io.IOError If an I/O error occurs.
    * @return  A string containing the line read from the console, not
    *          including any line-termination characters.
    *
    * @see java.lang.System#console()
    * @see java.io.Console#readLine()
    */
    public static String readLine() {
        var s = scanner;
        if (s == null) {
            s = new Scanner(System.in);
            scanner = s;
        }
        return s.nextLine();
    }
}
