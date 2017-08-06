
        <div class="col-sm-3 col-md-2 sidebar">
            <ul class="nav nav-sidebar">
                <li class="active"><a href="#">Genome Analyses</a></li>
                <li><a href="../qc.html">Quality Control</a></li>
                <li><a href="../assemblies.html">Assembly</a></li>
                <li><a href="../scaffolds.html">Scaffolds</a></li>
                <li><a href="../annotations.html">Annotation</a></li>
            </ul>
            <ul class="nav nav-sidebar">
                <li class="active"><a href="#">Genome Characterization</a></li>
                <li><a href="../taxonomy.html">Taxonomic Classification</a></li>
                <li><a href="../mlst.html">MLST</a></li>
                <li><a href="../abr.html">Antibiotic Resistances</a></li>
                <li><a href="../vf.html">Virulence Factors</a></li>
                <!--<li><a href="../plasmids.html">Plasmids</a></li>-->
                <!--<li><a href="../phages.html">Phages</a></li>-->
                <li><a href="../mapping.html">Reference Mapping</a></li>
                <li><a href="../snps.html">SNP Detection</a></li>
            </ul>
            <ul class="nav nav-sidebar">
                <li class="active"><a href="#">Comparative Analyses</a></li>
            <#list menu.analyses as ma>
                <li><a href="../${ma.link}.html">${ma.name}</a></li>
            </#list>
            </ul>
        </div>