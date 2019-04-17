# v1.1.1

## Enhancements
- Fixed and improved cloud orchestration logic
- Fixed allel detection in MLST 
- Fixed ABR gene detection and length calculation
- Applied minor improvements and bugfixes to report pages

# v1.1
## Features
- Added support for ONT sequencing platform
- Added option to skip comparative analyses

## Enhancements
- Simplified Docker based execution
- Removed necessarity of extra cloud images and tarballs
- Updated CARD db
- Updated VF db
- Applied many bugfixes and minor improvements to report pages

# v1.0.4
##Enhancements
- Updated NCBI tbl2asn
- Fixed VF reports bug introduced by CARD/ResFinder replacement

# v1.0.3
## Enhancements
- Added option to skip comparative analyses
- Applied many bugfixes and minor improvements to report pages
- Replaced ResFinder by CARD as annotation database
- Harmonized variable names in cloud property file
- Fixed NPE in ANI calculation

# v1.0.2
## Enhancements
- Applied many bugfixes and minor improvements

# v1.0.1
## Enhancements
- Applied many bugfixes and minor improvements

# v1.0
## Features
- Added support for PacBio Sequel protocol
- Added a virulence factor (VF) detection analyses based on VFDB

## Enhancements
- Fixed PacBio analysis workflow
- Fixed internal step selection
- Reviewed QC contamination plots
- Added comprehensive help sections to report pages
- Applied many bugfixes and minor improvements to report pages
- Replaced ResFinder by CARD as annotation database

# v0.9
## Features
- Added a 16S rRNA based taxonomic classification
- Added a core/pan genome analysis
- Replaced CONTIGuator by MeDuSa (single -> multi reference based scaffolding)

## Enhancements
- Updated annotation databases (Refseq Feb 17)
- Updated annotation databases (RFAM 12.2)
- Updated ABR database (CARD 1.1.8)
- Adjusted SGE CPU/RAM resource requests
- Applied many bugfixes and enhancements to HTML report pages

# v0.8
## Features
- Added an antimicrobial resistance detection analysis based on CARD db
- Added a MLST analysis
- Added support for draft reference genomes
- Added ANI computation for references
- Added a length and coverage based contig filter after assembly

## Enhancements
- Reorganized and improve all report pages
- Decoupled ASA³P from BCF ressources
- Added ResFinder and VFDB as annotation reference databases
- Provided Prokka with gram +/- information
- Updated annotation databases (RFAM 12.1)
- Provided unique locus tags to contigs during annotation

# v0.7
- Reimplemented cluster scripts
- Annotate SNPs with SnpEff
- Added circular genome plot to annotation report

# v0.6
- Handle failed/skipped steps
- Added interactive JS visualizations
- Several bugfixes and minor improvements

# v0.5
- Added an phylogeny analyses
- Added a reporting stage creating HTML reports
