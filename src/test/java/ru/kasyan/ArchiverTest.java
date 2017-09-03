package ru.kasyan;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static ru.kasyan.utils.Util.*;

/**
 * Created by Kasyanenko Konstantin
 * on 03.09.2017.
 */
public class ArchiverTest {
    private static final Logger LOG = LoggerFactory.getLogger(ArchiverTest.class);

    private File dir;
    private File outDir;
    private File inDir;
    private File inDir1;
    private final int lenghRandomString = 500;
    private Random rnd = new Random();

    @Before
    public void setup() throws IOException {
        dir = Files.createTempDirectory("apnd").toFile();
        dir.mkdirs();
        outDir = new File(dir, "out");
        outDir.mkdirs();
        inDir = new File(dir, "in");
        inDir.mkdirs();
        inDir1 = new File(dir, "in1");
        inDir1.mkdirs();
        LOG.info("Путь до папки с результатом: " + outDir.getCanonicalPath());
    }

    @Test
    public void testListOfFilesAndFolders() {
        File tartest = new File(dir.getAbsolutePath(), "in");
        File file = new File(dir.getAbsolutePath(), "in1");

        try {
            for (int i = 0; i < 10; i++) {
                writeStringToFile(getRandomStr(getRndInt()), new File(tartest, getRandomStr(10)));
            }
            for (int i = 0; i < 5; i++) {
                writeStringToFile(getRandomStr(getRndInt()), new File(file, getRandomStr(10)));
            }
            Archiver archiver = new Archiver();
            List<String> list = getListFiles(inDir.getPath()).stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            list.add(file.getCanonicalPath());
            archiver.addToArchive(list, dir.toString(), "tarName");
            archiver.extractArchive(dir.toString() + "\\tarName.tar", outDir.toString());

            List<File> listExtractFiles = getListFiles(outDir.toString());
            for (int i = 0; i < 10; i++) {
                String fileBefore = readFiles(list.get(i));
                String fileAfter = readFiles(listExtractFiles.get(i).getAbsolutePath());
                LOG.info("Файл до:" + fileBefore);
                LOG.info("Файл после:" + fileAfter);
                Assert.assertEquals("Файлы не совпали: " + list.get(i) +
                        " и " + listExtractFiles.get(i), fileBefore, fileAfter);
            }

            List<File> listFilesInDirBefore = getListFiles(inDir1.getAbsolutePath());
            List<File> listFilesInDirAfter = getListFiles(outDir.toString()+"\\in1");
            for (int i = 0; i < 5; i++) {
                String fileBefore = readFiles(listFilesInDirBefore.get(i).getAbsolutePath());
                String fileAfter = readFiles(listFilesInDirAfter.get(i).getAbsolutePath());
                LOG.info("Файл до:" + fileBefore);
                LOG.info("Файл после:" + fileAfter);
                Assert.assertEquals("Файлы не совпали: " + list.get(i) +
                        " и " + listExtractFiles.get(i), fileBefore, fileAfter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getRndInt() {
        return rnd.nextInt(lenghRandomString);
    }
}
