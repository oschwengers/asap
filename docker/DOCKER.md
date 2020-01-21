# ASA³P - Docker Container
In order to ease the distribution and installation procedure ASA³P offers a
containerized version exploiting Docker. This readme provides further information
on how to setup and run ASA³P using Docker.


# Setup
Due to the huge size of all necessary 3rd party software and database dependencies,
the ASA³P container only contains system packages and top level binaries as for
instance Python3, Java and Perl.
Therefore, the ASA³P directory containing everything else must be downloaded
and extracted before executing ASA³P:
```bash
$ wget https://zenodo.org/record/3606300/files/asap.tar.gz?download=1
$ tar -xzf asap.tar.gz
$ rm asap.tar.gz
```

Finally, pull the latest Docker container image:
```bash
$ sudo docker pull oschwengers/asap
```

# Running a container
For your convenience, hiding all Docker related options and further simplifing
the process, we offer a custom shell script within the ASA³P directory:
```bash
$ #<ASAP_DIR>/asap-docker.sh -p <PROJECT_DIR> [-s <SCRATCH_DIR>] [-a ASAP_DIR] [-z] [-c] [-d]
$ asap/asap-docker.sh -p example-lmonocytogenes -s /tmp
```

Parameters & Options:
* `-p <PROJECT_DIR>`: mandatory: path to the actual project directory (containing `config.xls` and `data` directory)
* `-a <ASAP_DIR>`: optional: path to the ASA³P dir in case the script was moved/copied somewhere else
* `-s <SCRATCH_DIR>`: optional: path to a distinct scratch/tmp dir
* `-z`: optional: skip characterization steps
* `-c`: optional: skip comparative analysis steps
* `-d`: optional: enable verbose logs for debugging purposes


**Note**
1. This shell wrapper script should remain within the ASA³P directory in order to
correctly extract related paths. In case the script was moved/copied somewhere else,
you have to provide the path via `-a <ASAP_DIR>`.
2. The script gathers user:group ids and passes these to the Docker container thus,
files created by ASA³P automatically have the correct user ownerships instead of sudo ones.
3. The script will ask for the sudo password as Docker containers can currently only
be executed as sudo. This is pure technical necessity unrelated to ASA³P itself.

Example project:
For demonstration purpose we offer an example project containing 4 Listeria monocytogenes genomes.
Just download and extract it:
```bash
$ wget https://zenodo.org/record/3606761/files/example-lmonocytogenes-4.tar.gz?download=1
$ tar -xzf example-lmonocytogenes.tar.gz
$ rm example-lmonocytogenes.tar.gz
$ asap/asap-docker.sh -p example-lmonocytogenes/
```

# Advanced usage
Alternatively, if you would like to gain further control you can start ASA³P directly using Docker:
```bash
$ USER=$(id -u)
$ GROUP=$(id -g)
$ sudo docker run \
    --rm \
    --privileged \
    --user $USER:$GROUP \
    -v <asap-dir>:/asap/:ro \
    -v <project-dir>:/data/ \
    [-v <scratch-dir>:/var/scratch/] \
    --volume="/etc/group:/etc/group:ro” \
    --volume="/etc/passwd:/etc/passwd:ro" \
    oschwengers/asap

#example
$ USER=$(id -u)
$ GROUP=$(id -g)
$ sudo docker run \
    --rm \
    --privileged \
    --user $USER:$GROUP \
    -v /home/ubuntu/asap:/asap/:ro \
    -v /home/ubuntu/example-lmonocytogenes:/data/ \
    [-v /tmp:/var/scratch/] \
    --volume="/etc/group:/etc/group:ro” \
    --volume="/etc/passwd:/etc/passwd:ro" \
    oschwengers/asap
```

Necessary options/paramters:
* `--privileged` provide the Docker container with additional permissions
* `--user $(id -u):$(id -g)`: passes user and group ids to the Docker container
* `-v` mounts directories from the host system inside the container. Inside the
container ASA³P expects the following mount points: /asap, /data, /var/scratch

As the list of ASA³P dependencies is contantly growing and some of them in turn
depend on a fairly long list of specific binaries and system packages, we decided
to use Singularity containers inside ASA³P as an additional abstraction layer
in oder to isolate dependencies on a per-analysis basis, i.e. starting analyses
bundled inside Singularity containers themselves. In order to bind/mount temporary
directories from inside the Docker container into Singularity containers ASA³P does
need this **security related** `--privileged` option.

Mounted volumes:
* `<asap-dir>`: absolute path to downloaded and extracted ASA3P directory (asap.tar.gz)
* `<project-dir>`: absolute path to local ASA3P project directory (containing config.xls and data subdirectory)
* `<scratch-dir>`: optionally path to a distinct scratch/tmp dir
* `/etc/group`: necessary to execute ASA3P as the current user instead of root within the Docker container
* `/etc/passwd`: necessary to execute ASA3P as the current user instead of root within the Docker container

Optional options/paramters:
* `--rm` removes ephemeral storage (container & data within in)
* `--name` specifies a name for this container instance

# Building a container
Put the Dockerfile into a new directory and build the container:
```
$ docker build <DOCKERFILE> --tag <IMAGE_NAME>
```
This was tested on **Ubuntu 16.04 LTS** and **Ubuntu 18.04 LTS** with **Docker version 17.03.1-ce**.
