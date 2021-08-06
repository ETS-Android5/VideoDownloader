package euphoria.psycho.share;

public class StringShare {

    public static String substring(String string, String first, String second) {
        int start = string.indexOf(first);
        if (start == -1) return null;
        start += first.length();
        int end = string.indexOf(second, start);
        if (end == -1) return null;
        return string.substring(start, end);
    }

    public static String substringAfter(String s, String delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) return s;
        return s.substring(index + delimiter.length());
    }

    public static String substringAfter(String s, char delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) return s;
        return s.substring(index + 1);
    }

    public static String substringAfterLast(String s, String delimiter) {
        int index = s.lastIndexOf(delimiter);
        if (index == -1) return s;
        return s.substring(index + delimiter.length());
    }

    public static String substringAfterLast(String s, char delimiter) {
        int index = s.lastIndexOf(delimiter);
        if (index == -1) return s;
        return s.substring(index + 1);
    }

    public static String substringBefore(String s, String delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) return s;
        return s.substring(0, index);
    }

    public static String substringBefore(String s, char delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) return s;
        return s.substring(0, index);
    }

    public static String substringBeforeLast(String s, String delimiter) {
        int index = s.lastIndexOf(delimiter);
        if (index == -1) return s;
        return s.substring(0, index);
    }

    public static String substringBeforeLast(String s, char delimiter) {
        int index = s.lastIndexOf(delimiter);
        if (index == -1) return s;
        return s.substring(0, index);
    }

}
