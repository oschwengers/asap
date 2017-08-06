# ASA³P - Docker Container
In order to ease distribution and installation ASA³P offers a containerized version based on Docker. This readme provides more information on how to build 
(for those who would like to build their own images) and how to run such a container. 


# Running a container
Due to their size, the ASA³P container does not include the actual ASA³P software and database but necessary system packages and dependencies.
Therefore, before starting ASA³P one needs to download and extract the ASA³P directory:
```
wget https://s3.computational.bio.uni-giessen.de/swift/v1/asap/latest/asap.tar.gz 
tar -xzf asap.tar.gz 
rm asap.tar.gz
```

Then pull the latest container image:
```
sudo docker pull oschwengers/asap
```

Finally, you only need to setup a proper ASA³P data project (read the manual for further information) and run the container mounting both ASA³P and project directory:
```
docker run --name <optional_name_for_the_container> -d -v <asap_dir>:/asap/ -v <project_dir>:/data/ <name_of_the_docker_image>
```
Paramters:
* `--name` is optional and allows to assign a name to the container
* `-d` starts the container in the background (detached mode)
* `-v` mounts folders from the host system inside the container

Mount points:
* `<asap_dir>`: absolute path to downloaded and extracted ASA³P directory (`asap`)
* `<project_dir>`: absolute path to the actual project data direcrory containing a `config.xls` and a `data` directory

After the analysis has completed, the container stops by itself and subsequent results are stored inside the ASA³P project directory on the host system.


# Building a container
This manual was tested with **Ubuntu 16.04 LTS (Xenial Xerus)** and **Docker version 17.03.1-ce**.

Put the Dockerfile into a new directory and build the container:
```
docker build -t <name_for_the_docker_image> <path_to_docker_project_dir>
```

