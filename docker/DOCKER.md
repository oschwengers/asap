# ASA³P - Docker Container
In order to ease the distribution and installation procedure ASA³P offers a
containerized version exploiting Docker. This readme provides further information
on how to setup and run ASA³P using Docker.


# Setup
Due to the huge size of all necessary 3rd party software and database dependencies,
the ASA³P container only contains system packages and top level binaries
as for instance Python3, Java and Perl.
Thus, before executing ASA³P one needs to download and extract the ASA³P directory
containing everything else:
```bash
$ wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap.tar.gz
$ tar -xzf asap.tar.gz
$ rm asap.tar.gz
```

Finally, pull the latest Docker container image:
```bash
$ sudo docker pull oschwengers/asap
```

# Running a container
In order to hide all Docker related options and further simplify the process we
offer a custom shell script within the ASA³P directory:
```bash
$ sudo asap/asap-docker.sh <PROJECT_DIR> [<SCRATCH_DIR>]
```

Parameters:
* `<PROJECT_DIR>`: path to the actual project directory (containing `config.xls` and `data` directory)
* `<SCRATCH_DIR>`: optionally path to a distinct scratch/tmp dir

Example project:
For demonstration purpose we offer an example project containing 4 Listeria monocytogenes genomes.
Just download and extract it:
```bash
$ wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes.tar.gz
$ tar -xzf example-lmonocytogenes.tar.gz
$ rm example-lmonocytogenes.tar.gz
$ sudo asap/asap-docker.sh example-lmonocytogenes/
```

# Advanced usage
Alternatively, if you would like to gain further control you can start ASA³P directly using Docker:
```bash
$ #sudo docker run --rm --privileged -v <ASAP_DIR>:/asap:ro -v <PROJECT_DIR>:/data oschwengers/asap
$ sudo docker run --rm --privileged -v /home/ubuntu/asap:/asap:ro -v /home/ubuntu/example-lmonocytogenes:/data --name asap-example oschwengers/asap
```

Necessary options/paramters:
* `--privileged` provide the Docker container with additional permissions
* `-v` mounts directories from the host system inside the container. Inside the
container ASA³P expects the following mount points: /asap, /data, /var/scratch

As the list of ASA³P dependencies is contantly growing and some of them in turn
depend on a fairly long list of specific binaries and system packages, we decided
to use Singularity containers inside ASA³P as an additional abstraction layer
in oder to isolate dependencies on a per-analysis basis, i.e. starting analyses
bundled inside Singularity containers themselves. In order to bind/mount temporary
directories from inside the Docker container into Singularity containers ASA³P does
need this **security related** `--privileged` option

Optional options/paramters:
* `--rm` removes ephemeral storage (container & data within in)
* `--name` specifies a name for this container instance

# Building a container
Put the Dockerfile into a new directory and build the container:
```
$ docker build <DOCKERFILE> --tag <IMAGE_NAME>
```
This was tested with **Ubuntu 16.04 LTS (Xenial Xerus)** and **Docker version 17.03.1-ce**.
