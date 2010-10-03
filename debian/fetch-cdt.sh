#!/bin/sh
# This file has been obtained from:
#   http://cvs.fedoraproject.org/viewvc/rpms/eclipse-cdt/devel/
#
# The author list below is not from the original file, but has been
# written based on the CVS commit log (in case the CVS should some day
# be unavailable).
#
# Written by: 2008, Andrew Overholt
# Modified by: 2008-2010, Jeff Johnston 

CDTTAG=v201002161416
UPSTREAM_VERSION=6.0.2
ECLIPSEBASE=/usr/lib/eclipse

mkdir -p temp && cd temp
mkdir -p home
mkdir -p ws
rm -rf org.eclipse.cdt-releng
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/tools export -r $CDTTAG org.eclipse.cdt-releng/org.eclipse.cdt.releng
cd org.eclipse.cdt-releng/org.eclipse.cdt.releng/

# The build.xml doesn't fetch master or testing features so we must add this ourselves.
sed --in-place -e'91,91i\\t\t<ant antfile="build.xml" dir="${pde.build.scripts}" target="fetch">\n\t\t\t<property name="builder" value="${basedir}/master"/>\n\t\t</ant>' build.xml
sed --in-place -e'91,91i\\t\t<ant antfile="build.xml" dir="${pde.build.scripts}" target="fetch">\n\t\t\t<property name="builder" value="${basedir}/testing"/>\n\t\t</ant>' build.xml
sed --in-place -e'71,71i\\t\t<ant antfile="build.xml" dir="${pde.build.scripts}" target="preBuild">\n\t\t\t<property name="builder" value="${basedir}/master"/>\n\t\t</ant>' build.xml
sed --in-place -e'71,71i\\t\t<ant antfile="build.xml" dir="${pde.build.scripts}" target="preBuild">\n\t\t\t<property name="builder" value="${basedir}/testing"/>\n\t\t</ant>' build.xml

# Remove copying of binary jar in build.xml.  We remove this jar so this operation will fail.
sed --in-place -e "/copy file=\"\${buildDirectory}.*net\.sourceforge\.lpg/,/\/>/"d build.xml 

sed --in-place -e "s,eclipse.cdt/mylyn,eclipse.cdt-old/mylyn,g" maps/cdt.map


PDEBUILDVERSION=$(ls $ECLIPSEBASE/plugins | grep pde.build_ | sed 's/org.eclipse.pde.build_//')
java -cp /usr/lib/eclipse/startup.jar \
     -Duser.home=../../home \
-XX:CompileCommand="exclude,org/eclipse/core/internal/dtree/DataTreeNode,forwardDeltaWith" \
-XX:CompileCommand="exclude,org/eclipse/jdt/internal/compiler/lookup/ParameterizedMethodBinding,<init>" \
-XX:CompileCommand="exclude,org/eclipse/cdt/internal/core/dom/parser/cpp/semantics/CPPTemplates,instantiateTemplate" \
-XX:CompileCommand="exclude,org/eclipse/cdt/internal/core/pdom/dom/cpp/PDOMCPPLinkage,addBinding" \
     org.eclipse.core.launcher.Main             \
  -Dpde.build.scripts=$ECLIPSEBASE/plugins/org.eclipse.pde.build_$PDEBUILDVERSION/scripts \
  -application org.eclipse.ant.core.antRunner \
  -buildfile build.xml -DbaseLocation=$ECLIPSEBASE \
  -Dpde.build.scripts=$ECLIPSEBASE/plugins/org.eclipse.pde.build_$PDEBUILDVERSION/scripts \
  -DcdtTag=$CDTTAG \
  -DdontUnzip=true fetch

cd ..
mv org.eclipse.cdt.releng eclipse-cdt-${UPSTREAM_VERSION}
find eclipse-cdt-${UPSTREAM_VERSION} \
    \( -name '*.so' -o -name '*.o' -o -name 'net.*.jar' -o -name '*.dll' -o -name '*.exe' -o -name 'exe' -o -name '*_g' \) \
    -a -delete
find eclipse-cdt-${UPSTREAM_VERSION} -depth -type d -empty -delete
tar jcf ../../eclipse-cdt_${UPSTREAM_VERSION}.orig.tar.bz2 eclipse-cdt-${UPSTREAM_VERSION}
cd ../../
rm -fr temp/
