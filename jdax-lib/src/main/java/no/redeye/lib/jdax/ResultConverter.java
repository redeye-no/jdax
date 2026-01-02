package no.redeye.lib.jdax;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class ResultConverter {

    private ResultConverter() {
    }

    /* =========================
     * Numeric helpers
     * ========================= */
    static Integer toInteger(Object v) {
        return ((Number) v).intValue();
    }

    static Long toLong(Object v) {
        return ((Number) v).longValue();
    }

    static Short toShort(Object v) {
        return ((Number) v).shortValue();
    }

    static Byte toByte(Object v) {
        return ((Number) v).byteValue();
    }

    static Double toDouble(Object v) {
        return ((Number) v).doubleValue();
    }

    static Float toFloat(Object v) {
        return ((Number) v).floatValue();
    }

    static BigDecimal toBigDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        return BigDecimal.valueOf(((Number) v).longValue());
    }

    static BigInteger toBigInteger(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigInteger bi) {
            return bi;
        }
        return BigInteger.valueOf(((Number) v).longValue());
    }

//
//    static BigDecimal toBigDecimal(Object v) {
//        return BigDecimal.valueOf(((Number) v).longValue());
//    }
//
//    static BigInteger toBigInteger(Object v) {
//        return BigInteger.valueOf(((Number) v).longValue());
//    }
    /* =========================
     * Boolean
     * ========================= */
    static Boolean toBoolean(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        if (v instanceof Number n) {
            return n.intValue() != 0;
        }
        return Boolean.parseBoolean(v.toString());
    }

    /* =========================
     * Character / text
     * ========================= */
    static String toStringValue(Object v) {
        return v.toString();
    }

    /* =========================
     * Binary
     * ========================= */
    static byte[] toBytes(Object v) throws SQLException {
        if (v == null) {
            return null;
        }
        if (v instanceof byte[] b) {
            return b;
        }
        if (v instanceof Blob blob) {
            return blob.getBytes(1, (int) blob.length());
        }
        throw new SQLException("Unsupported binary type: " + v.getClass());
    }

    static InputStream toBinaryStream(Object v) throws SQLException {
        if (v == null) {
            return null;
        }
        if (v instanceof InputStream is) {
            return is;
        }
        if (v instanceof Blob blob) {
            return blob.getBinaryStream();
        }
        if (v instanceof byte[] b) {
            return new ByteArrayInputStream(b);
        }
        throw new SQLException("Unsupported binary stream type: " + v.getClass());
    }

    /* =========================
     * Temporal
     * ========================= */
    static Date toDate(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Date d) {
            return d;
        }
        if (v instanceof Timestamp ts) {
            return new Date(ts.getTime());
        }
        throw new IllegalArgumentException("Unsupported date type: " + v.getClass());
    }

    static Time toTime(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Time t) {
            return t;
        }
        if (v instanceof Timestamp ts) {
            return new Time(ts.getTime());
        }
        throw new IllegalArgumentException("Unsupported time type: " + v.getClass());
    }

    static Timestamp toTimestamp(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Timestamp ts) {
            return ts;
        }
        if (v instanceof Date d) {
            return new Timestamp(d.getTime());
        }
        throw new IllegalArgumentException("Unsupported timestamp type: " + v.getClass());
    }

    /* =========================
     * Object passthrough
     * ========================= */
    static Object identity(Object v) {
        return v;
    }

    /* =========================
     * Void / NULL target
     * ========================= */
    static Object toVoid(Object v) {
        return null;
    }
}
