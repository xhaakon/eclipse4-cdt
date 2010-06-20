/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import java.util.Collections;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMNullIndexer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Scanner2Tests ported to use the CPreprocessor
 */
public class InclusionTests extends PreprocessorTestsBase {
    public static TestSuite suite() {
		return suite(InclusionTests.class);
	}

	private ICProject fProject;

	public InclusionTests() {
		super();
	}

	public InclusionTests(String name) {
		super(name);
	}

	protected void tearDown() throws Exception {
		if (fProject != null) {
			CProjectHelper.delete(fProject);
			fProject= null;
		}
		super.tearDown();
	}
	
	public final static int SIZEOF_TRUTHTABLE = 10;

	private IFile importFile(String fileName, String contents ) throws Exception{
    	if (fProject == null) {
    		fProject= CProjectHelper.createCProject(getClass().getName(), null, PDOMNullIndexer.ID);
    	}
    	return TestSourceReader.createFile(fProject.getProject(), fileName, contents);
	}

    private IFolder importFolder(String name) throws Exception{
    	if (fProject == null) {
    		fProject= CProjectHelper.createCProject(getClass().getName(), null, PDOMNullIndexer.ID);
    	}
    	IFolder folder= fProject.getProject().getFolder(name);
    	if (!folder.exists()) {
    		folder.create(true, true, new NullProgressMonitor());
    	}
    	return folder;
	}

    public void testIncludeNext() throws Exception	{    	
    	String baseFile = "int zero; \n#include \"foo.h\""; //$NON-NLS-1$
    	String i1Next = "int one; \n#include_next <bar/foo.h>"; //$NON-NLS-1$
    	String i2Next = "int two; \n#include_next \"bar/foo.h\""; //$NON-NLS-1$
    	String i3Next = "int three; \n"; //$NON-NLS-1$

    	IFolder one = importFolder("one"); //$NON-NLS-1$
    	IFolder oneTwo = importFolder("one/two"); //$NON-NLS-1$
    	IFolder oneTwoBar = importFolder("one/two/bar"); //$NON-NLS-1$
    	IFolder oneThree = importFolder("one/three"); //$NON-NLS-1$
    	IFolder oneThreeBar = importFolder("one/three/bar"); //$NON-NLS-1$
    	IFile base = importFile("base.cpp", baseFile); //$NON-NLS-1$
    	importFile("one/foo.h", i1Next); //$NON-NLS-1$
    	importFile("one/two/bar/foo.h", i2Next); //$NON-NLS-1$
    	importFile("one/three/bar/foo.h", i3Next); //$NON-NLS-1$
    	
    	String[] path = new String[3];
    	path[0] = one.getLocation().toOSString();
    	path[1] = oneTwo.getLocation().toOSString();
    	path[2] = oneThree.getLocation().toOSString();
    	
    	IScannerInfo scannerInfo = new ExtendedScannerInfo(Collections.EMPTY_MAP, path, new String[]{}, null);
    	CodeReader reader= new CodeReader(base.getLocation().toString());
    	initializeScanner(reader, ParserLanguage.C, ParserMode.COMPLETE_PARSE, scannerInfo);

    	validateToken(IToken.t_int);
    	validateIdentifier("zero");
    	validateToken(IToken.tSEMI);

    	validateToken(IToken.t_int);
    	validateIdentifier("one");
    	validateToken(IToken.tSEMI);

    	validateToken(IToken.t_int);
    	validateIdentifier("two");
    	validateToken(IToken.tSEMI);

    	validateToken(IToken.t_int);
    	validateIdentifier("three");
    	validateToken(IToken.tSEMI);
    	
    	validateEOF();
	}

    public void testIncludePathOrdering() throws Exception
	{    	
    	// create directory structure:
    	//  project/base.cpp
    	//  project/foo.h
    	//  project/two/foo.h
    	//  project/three/foo.h
    	
    	// this test sets the include path to be two;three and include foo.h (we should see the contents of two/foo.h
    	// then we change to three;two and we should see the contents of three/foo.h.
    	
    	String baseFile = "#include <foo.h>"; //$NON-NLS-1$
    	String i1Next = "int one;\n"; //$NON-NLS-1$
    	String i2Next = "int two;\n"; //$NON-NLS-1$
    	String i3Next = "int three;\n"; //$NON-NLS-1$   	
    	
    	IFile base = importFile( "base.cpp", baseFile ); //$NON-NLS-1$
    	importFile( "foo.h", i1Next ); //$NON-NLS-1$
    	IFolder twof = importFolder("two"); //$NON-NLS-1$
    	IFolder threef = importFolder("three"); //$NON-NLS-1$
    	importFile( "two/foo.h", i2Next ); //$NON-NLS-1$
    	importFile( "three/foo.h", i3Next ); //$NON-NLS-1$
    	
    	String [] path = new String[2];
    	path[0] = twof.getLocation().toOSString();
       	path[1] = threef.getLocation().toOSString();
   	
    	IScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, path, new String[]{}, null );
    	CodeReader reader= new CodeReader(base.getLocation().toString());
    	initializeScanner(reader, ParserLanguage.C, ParserMode.COMPLETE_PARSE, scannerInfo);

    	validateToken(IToken.t_int);
    	validateIdentifier("two");
    	validateToken(IToken.tSEMI);
    	validateEOF();
    	 	
    	path[0] = threef.getLocation().toOSString();
       	path[1] = twof.getLocation().toOSString();

    	scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, path, new String[]{}, null );
    	initializeScanner(reader, ParserLanguage.C, ParserMode.COMPLETE_PARSE, scannerInfo);

    	validateToken(IToken.t_int);
    	validateIdentifier("three");
    	validateToken(IToken.tSEMI);
    	validateEOF();
	}
    
    public void testBug91086() throws Exception {
        IFile inclusion = importFile( "file.h", "#define FOUND 666\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuffer buffer = new StringBuffer( "#include \"" ); //$NON-NLS-1$
        buffer.append( inclusion.getLocation().toOSString() );
        buffer.append( "\"\n"); //$NON-NLS-1$
        buffer.append( "int var = FOUND;\n"); //$NON-NLS-1$
    	IFile base = importFile( "base.cpp", buffer.toString() ); //$NON-NLS-1$

    	CodeReader reader= new CodeReader(base.getLocation().toString());
    	ParserLanguage lang[]= {ParserLanguage.C, ParserLanguage.CPP};
    	for (int i = 0; i < lang.length; i++) {
    		initializeScanner(reader, lang[i], ParserMode.COMPLETE_PARSE, new ScannerInfo());
    		validateToken(IToken.t_int);
    		validateIdentifier("var");
    		validateToken(IToken.tASSIGN);
    		validateInteger("666");
    		validateToken(IToken.tSEMI);
    		validateEOF();
        }
    }
    
    public void testBug156990() throws Exception {
        IFile inclusion = importFile( "file.h", "ok" ); 
        StringBuffer buffer = new StringBuffer( "#include \"file.h\"" );
    	IFile base = importFile( "base.cpp", buffer.toString() ); //$NON-NLS-1$

    	CodeReader reader= new CodeReader(base.getLocation().toString());
    	initializeScanner(reader, ParserLanguage.CPP, ParserMode.COMPLETE_PARSE, new ScannerInfo());
    	validateIdentifier("ok");
    	validateEOF();
    }
    
}