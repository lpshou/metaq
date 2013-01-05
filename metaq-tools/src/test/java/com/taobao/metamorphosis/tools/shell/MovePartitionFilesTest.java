package com.taobao.metamorphosis.tools.shell;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author �޻�
 * @since 2011-8-25 ����2:20:38
 */

public class MovePartitionFilesTest {
    MovePartitionFiles movePartitionFiles;
    File testMetaDataDir;


    @Before
    public void setUp() throws IOException {
        this.movePartitionFiles = new MovePartitionFiles(System.out);
        this.testMetaDataDir = new File("testMetaDataDir");
        FileUtils.forceMkdir(this.testMetaDataDir);
        System.out.println(this.testMetaDataDir.getAbsolutePath() + " created");
    }


    @Test
    public void testMove_normal_forward() throws Exception {
        // [4,5,6]-->[0,1,2]
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition5Dir = new File(this.testMetaDataDir + File.separator + "topicxx-5");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition5Dir);
        FileUtils.forceMkdir(partition6Dir);

        File partition4DataFile = new File(partition4Dir, "0000.meta");
        partition4DataFile.createNewFile();

        this.movePartitionFiles
            .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic topicxx -start 4 -end 6 -offset -4")
                .split(" "));

        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-0").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-1").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-2").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-0" + File.separator + "0000.meta")
            .exists());

        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-4").exists());
        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-5").exists());
        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-6").exists());

    }


    @Test
    public void testMove_normal_backward() throws Exception {
        // [0,1,2]-->[1,2,3]
        this.testMove_normal_forward();
        this.movePartitionFiles
            .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic topicxx -start 0 -end 2 -offset 1")
                .split(" "));

        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-1").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-2").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-3").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-1" + File.separator + "0000.meta")
            .exists());

        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-0").exists());

    }


    @Test
    public void testMove_newFileExists() throws Exception {
        // [4,5,6]-->[0,1,2]. 1�Ѿ�����
        FileUtils.forceMkdir(new File(this.testMetaDataDir + File.separator + "topicxx-1"));
        try {
            this.testMove_normal_forward();
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // û�仯
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-1").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-4").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-5").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-6").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-4" + File.separator + "0000.meta")
            .exists());

        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-0").exists());
        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-2").exists());

    }


    @Test
    public void testMove_oldFileNotExists() throws Exception {
        // [4,5,6]-->[0,1,2] . 5������
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition6Dir);

        File partition4DataFile = new File(partition4Dir, "0000.meta");
        partition4DataFile.createNewFile();
        try {
            this.movePartitionFiles
                .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic topicxx -start 4 -end 6 -offset -4")
                    .split(" "));
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // û�仯
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-4").exists());
        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-5").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-6").exists());
        assertTrue(new File(this.testMetaDataDir + File.separator + "topicxx-4" + File.separator + "0000.meta")
            .exists());

        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-0").exists());
        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-1").exists());
        assertFalse(new File(this.testMetaDataDir + File.separator + "topicxx-2").exists());

    }


    @Test
    public void testMove_illOffset() throws Exception {
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition5Dir = new File(this.testMetaDataDir + File.separator + "topicxx-5");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition5Dir);
        FileUtils.forceMkdir(partition6Dir);
        try {
            this.movePartitionFiles
                .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic topicxx -start 4 -end 6 -offset -5")
                    .split(" "));
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(partition4Dir.exists());
        assertTrue(partition5Dir.exists());
        assertTrue(partition6Dir.exists());
    }


    @Test
    public void testMove_blankTopic() throws Exception {
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition5Dir = new File(this.testMetaDataDir + File.separator + "topicxx-5");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition5Dir);
        FileUtils.forceMkdir(partition6Dir);
        try {
            this.movePartitionFiles
                .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic  -start 4 -end 6 -offset -4")
                    .split(" "));
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(partition4Dir.exists());
        assertTrue(partition5Dir.exists());
        assertTrue(partition6Dir.exists());
    }


    @Test
    public void testMove_startLessThenEnd() throws Exception {
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition5Dir = new File(this.testMetaDataDir + File.separator + "topicxx-5");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition5Dir);
        FileUtils.forceMkdir(partition6Dir);
        try {
            this.movePartitionFiles
                .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic topicxx -start 4 -end 3 -offset -5")
                    .split(" "));
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(partition4Dir.exists());
        assertTrue(partition5Dir.exists());
        assertTrue(partition6Dir.exists());
    }


    @Test
    public void testMove_0Offset() throws Exception {
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition5Dir = new File(this.testMetaDataDir + File.separator + "topicxx-5");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition5Dir);
        FileUtils.forceMkdir(partition6Dir);
        try {
            this.movePartitionFiles
                .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic topicxx -start 4 -end 5 -offset 0")
                    .split(" "));
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(partition4Dir.exists());
        assertTrue(partition5Dir.exists());
        assertTrue(partition6Dir.exists());
    }


    @Test
    public void testMove_startLessThen0() throws Exception {
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition5Dir = new File(this.testMetaDataDir + File.separator + "topicxx-5");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition5Dir);
        FileUtils.forceMkdir(partition6Dir);
        try {
            this.movePartitionFiles
                .doMain(("-dataDir " + this.testMetaDataDir.getAbsolutePath() + " -topic topicxx -start -1 -end 5 -offset 2")
                    .split(" "));
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(partition4Dir.exists());
        assertTrue(partition5Dir.exists());
        assertTrue(partition6Dir.exists());
    }


    @Test
    public void testDelete_dataDirBlank() throws Exception {
        File partition4Dir = new File(this.testMetaDataDir + File.separator + "topicxx-4");
        File partition5Dir = new File(this.testMetaDataDir + File.separator + "topicxx-5");
        File partition6Dir = new File(this.testMetaDataDir + File.separator + "topicxx-6");

        FileUtils.forceMkdir(partition4Dir);
        FileUtils.forceMkdir(partition5Dir);
        FileUtils.forceMkdir(partition6Dir);
        try {
            this.movePartitionFiles.doMain(("-dataDir  -topic topicxx -start -1 -end 5 -offset 2").split(" "));
            fail();
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        assertTrue(partition4Dir.exists());
        assertTrue(partition5Dir.exists());
        assertTrue(partition6Dir.exists());
    }


    @After
    public void tearDown() throws IOException {
        if (this.testMetaDataDir != null && this.testMetaDataDir.exists()) {
            FileUtils.deleteDirectory(this.testMetaDataDir);
            System.out.println(this.testMetaDataDir.getAbsolutePath() + " deleted");
        }
    }

}