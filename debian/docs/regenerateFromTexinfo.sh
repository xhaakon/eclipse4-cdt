#!/bin/sh
# Make sure d/patches are applied to upstream source before running this script.

mkdir -p tmp
cd tmp

CLASSPATH=$PWD/parsers

ant -f ../src/parsers/build.xml -Dbuild=$CLASSPATH

# Generate acmacros xml

for AC_SRCDIR in ../src/autoconf/*; do
	AC_VERSION=$(basename $AC_SRCDIR)
	AC_MACROS=acmacros-$AC_VERSION.xml

	java -cp $CLASSPATH \
		org.eclipse.linuxtools.cdt.libhover.texinfoparsers.ParseAutoconfTexinfo \
		$AC_SRCDIR ../$AC_MACROS

	echo Generated $AC_MACROS
done

# generate ammacros xml

for AM_SRCDIR in ../src/automake/*; do
	AM_VERSION=$(basename $AM_SRCDIR)
	AM_MACROS=ammacros-$AM_VERSION.xml

	java -cp $CLASSPATH \
		org.eclipse.linuxtools.cdt.libhover.texinfoparsers.ParseAutomakeTexinfo \
		$AM_SRCDIR ../$AM_MACROS

	echo Generated $AM_MACROS
done

cd ..
rm -rf tmp
