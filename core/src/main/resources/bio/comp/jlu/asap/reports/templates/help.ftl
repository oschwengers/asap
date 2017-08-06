<!DOCTYPE html>
<html>

<head>
  <#include "commons/meta.ftl">
</head>

<body data-spy="scroll" data-target="#myScrollspy">

  <nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
          <span class="sr-only">Toggle navigation</span>
        </button>
        <a class="navbar-brand" href="index.html"><span class="glyphicon glyphicon-home"></span>&nbsp;&nbsp;example-ecoli-ST410</a>
      </div>
      <div id="navbar" class="navbar-collapse collapse">
        <ul class="nav navbar-nav navbar-right">
          <li><a href="index.html">Dashboard</a></li>
          <li><a href="help.html">Help</a></li>
        </ul>
      </div>
    </div>
  </nav>

  <div class="container-fluid">
    <div class="row">

      <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
              <li class="active"><a href="#">Genome Analyses</a></li>
              <li><a href="qc.html">Quality Control</a></li>
              <li><a href="assemblies.html">Assembly</a></li>
              <li><a href="scaffolds.html">Scaffolds</a></li>
              <li><a href="annotations.html">Annotation</a></li>
          </ul>
          <ul class="nav nav-sidebar">
              <li class="active"><a href="#">Genome Characterization</a></li>
              <li><a href="taxonomy.html">Taxonomic Classification</a></li>
              <li><a href="mlst.html">MLST</a></li>
              <li><a href="abr.html">Antibiotic Resistances</a></li>
              <li><a href="vf.html">Virulence Factors</a></li>
              <!--<li><a href="plasmids.html">Plasmids</a></li>-->
              <!--<li><a href="phages.html">Phages</a></li>-->
              <li><a href="mapping.html">Reference Mapping</a></li>
              <li><a href="snps.html">SNP Detection</a></li>
          </ul>
          <ul class="nav nav-sidebar">
              <li class="active"><a href="#">Comparative Analyses</a></li>
                          <li><a href="corepan.html">Core/Pan Genome</a></li>
              <li><a href="phylogeny.html">Phylogeny</a></li>
          </ul>
      </div>

        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

          <div class="row">

            <div class="col-md-9">
              <h2 id="analysisoverview">Analysis overview</h2>

              <h3 id="generalinformation">General information</h3>

              <p>On the top of this page project information on <code>Biological background</code>, <code>User account</code>                    and <code>Runtime statistics</code> are displayed. Mouse over the symbols to display their meaning. A
                general multi analysis comparison of the analysed genomes is visualized as <code>Interactive infographics</code>                    and accessible in the <code>Interactive data table</code>.</p>

              <h3 id="interactiveinfographics">Interactive infographics</h3>

              <p>Provides an overview on <code>key data</code> of the analysed genomes. The vertical black lines display
                the result range of a particular analysis. The range of each analysis can be limited via drag and drop
                on the black line. The limited range is visualized as a box. Genome graphs not passing through all set
                ranges are greyed out. The box itself can also be dragged in position or adjusted in its range via dragging
                either top or bottom. To remove the range limitation click on the particular black line. Mouse over a
                genome graph to display its name.</p>

              <h3 id="interactivedatatable">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in columns containing numeric values.
                They visualize the relative relation of this value compared to the according values of the other genomes.
                The <code>yellow and red colored bar plots</code> indicates outliners based on Z-score.</p>

              <h3 id="downloads">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="links">Links</h3>

              <ul>
                <li><code># ABR</code> values of the data table redirect on click to their antibiotic resistance analysis.
                </li>

                <li><code># Contigs</code> values of the data table redirect on click to their assembly.</li>

                <li><code># Genes</code> values of the data table redirect on click to their annotation.</li>

                <li><code>GC</code> values of the data table redirect on click to their assembly.</li>

                <li><code>Genome Size</code> values of the data table redirect on click to their assembly.</li>

                <li><code># HI SNPs</code> values of the data table redirect on click to their SNP analysis.</li>

                <li><code>Index page</code> can be accessed from any report page via click on the home button on the top
                  left.</li>

                <li><code>Particular analysis results</code> can be accessed via the left handed menu.</li>

                <li><code>Tax Class</code> values of the data table redirect on click to their Taxonomy (kmer based taxonomic
                  classification).</li>
              </ul>

              <h3 id="glossary">Glossary</h3>

              <ul>
                <li><strong># ABRs</strong>: Number of antibiotic resistances found.</li>

                <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                <li><strong>GC</strong>: GC content [%]</li>

                <li><strong># Genes</strong>: Number of genes found.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>Genome Size</strong>: Genome size in 1000 bases [kb].</li>

                <li><strong># HI SNPs</strong>: Number of hi impact nucleotide polymorphisms found.</li>

                <li><strong>Input Type</strong>: Format of the provided sequence data.</li>

                <li><strong># Plasmids</strong>: Number of plasmids found.</li>

                <li><strong>Tax Class</strong>: Kmer based taxonomic classification.</li>
              </ul>

              <hr>

              <h2 id="qualitycontrol">Quality control</h2>

              <p>Provides an overview on the quality control of the analysed genomes. The quality of the sequenced reads
                is determined via <code>FastQC</code>. For reference based quality acquisition <code>FastQ Screen</code>                    is utilised. Depending on sequencing source (NGS or PacBio) filtering based on qualities is performed
                with <code>Trimmomatic</code>. Trimmomatic setting are: "ILLUMINACLIP:
                <PE or SE adapter> and
                  <phiX data base> :2:30:10", 'LEADING:15', 'TRAILING:15', 'SLIDINGWINDOW:4:20', 'MINLEN:20', 'TOPHRED33'. Only reads
                    that pass the quality control are included in the following analysis.</p>

              <h3 id="interactivedatatable-1">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in '# Reads' column. Their data field
                filling ratio corresponds to the ratio of field value to column maximum. Mouse over on underlined table
                headers to display further information on it.</p>

              <h3 id="downloads-1">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="links-1">Links</h3>

              <ul>
                <li><code>Details</code> on the quality control of a particular genome can be accessed via click on the magnifying
                  glass in the overview table.</li>

                <li><a href="https://www.bioinformatics.babraham.ac.uk/projects/fastqc/">FastQC</a>; Simon Andrews (2010).
                  FastQC: A quality control tool for high throughput sequence data.</li>

                <li><a href="http://www.bioinformatics.babraham.ac.uk/projects/fastq_screen/">FastQ Screen</a>; Steven Wingett
                  (2011). FastQ Screen allows you to screen a library of sequences in FastQ format against a set of sequence
                  databases so you can see if the composition of the library matches with what you expect.</li>

                <li><a href="http://www.usadellab.org/cms/?page=trimmomatic">Trimmomatic</a>: Bolger, A. M., Lohse, M., &amp;
                  Usadel, B. (2014). Trimmomatic: A flexible trimmer for Illumina Sequence Data. Bioinformatics, btu170.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/24695404">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-1">Glossary</h3>

              <ul>
                <li><strong>GC</strong>: GC content in percent.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>Length</strong>: Minimal/ mean/ maximal read length for this particular genome.</li>

                <li><strong>PC</strong>: Read percentage of potential contaminations. Based on a 10% random subset mapping
                  against a contamination references data base (e.g. containing phiX sequences).</li>

                <li><strong>Quality</strong>: Minimal/ mean/ maximal PHRED score of sequenced reads for this particular genome
                  (error probability; PHRED 20: 1 in 100; PHRED 30: 1 in 1000).</li>

                <li><strong># Reads</strong>: Number of sequenced reads for this particular genome.</li>
              </ul>

              <h2 id="qualitycontroldetail">Quality control detail</h2>

              <p>Contains several comparisons of read sets before (Raw) and after quality control (QC) generated by
                <code>Fast QC</code>. For paired end sequenced reads the comparison consists of four data sets. Forward
                and reverse reads, before and after quality control. Select a comparison by clicking on the genome name
                (top middle) to open the drop down menu.</p>

              <h3 id="tableraw">Table raw</h3>

              <p>Displays the properties of the raw data, including <code>File</code> names, the <code># Reads</code>, read
                <code>Lengths</code>, <code>Quality</code> and <code>GC</code> percentage.</p>

              <h3 id="tableqc">Table QC</h3>

              <p>Displays the properties of data after quality control, including <code>File</code> names, the <code># Reads</code>,
                read <code>Lengths</code>, <code>Quality</code> and <code>GC</code> percentage.</p>

              <h3 id="boxplotpotentialcontaminations">Boxplot potential contaminations [%]</h3>

              <p>The percentage of <code>reads that could not be mapped to the reference</code> but to different contamination
                targets is shown per target. The different targets include human, mouse PhiX and vectors.
              </p>

              <h3 id="interactivediagramgroups">Interactive diagram groups</h3>

              <p>The first diagram of each quartet refers to the forward reads of raw data, the second to forward quality
                controlled data, the third to reverse reads of raw data and the fourth to reverse reads of quality controlled
                data. Via mouse over on the diagram the according file name is displayed.</p>

              <h4 id="perbasequalities">Per base qualities</h4>

              <p>Diagrams with the <code>quality scores across all bases</code>. On the x-axis the base position in the
                reads is displayed. On the y-axis the <code>Quality</code> as PHRED score is shown.</p>

              <h4 id="persequencequalities">Per sequence qualities</h4>

              <p>Diagrams with the <code>quality score distribution over all sequences</code>. On the x-axis the mean sequence
                <code>Quality</code> as PHRED score of a read is shown. On the y-axis the number of reads is display.</p>

              <h5 id="perbasesequencecontents">Per base sequence contents</h5>

              <p>Diagrams with the <code>sequence content across all bases</code>. On the x-axis the base position in the
                reads is displayed. On the y-axis the percentage of each base (A, C, G, T) across all reads is displayed.</p>

              <h4 id="persequencegccontents">Per sequence GC contents</h4>

              <p>Diagrams with the <code>GC distribution over all sequences</code>. The red graph shows the GC count per
                read, the blue graph shows the theoretical distribution. On the x-axis the mean GC content of the reads
                is display. On the y-axis the number of reads is display.</p>

              <h4 id="perbasencontents">Per base N contents</h4>

              <p>Diagrams with the <code>N content across all bases</code>. On the x-axis the base position in the reads
                is displayed. On the y-axis the percentage of bases characterised as 'N' (not assignable) is displayed.</p>

              <h4 id="sequencelengthdistributions">Sequence length distributions</h4>

              <p>Diagrams with the <code>distribution of sequence lengths over all sequences</code>. On the x-axis the sequence
                lengths of the reads are displayed. On the y-axis the number of reads is displayed.</p>

              <h4 id="kmerprofiles">Kmer profiles</h4>

              <p>Diagrams with the <code>log2 ratio from observations to expected kmers</code>. The six kmers with the highest
                log2 obs/exp are displayed. On the x-axis the base position in the reads is display. On the y-axis the
                log2 ratio from observations to expected kmers is displayed.</p>

              <h3 id="glossary-2">Glossary</h3>

              <ul>
                <li><strong>GC</strong>: GC content in percent.</li>

                <li><strong>Length</strong>: Minimal/ mean/ maximal read length for this particular file.</li>

                <li><strong>Potential Contaminations</strong>: Read percentage of potential contaminations. Based on a 10%
                  random subset mapping against a contamination references data base (e.g. containing phiX sequences).</li>

                <li><strong>Quality</strong>: Minimal/ mean/ maximal PHRED score of sequenced reads for this particular genome
                  (error probability; PHRED 20: 1 in 100; PHRED 30: 1 in 1000).</li>

                <li><strong># Reads</strong>: Number of sequenced reads for this particular file.</li>
              </ul>

              <hr>

              <h2 id="assembly">Assembly</h2>

              <p>The reads that pass the quality control are assembled. For long read assemblies the tool <code>HGap3</code>                    is used. Assemblies of hybrid and short reads are performed with the tool <code>SPAdes</code>. This page
                provides an overview on assembly key data of all genomes in this analysis.</p>

              <h3 id="interactivedotplot">Interactive dotplot</h3>

              <p>Via the radio buttons on the right <code>key data</code> for X and Y axis can be selected. Mouse over a
                dot of interest to display the according <code>genome name</code> as well as horizontal and vertical
                value extensions. <code>Zooming</code> can be applied via marking the area of interest with left mouse
                button down. To reset the view right click.</p>

              <h3 id="interactivedatatable-2">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in most columns containing numeric values.
                Their data field filling ratio corresponds to the ratio of field value to column maximum. Mouse over
                on underlined table headers to display further information on it.</p>

              <h3 id="downloads-2">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file). To download the <code>fasta</code> file of a particular
                genome assembly click on fasta in the data table.</p>

              <h3 id="links-2">Links</h3>

              <ul>
                <li><code>Details</code> on the assembly of a particular genome can be accessed via click on the magnifying
                  glass in the overview table.</li>

                <li><a href="https://github.com/PacificBiosciences/Bioinformatics-Training/wiki/HGAP#overview">HGap3</a>:
                  Chin, Chen-Shan, et al. "Nonhybrid, finished microbial genome assemblies from long-read SMRT sequencing
                  data." Nature methods 10.6 (2013): 563-569. <a href="https://www.ncbi.nlm.nih.gov/pubmed/23644548">PubMed</a>.</li>

                <li><a href="http://cab.spbu.ru/software/spades/">SPAdes</a>: Bankevich A., Nurk S., Antipov D., Gurevich
                  A., Dvorkin M., Kulikov A. S., Lesin V., Nikolenko S., Pham S., Prjibelski A., Pyshkin A., Sirotkin
                  A., Vyahhi N., Tesler G., Alekseyev M. A., Pevzner P. A. SPAdes: A New Genome Assembly Algorithm and
                  Its Applications to Single-Cell Sequencing. Journal of Computational Biology, 2012. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22506599">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-3">Glossary</h3>

              <ul>
                <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                <li><strong>GC</strong>: GC content in percent.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>Genome size</strong>: Genome size in 1000 bases [kb].</li>

                <li><strong>Mean contig lengths</strong>: Mean contig lengths of this particular genome.</li>

                <li><strong>Median contig lengths</strong>: Median contig lengths of this particular genome.</li>

                <li><strong>N50</strong>: Given ordered contigs from longest to smallest, length of the contig at 50% of
                  the genome length.</li>

                <li><strong>N50 coverage</strong>: Length weighted mean coverage of sequences with N50 length or longer.
                </li>

                <li><strong>N90</strong>: Given ordered contigs from longest to smallest, length of the contig at 90% of
                  the genome length.</li>

                <li><strong>N90 coverage</strong>: Length weighted mean coverage with sequenced reads of N90 contigs.</li>
              </ul>

              <h2 id="assemblydetail">Assembly detail</h2>

              <h3 id="histogramsofcontigspecifications">Histograms of contig specifications</h3>

              <h5 id="contiglengths">Contig lengths</h5>

              <p>Histogram of <code>contig length</code> in kb. Via mouse over the number of contigs in each bin is displayed.
              </p>

              <h5 id="contigcoverage">Contig coverage</h5>

              <p>Histogram of the <code>average read coverage</code> per contig. Via mouse over the average coverage of
                each bin is displayed.</p>

              <h5 id="contiggccontents">Contig GC contents</h5>

              <p>Stacked histogram of <code>GC contents</code> per contig. Via mouse over the GC content of each individual
                contig is displayed.</p>

              <h3 id="basicassemblystatistics">Basic assembly statistics</h3>

              <p>Provides information on the assembly in general and on the contig length.</p>

              <h3 id="interactivedatatablecontigs">Interactive data table contigs</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. Mose over on underlined table headers to display further information on it.</p>

              <h3 id="downloads-3">Downloads</h3>

              <p>The contigs and scaffolds used in this assembly as well as the ones discarded (not used for assembly) can
                be downloaded as <code>fasta</code> on the right below the histograms. The table can be saved as comma
                separated value (<code>csv</code>) file via click on the csv button (search and sorting are contained
                in the downloaded file).</p>

              <h3 id="glossary-4">Glossary</h3>

              <ul>
                <li><strong>Contigs</strong>: Set of overlapping DNA segments (reads).</li>

                <li><strong>Coverage</strong>: Mean read coverage of this contig.</li>

                <li><strong># Gaps</strong>: Amount of space (bp) between assembled nucleotides in this contig.</li>

                <li><strong>GC</strong>: GC content in percent.</li>

                <li><strong>Length</strong>: Length of the contig in base pairs.</li>

                <li><strong>N50 length</strong>: Given ordered contigs from longest to smallest, length of the contig at
                  50% of the genome length.</li>

                <li><strong>N90 length</strong>: Given ordered contigs from longest to smallest, length of the contig at
                  90% of the genome length.</li>

                <li><strong>Name</strong>: Name of this contig.</li>

                <li><strong>Scaffolds</strong>: Consists of aligned contigs with the sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'
                  in between them.</li>
              </ul>

              <hr>

              <h2 id="scaffolds">Scaffolds</h2>

              <p>This page provides an overview on the genome polishing results. Matching contigs and reads are joined to
                scaffolds via the tools <code>MeDuSa</code> and <code>Nucmer</code> (package of <code>MUMmer</code>).
                Medusa utilizes multiple reference genomes to align the contigs and scaffolds as well as assigning them
                to minus or plus strand. For contig joining the sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN' which contains
                all six frame stop codons was used. Utilising the same sequence the scaffolds from the polished results
                are merged into a <code>Pseudo genome</code>.</p>

              <h3 id="interactivedatatable-3">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in columns containing numeric values.
                Their data field filling ratio corresponds to the ratio of field value to column maximum. Mouse over
                on underlined table headers to display further information on it.</p>

              <h3 id="downloads-4">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file). To download a <code>fasta</code> file containing the
                Scaffolds or the generated <code>Pseudo genome</code> click on the according name in the data table.</p>

              <h3 id="links-3">Links</h3>

              <ul>
                <li><code>Details</code> on the contig layout of a particular genome can be accessed via click on the magnifying
                  glass in the overview table.</li>

                <li><a href="http://combo.dbe.unifi.it/medusa">MeDuSa</a>: E Bosi, B Donati, M Galardini, S Brunetti, MF
                  Sagot, P LiÃ³, P Crescenzi, R Fani, and M Fondi. MeDuSa: a multi-draft based scaffolder. Bioinformatics
                  (2015): btv171. <a href="https://www.ncbi.nlm.nih.gov/pubmed/25810435">PubMed</a>.</li>

                <li><a href="http://mummer.sourceforge.net/">MUMmer/Nucmer</a>: Open source MUMmer 3.0 is described in "Versatile
                  and open software for comparing large genomes." S. Kurtz, A. Phillippy, A.L. Delcher, M. Smoot, M.
                  Shumway, C. Antonescu, and S.L. Salzberg, Genome Biology (2004), 5:R12.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/14759262">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-5">Glossary</h3>

              <ul>
                <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>N50</strong>: Given ordered contigs from longest to smallest, length of the contig at 50% of
                  the genome length.</li>

                <li><strong>Pseudo genome</strong>: Genome generated via joining all sequence elements after scaffolding
                  with the sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'.</li>

                <li><strong># Scaffolds</strong>: Number of scaffolds (joined, aligned and assigned contigs) after polishing.
                  Joined with the sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'.</li>
              </ul>

              <h2 id="scaffoldsdetail">Scaffolds detail</h2>

              <p>Provides information on contig alignment and assignment to reference genome(s). The contigs of the particular
                Whole Genome Assembly (<code>WGA</code>) are compared to each of the reference genomes via Synteny plots.
                In order to visualize the scaffolding quality the comparison is done before and after the scaffolding
                process.</p>

              <h3 id="basicscaffoldingstatistics">Basic scaffolding statistics</h3>

              <p>Provides information on scaffolding in general and on the scaffold length.</p>

              <h3 id="dnasyntenyplots">DNA synteny plots</h3>

              <p>The upper synteny plot of each genome comparison displays the position of all contigs in both genomes before
                the scaffolding process (<code>Pre Scaffolding</code>). The lower synteny plot after scaffolding (
                <code>Post Scaffolding</code>). On the x-axis the contig position in the reference genome is displayed.
                On the y-axis the contig position in the <code>WGA</code> is displayed. Contigs referenced to the minus
                strand are displayed in orange the ones referenced to the plus strand are displayed in blue. Mouse over
                a contig to receive information on its name, length assigned strand as well as start and end position
                in the reference.</p>

              <h3 id="downloads-5">Downloads</h3>

              <p>The scaffolds and the generated pseudo genome can be downloaded as <code>fasta</code> on the top right.
              </p>

              <h3 id="glossary-6">Glossary</h3>

              <ul>
                <li><strong># Contigs</strong>: Number of contigs (set of overlapping DNA segments).</li>

                <li><strong>Genome Size [Mb]</strong>: Size of the WGA in million/mega bases.</li>

                <li><strong>N50</strong>: Given ordered contigs from longest to smallest, length of the contig at 50% of
                  the genome length.</li>

                <li><strong>N90</strong>: Given ordered contigs from longest to smallest, length of the contig at 90% of
                  the genome length.</li>

                <li><strong># Scaffolds</strong>: Number of scaffolds (joined, aligned and assigned contigs) after polishing.
                </li>

                <li><strong>WGA</strong>: Whole Genome Assembly generated via joining all sequence elements after scaffolding
                  with the sequence 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'.</li>
              </ul>

              <hr>

              <h2 id="annotation">Annotation</h2>

              <p>To annotate the created scaffolds the tools <code>Prokka</code> and <code>Barrnap</code> are used. In this
                annotation the genus specific data bases are generate via <code>RefSeq</code>. To identify acquired antimicrobial
                resistance genes the <code>ResFinder</code> data base is utilised. The Identification of bacterial virulence
                factors is performed with the <code>VFDB</code> data base. This page provides an overview on annotation
                key data for this analysis.</p>

              <h3 id="interactivedotplot-1">Interactive dotplot</h3>

              <p>Via the radio buttons on the right <code>key data</code> for X and Y axis can be selected. Mouse over a
                dot of interest to display the according <code>genome name</code> as well as horizontal and vertical
                value extensions. <code>Zooming</code> can be applied via marking the area of interest with left mouse
                button down. To reset the view right click.</p>

              <h3 id="interactivedatatable-4">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in columns containing numeric values.
                They visualize the relative relation of this value compared to the according values of the other genomes.</p>

              <h3 id="downloads-6">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file). To download the GenBank (<code>gbk</code>) or General
                Feature Format (<code>gff</code>) file of a particular genome assembly click on gbk or gff in the data
                table.</p>

              <h3 id="links-4">Links</h3>

              <ul>
                <li><a href="http://www.vicbioinformatics.com/software.barrnap.shtml">Barrnap</a>; Barrnap predicts the location
                  of ribosomal RNA genes in genomes. It supports bacteria (5S,23S,16S), archaea (5S,5.8S,23S,16S), mitochondria
                  (12S,16S) and eukaryotes (5S,5.8S,28S,18S). <a href="https://github.com/tseemann/barrnap">GitHub</a>.</li>

                <li><code>Details</code> on the annotation of a particular genome can be accessed via click on the magnifying
                  glass in the overview table.</li>

                <li><a href="http://www.vicbioinformatics.com/software.prokka.shtml">Prokka</a>: Seemann T. Prokka: rapid
                  prokaryotic genome annotation. Bioinformatics. 2014 Jul 15;30(14):2068-9. PMID:24642063
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/24642063">PubMed</a>.</li>

                <li><a href="https://www.ncbi.nlm.nih.gov/refseq/">RefSeq</a>: O'Leary, Nuala A., et al. "Reference sequence
                  (RefSeq) database at NCBI: current status, taxonomic expansion, and functional annotation." Nucleic
                  acids research (2015): gkv1189. <a href="https://www.ncbi.nlm.nih.gov/pubmed/26553804">PubMed</a>.</li>

                <li><a href="https://cge.cbs.dtu.dk/services/ResFinder/">ResFinder</a>: Identification of acquired antimicrobial
                  resistance genes. Zankari E, Hasman H, Cosentino S, Vestergaard M, Rasmussen S, Lund O, Aarestrup FM,
                  Larsen MV. J Antimicrob Chemother. 2012 Jul 10. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22782487">PubMed</a>.</li>

                <li><a href="http://www.mgc.ac.cn/VFs/main.htm">VFDB</a>: Chen LH, Zheng DD, Liu B, Yang J and Jin Q, 2016.
                  VFDB 2016: hierarchical and refined dataset for big data analysis-10 years on. Nucleic Acids Res. 44(Database
                  issue):D694-D697. <a href="https://www.ncbi.nlm.nih.gov/pubmed/26578559">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-7">Glossary</h3>

              <ul>
                <li><strong># CDS</strong>: Number of coding DNA sequences found.</li>

                <li><strong># CRISPR/CAS</strong>: Number of CRISPR cassettes found.</li>

                <li><strong># Genes</strong>: Number of genes found.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong># Hyp. Proteins</strong>: Number of hypothetical protein coding genes found.</li>

                <li><strong># ncRNA</strong>: Number of non coding RNA genes found.</li>

                <li><strong># rRNA</strong>: Number of ribosomal RNA genes found.</li>

                <li><strong># tRNA</strong>: Number of transfer RNA genes found.</li>
              </ul>

              <h2 id="annotationdetail">Annotation detail</h2>

              <h3 id="interactivegenomeplot">Interactive genome plot</h3>

              <p>The circular genome plot is generated utilising the <code>BioCircos.js</code> library. The most outer circle
                displays the position <code>reference in million base pairs</code>. The most outer
                <code>gene feature circles</code> display all annotated gene features from forward and reverse strand.
                Mouse over the <code>gene features</code> to show feature start, end, type, gene name and product. The
                <code>CDSs</code> are displayed in greyscale, <code>RNAs</code> in green and
                <code>misc features</code> in orange. The outer circular boxplot visualizes the <code>GC content</code>                    of 1 kb bins. GC contents above the genome mean are colored in green and the ones below are colored in
                red. The inner circular boxplot visualizes the <code>GC Skew</code> of 1 kb bins. GC Skews above the
                genome mean are colored in purple and the ones below are colored in neon green. <code>Positioning</code>                    of the whole genome plot can be applied via drag and drop and <code>Zooming</code> can be applied via
                mouse wheel.</p>

              <h3 id="basicannotationstatistics">Basic annotation statistics</h3>

              <p>Abundance of the annotated feature types found in this genome. Visualization of the annotation prediction
                rate.
              </p>

              <h3 id="interactivedatatablefeatures">Interactive data table Features</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table.</p>

              <h3 id="downloads-7">Downloads</h3>

              <p>Several annotation based files can be downloaded, including the genome as <code>gbk</code>, annotations
                as <code>gff</code>, gene sequences as <code>ffn</code>, coding sequences as <code>faa</code> and the
                circular genome plot as <code>svg</code> file. The features table can be saved as comma separated value
                (<code>csv</code>) file via click on the csv button (search and sorting are contained in the downloaded
                file).</p>

              <h3 id="links-5">Links</h3>

              <ul>
                <li><a href="http://bioinfo.ibp.ac.cn/biocircos/">BioCircos.js</a>; BioCircos.js: an Interactive Circos JavaScript
                  Library for Biological Data Visualization on Web Applications. Cui, Y., et al. Bioinformatics. (2016).
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/26819473">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-8">Glossary</h3>

              <ul>
                <li><strong>End</strong>: End position of the feature in base pairs.</li>

                <li><strong>Gene</strong>: Gene name in case it is provided by the feature reference.</li>

                <li><strong>Inference</strong>: Source the feature prediction is based on.</li>

                <li><strong>Locus</strong>: Designation of the annotated genomic region.</li>

                <li><strong>misc features</strong>: Miscellaneous feature an annotated genomic area that is neither CDS nor
                  RNA.</li>

                <li><strong>Product</strong>: Short description of the product associated with the feature.</li>

                <li><strong>Start</strong>: Start position of the feature in base pairs.</li>

                <li><strong>Strand</strong>: The forward/plus strand is marked via '+' and the reverse/minus strand is marked
                  with '-'.</li>

                <li><strong>Type</strong>: Designated group of this gene feature.</li>
              </ul>

              <hr>

              <h2 id="taxonomy">Taxonomy</h2>

              <p>For the taxonomic classification the tools <code>Kraken</code> and <code>Infernal</code> together with
                an own implementation of <code>ANI</code> using <code>Nucmer</code> are used. Kraken and Infernal are
                reference free classification tools. Their results are mapped against taxomic databases. Kraken is based
                on exact alignments of kmers. <code>RefSeq</code> was used to provide the database for it. Infernal is
                based on homology of structural RNA sequences (Rfam 16S). The mapping is done against the <code>RDP</code>                    database. The ANI method is a reference based classification. The ANI analysis is based on a publication
                (see links) and realised with Nucmer for the large scale alignments. This page provides an overview on
                the taxonomy of the analysed genomes with key data from reference free classification and highest reference
                average nucleotide identity.</p>

              <h3 id="interactivedatatables">Interactive data tables</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. Mouse over on underlined table headers to display further information on it.</p>

              <h5 id="referencefreeclassifications">Reference Free Classifications</h5>

              <p>The results from Kraken and Infernal are displayed.</p>

              <h5 id="highestreferenceanis">Highest Reference ANIs</h5>

              <p>The results from Nucmer based ANI classification are displayed.</p>

              <h3 id="downloads-8">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="links-6">Links</h3>

              <ul>
                <li><a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">ANI</a>: Goris, Johan, et al. "DNAâ€“DNA hybridization
                  values and their relationship to whole-genome sequence similarities." International journal of systematic
                  and evolutionary microbiology 57.1 (2007): 81-91. <a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">PubMed</a>.</li>

                <li><code>Details</code> on the taxonomy of a particular genome can be accessed via click on the magnifying
                  glass in the overview table.</li>

                <li><code>kmer</code> column value redirects to kmer taxonomic classification in the ncbi Taxonomy Browser.
                </li>

                <li><code>16S rRNA</code> column value redirects to 16S rRNA taxonomic classification in the ncbi Taxonomy
                  Browser.</li>

                <li><code>ANI</code> Goris, Johan, et al. "DNAâ€“DNA hybridization values and their relationship to whole-genome
                  sequence similarities." International journal of systematic and evolutionary microbiology 57.1 (2007):
                  81-91. <a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">PubMed</a>.</li>

                <li><a href="https://ccb.jhu.edu/software/kraken/">Kraken</a>: Wood DE, Salzberg SL: Kraken: ultrafast metagenomic
                  sequence classification using exact alignments. Genome Biology 2014, 15:R46.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/24580807">PubMed</a>.</li>

                <li><a href="http://eddylab.org/infernal/">Infernal</a>: E. P. Nawrocki and S. R. Eddy, Infernal 1.1: 100-fold
                  faster RNA homology searches, Bioinformatics 29:2933-2935 (2013). <a href="https://www.ncbi.nlm.nih.gov/pubmed/24008419">PubMed</a>.</li>

                <li><a href="http://mummer.sourceforge.net/">MUMmer/Nucmer</a>: Open source MUMmer 3.0 is described in "Versatile
                  and open software for comparing large genomes." S. Kurtz, A. Phillippy, A.L. Delcher, M. Smoot, M.
                  Shumway, C. Antonescu, and S.L. Salzberg, Genome Biology (2004), 5:R12.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/14759262">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-9">Glossary</h3>

              <ul>
                <li><strong>16S Classification</strong>: Rfam 16S based taxonomic classification via Infernal.</li>

                <li><strong>ANI [%]</strong>: Percent average nucleotide identity. Based on the ANI publication the sequenced
                  genome is split into 1020 bp fragments which are compared against the reference (in our approach Nucmer
                  was used instead of blastN). For the calculation the length of the fragments with less than 30% non
                  identities and an alignment length higher than 70% are summed and divided by the total length of the
                  sequenced genome.</li>

                <li><strong>Conserved DNA [%]</strong>: Percent conserved DNA. Based on the ANI publication the sequenced
                  genome is split into 1020 bp fragments which are compared against the reference (in our approach Nucmer
                  was used instead of blastN). For the calculation the length of the fragments that matched with 90%
                  sequence identity or higher are summed and divided by the total length of the sequenced genome.
                </li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>Kmer Classification</strong>: Kmer based taxonomic classification via Kraken.</li>

                <li><strong>Reference</strong>: ID of the reference genome used for taconomic classification.</li>
              </ul>

              <h2 id="taxonomydetail">Taxonomy detail</h2>

              <h3 id="interactivephylogenyvisualization">Interactive phylogeny visualization</h3>

              <p>The height of the <code>phylogenetic levels</code> symbolizes the number of contigs classified as such.
                The number of classified contigs may decreases with classification depth. On mouse over the current and
                the next lower phylogenetic level together with the number of contigs classified (weight) is displayed.</p>

              <h5 id="kmercontigclassifications">Kmer Contig Classifications</h5>

              <p>Here the phylogeny was calculated based on kmers.</p>

              <h5 id="16srrnaclassifications">16S rRNA Classifications</h5>

              <p>Here the phylogeny was calculated based on 16S rRNAs.</p>

              <h3 id="interactivedatatablefeatures-1">Interactive data table Features</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. Mouse over on underlined table headers to display further information on it.</p>

              <h5 id="kmercontigclassifications-1">Kmer Contig Classifications</h5>

              <p>Contains the set of kmer classification results of all contigs.</p>

              <h5 id="16srrnaclassifications-1">16S rRNA Classifications</h5>

              <p>Contains the set of 16S rRNA classification results of all contigs based on highest scoring 16S RNA.
              </p>

              <h3 id="referenceanis">Reference ANIs</h3>

              <p>Table of reference genomes and their percent average nucleotide identity and percentage of conserved DNA.
              </p>

              <h3 id="downloads-9">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="glossary-10">Glossary</h3>

              <ul>
                <li><strong>ANI [%]</strong>: Percent average nucleotide identity. Based on the ANI publication the sequenced
                  genome is split into 1020 bp fragments which are compared against the reference (in our approach Nucmer
                  was used instead of blastN). For the calculation the length of the fragments with less than 30% non
                  identities and an alignment length higher than 70% are summed and divided by the total length of the
                  sequenced genome.</li>

                <li><strong>Classification</strong>: Deepest phylogenetic classification level for a single or group of contigs/16S
                  RNAs.</li>

                <li><strong>Contigs [#]</strong>: Number of contigs that have been identified to this phylogenetic level
                  depth.</li>

                <li><strong>Contigs [%]</strong>: Percentage out all contigs that have been identified to this phylogenetic
                  level depth.</li>

                <li><strong>Hits [#]</strong>: Number of 16S RNAs in the analysed genome that match this 16S RNA database
                  entry.</li>

                <li><strong>Hits [%]</strong>: Percentage of all 16S RNAs in the analysed genome that match this 16S RNA
                  database entry.</li>

                <li><strong>Linage</strong>: List of phylogenetic levels this particular level and the according contigs
                  are included.</li>

                <li><strong>Reference</strong>: Accession of the reference genome.</li>

                <li><strong>Conserved DNA [%]</strong>: Percent conserved DNA. Based on the ANI publication the sequenced
                  genome is split into 1020 bp fragments which are compared against the reference (in our approach Nucmer
                  was used instead of blastN). For the calculation the length of the fragments that matched with 90%
                  sequence identity or higher are summed and divided by the total length of the sequenced genome.
                </li>
              </ul>

              <hr>

              <h2 id="multilocussequencetyping">Multi locus sequence typing</h2>

              <p>The results from the genome classification are created via an own implementation based on Multi Locus Sequence
                Typing (<code>MLST</code>). To classify a genome a <code>BLASTn</code> search is done to search for a
                matching set of loci in the <code>PubMLST</code> database. If a genome contains exactly one reference
                loci set, the classification was successful. Otherwise the most similar reference is shown in case there
                where sufficient matches.</p>

              <h3 id="interactivedonutchart">Interactive donut chart</h3>

              <p>The distribution of the different Sequence Types, Clonal Clusters and Lineages are displayed.</p>

              <h3 id="interactivedatatable-5">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. In green the found classification elements are displayed.
              </p>

              <h3 id="downloads-10">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="links-7">Links</h3>

              <ul>
                <li><a href="https://pubmlst.org/general.shtml">MLST</a>; R. Urwin &amp; M.C. Maiden, 2003, Multi-locus sequence
                  typing: a tool for global epidemiology. Trends Microbiol., 11, 479-487 <a href="https://www.ncbi.nlm.nih.gov/pubmed/14557031?dopt=Abstract">PubMed</a>.</li>

                <li><a href="https://pubmlst.org/">PubMLST</a>; Database.</li>
              </ul>

              <h3 id="glossary-11">Glossary</h3>

              <ul>
                <li><strong>Alleles</strong>: Contiguous nucleotide sequence 350 to 600 base pairs in length of a housekeeping
                  gene fragment used in MLST analysis.</li>

                <li><strong>Clonal Cluster</strong>: Group of related sequence types.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>Lineage</strong>: Members of particular clonal complexes.</li>

                <li><strong>Scheme</strong>: Group of bacterial variants.</li>

                <li><strong>Sequence Type</strong>: Unique combination of MLST allele designations used in an MLST scheme.
                </li>
              </ul>

              <hr>

              <h2 id="antibioticresistances">Antibiotic resistances</h2>

              <p>The antibiotic resistances are detected and classified via the Comprehensive Antibiotic Resistance Database
                (<code>CARD</code>) and its according search tool. The database itself is manually curated on molecular
                basis. CARD covers a board range of antimicrobial resistances including intrinsic, mutation-driven and
                acquired mechanisms. The antibiotic resistance profile of each genome is visualized on this page.</p>

              <h3 id="interactivedatatable-6">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in columns containing numeric values.
                They visualize the relative relation of this value compared to the according values of the other genomes.
                The <code>red colored bar plots</code> indicates outliners based on Z-score. In the <code>ABR Profile</code>                    column found antibiotic agent resistances are visualized as colored circles. You can mouse over the circles
                to display the individual resistances. Mouse over on underlined term to display further information on
                it.</p>

              <h3 id="downloads-11">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="links-8">Links</h3>

              <ul>
                <li><code>Details</code> on the resistance of a particular genome can be accessed via click on the magnifying
                  glass in the overview table.</li>

                <li><a href="https://card.mcmaster.ca/">CARD</a>; Jia et al. 2017. CARD 2017: expansion and model-centric
                  curation of the Comprehensive Antibiotic Resistance Database. Nucleic Acids Research, 45, D566-573.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/27789705">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-12">Glossary</h3>

              <ul>
                <li><strong># ABR Genes</strong>: Number of antibiotic resistance genes found.</li>

                <li><strong>ABR Profile</strong>: Found antibiotic agent resistances.</li>

                <li><strong># ABR Target Drugs</strong>: Number of antibiotic agent resistances.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong># Poteintial ABR Genes</strong>: Number of potential antibiotic resistance genes found.</li>
              </ul>

              <h2 id="antibioticresistancesdetail">Antibiotic resistances detail</h2>

              <h3 id="interactivedatatables-1">Interactive data tables</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. To display <code>additional model information</code> mouse over a model. The 'Seq Identity'
                is categorised into four groups based on value. Entries below 80% sequence identity are highlighted in
                red, blow 95% in yellow, blow 98% in light green and above in green. To display the <code>aligned sequence</code>                    mouse over the bit score value. Mouse over on underlined term to display further information on it.</p>

              <h5 id="abrgenes">ABR Genes</h5>

              <p>Provides information on the genes with a <code>perfect</code> reference match (100%) in the ABR database.
              </p>

              <h5 id="potentialabrgenesbesthits">Potential ABR Genes - Best Hits</h5>

              <p>Provides information on genes and their <code>best</code> non perfect reference ABR database match (40%
                &lt; match &lt;=100%).</p>

              <h5 id="potentialabrgenesallhits">Potential ABR Genes - All Hits</h5>

              <p>Provides information on genes with <code>all</code> their non perfect reference ABR database matches (40%
                &lt; match &lt;=100%).</p>

              <h3 id="links-9">Links</h3>

              <p>Click on a model redirects to this <code>model reference</code> in the CARD database.</p>

              <h3 id="downloads-12">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="glossary-13">Glossary</h3>

              <ul>
                <li><strong>Model</strong>: Name of the resistance mechanism.</li>

                <li><strong>ABR Target Drugs</strong>: The drug or drug family the resistance is associated with.</li>

                <li><strong>Start</strong>: Start position of this resistance gene in this genome.</li>

                <li><strong>End</strong>: End position of this resistance gene in this genome.</li>

                <li><strong>Length</strong>: Length of this resistance gene in this genome.</li>

                <li><strong>Strand</strong>: The forward/plus strand is marked via '+' and the reverse/minus strand is marked
                  with '-'.</li>

                <li><strong>Bit Score</strong>: Normalized chance to find the score or a higher one of this match by chance
                  given in bit (bit score of 3 equals a chance of 2Â³= 8 -> 1 : 8).</li>

                <li><strong>eValue</strong>: Expected number of alignments in the database used with a score equivalent or
                  higher than this match.</li>

                <li><strong>Seq Identity</strong>: Percentage of identical positioned nucleotides in the alignment.</li>
              </ul>

              <hr>

              <h2 id="referencemapping">Reference mapping</h2>

              <p>The sequenced reads are mapped against the reference genome with <code>Bowtie 2</code>. The generated Sequence
                Alignment/Map (<code>SAM</code>) files are converted to ordered Binary Alignment/Map (
                <code>BAM</code>) files via <code>SAMtools</code>. The mapping results, including the number of aligned
                reads per genome and the percentage of read alignment can be viewed in the Interactive data table.</p>

              <h3 id="interactivedatatable-7">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. Mouse over on underlined term to display further information on it.</p>

              <h3 id="downloads-13">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file). To download the <code>bam</code> file of a particular
                genome mapping click on bam in the data table.</p>

              <h3 id="links-10">Links</h3>

              <ul>
                <li><a href="http://www.htslib.org/">SAMtools</a>; Li H., Handsaker B., Wysoker A., Fennell T., Ruan J.,
                  Homer N., Marth G., Abecasis G., Durbin R. and 1000 Genome Project Data Processing Subgroup (2009)
                  The Sequence alignment/map (SAM) format and SAMtools. Bioinformatics, 25, 2078-9.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/19505943">PubMed</a>.</li>

                <li><a href="http://bowtie-bio.sourceforge.net/bowtie2/index.shtml">Bowtie 2</a>; Langmead B, Salzberg S.
                  Fast gapped-read alignment with Bowtie 2. Nature Methods. 2012, 9:357-359. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22388286">PubMed</a></li>
              </ul>

              <h3 id="glossary-14">Glossary</h3>

              <ul>
                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong># Multiple</strong>: Number of reads that mapped multiple times.</li>

                <li><strong>Ratio</strong>: Ratio of total reads that could be mapped to the reference.</li>

                <li><strong># Reads</strong>: Total number of analysed reads.</li>

                <li><strong># Unique</strong>: Number of reads that mapped once.</li>

                <li><strong># Unmapped</strong>: Number of reads that could not be mapped to the reference.</li>
              </ul>

              <hr>

              <h2 id="singlenucleotidepolymorphism">Single nucleotide polymorphism</h2>

              <p>This analysis provides information on SNPs compared to the reference genome. Via the mpileup function of
                <code>SAMtools</code> the mapped BAM files together with the reference fasta are used to compute the
                likelihood of each possible genotype. The resulting likelihoods containing genomic positions are stored
                as Binary Variant Call Format (BCF). <code>BCFtools</code>is then used to call variants in the sequence
                compared to the reference. The genomic variants in the resulting Variant Call Format (VCF) file are then
                filtered via <code>SnpSift</code>. The filtered variants are then analysed by <code>SnpEff</code> to
                predict the resulting effects. To improve further processing and compressing <code>HTSlib</code> is used.
                Finally a consensus sequence and statistics is calculated with <code>BCFtools</code>. This page provides
                an average SNP distribution mapping and a SNP comparison of the analysed genome.</p>

              <h3 id="snpdistributiongraph">SNP distribution graph</h3>

              <p>The <code>mean number of SNPs</code> per <code>10 kb</code> compared to the reference genome are displayed.
                Mouse over the graph to display the position and mean SNP number of an individual peak.
              </p>

              <h3 id="interactivedatatable-8">Interactive data table</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in most columns containing numeric values.
                They visualize the relative relation of this value compared to the according values of the other genomes.
                Mouse over on underlined term to display further information on it.</p>

              <h3 id="downloads-14">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file). The <code>vcf</code> file of each genome can be downloaded.</p>

              <h3 id="links-11">Links</h3>

              <ul>
                <li><code>Details</code> on the SNPs of a particular genome can be accessed via click on the magnifying glass
                  in the overview table.</li>

                <li><a href="http://www.htslib.org/">SAMtools</a>; Li H., Handsaker B., Wysoker A., Fennell T., Ruan J.,
                  Homer N., Marth G., Abecasis G., Durbin R. and 1000 Genome Project Data Processing Subgroup (2009)
                  The Sequence alignment/map (SAM) format and SAMtools. Bioinformatics, 25, 2078-9.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/19505943">PubMed</a>.</li>

                <li><a href="https://github.com/samtools/bcftools">BCFtools</a>; Included in SAMtools.</li>

                <li><a href="http://snpeff.sourceforge.net/SnpSift.html">SnpSift</a>; "Using Drosophila melanogaster as a
                  model for genotoxic chemical mutational studies with a new program, SnpSift", Cingolani, P., et. al.,
                  Frontiers in Genetics, 3, 2012. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22435069">PubMed</a>.</li>

                <li><a href="http://snpeff.sourceforge.net/#">SnpEff</a>; "A program for annotating and predicting the effects
                  of single nucleotide polymorphisms, SnpEff: SNPs in the genome of Drosophila melanogaster strain w1118;
                  iso-2; iso-3.", Cingolani P, Platts A, Wang le L, Coon M, Nguyen T, Wang L, Land SJ, Lu X, Ruden DM.
                  Fly (Austin). 2012 Apr-Jun;6(2):80-92. <a href="https://www.ncbi.nlm.nih.gov/pubmed/22728672">PubMed</a>.</li>

                <li><a href="https://github.com/samtools/htslib">HTSlib</a>; Included in SAMtools.</li>
              </ul>

              <h3 id="glossary-15">Glossary</h3>

              <ul>
                <li><strong>Change Range</strong>: Ratio of number single nucleotide polymorphisms to genome size.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>HI SNPs</strong>: Number of high impact single nucleotide polymorphisms. SNPs are considered
                  high impact if they result in the gain or loss of a start or stop codon.</li>

                <li><strong>SNPs</strong>: Number of single nucleotide polymorphisms.</li>

                <li><strong>TS/TV</strong>: Ratio of number nucleotide transitions to number nucleotide transversions.</li>
              </ul>

              <h2 id="singlenucleotidepolymorphismdetail">Single nucleotide polymorphism detail</h2>

              <h3 id="snpdistributiongraph-1">SNP distribution graph</h3>

              <p>Displays the <code>number of SNPs</code> per <code>10 kb</code> of this particular genome in red and of
                the mean of all analysed genomes in blue. Mouse over the graph to display the position and the number
                of SNPs of an individual peak.</p>

              <h3 id="histograms">Histograms</h3>

              <p>Mouse over the individual bar to display the number of SNP occurrences for this individual category.</p>

              <h5 id="region">Region</h5>

              <p>Displays the position distribution of SNPs relative to known genes.</p>

              <h5 id="classes">Classes</h5>

              <p>Display the effect type distribution of the SNPs of this genome.</p>

              <h5 id="impacts">Impacts</h5>

              <p>Display the severity type distribution of the SNPs of this genome.</p>

              <h3 id="generalstatistics">General statistics</h3>

              <p>Statistical summary of the SNPs an their effects of this genome. Mouse over on underlined term to display
                further information on it.</p>

              <h3 id="interactivedatatablehighimpactsnps">Interactive data table High Impact SNPs</h3>

              <p>The table contains all SNPs that have been rated as 'high' by SnpEff. This includes the SnpEff categories:
                chromosome
                <em>number</em>variation, exon<em>loss</em>variant, frameshift<em>variant, rare</em>amino<em>acid</em>variant,
                splice
                <em>acceptor</em>variant, splice<em>donor</em>variant, start<em>lost, stop</em>gained, stop
                <em>lost, transcript</em>ablation. <code>Individual sorting</code> can be applied via clicking on the
                respective column header. Use the <code>Search</code> function (top right of the table) to display only
                genomes that contain the search term in any of their table fields. The <code>number of entries</code>                    displayed per page can be chosen on the top left of the table. Mouse over on underlined term to display
                further information on it.</p>

              <h3 id="downloads-15">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="glossary-16">Glossary</h3>

              <ul>
                <li><strong>Change Range</strong>: Ratio of number single nucleotide polymorphisms to genome size.</li>

                <li><strong>Contig</strong>: Reference genome accession of the contig this SNP was found.</li>

                <li><strong>downstream</strong>: Number of SNPs that are located 3' toward the transcription direction of
                  the closest gene.</li>

                <li><strong>Alt</strong>: Base(s) at the SNP position.</li>

                <li><strong>Coverage</strong>: Number of reads that display this SNP.</li>

                <li><strong>Effect</strong>: Of High Impact SNPs including stop gain and lost and start lost.</li>

                <li><strong>exon</strong>: In this eucaryotic setting referring to the number of SNPs that are located in
                  a translated region of the genome.</li>

                <li><strong>gene</strong>: Reference gene name for this SNP.</li>

                <li><strong>HI SNPs</strong>: Number of high impact single nucleotide polymorphisms.</li>

                <li><strong>high</strong>: Includes the SnpEff categories: chromosome<em>number</em>variation, exon<em>loss</em>variant,
                  frameshift
                  <em>variant, rare</em>amino<em>acid</em>variant, splice<em>acceptor</em>variant, splice
                  <em>donor</em>variant, start<em>lost, stop</em>gained, stop<em>lost, transcript</em>ablation.</li>

                <li><strong>intergenic</strong>: Number of SNPS that are located in non transcribed regions of this genome.
                </li>

                <li><strong>low</strong>: Includes the SnpEff categories: 5<em>prime</em>UTR<em>premature start</em>codon<em>gain</em>variant,
                  initiator
                  <em>codon</em>variant, splice<em>region</em>variant, start<em>retained, stop</em>retained<em>variant, synonymous</em>variant.</li>

                <li><strong>Mean Qual</strong>: Mean quality of the detected SNP base as PHRED score (error probability;
                  20: 1 in 100; 30: 1 in 1000).</li>

                <li><strong>missense</strong>: Number of SNPs that lead to a different amino acid in the resulting protein.
                </li>

                <li><strong>moderate</strong>: Includes the SnpEff categories: 3<em>prime</em>UTR<em>truncation +exon</em>loss,
                  5
                  <em>prime</em>UTR<em>truncation +exon</em>loss<em>variant, coding</em>sequence<em>variant, disruptive</em>inframe<em>deletion, disruptive</em>inframe<em>insertion, inframe</em>deletion,
                  inframe
                  <em>insertion, missense</em>variant, regulatory<em>region</em>ablation, splice<em>region</em>variant,
                  TFBS_ablation.
                </li>

                <li><strong>modifier</strong>: Includes the SnpEff categories: 3<em>prime</em>UTR<em>variant, 5</em>prime<em>UTR</em>variant,
                  coding
                  <em>sequence</em>variant, conserved<em>intergenic</em>variant, conserved<em>intron</em>variant, downstream
                  <em>gene</em>variant, exon<em>variant, feature</em>elongation, feature<em>truncation, gene</em>variant,
                  intergenic
                  <em>region, intragenic</em>variant, intron<em>variant, mature</em>miRNA<em>variant, miRNA, NMD</em>transcript<em>variant, non</em>coding<em>transcript</em>exon<em>variant, non</em>coding<em>transcript</em>variant,
                  regulatory
                  <em>region</em>amplification, regulatory<em>region</em>variant, TF<em>binding</em>site<em>variant, TFBS</em>amplification,
                  transcript
                  <em>amplification, transcript</em>variant, upstream<em>gene</em>variant.</li>

                <li><strong>nonsense</strong>: Number of SNPs that lead to a new stop codon in the translated sequence.</li>

                <li><strong>Position</strong>: Position in the reference genome this SNP occured in base pairs.</li>

                <li><strong>Ref</strong>: Base at the reference position.</li>

                <li><strong>SNPs</strong>: Number of single nucleotide polymorphisms.</li>

                <li><strong>silent</strong>: Number of SNPs with no direct effect on the resulting amino acid sequence.</li>

                <li><strong>Start lost</strong>: This SNP causes start codon loss of the associated gene.</li>

                <li><strong>Stop Gained</strong>: This SNP causes stop codon gain of the associated gene.</li>

                <li><strong>Stop lost</strong>: This SNP causes stop codon loss of the associated gene.</li>

                <li><strong>Synonymous Variant</strong>: Numbers of SNPs that do not lead to a change in the encoded amino
                  acid.</li>

                <li><strong>TS/TV</strong>: Ratio of number nucleotide transitions to number nucleotide transversions.</li>

                <li><strong>upstream</strong>: SNPs that are located 5' toward the transcription direction of the closest
                  gene.</li>
              </ul>

              <hr>

              <h2 id="coreandpangenome">Core and pan genome</h2>

              <p>The CDS (loci) of the analysed genomes are compared and they are grouped into <code>Core</code>,
                <code>Accessory</code> and <code>Singletons</code> via <code>Roary</code>. For each group <code>functional information</code>                    on the contained CDS (loci) is provided. Distribution of <code>Pan</code>, <code>Core</code> and <code>Singletons</code>                    are displayed in a graph and the genome based distribution of <code># Accessory</code> and <code># Singletions</code>                    is shown in a table.</p>

              <h3 id="interactivedonutchart-1">Interactive donut chart</h3>

              <p>The percentage distribution of Core, Accessory and Singleton genes is displayed.</p>

              <h3 id="genenumbers">Gene Numbers</h3>

              <p>Provides absolute numbers on Core, Pan, Accessory and Singleton genes.</p>

              <h3 id="interactivepancoresingletondevelopmentchart">Interactive PAN / Core / Singleton Development chart</h3>

              <p>Displays changes in number of CDS (loci) in <code>Pan</code>, <code>Core</code> and <code>Singletons</code>                    with increasing number of genomes included in comparison (x-axis). For each comparisons amount the number
                of genomes is picked randomly ten times and the average values are displayed. <code>Pan</code> and <code>Core</code>                    genome size is referenced by the left y-axis. The number of <code>Singletons</code> is referenced by
                the right y-axis. Highlighting of an individual graph can be done via clicking on the graph or the according
                legend. Individual values on the graphs can be accessed via mouse over. Individual data points can be
                highlighted via clicking on them.</p>

              <h3 id="skippedgenome">Skipped Genome</h3>

              <p>In case a sequenced genome could not be analysed this frame is displayed and shows the affected genomes.
              </p>

              <h3 id="interactivedatatables-2">Interactive data tables</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. <code>Blue horizontal bar plots</code> are displayed in columns containing numeric values.
                They visualize the relative relation of this value compared to the according values of the other genomes.</p>

              <h5 id="overview">Overview</h5>

              <p>Provides information on the <code># Accessory</code> and <code># Singletons</code> gene loci of each genome.
              </p>

              <h5 id="coregenome">Core Genome</h5>

              <p>Provides information on the <code>Product</code>(function) for each loci of the core genome.</p>

              <h5 id="accessorygenome">Accessory Genome</h5>

              <p>Provides information on the <code>Product</code>(function) and the <code>Abundance</code> for each loci
                of the accessory genome.</p>

              <h5 id="singletons">Singletons</h5>

              <p>Provides information on each <code>Locus</code>, its <code>Product</code>(function) and the genome it was
                found.</p>

              <h3 id="downloads-16">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file). A <code>fasta</code> file with all core gene sequences
                and a file with all the pan gene sequences can be downloaded. The matrix maps which gene is present in
                which sequenced organism (present = 1, absent = 0) can be downloaded as tab separated value 'tsv' file.</p>

              <h3 id="links-12">Links</h3>

              <ul>
                <li><code>Details</code> on the core and pan genome distribution of a particular genome can be accessed via
                  click on the magnifying glass in the overview table.</li>

                <li><a href="https://sanger-pathogens.github.io/Roary/">Roary</a>; "Roary: Rapid large-scale prokaryote pan
                  genome analysis", Andrew J. Page, Carla A. Cummins, Martin Hunt, Vanessa K. Wong, Sandra Reuter, Matthew
                  T. G. Holden, Maria Fookes, Daniel Falush, Jacqueline A. Keane, Julian Parkhill, Bioinformatics, (2015).
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/26198102">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-17">Glossary</h3>

              <ul>
                <li><strong>Abundance</strong>: Number of locus occurrence in this analysis.</li>

                <li><strong>Accessory</strong>: Number of genes that are contained in at least one other analysed organism
                  (also known as dispensable genome).</li>

                <li><strong>Core</strong>: Number of genes contained in all analysed genomes.</li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>Locus</strong>: Defined contiguous nucleotide sequence in the genome.</li>

                <li><strong>Pan</strong>: Total number of individual genes in this analysis.</li>

                <li><strong>Pan Genome Matrix</strong>: The matrix maps which gene is present in which sequenced organism
                  (present = 1, absent = 0).</li>

                <li><strong>Product</strong>: Functional information on the associated locus.</li>

                <li><strong>Singletons</strong>: Number of genes contained only in this genome out of the analysed set.
                </li>
              </ul>

              <h2 id="coreandpangenomedetail">Core and pan genome detail</h2>

              <h3 id="interactivedonutchart-2">Interactive donut chart</h3>

              <p>The percentage distribution of Core, Accessory and Singletons genes is displayed.</p>

              <h3 id="interactivedatatables-3">Interactive data tables</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table.</p>

              <h5 id="accessorygenome-1">Accessory Genome</h5>

              <p>In this table loci of this particular genome that are present in at least one other analysed genome can
                be compared. <code>Blue horizontal bar plots</code> are displayed in the Abundance column. They visualize
                the relative relation of this value compared to the according values of the other loci.
              </p>

              <h5 id="singletons-1">Singletons</h5>

              <p>In this table loci that have only been found in this particular genome can be observed.</p>

              <h3 id="downloads-17">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="glossary-18">Glossary</h3>

              <ul>
                <li><strong>Abundance</strong>: Number of locus occurrences in this analysis.</li>

                <li><strong>Accessory</strong>: Number of genes that are contained in at least one other analysed organism
                  (also known as dispensable genome).</li>

                <li><strong>Core</strong>: Number of genes contained in all analysed genomes.</li>

                <li><strong>Locus</strong>: Defined contiguous nucleotide sequence in the genome. The name can reefer to
                  a locus tag, gene name, synthetic cluster or group name.</li>

                <li><strong>Product</strong>: Functional information on the associated locus.</li>

                <li><strong>Singletons</strong>: Number of genes contained only in this genome out of the analysed set.
                </li>
              </ul>

              <h1 id="asaptaxonomyoverview">ASAP taxonomy overview</h1>

              <p>For the taxonomic classification the tools <code>Kraken</code> and <code>Infernal</code> together with
                an own implementation of <code>ANI</code> using <code>Nucmer</code> are used. Kraken and Infernal are
                reference free classification tools. Their results are mapped against taxomic databases. Kraken is based
                on exact alignments of kmers. <code>RefSeq</code> was used to provide the database for it. Infernal is
                based on homology of structural RNA sequences (Rfam 16S). The mapping is done against the <code>RDP</code>                    database. The ANI method is a reference based classification. The ANI analysis is based on a publication
                (see links) and realised with Nucmer for the large scale alignments. This page provides an overview on
                the taxonomy of the analysed genomes with key data from reference free classification and highest reference
                average nucleotide identity.</p>

              <h3 id="interactivedatatables-4">Interactive data tables</h3>

              <p><code>Individual sorting</code> can be applied via clicking on the respective column header. Use the <code>Search</code>                    function (top right of the table) to display only genomes that contain the search term in any of their
                table fields. The <code>number of entries</code> displayed per page can be chosen on the top left of
                the table. Mouse over on underlined table headers to display further information on it.</p>

              <h5 id="referencefreeclassifications-1">Reference Free Classifications</h5>

              <p>The results from Kraken and Infernal are displayed.</p>

              <h5 id="highestreferenceanis-1">Highest Reference ANIs</h5>

              <p>The results from Nucmer based ANI classification are displayed.</p>

              <h3 id="downloads-18">Downloads</h3>

              <p>The table can be saved as comma separated value (<code>csv</code>) file via click on the csv button (search
                and sorting are contained in the downloaded file).</p>

              <h3 id="links-13">Links</h3>

              <ul>
                <li><a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">ANI</a>: Goris, Johan, et al. "DNAâ€“DNA hybridization
                  values and their relationship to whole-genome sequence similarities." International journal of systematic
                  and evolutionary microbiology 57.1 (2007): 81-91. <a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">PubMed</a>.</li>

                <li><code>Details</code> on the taxonomy of a particular genome can be accessed via click on the magnifying
                  glass in the overview table.</li>

                <li><code>kmer</code> column value redirects to kmer taxonomic classification in the ncbi Taxonomy Browser.
                </li>

                <li><code>16S rRNA</code> column value redirects to 16S rRNA taxonomic classification in the ncbi Taxonomy
                  Browser.</li>

                <li><code>ANI</code> Goris, Johan, et al. "DNAâ€“DNA hybridization values and their relationship to whole-genome
                  sequence similarities." International journal of systematic and evolutionary microbiology 57.1 (2007):
                  81-91. <a href="https://www.ncbi.nlm.nih.gov/pubmed/17220447">PubMed</a>.</li>

                <li><a href="https://ccb.jhu.edu/software/kraken/">Kraken</a>: Wood DE, Salzberg SL: Kraken: ultrafast metagenomic
                  sequence classification using exact alignments. Genome Biology 2014, 15:R46.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/24580807">PubMed</a>.</li>

                <li><a href="http://eddylab.org/infernal/">Infernal</a>: E. P. Nawrocki and S. R. Eddy, Infernal 1.1: 100-fold
                  faster RNA homology searches, Bioinformatics 29:2933-2935 (2013). <a href="https://www.ncbi.nlm.nih.gov/pubmed/24008419">PubMed</a>.</li>

                <li><a href="http://mummer.sourceforge.net/">MUMmer/Nucmer</a>: Open source MUMmer 3.0 is described in "Versatile
                  and open software for comparing large genomes." S. Kurtz, A. Phillippy, A.L. Delcher, M. Smoot, M.
                  Shumway, C. Antonescu, and S.L. Salzberg, Genome Biology (2004), 5:R12.
                  <a href="https://www.ncbi.nlm.nih.gov/pubmed/14759262">PubMed</a>.</li>
              </ul>

              <h3 id="glossary-19">Glossary</h3>

              <ul>
                <li><strong>16S Classification</strong>: Rfam 16S based taxonomic classification via Infernal.</li>

                <li><strong>ANI [%]</strong>: Percent average nucleotide identity. Based on the ANI publication the sequenced
                  genome is split into 1020 bp fragments which are compared against the reference (in our approach Nucmer
                  was used instead of blastN). For the calculation the length of the fragments with less than 30% non
                  identities and an alignment length higher than 70% are summed and divided by the total length of the
                  sequenced genome.</li>

                <li><strong>Conserved DNA [%]</strong>: Percent conserved DNA. Based on the ANI publication the sequenced
                  genome is split into 1020 bp fragments which are compared against the reference (in our approach Nucmer
                  was used instead of blastN). For the calculation the length of the fragments that matched with 90%
                  sequence identity or higher are summed and divided by the total length of the sequenced genome.
                </li>

                <li><strong>Genome</strong>: Name of the processed genome.</li>

                <li><strong>Kmer Classification</strong>: Kmer based taxonomic classification via Kraken.</li>

                <li><strong>Reference</strong>: ID of the reference genome used for taconomic classification.</li>
              </ul>

              <hr>

              <h2 id="phylogenyoverview">Phylogeny overview</h2>

              <p>The phylogenetic distances of the analysed genomes are calculated via <code>FastTreeMP</code> based on
                their SNPs. Information on the runtime of the analysis is provided. The calculated phylogenetic trees
                are displayed via <code>Phylocanvas</code>.</p>

              <h3 id="phylogenetictreedisplay">Phylogenetic tree display</h3>

              <p>One of the tree types <code>rectangular</code>, <code>radial</code>, <code>circular</code>, <code>diagonal</code>                    and <code>hierarchical</code> can be chosen via the drop down menu. The tree can be positioned via mouse
                drag and drop. The zoom function is controlled via mouse wheel. Via right click in a blank area of the
                diagram further display and export options show up (like <code>Export as Image</code>). Via mouse over
                on a tree node the number of leaves associated with this subtree is displayed. Via left click on a tree
                node the subtree is highlighted in blue. Via right click on a tree node additional display and export
                options are available (including <code>Collapse/Expand Subtree</code> and <code>Export Subtree as Newick File</code>)</p>

              <h3 id="downloads-19">Downloads</h3>

              <p>The SNP based phylogenetic distances can be downloaded as <code>nwk</code> file under <code>Downloads</code>                    on the top right.</p>

              <h3 id="links-14">Links</h3>

              <ul>
                <li><a href="http://www.microbesonline.org/fasttree/">FastTreeMP</a>; Price, M.N., Dehal, P.S., and Arkin,
                  A.P. (2010) FastTree 2 -- Approximately Maximum-Likelihood Trees for Large Alignments. PLoS ONE, 5(3):e9490.
                  doi:10.1371/journal.pone.0009490. <a href="https://www.ncbi.nlm.nih.gov/pubmed/20224823">PubMed</a>.</li>

                <li><a href="http://phylocanvas.org/">Phylocanvas</a>; Centre for Genomic Pathogen Surveillance (2016 ).
                  Interactive tree visualisation for the web.</li>
              </ul>
            </div>

            <nav id="myScrollspy" class="col-md-2" style="position:fixed; right:0;">
              <ul class="nav nav-pills nav-stacked">
                <li><a href="#analysisoverview">Analysis overview</a></li>
                <li><a href="#qualitycontrol">Quality control</a></li>
                <li><a href="#assembly">Assembly</a></li>
                <li><a href="#scaffolds">Scaffolds</a></li>
                <li><a href="#annotation">Annotation</a></li>
                <li><a href="#taxonomy">Taxonomy</a></li>
                <li><a href="#multilocussequencetyping">Multi locus sequence typing</a></li>
                <li><a href="#antibioticresistances">Antibiotic resistances</a></li>
                <li><a href="#referencemapping">Reference mapping</a></li>
                <li><a href="#singlenucleotidepolymorphism">Single nucleotide polymorphism</a></li>
                <li><a href="#coreandpangenome">Core and pan genome</a></li>
                <li><a href="#phylogenyoverview">Phylogeny overview</a></li>
              </ul>
            </nav>

          </div>

        </div>
    </div>
  </div>

</body>

</html>