package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'segmentIdentifier', description = 'segmentIdentifier that describes the Project:Procedure to kick off manual segment of. <project name>:<segment name>'),
	@Require(parameter = 'selectedVersions', description = 'string containing the chosen versions of the segment dependencies. Comma Delimited. ex: <group>:<app>:<version>,<group2>:<app2>:<version2>')
	])
class UploadGradleWithSelectedDependencyVersionsTask extends WatchmenTask {
	def logger = LogFactory.getLog(com.gap.gradle.tasks.UploadGradleWithSelectedDependencyVersionsTask)
	Project project
	CommanderClient commanderClient
	SegmentRegistry segmentRegistry

	public UploadGradleWithSelectedDependencyVersionsTask(Project project, commanderClient = new CommanderClient(), segmentRegistry = new SegmentRegistry()) {
		super(project);
		this.project = project
		this.commanderClient = commanderClient
		this.segmentRegistry = segmentRegistry
	}

	def execute() {
		validate()
		logger.info("Passed param segmentIdentifier as = " + project.segmentIdentifier)
		logger.info("Passed param selectedVersions as = " + project.selectedVersions)
		def gradleFileName = segmentRegistry.getSegmentRegistryValue(project.segmentIdentifier, "gradleFile")
		logger.info("Gradle file of the segmentIdentifier is = $gradleFileName")
		
		/*
		 * this execute task implements this below ec-perl script on EC in this gradle task:
		 *
		 *
		 * use ElectricCommander;
			use strict;
			
			my $gradleFileName = "$[/projects[WM Segment Registry]/SegmentRegistry/$[segmentIdentifier]/gradleFile]";
			print STDERR "gradleFileName = $gradleFileName\n";
			
			open(my $fh1, "<", "ci/$gradleFileName") or die $!;
			local $/;
			my $gradleFile = <$fh1>;
			print STDERR "******************************************************\n";
			print STDERR "$gradleFile\n";
			print STDERR "******************************************************\n";
			my @dependencies = split(",", "$[selectedVersions]");
			
			foreach my $dependency(@dependencies){
			  print STDERR "dependency = $dependency\n";
			  my ($group, $name, $version) = split(":", $dependency);
			  my $find = "group: \'$group\', name: \'$name\', version: \'\.*?\'";
			  my $replace = "group: \'$group\', name: \'$name\', version: \'$version\'";
			  print STDERR "find = $find\n";
			  print STDERR "replace = $replace\n";
			  $gradleFile =~ s/$find/$replace/g or die "Dependency $group:$name could not be updated!";
			}
			print STDERR $gradleFile;
			close($fh1);
			
			open(my $fh2, ">", "ci/$gradleFileName") or die $!;
			print {$fh2} $gradleFile;
		 *
		 */
	}
	
	def replaceGradleWithSelectedVersions() {
		
	}
}
