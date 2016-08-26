/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
package com.mango.mif.core.exec.invoker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.mango.mif.connector.TestsHelper;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.ce.CeInvoker;
import com.mango.mif.core.exec.jsch.JschInvoker;
import com.mango.mif.core.exec.jsch.JschParameters;
import com.mango.mif.utils.TestProperties;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.encrypt.DesEncrypter;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * Tests {@link InvokerHelper}
 */
public class InvokerHelperTest {
    /**
     * Logger
     */
    private final static Logger LOG = Logger.getLogger(InvokerHelperTest.class);
    /**
     * user name that will be used
     */
    private String userName;
    /**
     * encrypted password that will be used
     */
    private String encryptedPassword;
    /**
     * SSH port
     */
    private int port;
    /**
     * JSCH invoker
     */
    private Invoker jschInvoker;
    /**
     * CE invoker
     */
    private Invoker ceInvoker;
    /**
     * instance
     */
    private InvokerHelper invokerHelperForJsch;
    private InvokerHelper invokerHelperForCE;

    @Before
    public void setup() throws Exception {

        TestPropertiesConfigurator.initProperties();
        userName = System.getProperty(TestProperties.MIF_OTHER_CLIENT_USER_NAME);
        encryptedPassword = System.getProperty(TestProperties.MIF_OTHER_CLIENT_USER_PASSWORD);
        String portProp = System.getProperty(TestProperties.MIF_JSCH_PORT);
        Preconditions.checkNotNull(userName, TestProperties.MIF_OTHER_CLIENT_USER_NAME + " property is not set.");
        Preconditions.checkNotNull(encryptedPassword, TestProperties.MIF_OTHER_CLIENT_USER_PASSWORD + " property is not set.");
        Preconditions.checkNotNull(portProp, TestProperties.MIF_JSCH_PORT + " property is not set.");
        port = Integer.parseInt(portProp);

        JschParameters params = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
        params.setPort(port);
        jschInvoker = new JschInvoker(params);

        ceInvoker = new CeInvoker();

        invokerHelperForJsch = new InvokerHelper(jschInvoker);
        invokerHelperForCE = new InvokerHelper(ceInvoker);
    }

    /**
     * Create temporary directory for testing purpose.
     * @return
     * @throws ExecutionException
     */
    private File createTempDirectory() throws ExecutionException{
        UUID uuid = UUID.randomUUID();
        File tempDirectory = TestsHelper.tmpDirectoryPath(uuid.toString());
        if (!invokerHelperForJsch.mkdir(tempDirectory.getAbsolutePath(), 0777)) {
                throw new RuntimeException("Unable to create directory " + tempDirectory.getAbsolutePath() + " as user");
        }
        return tempDirectory;
    }

    @Test
    public void shouldCompleteIfNoOutput() throws ExecutionException {
        InvokerResult results = invokerHelperForJsch.run("[[ -f /etc/passwd ]]");
        assertTrue(results.getStdout().length() == 0);
        assertTrue(results.getStderr().length() == 0);
        assertTrue(results.getExitStatus() == 0);
    }

    @Test
    public void shouldNotHangEvenThoughThereIsNoInput() throws ExecutionException {
        InvokerResult results = invokerHelperForJsch.run("cat > /dev/null");
        assertTrue(results.getStdout().length() == 0);
        assertTrue(results.getStderr().length() == 0);
        assertTrue(results.getExitStatus() == 0);
    }

    @Test
    public void shouldExecuteSimpleLinuxCommand() throws ExecutionException {
        InvokerResult results = invokerHelperForCE.run("/bin/ls -l /etc/passwd /etc/rubbish");
        assertTrue(results.getStdout().length() > 0);
        assertTrue(results.getStderr().length() > 0);
        assertTrue(results.getExitStatus() != 0);

        results = invokerHelperForJsch.run("/bin/ls -l /etc/passwd /etc/rubbish");
        assertTrue(results.getStdout().length() > 0);
        assertTrue(results.getStderr().length() > 0);
        assertTrue(results.getExitStatus() != 0);
    }

    @Test
    public void shouldConfirmEtcDirectoryExistsOnLinux() throws ExecutionException {
        assertTrue(invokerHelperForCE.directoryExists("/etc"));
        assertTrue(invokerHelperForJsch.directoryExists("/etc"));
        assertFalse(invokerHelperForCE.directoryExists("/etc.xx"));
        assertFalse(invokerHelperForJsch.directoryExists("/etc.xx"));
    }

    @Test
    public void shouldConfirmPasswdFileExistsOnLinux() throws ExecutionException {
        assertTrue(invokerHelperForJsch.fileExists("/etc/passwd"));
        assertTrue(invokerHelperForCE.fileExists("/etc/passwd"));
        assertFalse(invokerHelperForJsch.fileExists("/etc/passwd.xx"));
        assertFalse(invokerHelperForCE.fileExists("/etc/passwd.xx"));
    }

    @Test
    public void shouldExpandEtcOnLinux() throws ExecutionException {
        String[] filePaths1 = invokerHelperForJsch.getFilesInDirectory("/etc");
        testit(filePaths1);

        String[] filePaths2 = invokerHelperForCE.getFilesInDirectory("/etc");
        testit(filePaths2);
    }

    private void testit(String[] paths) {
        boolean foundPasswd = false;
        boolean foundShadow = false;
        for (String filePath : paths) {
            if (filePath.equals("/etc/passwd")) {
                foundPasswd = true;
            }
            if (filePath.equals("/etc/shadow")) {
                foundShadow = true;
            }
        }
        assertTrue(foundPasswd);
        assertTrue(foundShadow);
    }

    @Test
    public void shouldGetPasswdConentsOnLinux() throws ExecutionException {
        String contents = invokerHelperForJsch.getFileContents("/etc/passwd");
        assertTrue(contents != null);
        assertTrue(contents.length() > 0);

        contents = invokerHelperForCE.getFileContents("/etc/passwd");
        assertTrue(contents != null);
        assertTrue(contents.length() > 0);
    }

    @Test
    public void shouldCopyFileAcrossOnLinux() throws Exception {

        UUID uuid = UUID.randomUUID();
        File base = TestsHelper.tmpDirectoryPath(uuid.toString());

        String tmpSourceDirectoryPath = base.getAbsoluteFile() + "-SRC";
        File tmpSourceDirectory = new File(tmpSourceDirectoryPath);
        tmpSourceDirectory.mkdirs();

        String tmpTargetDirectoryPath = base.getAbsoluteFile() + "-DEST";
        if (!invokerHelperForCE.mkdir(tmpTargetDirectoryPath, 0777)) {
            throw new Exception("Unable to create directory " + tmpTargetDirectoryPath);
        }
        File tmpTargetDirectory = new File(tmpTargetDirectoryPath);

        LOG.info("SOURCE: " + tmpSourceDirectory.getAbsolutePath());
        LOG.info("DEST:   " + tmpTargetDirectory.getAbsolutePath());

        try {
            File sourceDirA = new File(tmpSourceDirectory, "A");
            File targetDirA = new File(tmpTargetDirectory, "A");
            sourceDirA.mkdir();

            File sourceDirB = new File(tmpSourceDirectory, "B");
            File targetDirB = new File(tmpTargetDirectory, "B");
            sourceDirB.mkdir();

            File sourceDirC = new File(tmpSourceDirectory, "C");
            File targetDirC = new File(tmpTargetDirectory, "C");
            sourceDirC.mkdir();

            File aSourceFile1 = new File(sourceDirA, "a1.txt");
            File aTargetFile1 = new File(targetDirA, "a1.txt");
            String aFile1Content = "This is the content of A/a1.txt.";
            FileUtils.writeStringToFile(aSourceFile1, aFile1Content);

            File aSourceFile2 = new File(sourceDirA, "a2.txt");
            File aTargetFile2 = new File(targetDirA, "a2.txt");
            String aFile2Content = "This is the content of A/a2.txt.";
            FileUtils.writeStringToFile(aSourceFile2, aFile2Content);

            File bSourceFile = new File(sourceDirB, "b.txt");
            String bFileContent = "This is the content of B/b.txt file.";
            FileUtils.writeStringToFile(bSourceFile, bFileContent);

            String[] relativePaths = new String[] { "A/a1.txt", "B/b.txt" };

            invokerHelperForCE.copySpecificFiles(tmpSourceDirectory.getAbsolutePath(), relativePaths, tmpTargetDirectory.getAbsolutePath());

            assertTrue(targetDirA.isDirectory() && targetDirA.exists());
            assertTrue(targetDirB.isDirectory() && targetDirB.exists());
            assertTrue(!targetDirC.exists());

            assertTrue(aTargetFile1.isFile() && aTargetFile1.exists());
            assertTrue(!aTargetFile2.exists());

        } finally {
            try {
                FileUtils.deleteDirectory(tmpSourceDirectory);
            } catch (Exception ignored) {
            }
            try {
                FileUtils.deleteDirectory(tmpTargetDirectory);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    public void shouldCreateAFileWithContentsOnLinuxUsingJschAndCe() throws ExecutionException {

        int jschFileMode = 0612;    // daft mode I know, but just to check things work
        int ceFileMode = 0651;      // as above...
        File tempDirectory = createTempDirectory();
        try {
            File jschTempFile = new File(tempDirectory, "A.txt");
            File ceTempFile = new File(tempDirectory, "B.txt");
            String contents = "Hello world!";
            invokerHelperForJsch.createFileFromContents(contents, jschTempFile.getAbsolutePath(), jschFileMode);
            invokerHelperForCE.createFileFromContents(contents, ceTempFile.getAbsolutePath(), ceFileMode);

            assert (invokerHelperForJsch.fileExists(jschTempFile.getAbsolutePath()));
            assert (invokerHelperForCE.fileExists(ceTempFile.getAbsolutePath()));

            String actualContents = invokerHelperForJsch.getFileContents(jschTempFile.getAbsolutePath());

            assertTrue(actualContents != null);

            LOG.info(this.getClass().getName());
            LOG.info("Expected contents: \"" + contents + "\" (length " + contents.length() + ")");
            LOG.info("Actual JSCH contents: \"" + actualContents + "\" (length " + actualContents.length() + ")");
            assert (actualContents.equals(contents));

            actualContents = invokerHelperForCE.getFileContents(ceTempFile.getAbsolutePath());

            assertTrue(actualContents != null);

            LOG.info("Actual CE contents: \"" + actualContents + "\" (length " + actualContents.length() + ")");
            assert (actualContents.equals(contents));

            InvokerResult results = invokerHelperForJsch.run("stat -c %a \"" + jschTempFile.getAbsolutePath() + "\"");
            LOG.info("Expected mode: " + Integer.toOctalString(jschFileMode));
            LOG.info("Actual mode: " + results.getFirstLineOutputStream());

            assertTrue(Integer.toOctalString(jschFileMode).equals(results.getFirstLineOutputStream()));

            results = invokerHelperForJsch.run("stat -c %a \"" + ceTempFile.getAbsolutePath() + "\"");
            LOG.info("Expected mode: " + Integer.toOctalString(ceFileMode));
            LOG.info("Actual mode: " + results.getFirstLineOutputStream());

            assertTrue(Integer.toOctalString(ceFileMode).equals(results.getFirstLineOutputStream()));

        } finally {
            try {
                if(tempDirectory != null){
                    invokerHelperForJsch.rmdir(tempDirectory.getAbsolutePath());
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    public void shouldBailOnFirstErrorInJsh() throws ExecutionException, IOException {
        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/error-set-e.sh")));
        InvokerResult result = invokerHelperForJsch.run(command);
        LOG.info("Error: " + result.getErrorStream());
        LOG.info("Output: " + result.getOutputStream());
        assertTrue(result.getExitStatus() != 0);
    }

    @Test
    public void shouldBailOnFirstErrorInCE() throws ExecutionException, IOException {
        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/error-set-e.sh")));
        InvokerResult result = invokerHelperForCE.run(command);
        LOG.info("Error: " + result.getErrorStream());
        LOG.info("Output: " + result.getOutputStream());
        assertTrue(result.getExitStatus() != 0);
    }

    @Test
    public void shouldNotBailOnFirstErrorInJsh() throws ExecutionException, IOException {
        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/not-error-set-e.sh")));
        InvokerResult result = invokerHelperForJsch.run(command);
        LOG.info("Error: " + result.getErrorStream());
        LOG.info("Output: " + result.getOutputStream());
        assertTrue(result.getExitStatus() == 0);
    }

    @Test
    public void shouldNotBailOnFirstErrorInCE() throws ExecutionException, IOException {
        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/not-error-set-e.sh")));
        InvokerResult result = invokerHelperForCE.run(command);
        LOG.info("Error: " + result.getErrorStream());
        LOG.info("Output: " + result.getOutputStream());
        assertTrue(result.getExitStatus() == 0);
    }

    @Test
    public void shouldExecuteScriptWithHeaderInJsch() throws ExecutionException, IOException {
        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/with-header.sh")));
        InvokerResult result = invokerHelperForJsch.run(command);
        LOG.info("Error: " + result.getErrorStream());
        LOG.info("Output: " + result.getOutputStream());
        assertTrue(result.getExitStatus() == 0);
    }

    @Test
    public void shouldExecuteStupidlyLongScriptInJsch() throws ExecutionException, IOException {
        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/stupidly-long-script.sh")));
        InvokerResult result = invokerHelperForJsch.run(command);
        assertTrue(result.getStdout().length() == 0);
        assertTrue(result.getStderr().length() == 0);
        assertTrue(result.getExitStatus() == 0);
    }

    @Test
    public void shouldExecuteScriptWithHeaderInCE() throws ExecutionException, IOException {
        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/with-header.sh")));
        InvokerResult result = invokerHelperForCE.run(command);
        LOG.info("Error: " + result.getErrorStream());
        LOG.info("Output: " + result.getOutputStream());
        assertTrue(result.getExitStatus() == 0);
    }

    @Test
    public void shouldRunWithDifferentScriptHome() throws ExecutionException, IOException, EncryptionException {
        JschParameters params = new JschParameters(userName, new DesEncrypter().decrypt(encryptedPassword));
        params.setPort(port);
        params.setScriptDirectory("/tmp");
        JschInvoker jschInvoker = new JschInvoker(params);
        InvokerHelper invokerHelperForJsch = new InvokerHelper(jschInvoker);

        InvokerResult results = invokerHelperForJsch.run("[[ -f /etc/passwd ]]");
        assertTrue(results.getStdout().length() == 0);
        assertTrue(results.getStderr().length() == 0);
        assertTrue(results.getExitStatus() == 0);
    }

    @Test
    public void shouldRetrieveAFileContentAsArrayOfLines() throws ExecutionException, IOException {
        String content = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/file.txt")));

        int jschFileMode = 0612;
        int ceFileMode = 0651;

        File tempDirectory = createTempDirectory();
        try {
            File jschTempFile = new File(tempDirectory, "A.txt");
            File ceTempFile = new File(tempDirectory, "B.txt");
            invokerHelperForJsch.createFileFromContents(content, jschTempFile.getAbsolutePath(), jschFileMode);
            invokerHelperForCE.createFileFromContents(content, ceTempFile.getAbsolutePath(), ceFileMode);
    
            String[] jschResult = invokerHelperForJsch.readLines(jschTempFile);
            String[] ceResult = invokerHelperForCE.readLines(ceTempFile);
            assertEquals(11, jschResult.length);
            assertEquals(11, ceResult.length); 
        } finally {
            invokerHelperForJsch.rmdir(tempDirectory.getAbsolutePath());
        }
        
    }

    @Test
    public void shouldDeleteFileSpecified() throws ExecutionException{

        File tempDirectory = createTempDirectory();
        try {
            File jschTempFile = new File(tempDirectory, "A.txt");
            invokerHelperForJsch.createEmptyFile(jschTempFile.getAbsolutePath());

            assertTrue(invokerHelperForJsch.deleteSpecifiedFile(jschTempFile.getAbsolutePath()));
        } catch(ExecutionException e) {
            LOG.error("Exception while testing deleteSpecifiedFile : "+ e.getMessage());
        } finally {
            invokerHelperForJsch.rmdir(tempDirectory.getAbsolutePath());
        }

    }

    // Ignore this test because it takes a SIGNIFICANT amount of time to run.
    @Ignore
    @Test
    public void shouldReadAllLinesOfOutput() throws ExecutionException, IOException {

        String command = FileUtils.readFileToString(FileUtils.toFile(InvokerHelperTest.class.getResource("/com/mango/mif/core/exec/invoker/jsch-out-err-test.sh")));
        File temp = null;
        try {
            temp = File.createTempFile(Long.toString(System.currentTimeMillis()), ".sh");
            temp.setReadable(true);
            temp.setExecutable(true);
            FileUtils.writeStringToFile(temp, command);


            int repeatLoop = 5000;
            String text = "hello world";

            for (int run = 0; run < 10; run++) {
                System.out.print("run #" + (run + 1) + ": Invoking the command to generate " + repeatLoop + " lines");
                InvokerResult results = invokerHelperForJsch.run("/bin/bash " + temp.getAbsolutePath() + " -s -T " + repeatLoop + " " + text);
                System.out.println(" command exit status was " + results.getExitStatus());

                // Now scan all the lines of output to ensure we did not miss one.
                // Each line is numbered sequentially
                //
                Pattern pattern = Pattern.compile("(\\d+): stdout: " + text);

                boolean allLinesGood = true;
                int lastCount = 0;
                String lastLine = "";

                Scanner scanner = new Scanner(results.getOutputStream()).useDelimiter("\n");
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    Matcher matcher = pattern.matcher(line);

                    if (!matcher.find() || matcher.groupCount() != 1) {
                        System.out.println("run #" + (run + 1) + ": Failed to extract line number from the line: \"" + line + "\"");
                        assertTrue(false);
                    }
                    String counter = matcher.group(1);

                    try {
                        int i = Integer.parseInt(counter);
                        if (i != lastCount + 1) {
                            System.out.println("run #" + (run + 1) + ": missing output for line number " + i);
                            System.out.println("The last line I saw was line number " + lastCount);
                            System.out.println("Last line: " + lastLine);
                            System.out.println("This line: " + line);
                            allLinesGood = false;
                        }
                        lastCount = i;
                        lastLine = line;

                    } catch (NumberFormatException nfe) {
                        // This should not happen.
                        System.out.println("Caught number format exception when processing line \"" + line + "\"");
                        assertTrue(false);
                        allLinesGood = false;
                    }
                }

                if (!allLinesGood || lastCount != repeatLoop) {
                    System.out.println("run #" + (run + 1) + ": One or more lines missing from command's output (lastCount " + lastCount + ")");
                    assertTrue(false);
                } else {
                    System.out.println("run #" + (run + 1) + ": Checked through " + lastCount + " lines of output, all present and correct");
                }
            }
            assertTrue(true);

        } catch (IOException ioe) {
            System.out.println("CONFIGURATION ERROR: You do not have permission to write to the temporary directory " + System.getProperty("java.io.tmpdir"));
            System.out.println("The actual exception is: ");
            System.out.println(ioe);
            throw ioe;
        } finally {
            // We simply MUST ensure this file is deleted after we've finished with it, otherwise
            // we'll get thousands of the little buggers cluttering the file system.
            if (temp != null) {
                FileUtils.deleteQuietly(temp);
            }
        }

    }
}
