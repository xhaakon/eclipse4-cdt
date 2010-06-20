/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.errorparsers.AbstractErrorParser;
import org.eclipse.cdt.core.errorparsers.ErrorPattern;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * The test case includes a few tests checking that {@link AbstractErrorParser}/{@link ErrorPattern}
 * properly locate and resolve filenames found in build output in case of EFS files/folders.
 */
public class ErrorParserEfsFileMatchingTest extends TestCase {
	private static final String MAKE_ERRORPARSER_ID = "org.eclipse.cdt.core.MakeErrorParser";
	private String mockErrorParserId = null;

	private final static String testName = "FindMatchingFilesEfsTest";

	// Default project gets created once then used by all test cases.
	private IProject fProject = null;
	private ArrayList<ProblemMarkerInfo> errorList;

	private final IMarkerGenerator markerGenerator = new IMarkerGenerator() {
		// deprecated
		public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {}

		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
			errorList.add(problemMarkerInfo);
		}
	};

	/**
	 * Simple error parser parsing line like "file:line:description"
	 */
	public static class MockErrorParser extends AbstractErrorParser {
		/**
		 * Constructor to set the error pattern.
		 */
		public MockErrorParser() {
			super(new ErrorPattern[] {
				new ErrorPattern("(.*):(.*):(.*)", 1, 2, 3, 0, IMarkerGenerator.SEVERITY_ERROR_RESOURCE)
			});
		}
	}

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public ErrorParserEfsFileMatchingTest(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
		if (fProject==null) {
			fProject = ResourceHelper.createCDTProject(testName);
			Assert.assertNotNull(fProject);
			mockErrorParserId = addErrorParserExtension("MockErrorParser", MockErrorParser.class);
		}
		errorList = new ArrayList<ProblemMarkerInfo>();
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
		fProject = null;
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(ErrorParserEfsFileMatchingTest.class);
	}

	/**
	 * main function of the class.
	 *
	 * @param args - arguments
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Adds Error Parser extension to the global repository.
	 * Note that this function will "pollute" the working environment and
	 * the error parser will be seen by other test cases as well.
	 *
	 * @param shortId - last portion of ID with which error parser will be added.
	 * @param cl - Error Parser class
	 * @return - full ID of the error parser (generated by the method).
	 */
	private static String addErrorParserExtension(String shortId, Class cl) {
		String ext = "<plugin><extension id=\"" + shortId + "\" name=\"" + shortId
				+ "\" point=\"org.eclipse.cdt.core.ErrorParser\">" + "<errorparser class=\"" + cl.getName() + "\"/>"
				+ "</extension></plugin>";
		IContributor contributor = ContributorFactoryOSGi.createContributor(CTestPlugin.getDefault().getBundle());
		boolean added = Platform.getExtensionRegistry().addContribution(new ByteArrayInputStream(ext.getBytes()),
				contributor, false, shortId, null,
				((ExtensionRegistry) Platform.getExtensionRegistry()).getTemporaryUserToken());
		assertTrue("failed to add extension", added);
		String fullId = "org.eclipse.cdt.core.tests." + shortId;
		IErrorParser[] errorParser = CCorePlugin.getDefault().getErrorParser(fullId);
		assertTrue(errorParser.length > 0);
		return fullId;
	}

	/**
	 * Convenience method to let {@link ErrorParserManager} parse one line of output.
	 * This method goes through the whole working cycle every time creating
	 * new {@link ErrorParserManager}.
	 *
	 * @param project - for which project to parse output.
	 * @param buildDir - location of build for {@link ErrorParserManager}.
	 * @param errorParsers - error parsers used.
	 * @param line - one line of output.
	 * @throws Exception
	 */
	private void parseOutput(IProject project, URI buildDirURI, String[] errorParsers, String line) throws Exception {
		ErrorParserManager epManager = new ErrorParserManager(project, buildDirURI, markerGenerator, errorParsers);
		line = line + '\n';
		epManager.write(line.getBytes(), 0, line.length());
		epManager.close();
		epManager.reportProblems();
	}

	/**
	 * Convenience method to let {@link ErrorParserManager} parse one line of output.
	 * This method goes through the whole working cycle every time creating
	 * new {@link ErrorParserManager}.
	 *
	 * @param project - for which project to parse output.
	 * @param buildDir - location of build for {@link ErrorParserManager}.
	 * @param errorParsers - error parsers used.
	 * @param line - one line of output.
	 * @throws Exception
	 */
	private void parseOutput(IProject project, IPath buildDir, String[] errorParsers, String line) throws Exception {
		ErrorParserManager epManager = new ErrorParserManager(project, buildDir, markerGenerator, errorParsers);
		line = line + '\n';
		epManager.write(line.getBytes(), 0, line.length());
		epManager.close();
		epManager.reportProblems();
	}

	/**
	 * Convenience method to parse one line of output.
	 */
	private void parseOutput(IProject project, String buildDir, String line) throws Exception {
		parseOutput(project, new Path(buildDir), new String[] {mockErrorParserId}, line);
	}

	/**
	 * Convenience method to parse one line of output.
	 *  Search is done in project location.
	 */
	private void parseOutput(IProject project, String line) throws Exception {
		parseOutput(project, project.getLocation(), new String[] {mockErrorParserId}, line);
	}

	/**
	 * Convenience method to parse one line of output.
	 * Search is done for current project in default location.
	 */
	private void parseOutput(String line) throws Exception {
		parseOutput(fProject, fProject.getLocation(), new String[] {mockErrorParserId}, line);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testSingle() throws Exception {
		ResourceHelper.createEfsFile(fProject, "testSingle.c", "null:/efsTestSingle.c");

		parseOutput("efsTestSingle.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/testSingle.c",problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testEfsVsRegular() throws Exception {
		ResourceHelper.createFile(fProject, "testEfsVsRegular.c");
		ResourceHelper.createEfsFile(fProject, "efsTestEfsVsRegular.c", "null:/testEfsVsRegular.c");

		parseOutput("testEfsVsRegular.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/testEfsVsRegular.c",problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testFullPath() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Folder", "null:/Folder");
		ResourceHelper.createEfsFile(fProject, "Folder/testFullPath.c",
				"null:/EfsFolder/efsTestFullPath.c");

		parseOutput("EfsFolder/efsTestFullPath.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/Folder/testFullPath.c",problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);

	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testInNonEfsFolder() throws Exception {
		ResourceHelper.createFolder(fProject, "NonEfsFolder");
		ResourceHelper.createEfsFile(fProject, "NonEfsFolder/testInNonEfsFolder.c",
				"null:/EfsFolder/efsTestInNonEfsFolder.c");

		parseOutput("efsTestInNonEfsFolder.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/NonEfsFolder/testInNonEfsFolder.c",problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);

	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testInFolder() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Folder", "null:/Folder");
		ResourceHelper.createEfsFile(fProject, "Folder/testInFolder.c",
				"null:/EfsFolder/efsTestInFolder.c");

		parseOutput("efsTestInFolder.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/Folder/testInFolder.c",problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);

	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testDuplicateInRoot() throws Exception {
		ResourceHelper.createEfsFile(fProject, "testDuplicateInRoot.c", "null:/testDuplicateInRoot.c");

		ResourceHelper.createEfsFolder(fProject, "Folder", "null:/Folder");
		ResourceHelper.createEfsFile(fProject, "Folder/testDuplicateInRoot.c",
				"null:/Folder/testDuplicateInRoot.c");

		// Resolved to the file in root folder
		parseOutput("testDuplicateInRoot.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("L/FindMatchingFilesEfsTest/testDuplicateInRoot.c",problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testRelativePathFromProjectRoot() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Folder", "null:/Folder");
		ResourceHelper.createEfsFile(fProject, "Folder/testRelativePathFromProjectRoot.c",
				"null:/EfsFolder/testRelativePathFromProjectRoot.c");

		parseOutput("EfsFolder/testRelativePathFromProjectRoot.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/Folder/testRelativePathFromProjectRoot.c",problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testRelativePathFromSubfolder() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Subfolder", "null:/Subfolder");
		ResourceHelper.createEfsFolder(fProject, "Subfolder/Folder", "null:/Subfolder/Folder");
		ResourceHelper.createEfsFile(fProject, "Subfolder/Folder/testRelativePathFromSubfolder.c",
				"null:/Subfolder/Folder/testRelativePathFromSubfolder.c");

		parseOutput("Folder/testRelativePathFromSubfolder.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/Subfolder/Folder/testRelativePathFromSubfolder.c",problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testRelativePathNotMatchingFolder() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Folder", "null:/Folder");
		ResourceHelper.createEfsFile(fProject, "Folder/testRelativePathNotMatchingFolder.c",
				"null:/Folder/testRelativePathNotMatchingFolder.c");

		parseOutput("NotMatchingFolder/testRelativePathNotMatchingFolder.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		// No match
		assertEquals("P/FindMatchingFilesEfsTest",problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
		assertEquals(new Path("NotMatchingFolder/testRelativePathNotMatchingFolder.c"),problemMarkerInfo.externalPath);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testRelativePathDuplicate() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "SubfolderA", "null:/SubfolderA");
		ResourceHelper.createEfsFolder(fProject, "SubfolderA/Folder", "null:/SubfolderA/Folder");
		ResourceHelper.createEfsFile(fProject, "SubfolderA/Folder/testRelativePathDuplicate.c",
				"null:/SubfolderA/Folder/testRelativePathDuplicate.c");
		ResourceHelper.createEfsFolder(fProject, "SubfolderB", "null:/SubfolderB");
		ResourceHelper.createEfsFolder(fProject, "SubfolderB/Folder", "null:/SubfolderB/Folder");
		ResourceHelper.createEfsFile(fProject, "SubfolderB/Folder/testRelativePathDuplicate.c",
				"null:/SubfolderBS/Folder/testRelativePathDuplicate.c");

		parseOutput("Folder/testRelativePathDuplicate.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		// No match found
		assertEquals("P/FindMatchingFilesEfsTest",problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
		assertEquals(new Path("Folder/testRelativePathDuplicate.c"),problemMarkerInfo.externalPath);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testRelativePathUpSubfolder() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Folder", "null:/Folder");
		ResourceHelper.createEfsFile(fProject, "Folder/testRelativePathUpSubfolder.c",
				"null:/Folder/testRelativePathUpSubfolder.c");

		parseOutput("../Folder/testRelativePathUpSubfolder.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/Folder/testRelativePathUpSubfolder.c",problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testRelativePathDotFromSubfolder() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Subfolder", "null:/Subfolder");
		ResourceHelper.createEfsFolder(fProject, "Subfolder/Folder", "null:/Subfolder/Folder");
		ResourceHelper.createEfsFile(fProject, "Subfolder/Folder/testRelativePathDotFromSubfolder.c",
				"null:/Subfolder/Folder/testRelativePathDotFromSubfolder.c");

		parseOutput("./Folder/testRelativePathDotFromSubfolder.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/Subfolder/Folder/testRelativePathDotFromSubfolder.c",problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testBuildDir() throws Exception {
		ResourceHelper.createEfsFolder(fProject, "Folder", "null:/Folder");
		ResourceHelper.createEfsFile(fProject, "Folder/testBuildDir.c", "null:/Folder/testBuildDir.c");
		ResourceHelper.createEfsFolder(fProject, "BuildDir", "null:/BuildDir");
		ResourceHelper.createEfsFile(fProject, "BuildDir/testBuildDir.c", "null:/BuildDir/testBuildDir.c");

		String buildDir = fProject.getLocation().append("BuildDir").toOSString();
		parseOutput(fProject, buildDir, "testBuildDir.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/FindMatchingFilesEfsTest/BuildDir/testBuildDir.c",problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testEfsProject() throws Exception {
		IFile efsSmokeTest = ResourceHelper.createEfsFile(fProject, "efsSmokeTest.c", "mem:/efsSmokeTest.c");
		Assert.assertTrue(efsSmokeTest.exists());

		IProject efsProject = ResourceHelper.createCDTProject("EfsProject", new URI("mem:/EfsProject"));
		ResourceHelper.createFolder(efsProject, "Folder");
		ResourceHelper.createFile(efsProject, "Folder/testEfsProject.c");

		parseOutput(efsProject, "testEfsProject.c:1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/EfsProject/Folder/testEfsProject.c",problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 * @throws Exception...
	 */
	public void testEfsProjectBuildDirURI() throws Exception {
		String fileName = "testEfsProjectBuildDirURI.c";

		IProject efsProject = ResourceHelper.createCDTProject("EfsProject", new URI("mem:/EfsProject"));
		ResourceHelper.createFolder(efsProject, "Folder");
		ResourceHelper.createFile(efsProject, "Folder/" + fileName);
		ResourceHelper.createFolder(efsProject, "BuildDir");
		ResourceHelper.createFile(efsProject, "BuildDir/" + fileName);

		URI buildDirURI = new URI("mem:/EfsProject/BuildDir/");
		parseOutput(efsProject, buildDirURI, new String[] {mockErrorParserId}, fileName+":1:error");
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/EfsProject/BuildDir/"+fileName,problemMarkerInfo.file.toString());
		assertEquals("error",problemMarkerInfo.description);
	}

	/**
	 * Checks if a file from error output can be found.
	 *
	 * @throws Exception...
	 */
	public void testEfsProjectPushPopDirectory() throws Exception {
		String fileName = "testEfsProjectPushPopDirectory.c";

		IProject efsProject = ResourceHelper.createCDTProject("EfsProject", new URI("mem:/ProjectPushPopDirectory"));
		ResourceHelper.createFolder(efsProject, "Folder");
		ResourceHelper.createFolder(efsProject, "Folder/SubFolder");
		ResourceHelper.createFile(efsProject, fileName);
		ResourceHelper.createFile(efsProject, "Folder/"+fileName);
		ResourceHelper.createFile(efsProject, "Folder/SubFolder/"+fileName);

		String lines = "make[1]: Entering directory `Folder'\n"
			+ "make[2]: Entering directory `SubFolder'\n"
			+ "make[2]: Leaving directory `SubFolder'\n"
			+ fileName+":1:error\n";

		String[] errorParsers = {MAKE_ERRORPARSER_ID, mockErrorParserId };
		parseOutput(efsProject, efsProject.getLocation(), errorParsers, lines);
		assertEquals(1, errorList.size());

		ProblemMarkerInfo problemMarkerInfo = errorList.get(0);
		assertEquals("L/EfsProject/Folder/"+fileName,problemMarkerInfo.file.toString());
		assertEquals(1,problemMarkerInfo.lineNumber);
		assertEquals("error",problemMarkerInfo.description);
	}
}
