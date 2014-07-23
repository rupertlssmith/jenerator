#!/bin/bash

RAMDISK=/ramdisk

mkdir -p "$RAMDISK$1/target";
ln -s "$RAMDISK$1/target" "$1/target";
