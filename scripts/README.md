# ASA³P - Scripts
This repository is a collection of scripts which get submitted to the SGE cluster by the ASA³P pipeline.

# Conventions
All scripts which are not called with specific input/output paths will accept:
 *  a **project path** (-p / --project-path) parameter pointing to the project directory
 *  a **genome id** (-g / --gid) parameter *or* a SGE_TASK_ID environment variable
