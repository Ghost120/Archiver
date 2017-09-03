package ru.kasyan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created by Kasyanenko Konstantin
 * on 03.09.2017.
 */
public class Archiver {
    private static final int BUFFER = 2048;
    private static final Logger LOG = LoggerFactory.getLogger(Archiver.class);

    public void addToArchive(List<String> fileList, String pathToArchive, String tarName) throws IOException {
        FileOutputStream dest = new FileOutputStream(new File(pathToArchive, tarName + ".tar"));
        TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));
        for (String file : fileList) {
            tarFolder(null, file, out);
        }
        out.close();
    }

    public void extractArchive(String pathToArchive, String destFolder) throws IOException {
        TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(pathToArchive)));
        File folderTo = new File(destFolder);
        extract(tis, folderTo.getAbsolutePath());
    }

    private void tarFolder(String parent, String path, TarOutputStream out) throws IOException {
        BufferedInputStream origin = null;
        File f = new File(path);
        String files[] = f.list();

        if (files == null) {
            files = new String[1];
            files[0] = f.getName();
        }

        parent = ((parent == null) ? (f.isFile()) ? "" : f.getName() + "/" : parent + f.getName() + "/");

        for (int i = 0; i < files.length; i++) {
            LOG.info("Adding: " + files[i]);
            File fileToEntry = f;
            byte data[] = new byte[BUFFER];

            if (f.isDirectory()) {
                fileToEntry = new File(f, files[i]);
            }

            if (fileToEntry.isDirectory()) {
                String[] fl = fileToEntry.list();
                if (fl != null && fl.length != 0) {
                    tarFolder(parent, fileToEntry.getPath(), out);
                } else {
                    Entry entry = new Entry(fileToEntry, parent + files[i] + "/");
                    out.putNextEntry(entry);
                }
                continue;
            }

            FileInputStream fi = new FileInputStream(fileToEntry);
            origin = new BufferedInputStream(fi);
            Entry entry = new Entry(fileToEntry, parent + files[i]);
            out.putNextEntry(entry);

            int count;

            while ((count = origin.read(data)) != -1) {
                out.write(data, 0, count);
            }

            out.flush();

            origin.close();
        }
    }

    private void extract(TarInputStream tis, String destFolder) throws IOException {
        BufferedOutputStream dest = null;

        Entry entry;
        while ((entry = tis.getNextEntry()) != null) {
            LOG.info("Extracting: " + entry.getName());
            int count;
            byte data[] = new byte[BUFFER];

            if (entry.isDirectory()) {
                new File(destFolder + "/" + entry.getName()).mkdirs();
                continue;
            } else {
                int di = entry.getName().lastIndexOf('/');
                if (di != -1) {
                    new File(destFolder + "/" + entry.getName().substring(0, di)).mkdirs();
                }
            }

            FileOutputStream fos = new FileOutputStream(destFolder + "/" + entry.getName());
            dest = new BufferedOutputStream(fos);

            while ((count = tis.read(data)) != -1) {
                dest.write(data, 0, count);
            }

            dest.flush();
            dest.close();
        }
    }

}
