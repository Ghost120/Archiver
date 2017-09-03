package ru.kasyan;

/**
 * Created by Kasyanenko Konstantin
 * on 03.09.2017.
 */
class Octal {

    /**
     * Разбор восьмеричной строки из буфера заголовка
     */
    static long parseOctal(byte[] header, int offset, int length) {
        long result = 0;
        boolean stillPadding = true;

        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            if (header[i] == 0)
                break;

            if (header[i] == (byte) ' ' || header[i] == '0') {
                if (stillPadding)
                    continue;

                if (header[i] == (byte) ' ')
                    break;
            }

            stillPadding = false;

            result = ( result << 3 ) + ( header[i] - '0' );
        }

        return result;
    }

    /**
     * Write an octal integer to a header buffer.
     * 
     * @param value Значение для записи
     *
     * @param buf Буфер заголовка
     *
     * @param offset   Смещение
     *
     * @param length   Количество байтов
     * 
     * @return integer value of the octal bytes.
     */
    static int getOctalBytes(long value, byte[] buf, int offset, int length) {
        int idx = length - 1;

        buf[offset + idx] = 0;
        --idx;
        buf[offset + idx] = (byte) ' ';
        --idx;

        if (value == 0) {
            buf[offset + idx] = (byte) '0';
            --idx;
        } else {
            for (long val = value; idx >= 0 && val > 0; --idx) {
                buf[offset + idx] = (byte) ( (byte) '0' + (byte) ( val & 7 ) );
                val = val >> 3;
            }
        }

        for (; idx >= 0; --idx) {
            buf[offset + idx] = (byte) ' ';
        }

        return offset + length;
    }

    /**
     * Write the checksum octal integer to a header buffer.
     *
     * @param value
     *            The value to write.
     * @param buf
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * @return The integer value of the entry's checksum.
     */
    static int getCheckSumOctalBytes(long value, byte[] buf, int offset, int length) {
        getOctalBytes( value, buf, offset, length );
        buf[offset + length - 1] = (byte) ' ';
        buf[offset + length - 2] = 0;
        return offset + length;
    }

    /**
     * Write an octal long integer to a header buffer.
     * 
     * @param value
     *            The value to write.
     * @param buf
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * 
     * @return The long value of the octal bytes.
     */
    public static int getLongOctalBytes(long value, byte[] buf, int offset, int length) {
        byte[] temp = new byte[length + 1];
        getOctalBytes( value, temp, 0, length + 1 );
        System.arraycopy( temp, 0, buf, offset, length );
        return offset + length;
    }

}