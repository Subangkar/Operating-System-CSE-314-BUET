cmake_minimum_required(VERSION 3.12)
project(Online)

set(CMAKE_CXX_STANDARD 17)

add_executable(Online SemaphoreUtils.h SharedMem.h main.cpp)