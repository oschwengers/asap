# ASA³P - cloud cluster images
This branch contains an installation script and manual to create ASA³P master and slave OpenStack cloud images based on Ubuntu 14.04.
These images can be used to create a Grid Engine cluster in OpenStack that runs ASA³P.

# Boot BiBiGrid master and slave images
The ASA³P cluster inside the cloud is created with the help of the [BiBiGrid
Framework](https://wiki.cebitec.uni-bielefeld.de/bibiserv-1.25.2/index.php/BiBiGrid) developed at the university of
Bielefeld. Because BiBiGrid is still under development ASA³P cluster images are created by building snapshots
of modified existent BiBiGrid images. To build a cluster it is necessary to create a master and a slave image.
Therefore, two new instances in the OpenStack web interface based on the newest BiBiGrid master and slave images
are created.

Current instance creation guide (as of 01.08.2017):

**Launch Instance** fill in a name and choose **Image** as **Boot Source**. Afterwards select the following images:

* master image = "BiBiGrid Master 14.04 (5/30/17)"
* slave image = "BiBiGrid Slave 14.04 (5/30/17)"

Select: "de.NBI default" as **Flavor**, add a **Network** and **Key Pair** and finally launch the instance.

# Preparation of the master and slave images
As ASA³P itself requires a lot of dependencies, e.g. software libraries and additional software, the images need to
be extended. Therefore upload the script "install_asap_cloud_dependencies.sh" to the master and slave image (e.g. via
scp) and execute the script as root.

```
sudo sh install_asap_cloud_depedencies.sh
```
The script automatically updates the image and installs the following dependencies:

* Bioperl
* Biopython
* Gnuplot-nox
* English language pack
* libtbb2 (c++ library)
* networkx (Python module)
* numpy (Python module)
* Python PIP
* OpenJDK 8
* Roary
* Unzip

Also the the directory

* /var/scratch/

with its required access permissions is created. Finally the environment variables

* ASAP_HOME = /mnt/asap/

are permanently set in the ~/.profile file.

# Creation of master and slave images
After the successful installation the install script can be deleted. Disconnect from the instance and log in to the
OpenStack web interface and choose **Instances**. Select **Create Snapshot** in the **Action** column for the master
and slave instances to create the images. Afterwards the master and slave instances can be deleted and
images can be found at **Images** in the OpenStack web interface. To view the IDs of the images for usage
with the asap-cloud script click on the name of the image.
