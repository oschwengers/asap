# ASA³P - Automatic Bacterial Isolate Assembly, Annotation and Analyses Pipeline

## Contents
- [Description](#description)
- [Features](#features)
- [Availability](#availability)
- [Input & Output](#input-output)
- [FAQ](#faq)
- [License](#license)
- [Bugs](#bugs)


## Description
ASA³P is an automatic and highly scalable assembly, annotation and higher-level analyses pipeline for closely related bacterial isolates.

At its core it's a command line tool creating standard bioinformatics file formats as well as sophisticated HTML5 documents.
Its main purpose is the automatic processing of large scale NGS data of multiple closely related isolates, thus transforming raw reads into assembled and annotated genomes and finally getting as much information on every single bacterial genome as possible. Per-isolate analyses are finally complemented by first comparative insights.
Hereby, the pipeline incorporates many best-in-class open source bioinformatics tools and thus minimizes the burden of ever repeating tasks. Envisaged as an upfront tool it provides comprehensive insights as well as a general overview and comparison of analysed genomes along with all necessary result files for subsequent deeper analyses.
All results are presented via modern HTML5 documents comprising interactive visualizations.


## Features

### Per isolate
- quality clipping & control
- assembly (**Illumnia** & **PacBio**)
- scaffolding
- annotation
- taxonomic classification (**Kmer**, **16S** and **ANI**)
- multi locus sequence typing (**MLST**)
- antibiotic resistance detection
- reference mapping
- SNP detection

## Comparative
- core/pan genome calculation
- phylogenetic tree creation


## Availability
Due to its complex dependencies installation is a non-trivial task. Therefore, ASA³P comes in two versions:
- **Docker**: linux container for small projects
- **OpenStack**: highly scalable cloud version for (very) large projects

### Docker
For the sake of simplicity ASA³P offers a **Docker** container hosted at **Docker Hub** (https://hub.docker.com/r/oschwengers/asap/).

Setup:
```bash
sudo docker pull oschwengers/asap
wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap.tar.gz
tar -xzf asap.tar.gz
rm asap.tar.gz
wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap-docker.sh
chmod 755 asap-docker.sh
```

Running an ASA³P Container using the `asap-docker.sh` shell wrapper script:
```bash
sudo asap-docker.sh <ASAP_DIR> <PROJECT_DIR>
```

Parameters:
* `<ASAP_DIR>`: absolute path to downloaded and extracted ASA³P directory
* `<PROJECT_DIR>`: absolute path to the actual project data directory (containing `config.xls` and `data` directory)

**Complete example**: (user name: ubuntu)
```bash
sudo docker pull oschwengers/asap
wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap.tar.gz
tar -xzf asap.tar.gz
rm asap.tar.gz
wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes.tar.gz
tar -xzf example-lmonocytogenes.tar.gz
rm example-lmonocytogenes.tar.gz
wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap-docker.sh
chmod 755 asap-docker.sh
sudo ./asap-docker.sh asap/ example-lmonocytogenes/
```

For further information have a look at the Docker readme (DOCKER.md ).

### Cloud - OpenStack
ASA³P's **OpenStack** based cloud version offers automatic creation, setup and orchestration of a **SGE** based compute cluster and its entire underlying infrastructure.
Hence, analysis of thousands of genomes can be achieved in a highly parallel manner and adequate amount of time.
Hereby, ASA³P takes care of all setup and orchestration aspects and thus hiding almost all technical complexity.
For further information please have a look at the user manual (https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/manual.pdf)

### Downloads
ASA³P directory containing software and databases: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap.tar.gz

As an internally used framework (BiBiGrid) is currently based on Ubuntu 14.04 LTS we have to provide a distinct directory containing certain workarounds: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap-os.tar.gz

Additional files:
- comprehensive manual: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/manual.pdf
- configuration template: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/config.xls
- example project: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes.tar.gz


## Input & Output

### Input
ASA³P expects all input files and information regarding a single batch run (i.e. a "project") within a dedicated directory.
All necessary information (meta information, reference genomes, isolate/sample names and files) are provided via an Excel config file named *config.xls*.
A corresponding template can be downloaded [here](https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/config.xls). For further details on how to fill out a proper configuration file,
please have a look at the [manual](https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/manual.pdf).
All input files referenced in a configuration spreadsheet need to be placed in a subdirectory called *data*.

**Example**:
```
project-data-dir
├── config.xls
├── data
│   ├── reference-genome-1.gbk
│   ├── reference-genome-2.fasta
│   ├── isolate-1-1.fastq.gz
│   ├── isolate-1-2.fastq.gz
│   ├── isolate-2-1.fastq.gz
│   ├── isolate-2-2.fastq.gz
│   ├── isolate-3.1.bax.h5
│   ├── isolate-3.2.bax.h5
│   ├── isolate-3.3.bax.h5
│   ├── ...
```

### Output
The pipeline stores all output files within the specified project directory leaving all input files untouched:
- empty status file indicating ASA³P current status (*status.running*, *status.finished* or *status.failed*)
- logging file (*asap.log*)
- internal configuration file (*config.json*)
- report directory containing **HTML5** report pages

Furthermore, for each analysis ASA³P creates a corresponding subdirectory containing all results such as:
- empty status file indicating an analysis' status (*status.running*, *status.finished* or *status.failed*)
- **JSON** file (*info.json*) containig all collected/aggregated information
- binary result files in standard file formats (**.fasta**, **.gbk**, **.gff**, **.bam**, **.vcf.gz**, etc...)

Where apropriate ASA³P creates subdirectories for each isolate within an analysis directory.

**Example**:
```
project-data-dir
├── [state.running | state.finished | state.failed]
├── asap.log   (global logging file)
├── config.xls   (config spreadsheet)
├── config.json   (internal config)
├── reports   (HTML5 reports)
│   ├── index.html
│   ├── ...
├── reads_qc   (quality clipped read files)
│   ├── <sample-name>
│   ├── ├── [state.finished | state.failed]
│   ├── ├── isolate-1-1.fastq.gz
│   ├── ├── isolate-1-2.fastq.gz
│   ├── ├── info.json
│   ├── ...
├── assembly   (assemblies)
│   ├── <sample-name>
│   ├── ├── [state.finished | state.failed]
│   ├── ├── <sample-name>.fasta
│   ├── ├── <sample-name>-discarded.fasta
│   ├── ├── info.json
│   ├── ...
├── scaffolds   (scaffolded contigs)
│   ├── <sample-name>
│   ├── ├── [state.finished | state.failed]
│   ├── ├── <sample-name>.fasta   (scaffolds)
│   ├── ├── <sample-name>-pseudo.fasta   (pseudo genome)
│   ├── ├── info.json
│   ├── ...
├── annotations
│   ├── <sample-name>
│   ├── ├── [state.finished | state.failed]
│   ├── ├── <sample-name>.gbk   (Genbank)
│   ├── ├── <sample-name>.gff   (GFF3)
│   ├── ├── <sample-name>.ffn   (gene sequences)
│   ├── ├── <sample-name>.faa   (protein sequences)
│   ├── ├── info.json
│   ├── ...
├── taxonomy   (taxonomic classfication results)
│   ├── [<sample-name>.finished | <sample-name>.failed]
│   ├── <sample-name>.json
│   ├── ...
├── mlst   (multi-locus sequence typing results)
│   ├── [<sample-name>.finished | <sample-name>.failed]
│   ├── <sample-name>.json
│   ├── ...
├── abr   (antibiotic resistance genes detection)
│   ├── [<sample-name>.finished | <sample-name>.failed]
│   ├── <sample-name>.json
│   ├── ...
├── vf   (virulence factor detection results)
│   ├── [<sample-name>.finished | <sample-name>.failed]
│   ├── <sample-name>.json
│   ├── ...
├── mappings   (reference mappings)
│   ├── [<sample-name>.finished | <sample-name>.failed]
│   ├── <sample-name>.json
│   ├── <sample-name>.bam
│   ├── <sample-name>.bam.bai
│   ├── ...
├── snps   (called single nucleotide polymorphisms)
│   ├── [<sample-name>.finished | <sample-name>.failed]
│   ├── <sample-name>.json
│   ├── <sample-name>.consensus.fasta   (mpileup consensus file)
│   ├── <sample-name>.vcf.gz   (SNPs in variant calling format file)
│   ├── <sample-name>.vcf.gz.tbi
│   ├── <sample-name>.chk   (bcftools stats)
│   ├── <sample-name>.csv   (SNPeff per gene statisics)
│   ├── ...
├── corepan
│   ├── [state.finished | state.failed]
│   ├── info.json
│   ├── core.fasta   (core genome sequences)
│   ├── pan.fasta   (pan genome sequences)
│   ├── pan-matrix.tsv   (pan genome matrix)
│   ├── <sample-name>.json
│   ├── ...
├── phylogeny
│   ├── [state.finished | state.failed]
│   ├── info.json
│   ├── tree.nwk   (phylogenetic tree in newick file)
│   ├── consensus.fasta   (global consensus file)
├── data
```


## FAQ
* __Is there a public example project?__
Just download this exemplary project containing a set of public *Listeria monocytogenes* genomes from **SRA**: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes.tar.gz

* __How to cite ASA³P?__
A manuscript is currently in preparation. Stay tuned!

* __Can I install ASA³P by myself?__
Yes you can! Nevertheless, we highly encourage everyone to use either the **Docker** container or the **OpenStack** images. As there are too many combinations of linux distributions and tool/database versions we cannot give any support for this.


## License
ASA³P itself is published and distributed under GPL3 license. In contradiction, some of its dependencies bundled within the ASA³P directory (asap.tar.gz file) are published 
under different licenses, e.g. GPL2, BSD, MIT, LGPL, etc. A file (README.md) within the ASA³P directory contains a list of all dependencies and licenses.

**Please notice**
that some bundled dependecies are published under a **free-for-academic** or **free-for-non-commercial** usage license model. 
To our best knowledge this is true for at least the following databases:
- CARD: free for academic usage
- PubMLST: proprietary but free to use


## Bugs
If you face any problems using the pipeline please have a look at the manual. If the problems remain, please send me an email: <oliver.schwengers@computational.bio.uni-giessen.de>

For technical problems or feature requests please submit an issue: https://github.com/oschwengers/asap/issues
