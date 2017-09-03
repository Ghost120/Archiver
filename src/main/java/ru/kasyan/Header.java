package ru.kasyan;

import java.io.File;

/**
 * Created by Kasyanenko Konstantin
 * on 03.09.2017.
 */
public class Header {

    static final int NAMELEN = 100;
    static final int MODELEN = 8;
    static final int UIDLEN = 8;
    static final int GIDLEN = 8;
    static final int SIZELEN = 12;
    static final int MODTIMELEN = 12;
    static final int CHKSUMLEN = 8;

    static final byte LF_DIR = (byte) '5';
    private static final byte LF_NORMAL = (byte) '0';
    private static final String USTAR_MAGIC = "ustar"; // POSIX

    static final int USTAR_MAGICLEN = 8;
    static final int USTAR_USER_NAMELEN = 32;
    static final int USTAR_GROUP_NAMELEN = 32;
    static final int USTAR_DEVLEN = 8;
    static final int USTAR_FILENAME_PREFIX = 155;

    // Header values
    StringBuffer name;
    int mode;
    int userId;
    int groupId;
    long size;
    long modTime;
    int checkSum;
    byte linkFlag;
    StringBuffer linkName;
    StringBuffer magic; // ustar indicator and version
    StringBuffer userName;
    StringBuffer groupName;
    int devMajor;
    int devMinor;
    StringBuffer namePrefix;

    Header() {
        this.magic = new StringBuffer(Header.USTAR_MAGIC);

        this.name = new StringBuffer();
        this.linkName = new StringBuffer();

        String user = System.getProperty("user.name", "");

        if (user.length() > 31)
            user = user.substring(0, 31);

        this.userId = 0;
        this.groupId = 0;
        this.userName = new StringBuffer(user);
        this.groupName = new StringBuffer("");
        this.namePrefix = new StringBuffer();
    }

    /**
     * Parse an entry name from a header buffer.
     *
     * @param header
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * @return The header's entry name.
     */
    public static StringBuffer parseName(byte[] header, int offset, int length) {
        StringBuffer result = new StringBuffer(length);

        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            if (header[i] == 0)
                break;
            result.append((char) header[i]);
        }

        return result;
    }

    /**
     * Determine the number of bytes in an entry name.
     *
     * @param name
     *            The header buffer from which to parse.
     * @param offset
     *            The offset into the buffer from which to parse.
     * @param length
     *            The number of header bytes to parse.
     * @return The number of bytes in a header's entry name.
     */
    static int getNameBytes(StringBuffer name, byte[] buf, int offset, int length) {
        int i;

        for (i = 0; i < length && i < name.length(); ++i) {
            buf[offset + i] = (byte) name.charAt(i);
        }

        for (; i < length; ++i) {
            buf[offset + i] = 0;
        }

        return offset + length;
    }

    /**
     * Creates a new header for a file/directory entry.
     *
     *
     * @param entryName
     *            File name
     * @param size
     *            File size in bytes
     * @param modTime
     *            Last modification time in numeric Unix time format
     * @param dir
     *            Is directory
     */
    static Header createHeader(String entryName, long size, long modTime, boolean dir, int permissions) {
        String name = entryName;
        name = trim(name.replace(File.separatorChar, '/'), '/');

        Header header = new Header();
        header.linkName = new StringBuffer("");
        header.mode = permissions;

        if (name.length() > 100) {
            header.namePrefix = new StringBuffer(name.substring(0, name.lastIndexOf('/')));
            header.name = new StringBuffer(name.substring(name.lastIndexOf('/') + 1));
        } else {
            header.name = new StringBuffer(name);
        }
        if (dir) {
            header.linkFlag = Header.LF_DIR;
            if (header.name.charAt(header.name.length() - 1) != '/') {
                header.name.append("/");
            }
            header.size = 0;
        } else {
            header.linkFlag = Header.LF_NORMAL;
            header.size = size;
        }

        header.modTime = modTime;
        header.checkSum = 0;
        header.devMajor = 0;
        header.devMinor = 0;

        return header;
    }

    private static String trim(String s, char c) {
        StringBuffer tmp = new StringBuffer(s);
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        for (int i = tmp.length() - 1; i >= 0; i--) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        return tmp.toString();
    }
}