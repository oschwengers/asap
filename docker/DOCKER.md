# ASA³P - Docker Container
In order to ease distribution and installation ASA³P offers a containerized version based on Docker.
This readme provides more information on how to setup and run ASA³P based on Docker.


# Setup
Due to their size, the ASA³P container does not include the actual ASA³P software and database but necessary system packages and dependencies.
Therefore, before starting ASA³P one needs to download and extract the ASA³P directory:
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
In order to further simplify the process we offer a shell script hiding Docker specific commands and options:
```bash
$ sudo ./asap-docker.sh <ASAP_DIR> <PROJECT_DIR> [<SCRATCH_DIR>]
```

Parameters:
* `<ASAP_DIR>`: path to the downloaded and extracted ASA³P directory
* `<PROJECT_DIR>`: path to the actual project directory (containing `config.xls` and `data` directory)
* `<SCRATCH_DIR>`: optionally path to a distinct scratch/tmp dir

Example project:
For demonstration purpose we offer an example project containing 8 Listeria monocytogenes genomes.
Just download and extract it:
```bash
$ wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/example-lmonocytogenes.tar.gz
$ tar -xzf example-lmonocytogenes.tar.gz
$ rm example-lmonocytogenes.tar.gz
$ sudo ./asap-docker.sh asap/ example-lmonocytogenes/
```

# Advanced usage
Alternatively, if you would like to gain further control you can start ASA³P directly using Docker:
```bash
$ #sudo docker run --rm -v <ASAP_DIR>:/asap:ro -v <PROJECT_DIR>:/data oschwengers/asap
$ sudo docker run --rm -v /home/ubuntu/asap:/asap:ro -v /home/ubuntu/example-lmonocytogenes:/data --name <NAME> oschwengers/asap
```

Paramters:
* `--rm` removes ephemeral storage (container & data within in)
* `--name` specifies a name for this container instance
* `-v` mounts folders from the host system inside the container

# Building a container
Put the Dockerfile into a new directory and build the container:
```
$ docker build <DOCKERFILE> --tag <IMAGE_NAME>
```
This was tested with **Ubuntu 16.04 LTS (Xenial Xerus)** and **Docker version 17.03.1-ce**.
