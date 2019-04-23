# ASA³P - Automatic Bacterial Isolate Assembly, Annotation and Analyses Pipeline

![ASA³P Overview](asap.png)

## Contents
- [Description](#description)
- [Features](#features)
- [Availability](#availability)
- [Input & Output](#input-output)
- [FAQ](#faq)
- [License](#license)
- [Bugs](#bugs)


## Description
ASA³P is an automatic and highly scalable assembly, annotation and higher-level
analyses pipeline for closely related bacterial isolates.

At its core it's a command line tool creating standard bioinformatics file
formats as well as sophisticated HTML5 documents. Its main purpose is the
automatic processing of NGS data of multiple closely related isolates, thus
transforming raw reads into assembled and annotated genomes and finally gathering
as much information on every single bacterial genome as possible. Per-isolate
analyses are finally complemented by first comparative insights. Therefore, the
pipeline incorporates many best-in-class open source bioinformatics tools and
thus minimizes the burden of ever repeating tasks. Envisaged as an upfront tool
it provides comprehensive insights as well as a general overview and comparison
of analysed genomes along with all necessary result files for subsequent deeper
analyses. All results are presented via modern HTML5 documents comprising
interactive visualizations.


## Features

### Per isolate
- quality/adapter clipping
- assembly (**Illumnia**, **PacBio** & **ONT**)
- scaffolding
- annotation
- taxonomic classification (**Kmer**, **16S** and **ANI**)
- multi locus sequence typing (**MLST**)
- antibiotic resistance detection
- virulence factor detection
- reference mapping
- SNP detection

### Comparative
- calculation of core/pan genome and singleton genes
- phylogenetic tree creation


## Availability
Targeting different project sizes, i.e. number of genomes which should be
analysed as a single project, we offer ASA³P in two versions:
- **Docker**: linux container for small to medium projects
- **OpenStack**: highly scalable cloud version for (very) large projects

For both you will need the following file:
- ASA³P tarball (containing binaries, 3rd party executables and databases):
https://s3.computational.bio.uni-giessen.de/swift/v1/asap/asap.tar.gz
- configuration template: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/config.xls

Additional files:
- comprehensive manual: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/manual.pdf
- configuration example: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/config-example.xls
- example projects:
   - 4 public *L. monocytogenes* genomes: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes-4.tar.gz
   - 32 public *L. monocytogenes* genomes: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes-32.tar.gz
   - 8 *E. coli* project merely showing support of different input types: https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-ecoli-input.tar.gz


### Docker
For small to medium projects (up to ~200 isolates) but also for the sake of
simplicity, reproducibility and easy distribution, we offer ASA³P as a
**Docker** image hosted at:
**Docker Hub** (https://hub.docker.com/r/oschwengers/asap/).

Setup:
```bash
$ sudo docker pull oschwengers/asap
$ wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/asap.tar.gz
$ tar -xzf asap.tar.gz
$ rm asap.tar.gz
```

Running an ASA³P Container using the `asap-docker.sh` shell wrapper script:
```bash
$ sudo asap/asap-docker.sh <PROJECT_DIR> [<SCRATCH_DIR>]
```

Parameters:
* `<PROJECT_DIR>`: path to the actual project directory (containing `config.xls` and `data` directory)
* `<SCRATCH_DIR>`: optionally path to a distinct scratch/tmp dir

**Note**
Make sure to always leave the `asap-docker.sh` script within the ASA³P directory
as by this the right internal paths will automatically be detected and forwarded.

**Complete example**:
```bash
$ sudo docker pull oschwengers/asap
$ wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/asap.tar.gz
$ tar -xzf asap.tar.gz
$ rm asap.tar.gz
$ wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes-4.tar.gz
$ tar -xzf example-lmonocytogenes-4.tar.gz
$ rm example-lmonocytogenes-4.tar.gz
$ sudo asap/asap-docker.sh example-lmonocytogenes-4/
```

For further information have a look at the Docker readme (DOCKER.md ).


### Cloud - OpenStack
ASA³P's **OpenStack** based cloud version targets the analysis of hundreds to
even thousands of bacterial isolates. Therefore, it features automatic creation,
setup and orchestration of an **SGE** based compute cluster and its entire
underlying infrastructure. Therefore, the **OpenStack** cloud version internally
takes advantage of the BiBiGrid (https://github.com/BiBiServ/bibigrid) framework.
Hence, analysis of thousands of genomes can be achieved in a highly parallel
manner and adequate amount of time. ASA³P takes care of all setup and orchestration
aspects and thus hides away as much technical complexity as possible. For further
information please have a look at our user manual.

In order to trigger an **OpenStack** based cloud project, you need the following
additional cloud related files:
- ASA³P cloud tarball (containing binaries, property files and a customized BiBiGrid version):
https://s3.computational.bio.uni-giessen.de/swift/v1/asap/asap-cloud.tar.gz

Once ASA³P is properly setup you can start it by executing a single shell script:
```bash
$ ~/asap-cloud/asap-cloud.sh -i <INSTANCE_ID> -o <OPEN_STACK_RC_FILE> -p <PROJECT_DIR>
```

Parameters:
* `<INSTANCE_ID>`: VM id of the gateway instance (VM you start ASA³P from)
* `<OPEN_STACK_RC_FILE>`: OpenStack RC file providing cloud and project information
* `<PROJECT_DIR>`: path to the actual project directory (containing `config.xls` and `data` directory)

For a comprehensive and detailed description of how to setup an OpenStack project
and ASA³P therein, please have a look at our
[manual](https://s3.computational.bio.uni-giessen.de/swift/v1/asap/manual.pdf).


## Input & Output

### Input
ASA³P expects all input files and information regarding a single batch run
(i.e. a "project") within a dedicated directory. All necessary information
(meta information, reference genomes, isolate/sample names and files) are
provided via an Excel config file named *config.xls*.
A corresponding template can be downloaded [here](https://s3.computational.bio.uni-giessen.de/swift/v1/asap/config.xls).
For further details on how to fill out a proper configuration file, please have
a look at the [manual](https://s3.computational.bio.uni-giessen.de/swift/v1/asap/manual.pdf)
and the exemplary projects listed above. All input files referenced in a configuration
spreadsheet need to be placed in a subdirectory called *data*.

**Example**:
```
project-dir
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
**tl; dr**
Just open your browser and open the **index.html** file located at:
```
project-dir
├── reports   (HTML5 reports)
│   ├── index.html
```

ASA³P stores all output files within the specified project directory
leaving input files untouched:
- empty status file indicating ASA³P current status, one of:
   - *status.running*
   - *status.finished*
   - *status.failed*
- log file (*asap.log*)
- internal configuration file (*config.json*)
- report directory containing **HTML5** report pages (*reports*)

Furthermore, for each analysis ASA³P creates a corresponding subdirectory
containing result files such as:
- empty status file indicating an analysis' status, one of:
   - *status.running*
   - *status.finished*
   - *status.failed*
- **JSON** file (*info.json*) comprising collected and aggregated information
- binary result files in standard file formats (**.fasta**, **.gbk**, **.gff**, **.bam**, **.vcf.gz**, etc...)

Where necessary ASA³P creates subdirectories for each isolate within an
analysis directory.

**Example**:
```
project-dir
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
Just download this exemplary project containing a set of 4 public
*Listeria monocytogenes* genomes from **SRA**:
https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes-4.tar.gz

* __How to cite ASA³P?__
A manuscript is currently in preparation. Stay tuned!

* __Can I install ASA³P by myself?__
Yes you can! Nevertheless, we highly encourage to use either the **Docker**
container or the **OpenStack** images. As there are too many combinations of
linux distributions and tool/database versions, we cannot give any support for this.


## License
ASA³P itself is published and distributed under GPL3 license. In contradiction,
some of its dependencies bundled within the ASA³P tarball (asap.tar.gz file) are
published under different licenses, e.g. GPL2, BSD, MIT, LGPL, etc.
A file (README.md) within the ASA³P directory contains a list of all
dependencies and related licenses.

**NOTE**
Please, notice that some bundled dependecies are published under a
**free-for-academic** or **free-for-non-commercial** usage license model.
To our best knowledge this is true for at least the following databases:
- CARD: free for academic usage
- PubMLST: proprietary but free to use


## Bugs
If you face any problems using the pipeline please have a look at the manual.
If the problems remain, please send us an email: <asap@computational.bio.uni-giessen.de>

For technical issues or feature requests please submit an issue:
https://github.com/oschwengers/asap/issues
