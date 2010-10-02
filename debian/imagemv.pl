#!/usr/bin/perl

use strict;
use warnings;

my $dryrun = defined($ARGV[0]) && $ARGV[0] eq '--dry-run';

while( my $image = <STDIN> ){
    my $des;
    my $desdir;
    my $relpath;
    my $linktarget;
    chomp($image);
    if( -l $image ){
	# already a link - not touching
	next;
    }
    if( ! -e $image ){
	choke("Cannot \"mv + ln\" $image: it does not exists.");
    }
    # Remove the leading ./ (from find output).
    $image =~ s@^./@@o;
    $des = $image;
    $des =~ s@lib/@share/@o;
    if($des =~ m@eclipse/features/@o){
	# They have some names that changes based on arch/build/something.
	# So we will just remove that so the link is always correct.
	# e.g. 
	#   org.eclipse.help_1.1.1.R35x_v20090811-7e7eFAnFEx2XZoYwvOe8duD
	# becomes
	#   org.eclipse.help_1.1.1.R35x_v20090811
	$des =~ s@(_v\d{8})-[^/]++@$1@o;
    }
    $desdir = $des;
    # Remove the last part (the name of the file)
    $desdir =~ s@[^/]++$@@o;
    $relpath = $desdir;
    # Replace path parts with ".."
    $relpath =~ s@[^/]++@..@go;
    # concat with the original target and we have
    # the relative link - There is no / in between
    # because $relpath already contains it.
    $linktarget = "$relpath$des";
    if($dryrun){
	print "mkdir -p \"$desdir\" && mv -f \"$image\" \"$des\" && ln -s \"$linktarget\" \"$image\"\n";
    } else {
	system("mkdir -p \"$desdir\" && mv -f \"$image\" \"$des\" && ln -s \"$linktarget\" \"$image\"") == 0
	    or choke("Could not complete link of $image.");
    }
}

sub choke{
    my $msg = shift;
    print STDERR "$msg\n";
    exit(1);
}
