package ru.kasyan;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * Created by Kasyanenko Konstantin
 * on 03.09.2017.
 */
public class Entry {
	protected File file;
	protected Header header;
	private static Map<PosixFilePermission, Integer> posixPermissionToInteger = new HashMap<>();

	private Entry() {
		this.file = null;
		header = new Header();
	}

	public Entry(File file, String entryName) {
		this();
		this.file = file;
		this.extractTarHeader(entryName);
	}

	public Entry(byte[] headerBuf) {
		this();
		this.parseTarHeader(headerBuf);
	}

	/**
	 * Constructor to create an entry from an existing Header object.
	 * 
	 * This method is useful to add new entries programmatically (e.g. for
	 * adding files or directories that do not exist in the file system).
	 */
	public Entry(Header header) {
		this.file = null;
		this.header = header;
	}

	@Override
	public boolean equals(Object it) {
		if (!(it instanceof Entry)) {
			return false;
		}
		return header.name.toString().equals(
				((Entry) it).header.name.toString());
	}

	@Override
	public int hashCode() {
		return header.name.hashCode();
	}

	public boolean isDescendent(Entry desc) {
		return desc.header.name.toString().startsWith(header.name.toString());
	}

	public Header getHeader() {
		return header;
	}

	public String getName() {
		String name = header.name.toString();
		if (header.namePrefix != null && !header.namePrefix.toString().equals("")) {
			name = header.namePrefix.toString() + "/" + name;
		}

		return name;
	}

	public void setName(String name) {
		header.name = new StringBuffer(name);
	}

	public int getUserId() {
		return header.userId;
	}

	public void setUserId(int userId) {
		header.userId = userId;
	}

	public int getGroupId() {
		return header.groupId;
	}

	public void setGroupId(int groupId) {
		header.groupId = groupId;
	}

	public String getUserName() {
		return header.userName.toString();
	}

	public void setUserName(String userName) {
		header.userName = new StringBuffer(userName);
	}

	public String getGroupName() {
		return header.groupName.toString();
	}

	public void setGroupName(String groupName) {
		header.groupName = new StringBuffer(groupName);
	}

	public void setIds(int userId, int groupId) {
		this.setUserId(userId);
		this.setGroupId(groupId);
	}

	public void setModTime(long time) {
		header.modTime = time / 1000;
	}

	public void setModTime(Date time) {
		header.modTime = time.getTime() / 1000;
	}

	public Date getModTime() {
		return new Date(header.modTime * 1000);
	}

	public File getFile() {
		return this.file;
	}

	public long getSize() {
		return header.size;
	}

	public void setSize(long size) {
		header.size = size;
	}

	/**
	 * Checks if the org.kamrazafar.jtar entry is a directory
	 */
	public boolean isDirectory() {
		if (this.file != null)
			return this.file.isDirectory();

		if (header != null) {
			if (header.linkFlag == Header.LF_DIR)
				return true;

			if (header.name.toString().endsWith("/"))
				return true;
		}

		return false;
	}

	/**
	 * Extract header from File
	 */
	public void extractTarHeader(String entryName) {
		int permissions = permissions(file);
		header = Header.createHeader(entryName, file.length(), file.lastModified() / 1000, file.isDirectory(), permissions);
	}

	/**
	 * Calculate checksum
	 */
	public long computeCheckSum(byte[] buf) {
		long sum = 0;

		for (int i = 0; i < buf.length; ++i) {
			sum += 255 & buf[i];
		}

		return sum;
	}

	/**
	 * Writes the header to the byte buffer
	 */
	public void writeEntryHeader(byte[] outbuf) {
		int offset = 0;

		offset = Header.getNameBytes(header.name, outbuf, offset, Header.NAMELEN);
		offset = Octal.getOctalBytes(header.mode, outbuf, offset, Header.MODELEN);
		offset = Octal.getOctalBytes(header.userId, outbuf, offset, Header.UIDLEN);
		offset = Octal.getOctalBytes(header.groupId, outbuf, offset, Header.GIDLEN);

		long size = header.size;

		offset = Octal.getLongOctalBytes(size, outbuf, offset, Header.SIZELEN);
		offset = Octal.getLongOctalBytes(header.modTime, outbuf, offset, Header.MODTIMELEN);

		int csOffset = offset;
		for (int c = 0; c < Header.CHKSUMLEN; ++c)
			outbuf[offset++] = (byte) ' ';

		outbuf[offset++] = header.linkFlag;

		offset = Header.getNameBytes(header.linkName, outbuf, offset, Header.NAMELEN);
		offset = Header.getNameBytes(header.magic, outbuf, offset, Header.USTAR_MAGICLEN);
		offset = Header.getNameBytes(header.userName, outbuf, offset, Header.USTAR_USER_NAMELEN);
		offset = Header.getNameBytes(header.groupName, outbuf, offset, Header.USTAR_GROUP_NAMELEN);
		offset = Octal.getOctalBytes(header.devMajor, outbuf, offset, Header.USTAR_DEVLEN);
		offset = Octal.getOctalBytes(header.devMinor, outbuf, offset, Header.USTAR_DEVLEN);
		offset = Header.getNameBytes(header.namePrefix, outbuf, offset, Header.USTAR_FILENAME_PREFIX);

		for (; offset < outbuf.length;)
			outbuf[offset++] = 0;

		long checkSum = this.computeCheckSum(outbuf);

		Octal.getCheckSumOctalBytes(checkSum, outbuf, csOffset, Header.CHKSUMLEN);
	}

	/**
	 * Parses the tar header to the byte buffer
	 */
	public void parseTarHeader(byte[] bh) {
		int offset = 0;

		header.name = Header.parseName(bh, offset, Header.NAMELEN);
		offset += Header.NAMELEN;

		header.mode = (int) Octal.parseOctal(bh, offset, Header.MODELEN);
		offset += Header.MODELEN;

		header.userId = (int) Octal.parseOctal(bh, offset, Header.UIDLEN);
		offset += Header.UIDLEN;

		header.groupId = (int) Octal.parseOctal(bh, offset, Header.GIDLEN);
		offset += Header.GIDLEN;

		header.size = Octal.parseOctal(bh, offset, Header.SIZELEN);
		offset += Header.SIZELEN;

		header.modTime = Octal.parseOctal(bh, offset, Header.MODTIMELEN);
		offset += Header.MODTIMELEN;

		header.checkSum = (int) Octal.parseOctal(bh, offset, Header.CHKSUMLEN);
		offset += Header.CHKSUMLEN;

		header.linkFlag = bh[offset++];

		header.linkName = Header.parseName(bh, offset, Header.NAMELEN);
		offset += Header.NAMELEN;

		header.magic = Header.parseName(bh, offset, Header.USTAR_MAGICLEN);
		offset += Header.USTAR_MAGICLEN;

		header.userName = Header.parseName(bh, offset, Header.USTAR_USER_NAMELEN);
		offset += Header.USTAR_USER_NAMELEN;

		header.groupName = Header.parseName(bh, offset, Header.USTAR_GROUP_NAMELEN);
		offset += Header.USTAR_GROUP_NAMELEN;

		header.devMajor = (int) Octal.parseOctal(bh, offset, Header.USTAR_DEVLEN);
		offset += Header.USTAR_DEVLEN;

		header.devMinor = (int) Octal.parseOctal(bh, offset, Header.USTAR_DEVLEN);
		offset += Header.USTAR_DEVLEN;

		header.namePrefix = Header.parseName(bh, offset, Header.USTAR_FILENAME_PREFIX);
	}

	public static int permissions(File f) {
		if(f == null) {
			throw new NullPointerException("File is null.");
		}
		if(!f.exists()) {
			throw new IllegalArgumentException("File " + f + " does not exist.");
		}

		return isPosix ? posixPermissions(f) : standardPermissions(f);
	}

	private static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

	private static int posixPermissions(File f) {
		int number = 0;
		try {
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(f.toPath());
			for (Map.Entry<PosixFilePermission, Integer> entry : posixPermissionToInteger.entrySet()) {
				if (permissions.contains(entry.getKey())) {
					number += entry.getValue();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return number;
	}

	private static Integer standardPermissions(File f) {
		int number = 0;
		Set<StandardFilePermission> permissions = readStandardPermissions(f);
		for (StandardFilePermission permission : permissions) {
			number += permission.mode;
		}
		return number;
	}
	private static enum StandardFilePermission {
		EXECUTE(0110), WRITE(0220), READ(0440);

		private int mode;

		private StandardFilePermission(int mode) {
			this.mode = mode;
		}
	}
	private static Set<StandardFilePermission> readStandardPermissions(File f) {
		Set<StandardFilePermission> permissions = new HashSet<>();
		if(f.canExecute()) {
			permissions.add(StandardFilePermission.EXECUTE);
		}
		if(f.canWrite()) {
			permissions.add(StandardFilePermission.WRITE);
		}
		if(f.canRead()) {
			permissions.add(StandardFilePermission.READ);
		}
		return permissions;
	}
}