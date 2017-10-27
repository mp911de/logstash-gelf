package biz.paluch.logging.gelf.intern;

/**
 * Data type discovery for {@link String} value types. Discovers an indicator whether a type is a {@link Result#LONG}, a
 * {@link Result#DOUBLE} or {@link Result#STRING} type.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class ValueDiscovery {

    static Result discover(String value) {

        long len = value.length();

        if (len == 0 || len > 32) {
            return Result.STRING;
        }

        int points = -1;

        char firstChar = value.charAt(0);
        if (firstChar < '0' || firstChar > '9') { // Possible leading "+" or "-"

            if (firstChar == 'N' && value.contains("NaN")) {
                return Result.DOUBLE;
            }

            if (firstChar == 'I' && value.contains("Infinity")) {
                return Result.DOUBLE;
            }

            if (firstChar != '-' && firstChar != '+') {
                return Result.STRING;
            }
        }

        Result discoveryResult = Result.LONG;

        for (int i = 1; i < len; i++) {

            char c = value.charAt(i);

            if (c >= '0' && c <= '9') {
                continue;
            }

            switch (c) {

                case '.':

                    if (points != -1) {
                        return Result.STRING;
                    }

                    points = i;
                    discoveryResult = Result.DOUBLE;
                    break;

                case '-':
                case '+':
                    discoveryResult = Result.DOUBLE;
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'x':
                case 'X':
                case 'P':
                case 'p':
                    discoveryResult = Result.DOUBLE;
                    break;
                default:
                    return Result.STRING;

            }
        }

        return discoveryResult;
    }

    enum Result {

        STRING, LONG, DOUBLE;

    }
}
