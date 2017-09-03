package ru.kasyan;

import java.io.*;

/**
 * Created by Kasyanenko Konstantin
 * on 03.09.2017.
 */
public class TarOutputStream extends OutputStream {
    private static final int EOF_BLOCK = 1024;
    private final OutputStream out;
    private long bytesWritten;
    private long currentFileSize;
    private Entry currentEntry;

    TarOutputStream(OutputStream out) {
        this.out = out;
        bytesWritten = 0;
        currentFileSize = 0;
    }

	public TarOutputStream(final File fout) throws FileNotFoundException {
		this.out = new BufferedOutputStream(new FileOutputStream(fout));
		bytesWritten = 0;
		currentFileSize = 0;
	}

	/**
	 * Opens a file for writing. 
	 */
	public TarOutputStream(final File fout, final boolean append) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(fout, "rw");
		final long fileSize = fout.length();
		if (append && fileSize > EOF_BLOCK) {
			raf.seek(fileSize - EOF_BLOCK);
		}
		out = new BufferedOutputStream(new FileOutputStream(raf.getFD()));
	}

    /**
     * Appends the EOF record and closes the stream
     * 
     * @see java.io.FilterOutputStream#close()
     */
    @Override
    public void close() throws IOException {
        closeCurrentEntry();
        write( new byte[EOF_BLOCK] );
        out.close();
    }
    /**
     * Writes a byte to the stream and updates byte counters
     * 
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        out.write( b );
        bytesWritten++;

        if (currentEntry != null) {
            currentFileSize++;
        }
    }

    /**
     * Checks if the bytes being written exceed the current entry size.
     * 
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (currentEntry != null && !currentEntry.isDirectory()) {
            if (currentEntry.getSize() < currentFileSize + len) {
                throw new IOException( "The current entry[" + currentEntry.getName() + "] size["
                        + currentEntry.getSize() + "] is smaller than the bytes[" + ( currentFileSize + len )
                        + "] being written." );
            }
        }

        out.write( b, off, len );
        
        bytesWritten += len;

        if (currentEntry != null) {
            currentFileSize += len;
        }        
    }

    /**
     * Writes the next tar entry header on the stream
     *
     * @throws IOException
     */
    void putNextEntry(Entry entry) throws IOException {
        closeCurrentEntry();

        byte[] header = new byte[512];
        entry.writeEntryHeader( header );

        write( header );

        currentEntry = entry;
    }

    /**
     * Closes the current tar entry
     * 
     * @throws IOException
     */
    protected void closeCurrentEntry() throws IOException {
        if (currentEntry != null) {
            if (currentEntry.getSize() > currentFileSize) {
                throw new IOException( "The current entry[" + currentEntry.getName() + "] of size["
                        + currentEntry.getSize() + "] has not been fully written." );
            }

            currentEntry = null;
            currentFileSize = 0;

            pad();
        }
    }

    /**
     * Pads the last content block
     * 
     * @throws IOException
     */
    private void pad() throws IOException {
        if (bytesWritten > 0) {
            int extra = (int) ( bytesWritten % 512 );

            if (extra > 0) {
                write( new byte[512 - extra] );
            }
        }
    }
}
