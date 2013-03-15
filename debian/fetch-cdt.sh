#!/bin/sh

set -e

NAME=eclipse-cdt
VERSION=8.1.2
DEB_VERSION=${VERSION}

CDT_GIT_WEB=http://git.eclipse.org/c/cdt/org.eclipse.cdt.git/

OUT_DIR=${NAME}-${VERSION}

downloadSnapshot() {
	VERSION_UNDERSCORE=$(echo $VERSION | sed "s/\./_/g")
	SRC_DIR=org.eclipse.cdt-CDT_$VERSION_UNDERSCORE
	TARBALL=$SRC_DIR.tar.bz2

	rm -rf $OUT_DIR
	wget "$CDT_GIT_WEB/snapshot/$TARBALL"
	tar xf $TARBALL
	rm -f $TARBALL
	mv $SRC_DIR $OUT_DIR
}

downloadSnapshot

cd $OUT_DIR

find -type f -name .gitignore -delete

# Remove files generated from texi documentation
rm -rf build/org.eclipse.cdt.autotools.ui/macros

# Remove precompiled binaries
rm -rf core/org.eclipse.cdt.core.aix/os \
       core/org.eclipse.cdt.core.aix/cdtaix.jar
rm -rf core/org.eclipse.cdt.core.linux.ia64/os \
       core/org.eclipse.cdt.core.linux.ppc/os \
       core/org.eclipse.cdt.core.linux.ppc64/os \
       core/org.eclipse.cdt.core.linux.x86/os \
       core/org.eclipse.cdt.core.linux.x86_64/os \
       core/org.eclipse.cdt.core.linux/cdt_linux.jar
rm -rf core/org.eclipse.cdt.core.macosx/os \
       core/org.eclipse.cdt.core.macosx/cdt_macosx.jar
rm -rf core/org.eclipse.cdt.core.qnx/os
rm -rf core/org.eclipse.cdt.core.solaris/os \
       core/org.eclipse.cdt.core.solaris/cdt_solaris.jar
rm -rf core/org.eclipse.cdt.core.win32.x86/os \
       core/org.eclipse.cdt.core.win32.x86_64/os \
       core/org.eclipse.cdt.core.win32/cdt_win32.jar

find core/org.eclipse.cdt.core.tests/resources/exe -type f \
     \( -name exe -o -name exe_g -o -name *.o \) -delete
find core/org.eclipse.cdt.core.tests/resources/exebig -type f \
     \( -name exebig_g -o -name *.o \) -delete
find core/org.eclipse.cdt.core.tests/resources/testlib/x86 -type f \
     \( -name *.a -o -name *.o -o -name *.so \) -delete

cd ..

echo "Creating tarball '${NAME}_${DEB_VERSION}.orig.tar.bz2'..."
tar -cjf ../${NAME}_${DEB_VERSION}.orig.tar.bz2 $OUT_DIR

rm -rf $OUT_DIR

