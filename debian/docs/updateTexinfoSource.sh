#!/bin/sh

mkdir -p tmp
cd tmp

echo "Downloading source packages..."

apt-get source autoconf automake > /dev/null

# Pick autoconf documentation sources

AC_SRCDIR=$(find -maxdepth 1 -type d -name 'autoconf-*')
AC_VERSION=$(dpkg-parsechangelog -l$AC_SRCDIR/debian/changelog | sed -ne 's/^Version: \(.*\)-.*/\1/p')
AC_DESTDIR=../src/autoconf/$AC_VERSION

rm -rf $AC_DESTDIR
mkdir -p $AC_DESTDIR

mv $AC_SRCDIR/doc/*.texi $AC_DESTDIR

echo Downloaded Autoconf doc source into src/autoconf/$AC_VERSION

# Pick automake documentation sources

AM_SRCDIR=$(find -maxdepth 1 -type d -name 'automake*')
AM_VERSION=$(dpkg-parsechangelog -l$AM_SRCDIR/debian/changelog | sed -ne 's/^Version: \([0-9]\+\:\)\?\(.*\)-.*/\2/p')
AM_DESTDIR=../src/automake/$AM_VERSION

rm -rf $AM_DESTDIR
mkdir -p $AM_DESTDIR

mv $AM_SRCDIR/doc/*.texi $AM_DESTDIR

echo Downloaded Automake doc source into src/automake/$AM_VERSION

echo "Don't forget to update autotools-add-distro-version-to-list.patch if Autoconf or Automake version changed."

cd ..
rm -rf tmp
